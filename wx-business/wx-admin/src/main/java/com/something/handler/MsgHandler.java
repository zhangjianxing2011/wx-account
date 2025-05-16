package com.something.handler;

import com.something.utils.TextBuilder;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType;

/**
 * https://github.com/binarywang/weixin-java-mp-demo/tree/master/src/main/java/com/github/binarywang/demo/wx/mp/handler
 */
@Component
public class MsgHandler extends AbstractHandler {

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) {

        if (!wxMessage.getMsgType().equals(XmlMsgType.EVENT)) {
            //TODO 可以选择将消息保存到本地
        }

        //当用户输入关键词如“你好”，“客服”等，并且有客服在线时，把消息转发给在线客服
        //TODO rock 个人的没有客服，所以这里没有处理
//        try {
//            if (StringUtils.startsWithAny(wxMessage.getContent(), "你好", "客服")
//                    && !weixinService.getKefuService().kfOnlineList()
//                    .getKfOnlineList().isEmpty()) {
//                return WxMpXmlOutMessage.TRANSFER_CUSTOMER_SERVICE()
//                        .fromUser(wxMessage.getToUser())
//                        .toUser(wxMessage.getFromUser()).build();
//            }
//        } catch (WxErrorException e) {
//            e.printStackTrace();
//        }

        //TODO 组装回复消息
//        String content = JsonUtils.toJson(wxMessage);
        String content = "【自动回复】" + wxMessage.getContent();

        return new TextBuilder().build(content, wxMessage, weixinService);

    }
}
