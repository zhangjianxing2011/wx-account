package com.something.controller;

import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.something.core.exception.MyException;
import com.something.dto.JsonRequest;
import com.something.utils.JsonUtils;
import com.something.utils.MarkdownToXmlConverter;
import com.something.utils.OkHttpUtils;
import com.something.utils.TextBuilder;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.draft.WxMpDraftList;
import me.chanjar.weixin.mp.bean.menu.WxMpMenu;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@RestController
public class CallbackController {
    private final Long outTime = 4750L;
    private final Long secondTime = 3000L;
    private final TimeUnit outTimeUnit = TimeUnit.MILLISECONDS;
    public static final ConcurrentHashMap<Long, Future<String>> map = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Boolean> bMap = new ConcurrentHashMap<>();


    @Autowired
    private WxMpService wxMpService;
    @Resource
    private WxMpMessageRouter wxMpMessageRouter;
    @Autowired
    private ThreadPoolTaskExecutor chatThreadPool;


    @Value("${wx.mp.token}")
    private String token;
    @Value("${wx.mp.appId}")
    private String appId;
    @Value("${chat.api-key}")
    private String apiKey;
    @Value("${custom.pic-path}")
    private String picPath;

    @GetMapping("/callback")
    public void getWxCallback(@RequestParam(value = "signature", required = false) String signature,
                              @RequestParam(value = "timestamp", required = false) String timestamp,
                              @RequestParam(value = "nonce", required = false) String nonce,
                              @RequestParam(value = "echostr", required = false) String echostr,
                              HttpServletResponse response) throws IOException {
        log.info("signature:{}, timestamp:{}, nonce:{}, echostr:{}", signature, timestamp, nonce, echostr);
        if (checkSignature(timestamp, nonce, signature)) {
            try (PrintWriter writer = response.getWriter()) {
                writer.println(echostr);
                writer.flush();
                return;
            }
        }
        throw new MyException(400, "非法请求");
    }


