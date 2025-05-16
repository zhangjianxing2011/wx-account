package com.something.config;

import com.something.handler.*;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static me.chanjar.weixin.common.api.WxConsts.EventType.SUBSCRIBE;
import static me.chanjar.weixin.common.api.WxConsts.EventType.UNSUBSCRIBE;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType.EVENT;
import static me.chanjar.weixin.mp.constant.WxMpEventConstants.CustomerService.*;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(WxMpProperties.class)
public class WxConfiguration {
    private final WxMpProperties properties;
    private final KfSessionHandler kfSessionHandler;
    private final LogHandler logHandler;
    private final MsgHandler msgHandler;
    private final LocationHandler locationHandler;
    private final SubscribeHandler subscribeHandler;
    private final ScanHandler scanHandler;
    private final UnsubscribeHandler unsubscribeHandler;
    private final MenuHandler menuHandler;

    @Bean
    public WxMpService wxMpService() {
        // 代码里 getConfigs()处报错的同学，请注意仔细阅读项目说明，你的IDE需要引入lombok插件！！！！
        final WxMpProperties configs = this.properties;
        if (configs == null) {
            throw new RuntimeException("大哥，拜托先看下项目首页的说明（readme文件），添加下相关配置，注意别配错了！");
        }

        WxMpService service = new WxMpServiceImpl();
        WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
        configStorage.setAppId(configs.getAppId());
        configStorage.setSecret(configs.getSecret());
        configStorage.setToken(configs.getToken());
        configStorage.setAesKey(configs.getAesKey());
//        Map<String, WxMpConfigStorage> map = new HashMap<>();
//        map.put(configStorage.getAppId(), configStorage);
//        service.setMultiConfigStorages(map);
        service.addConfigStorage(configs.getAppId(), configStorage);
        return service;
    }

    @Bean
    public WxMpMessageRouter wxMpMessageRouter(WxMpService wxMpService) {
        final WxMpMessageRouter newRouter = new WxMpMessageRouter(wxMpService);
        // 记录所有事件的日志 （异步执行）
        newRouter.rule().handler(this.logHandler).next();

        // 接收客服会话管理事件
        newRouter.rule().async(false).msgType(EVENT).event(KF_CREATE_SESSION).handler(this.kfSessionHandler).end();
        newRouter.rule().async(false).msgType(EVENT).event(KF_CLOSE_SESSION) .handler(this.kfSessionHandler).end();
        newRouter.rule().async(false).msgType(EVENT).event(KF_SWITCH_SESSION).handler(this.kfSessionHandler).end();
//
//        // 门店审核事件
//        newRouter.rule().async(false).msgType(EVENT).event(POI_CHECK_NOTIFY).handler(this.storeCheckNotifyHandler).end();
//
        // 自定义菜单事件
        newRouter.rule().async(false).msgType(EVENT).event(WxConsts.EventType.CLICK).handler(this.menuHandler).end();
        // 点击菜单连接事件
        newRouter.rule().async(false).msgType(EVENT).event(WxConsts.EventType.VIEW).handler(this.menuHandler).end();

        // 关注事件
        newRouter.rule().async(false).msgType(EVENT).event(SUBSCRIBE).handler(this.subscribeHandler).end();
        // 取消关注事件
        newRouter.rule().async(false).msgType(EVENT).event(UNSUBSCRIBE).handler(this.unsubscribeHandler).end();
        // 上报地理位置事件
        newRouter.rule().async(false).msgType(EVENT).event(WxConsts.EventType.LOCATION).handler(this.locationHandler).end();
        // 接收地理位置消息
        newRouter.rule().async(false).msgType(WxConsts.XmlMsgType.LOCATION).handler(this.locationHandler).end();
        // 扫码事件
        newRouter.rule().async(false).msgType(EVENT).event(WxConsts.EventType.SCAN).handler(this.scanHandler).end();
//        // 默认
        newRouter.rule().async(false).handler(this.msgHandler).end();
        return newRouter;
    }


}
