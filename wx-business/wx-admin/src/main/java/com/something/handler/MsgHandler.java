package com.something.handler;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.something.dto.JsonRequest;
import com.something.utils.MarkdownToXmlConverter;
import com.something.utils.OkHttpUtils;
import com.something.utils.TextBuilder;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <a href="https://github.com/binarywang/weixin-java-mp-demo/tree/master/src/main/java/com/github/binarywang/demo/wx/mp/handler">...</a>
 *
 * <a href="https://juejin.cn/post/7289394615384555580" />...</a>
 */

@Slf4j
@Component
public class MsgHandler extends AbstractHandler {

    @Value("${chat.api-key}")
    private String apiKey;
    private final Long outTime = 4800L;
    private final TimeUnit outTimeUnit = TimeUnit.MILLISECONDS;

    private static final ConcurrentHashMap<Long, Future<String>> map = new ConcurrentHashMap<>();

    @Autowired
    private ThreadPoolTaskExecutor chatThreadPool;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) {
        long start = System.currentTimeMillis();
        Long messageId = wxMessage.getMsgId();
        Future<String> future = map.get(messageId);

        String threadName = Thread.currentThread().getName();
        String res;
        if (future == null) {
            log.error("<-----开始执行完gemini任务----->");
            future = chatThreadPool.submit(() -> gemini(wxMessage.getContent()));
            try {
                res = future.get(outTime, outTimeUnit);
                long end = System.currentTimeMillis();
                log.error("线程：{} 执行完gemini任务，任务成功返回，耗时：{} 毫秒", threadName, end - start);
                if (StringUtils.isEmpty(res)) {
                    return null;
                }
                if (future.isDone()) {
                    return new TextBuilder().build(MarkdownToXmlConverter.convert(res), wxMessage, weixinService);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.info("线程：{} 获取第一次请求的结果，异常:{}", threadName, e.getMessage());
            } catch (TimeoutException e) {
                long end = System.currentTimeMillis();
                log.info("线程：{} 执行完gemini任务，耗时{}毫秒，任务超时", threadName, end - start);
                // TODO 超时了，任务添加到map，直接返回，不管了，让后面的请求重试
                map.put(messageId, future);
            }
            return null;
        }
        // 这是重试请求，直接从 Future 获取第一次请求的结果
        try {
            log.info("线程：{} 开始尝试从 Future 获取第一次请求的结果，任务结果", threadName);
            res = future.get(outTime, outTimeUnit);
            log.info("线程：{} 在规定时间内从 Future 获取第一次请求的结果：{}", threadName, res);
            if (future.isDone()) {
                // 移除阻塞队列
                map.remove(messageId);
                return new TextBuilder().build(MarkdownToXmlConverter.convert(res), wxMessage, weixinService);
            }
        } catch (InterruptedException | ExecutionException e) {
            log.info("线程：{} 获取第二或三次请求的结果，异常:{}", threadName, e.getMessage());
        } catch (TimeoutException e) {
            // 获取失败
            log.info("线程：{} 获取第二或三次请求的结果，超时", threadName);
        }
        return null;
    }

    private String gemini(String question) {
        JsonRequest.Part part = new JsonRequest.Part(question);
        JsonRequest.Content content = new JsonRequest.Content(Lists.newArrayList(part));
        JsonRequest request = new JsonRequest(Lists.newArrayList(content));
        Gson gson = new Gson();

        String json = gson.toJson(request);

        Map<String, String> headers = new HashMap<>();
        headers.put("x-goog-api-key", apiKey);
        headers.put("Content-Type", "application/json");
        String result = OkHttpUtils.postJsonAndHeaders("https://text.somethingeval.com", JSONObject.parseObject(json), headers);
        log.info("result:{}", result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        return jsonObject.getString("result");
    }

}
