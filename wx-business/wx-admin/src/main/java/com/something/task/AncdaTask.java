package com.something.task;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.something.config.CustomConfigProperties;
import com.something.constants.DateFormatConstant;
import com.something.constants.SignTypeEnum;
import com.something.constants.SpiderStatusEnum;
import com.something.core.utils.SpringUtil;
import com.something.dao.domain.MealEntity;
import com.something.dao.domain.SignEntity;
import com.something.dao.domain.SignPictureEntity;
import com.something.dao.service.IMealService;
import com.something.dao.service.ISignPictureService;
import com.something.dao.service.ISignService;
import com.something.utils.AncdaUtil;
import com.something.utils.OkHttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.draft.WxMpAddDraft;
import me.chanjar.weixin.mp.bean.draft.WxMpDraftArticles;
import me.chanjar.weixin.mp.bean.material.WxMediaImgUploadResult;
import me.chanjar.weixin.mp.bean.material.WxMpMaterial;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialUploadResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 爬取ancda数据
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class AncdaTask implements ApplicationRunner {

    private static final String strMonth = "2024-04";
    private static volatile boolean flag = true;

    private final ISignService signService;
    private final ISignPictureService signPictureService;
    private final IMealService mealService;
    private final AncdaUtil ancdaUtil;
    private final ThreadPoolTaskExecutor imageDownloadThreadPoolExecutor;
    private final WxMpService wxMpService;
    private final CustomConfigProperties customConfigProperties;




    @Value("${custom.pic-path}")
    private String picPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!customConfigProperties.getTaskStatus()) {
            return;
        }
        fetchSignHistoryData();
    }

    @Scheduled(cron = "0 40-59 16 ? 1-7,9-12 1-5", zone = "Asia/Shanghai")
    public void getTodaySignDetail() {
        String day = LocalDate.now().format(DateFormatConstant.STANDARD_FORMAT);
        log.info("---------------------------爬取今日数据开始:{}--------------------------", day);
        startTask();
        log.info("---------------------------爬取今日数据结束:{}--------------------------", day);
    }

    @Scheduled(cron = "0  0-30/5,30-59/10,0-30/10,30-59/10 17,17,18,18 ? 1-7,9-12 1-5", zone = "Asia/Shanghai")
    public void getTodaySignDetail2() {
        if (!flag) {
            return;
        }
        String day = LocalDate.now().format(DateFormatConstant.STANDARD_FORMAT);
        log.info("---------------------------爬取今日数据开始:{}--------------------------", day);
        startTask();
        log.info("---------------------------爬取今日数据结束:{}--------------------------", day);
    }

    public void startTask() {
        try {
            LocalDate localDate = LocalDate.now();
            String today = localDate.format(DateFormatConstant.STANDARD_FORMAT);
            SignEntity signEntity = signService.lambdaQuery().eq(SignEntity::getTimeNow, today).one();
            if (null != signEntity && StringUtils.isNotEmpty(signEntity.getSignOutTime())) {
                return;
            }
            SignEntity entity = ((signEntity == null || StringUtils.isEmpty(signEntity.getSignOutTime())) ? saveSign(today) : signEntity);
            List<SignPictureEntity> picList = signPictureService.lambdaQuery().eq(SignPictureEntity::getSignId, entity.getId()).eq(SignPictureEntity::getSpiderStatus, SpiderStatusEnum.TODO.getCode()).list();
            //download images
            if (CollectionUtils.isNotEmpty(picList)) {
                for (SignPictureEntity item : picList) {
                    imageDownloadThreadPoolExecutor.execute(() -> {
                        try {
                            OkHttpUtils.downloadImageSync(item.getSignPictureOrigin(), picPath + "/" + item.getSignPicture());
                        } catch (IOException e) {
                            log.error("下载图片失败:{}", e.getMessage());
                        }
                    });
                }
            }
            //get meal today
            if (signEntity == null) {
                LocalDate date = LocalDate.parse(today, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                int day = date.getDayOfMonth();
                String startMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                SpringUtil.getBean(MealTask.class).dayLoop(startMonth, startMonth, day);
                TimeUnit.SECONDS.sleep(2);
            }

            if (StringUtils.isNotEmpty(entity.getSignOutTime())) {
                String mediaId = uploadPicture(entity.getSignOutPic());
                String imgUrl = mediaImgUpload(entity.getSignOutPic());
                MealEntity meal = mealService.lambdaQuery().eq(MealEntity::getSignId, entity.getId()).one();
                String imageTag = "<img src=\"" + imgUrl + "\" alt=\"图片描述\" style=\"width:100%;max-width:640px;\">";
                String content = "<p>今日签退时间:  %s</p><br />" ;
                if (meal != null) {
                    content = "<p>今日签退时间:  %s</p><br /><p>早餐:  %s</p><br /><p>午餐:  %s</p><br /><p>水果:  %s</p><br /><p>下午点心:  %s</p><br />";
                }

                String detailContent = meal == null ? String.format(content, today + " " + entity.getSignOutTime()) : String.format(content, today + "&nbsp;" + entity.getSignOutTime(), meal.getBreakfast(), meal.getLunch(), meal.getFruit(), meal.getLunchMiddle());
                detailContent += imageTag;
                WxMpDraftArticles article = WxMpDraftArticles.builder().build()
                        .setTitle(today)
                        .setContent(detailContent)
                        .setNeedOpenComment(1)
                        .setOnlyFansCanComment(1)
                        .setAuthor("AL")
                        .setThumbMediaId(mediaId);
                WxMpAddDraft addDraft = WxMpAddDraft.builder().build().setArticles(Lists.newArrayList(article));
                log.info("发布内容:{}", addDraft);
                String result = wxMpService.getDraftService().addDraft(addDraft);
                log.info("发布结果:{}", result);
                wxMpService.getFreePublishService().submit(result);
            }
        } catch (RuntimeException | InterruptedException | WxErrorException e) {
            log.error("数据异常:{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    /**
     * 获取历史数据不包含今天
     */
    public void fetchSignHistoryData() {
        List<String> months = getMonths();
        SignEntity signEntity = signService.getMaxTimeNow();
        String startMonth = months.get(0);
        int day = 1;

        LocalDate localDate = LocalDate.now();
        int today = localDate.getDayOfMonth();
        String todayMonth = localDate.format(DateFormatConstant.SHORT_FORMAT);

        if (signEntity != null) {
            String lastDay = signEntity.getTimeNow();
            day = Integer.parseInt(lastDay.split("-")[2]);
            startMonth = getLastDayOfMonth(lastDay);

            LocalDate date = LocalDate.now().plusDays(-1);
            String todayDate = date.format(DateFormatConstant.STANDARD_FORMAT);
            if (todayDate.equals(lastDay)) {
                return;
            }

        }
        log.info("爬取签到月份开始:{}", startMonth);
        for (String month : months) {
            if (month.compareTo(startMonth) < 0) {
                continue;
            }

            String result = ancdaUtil.getMonthSign(month);
            JSONArray jsonArray = JSONObject.parseObject(result).getJSONObject("data").getJSONArray("calendar");
            for (Object object : jsonArray) {
                //object -> {"day":1,"startTime":"","endTime":"","status":0,"isWork":0
                JSONObject jsonObject = JSONObject.parseObject(object.toString());
                int monthDay = jsonObject.getInteger("day");
                if ((month.equals(todayMonth) && monthDay >= today)) {
                    return;
                }
                if ((month.equals(todayMonth) && monthDay <= day) || jsonObject.getInteger("isWork") != 1) {
                    continue;
                }
                String formatDay = monthDay < 10 ? "0" + monthDay : monthDay + "";
                String queryDay = month + "-" + formatDay;
                saveSign(queryDay);

                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private SignEntity saveSign(String queryDay) {
        String dailyResult = ancdaUtil.getSingleSign(queryDay);
        log.info("time: {},sign-result:{}", queryDay, dailyResult);
        JSONArray results = JSONObject.parse(dailyResult).getJSONObject("data").getJSONArray("classes");
        String signInTime = null, signOutTime = null, name = "", signInPic = "", signOutPic = "";
        List<String> signInPics = Collections.emptyList(), signOutPics = Collections.emptyList();

        for (Object signDetail : results) {
            JSONObject detailData = JSONObject.parseObject(signDetail.toString());
            name = detailData.getString("name");
            int timeSlotNum = detailData.getInteger("timeSlotNum");
            if (timeSlotNum == 1) {//签到
                signInTime = detailData.getString("signTime");
                signInPics = detailData.getList("signCapture", String.class);
            }
            if (timeSlotNum == 2) {//签退
                signOutTime = StringUtils.isNotEmpty(detailData.getString("signTime")) ? detailData.getString("signTime") : null;
                signOutPics = detailData.getList("signCapture", String.class);
            }
        }
        if (CollectionUtils.isNotEmpty(signInPics)) {
            signInPic = imageName(signInPics.get(0));
        }

        if (CollectionUtils.isNotEmpty(signOutPics)) {
            signOutPic = imageName(signOutPics.get(0));
        }

        String today = LocalDate.now().format(DateFormatConstant.STANDARD_FORMAT);
        setFlag(queryDay, today, signOutTime);

        SignEntity signEntity = signService.lambdaQuery().eq(SignEntity::getTimeNow, queryDay).one();
        SignEntity addSign = new SignEntity();

        if (signEntity != null) {
            if (StringUtils.isEmpty(signOutTime)) {
                return signEntity;
            }
            addSign.setId(signEntity.getId());
            addSign.setSignOutTime(signOutTime);
            addSign.setSignOutPic(signOutPic);
            signService.updateById(addSign);
        } else {
            addSign.setName(name);
            addSign.setSignInTime(signInTime);
            addSign.setSignInPic(signInPic);
            addSign.setSignOutTime(signOutTime);
            addSign.setSignOutPic(signOutPic);
            addSign.setTimeNow(queryDay);
            signService.save(addSign);
        }

        if (CollectionUtils.isNotEmpty(signInPics)) {
            List<SignPictureEntity> list = coverToSign(addSign.getId(), signInPics, SignTypeEnum.SIGNING.getCode());
            signPictureService.saveBatch(list);
        }
        if (CollectionUtils.isNotEmpty(signOutPics)) {
            List<SignPictureEntity> list = coverToSign(addSign.getId(), signOutPics, SignTypeEnum.SINGOUT.getCode());
            signPictureService.saveBatch(list);
        }
        return addSign;
    }

    private List<SignPictureEntity> coverToSign(Long signId, List<String> list, int type) {
        List<SignPictureEntity> signPictureEntities = new ArrayList<>(list.size());
        for (String str : list) {
            SignPictureEntity signPicture = new SignPictureEntity();
            signPicture.setSignPicture(imageName(str));
            signPicture.setSignPictureOrigin(str);
            signPicture.setSignId(signId);
            signPicture.setType(type);
            signPicture.setSpiderStatus(SpiderStatusEnum.TODO.getCode());
            signPictureEntities.add(signPicture);
        }
        return signPictureEntities;
    }

    public static String getLastDayOfMonth(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        int lastDayOfMonth = date.lengthOfMonth();
        System.out.println("lastDayOfMonth:" + lastDayOfMonth);
        System.out.println("date.getDayOfMonth():" + date.getDayOfMonth());
        return date.getDayOfMonth() == lastDayOfMonth ? date.plusMonths(1L).format(DateFormatConstant.SHORT_FORMAT) : date.format(DateFormatConstant.SHORT_FORMAT);
    }

    private String imageName(String url) {
        int lastIndex = url.lastIndexOf('/');
        if (lastIndex == -1) {
            return null; // 没有路径分隔符
        }
        if (lastIndex == url.length() - 1) {
            return null; // URL 以 '/' 结尾，无法提取文件名
        }
        return url.substring(lastIndex + 1);
    }

    public List<String> getMonths() {
        List<String> months = new ArrayList<>();
        // Attention 起始时间起始时间与签到一致
        YearMonth start = YearMonth.parse(strMonth, DateFormatConstant.SHORT_FORMAT);
        YearMonth end = YearMonth.now();
        YearMonth current = start;
        while (!current.isAfter(end)) {
            months.add(current.format(DateFormatConstant.SHORT_FORMAT));
            current = current.plusMonths(1);
        }
        return months;
    }

    public void setFlag(String queryDay, String today, String signOutTime) {
        if (!today.equals(queryDay)) {
            return;
        }
        flag = StringUtils.isEmpty(signOutTime);
    }

    public String uploadPicture(String picture) throws WxErrorException {
        WxMpMaterial material = new WxMpMaterial();
        material.setFile(new File(picPath + "/" + picture));
        material.setName(picture);
        WxMpMaterialUploadResult uploadResult = wxMpService.getMaterialService().materialFileUpload(WxConsts.MediaFileType.IMAGE, material);
        return uploadResult.getMediaId();
    }

    public String  mediaImgUpload(String picture) throws WxErrorException {
        WxMediaImgUploadResult uploadResult = wxMpService.getMaterialService().mediaImgUpload(new File(picPath + "/" + picture));
        return uploadResult.getUrl();
    }

}
