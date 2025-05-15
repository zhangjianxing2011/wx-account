package com.something.utils;

import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpMaterialService;
import me.chanjar.weixin.mp.api.impl.WxMpMaterialServiceImpl;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.material.WxMediaImgUploadResult;

import java.io.File;

//@RequiredArgsConstructor
//@Component
public class WxUtil {


//    private final WxMpFreePublishService wxMpFreePublishService;
//    private final WxMpDraftService wxMpDraftService;
//    private final WxMpMaterialService wxMpMaterialService;
//
//
//
//    public void submitDraft() {
//        wxMpDraftService.addDraft();
//    }
//
//    public void getWxMpFreePublishService() {
//        wxMpFreePublishService.submit();
//    }
//
//
//    public void submitDraft(String filePath) throws WxErrorException {
//        WxMediaImgUploadResult result = wxMpMaterialService.mediaImgUpload(new File(filePath));
//    }


    public static void main(String[] args) throws WxErrorException {
        String path = "C:\\Users\\rock\\Desktop\\1746607800423100.jpg";
        WxMpMaterialService wxMpMaterialService = new WxMpMaterialServiceImpl(new WxMpServiceImpl());
        WxMediaImgUploadResult result = wxMpMaterialService.mediaImgUpload(new File(path));
        System.out.println(result);
        System.out.println(result.getUrl());
    }

}
