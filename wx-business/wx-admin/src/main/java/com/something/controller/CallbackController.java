package com.something.controller;

import com.something.core.exception.MyException;
import com.something.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.menu.WxMenu;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.draft.WxMpDraftList;
import me.chanjar.weixin.mp.bean.menu.WxMpMenu;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/callback")
public class CallbackController {
    @Autowired
    private WxMpService wxMpService;
    @Resource
    private WxMpMessageRouter wxMpMessageRouter;

    @Value("${wx.mp.token}")
    private String token;
    @Value("${wx.mp.appId}")
    private String appId;

    @GetMapping
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


    @PostMapping(produces = "application/xml; charset=UTF-8")
    public void getWxCallbackContent(@RequestBody String requestBody,
                                     @RequestParam("signature") String signature,
                                     @RequestParam("timestamp") String timestamp,
                                     @RequestParam("nonce") String nonce,
                                     @RequestParam(name = "encrypt_type", required = false) String encType,
                                     @RequestParam(name = "msg_signature", required = false) String msgSignature,
                                     HttpServletResponse response) throws IOException {
        log.info("接收微信请求：[signature=[{}], encType=[{}], msgSignature=[{}]," + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ", signature, encType, msgSignature, timestamp, nonce, requestBody);

        if (!checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }

        String out = null;
        if (encType == null) {
            // 明文传输的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            WxMpXmlOutMessage outMessage = route(inMessage);
            log.info("返回消息：{}", outMessage);
            if (outMessage == null) {
                return;
            }
            out = outMessage.toXml();
        } else if ("aes".equals(encType)) {
            // aes加密的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody,
                    wxMpService.getWxMpConfigStorage(), timestamp, nonce, msgSignature);
            log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return;
            }
            out = outMessage.toEncryptedXml(wxMpService.getWxMpConfigStorage());
        }
        log.info("\n组装回复信息：{}", out);
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.println(out);
            writer.flush();
        }
    }

    @GetMapping("/test")
    public void testDraft() {
        try {
            WxMpDraftList draftList = wxMpService.getDraftService().listDraft(0, 1);
            System.out.println(JsonUtils.toJson(draftList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/getMenu")
    public void getMenu() {
        try {
            WxMpMenu menuGet = wxMpService.getMenuService().menuGet();
            System.out.println(JsonUtils.toJson(menuGet));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @GetMapping("/setMenu")
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
}
