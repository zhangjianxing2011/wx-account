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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * https://github.com/binarywang/weixin-java-mp-demo/tree/master/src/main/java/com/github/binarywang/demo/wx/mp/handler
 */

@Slf4j
@Component
public class MsgHandler extends AbstractHandler {

    @Value("${chat.api-key}")
    private String apiKey;
    private final Long outTime = 4800L;
    private final TimeUnit outTimeUnit = TimeUnit.MILLISECONDS;

    public static final ConcurrentHashMap<Long, Future<String>> map = new ConcurrentHashMap<>();

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) {
        String res;
        try {
            Future<String> future = map.get(wxMessage.getMsgId());
            if (future == null) {
                //TODO 组装回复消息
                JsonRequest.Part part = new JsonRequest.Part(wxMessage.getContent());
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

                //TODO 超时问题
                return new TextBuilder().build(MarkdownToXmlConverter.convert(jsonObject.getString("result")), wxMessage, weixinService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
