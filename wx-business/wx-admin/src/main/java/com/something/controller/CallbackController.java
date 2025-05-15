package com.something.controller;

import com.something.core.exception.MyException;
import com.something.dao.service.ISignService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/callback")
public class CallbackController {
    @Autowired
    public ISignService signService;
    @Autowired
    private WxMpService wxMpService;
    @Resource
    private WxMpMessageRouter wxMpMessageRouter;

    @Value("${wx.mp.token}")
    private String token;

    @GetMapping("/init")
    public String getWxCallback(@RequestParam(value = "signature", required = false) String signature,
                                @RequestParam(value = "timestamp", required = false) String timestamp,
                                @RequestParam(value = "nonce", required = false) String nonce,
                                @RequestParam(value = "echostr", required = false) String echostr) {
        log.info("signature:{}, timestamp:{}, nonce:{}, echostr:{}", signature, timestamp, nonce, echostr);
        signService.getMaxTimeNow();
        boolean flag = checkSignature(signature, timestamp, nonce);
        if (flag) {
            return echostr;
        }
        return "";
    }


    @PostMapping(value = "/init", produces = "application/xml; charset=UTF-8")
    public String getWxCallback2(@RequestBody String requestBody, @RequestParam("signature") String signature,
                       @RequestParam("timestamp") String timestamp, @RequestParam("nonce") String nonce,
                       @RequestParam(name = "encrypt_type", required = false) String encType,
                       @RequestParam(name = "msg_signature", required = false) String msgSignature) {
        log.info("\n接收微信请求：[signature=[{}], encType=[{}], msgSignature=[{}]," + " timestamp=[{}], nonce=[{}], requestBody=[\n{}\n] ", signature, encType, msgSignature, timestamp, nonce, requestBody);

        if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
            throw new IllegalArgumentException("非法请求，可能属于伪造的请求！");
        }

        String out = null;
        if (encType == null) {
            // 明文传输的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromXml(requestBody);
            log.info("接收到消息：{}", inMessage);
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }

            out = outMessage.toXml();
        } else if ("aes".equals(encType)) {
            // aes加密的消息
            WxMpXmlMessage inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody,
                    wxMpService.getWxMpConfigStorage(), timestamp, nonce, msgSignature);
            log.debug("\n消息解密后内容为：\n{} ", inMessage.toString());
            WxMpXmlOutMessage outMessage = this.route(inMessage);
            if (outMessage == null) {
                return "";
            }
            out = outMessage.toEncryptedXml(wxMpService.getWxMpConfigStorage());
        }

        log.debug("\n组装回复信息：{}", out);

        return out;
    }

    private WxMpXmlOutMessage route(WxMpXmlMessage message) {
        try {
            return wxMpMessageRouter.route(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    



    private Boolean checkSignature(String signature, String timestamp, String nonce) {
        try {
            String[] array = {token, timestamp, nonce};
            Arrays.sort(array);
            String concatenated = String.join("", array);
            System.out.println(concatenated);
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
