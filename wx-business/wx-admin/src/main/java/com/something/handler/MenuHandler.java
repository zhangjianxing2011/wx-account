package com.something.handler;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * https://github.com/binarywang/weixin-java-mp-demo/tree/master/src/main/java/com/github/binarywang/demo/wx/mp/handler
 */
@Slf4j
@Component
public class MenuHandler extends AbstractHandler {

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context,
                                    WxMpService weixinService, WxSessionManager sessionManager) {
        String msg = String.format("type:%s, event:%s, key:%s, content:%s", wxMessage.getMsgType(), wxMessage.getEvent(), wxMessage.getEventKey(), wxMessage.getContent());
        log.info(msg);
        if (WxConsts.EventType.VIEW.equals(wxMessage.getEvent())) {
            return null;
        }

        if (WxConsts.EventType.VIEW.equals(wxMessage.getEvent())) {
            return null;
        }

        return WxMpXmlOutMessage.TEXT().content(msg)
                .fromUser(wxMessage.getToUser()).toUser(wxMessage.getFromUser())
                .build();
    }

}