    @PostMapping(value = "/callback", produces = "application/xml; charset=UTF-8")
    public void getWxCallbackContent(@RequestBody String requestBody,
                                     @RequestParam("signature") String signature,
                                     @RequestParam("timestamp") String timestamp,
                                     @RequestParam("nonce") String nonce,
                                     @RequestParam(name = "encrypt_type", required = false) String encType,
                                     @RequestParam(name = "msg_signature", required = false) String msgSignature,
                                     HttpServletResponse response) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        log.info("接收微信请求：[signature=[{}], encType=[{}], msgSignature=[{}]," + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ", signature, encType, msgSignature, timestamp, nonce, requestBody);

        if (!checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }
        Long messageId = null;
        String out = null;
        try {
            if (encType == null) {
                // 明文传输的消息
                WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
                WxMpXmlOutMessage outMessage = null;
                messageId = inMessage.getMsgId();
                if (!WxConsts.XmlMsgType.TEXT.equals(inMessage.getMsgType())) {
                    outMessage = route(inMessage);
                } else {
                    outMessage = getOutMessage(outMessage, inMessage);
                }
                if (outMessage == null) {
                    log.error("outMessage is null");
                    long sleepSeconds = (null == bMap.get(inMessage.getMsgId())) ? 8L : 5L;
                    TimeUnit.SECONDS.sleep(sleepSeconds);
//                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                    response.getOutputStream().close();
                    return;
                }
                outMessage.setCreateTime(inMessage.getCreateTime() + 4);
                out = outMessage.toXml();
            } else if ("aes".equals(encType)) {
                // aes加密的消息
                WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody,
                        wxMpService.getWxMpConfigStorage(), timestamp, nonce, msgSignature);
                log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
                WxMpXmlOutMessage outMessage = this.route(inMessage);
                if (outMessage == null) {
                    throw new MyException(500, "no message here!");
                }

                out = outMessage.toEncryptedXml(wxMpService.getWxMpConfigStorage());
            }
        } catch (Exception e) {
            log.error("处理时出错！", e);
            return;
        }
        log.info("\n组装回复信息：{}", out);
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            if (null != messageId) {
                map.remove(messageId);
                bMap.remove(messageId);
            }
            writer.println(out);
            writer.flush();
        }
    }

    @GetMapping("/callback/test2")
    public void testDraft() {
        try {
            WxMpDraftList draftList = wxMpService.getDraftService().listDraft(0, 1);
            System.out.println(JsonUtils.toJson(draftList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/callback/getMenu")
    public void getMenu() {
        try {
            WxMpMenu menuGet = wxMpService.getMenuService().menuGet();
            System.out.println(JsonUtils.toJson(menuGet));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/callback/setMenu")
    public void setMenu() {
        try {
            WxMenu menu = new WxMenu();
//            menu.setButtons();
//            menu.setMatchRule();

            String result = wxMpService.getMenuService().menuCreate(menu);
            log.info("更新menu返回:{}", result);
        } catch (Exception e) {
            log.error("新建menu错误:{}", e.getMessage());
        }
    }

    @RequestMapping("/qiniu/getCallback")
    public void getReturnUrl(HttpServletRequest req, @RequestParam(value = "callbackBody", required = false) String callbackBody) {
        log.info("url：{}, method:{}", req.getRequestURL(), req.getMethod());
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info("Request Body: {}", body.toString());

        Map<String, String[]> parameterMap = req.getParameterMap();
        log.info("Request Parameters: ");
        parameterMap.forEach((key, values) -> {
            log.info(key + " = " + String.join(", ", values));
        });

        log.info("callbackBody: {}", setCallbackBody(req));
    }

    private String setCallbackBody(HttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        StringBuilder stringBuilder = new StringBuilder();
        parameterMap.forEach((key, values) -> {
            stringBuilder.append(key).append("=").append(values[0]).append("&");
        });
        return stringBuilder.toString();
    }

    private WxMpXmlOutMessage route(WxMpXmlMessage message) {
        try {
            return wxMpMessageRouter.route(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e.getMessage());
        }
        return null;
    }

    private Boolean checkSignature(String timestamp, String nonce, String signature) {
        try {
            String[] array = {token, timestamp, nonce};
            Arrays.sort(array);
            String concatenated = String.join("", array);
            // Compute SHA-1 hash
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = sha1.digest(concatenated.getBytes());

            // Convert hash to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            if (hexString.toString().equals(signature)) {
                return true;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MyException(400, e.getMessage());
        }
        return false;
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


    private WxMpXmlOutMessage getOutMessage(WxMpXmlOutMessage outMessage, WxMpXmlMessage inMessage) throws InterruptedException {
        Long messageId = inMessage.getMsgId();
        long start = System.currentTimeMillis();
        Future<String> future = map.get(messageId);
        Boolean flag = bMap.get(messageId);
        log.info("thread-name:{},future is null:{},flag:{}", Thread.currentThread().getName(), future == null, flag);
        String threadName = Thread.currentThread().getName();
        String res;

        if (future == null && (flag == null || !flag)) {//第一次
            bMap.put(messageId, true);
            log.error("<-----开始执行完gemini任务----->");
            future = chatThreadPool.submit(() -> gemini(inMessage.getContent()));
            try {
                res = future.get(outTime, outTimeUnit);
                long end = System.currentTimeMillis();
                log.info("线程：{} 执行完gemini任务，任务成功返回，耗时：{} 毫秒，结果：{}", threadName, end - start, res);
                if (future.isDone()) {
                    outMessage = new TextBuilder().build(MarkdownToXmlConverter.convert(res), inMessage, wxMpService);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("线程：{} 获取第一次请求的结果，异常:{}", threadName, e.getMessage());
            } catch (TimeoutException e) {
                long end = System.currentTimeMillis();
                log.error("线程：{} 执行完gemini任务，耗时{}毫秒，任务超时,feature isDone:{}", threadName, end - start, future.isDone());
                // TODO 超时了，任务添加到map，直接返回，不管了，让后面的请求重试
                map.put(messageId, future);
            }
            return outMessage;
        }

        if (future != null && future.isDone()) {
            log.info("<-----线程：{}  here you are done----->", Thread.currentThread().getName());
            try {
                String result = future.get(outTime, TimeUnit.MILLISECONDS);
                log.info("result done:{}", result);
                outMessage = new TextBuilder().build(MarkdownToXmlConverter.convert(result), inMessage, wxMpService);
            } catch (RuntimeException | ExecutionException | TimeoutException e) {
                log.error("future isDone result error:{}", e.getMessage());
                throw new RuntimeException(e);
            }
            return outMessage;
        }

        log.info("<-----线程：{}  here you are wait----->", Thread.currentThread().getName());
        try {
            String result = future.get(secondTime, TimeUnit.MILLISECONDS);
            log.info("result wait:{}", result);
            outMessage = new TextBuilder().build(MarkdownToXmlConverter.convert(result), inMessage, wxMpService);
        }  catch (RuntimeException | ExecutionException | TimeoutException e) {
            log.error("future wait result error:{}, exception.class:{}", e.getMessage(), e.getClass().getName());
        }
        return outMessage;
    }

}
