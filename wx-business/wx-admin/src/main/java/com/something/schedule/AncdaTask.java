package com.something.schedule;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.something.constants.DateFormatConstant;
import com.something.constants.SignTypeEnum;
import com.something.constants.SpiderStatusEnum;
import com.something.core.utils.SpringUtil;
import com.something.dao.domain.SignEntity;
import com.something.dao.domain.SignPictureEntity;
import com.something.dao.service.ISignPictureService;
import com.something.dao.service.ISignService;
import com.something.utils.AncdaUtil;
import com.something.utils.OkHttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.draft.WxMpAddDraft;
import me.chanjar.weixin.mp.bean.draft.WxMpDraftArticles;
import me.chanjar.weixin.mp.bean.draft.WxMpDraftList;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

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

    private final ISignService signService;
    private final ISignPictureService signPictureService;
    private final AncdaUtil ancdaUtil;
    private final ThreadPoolTaskExecutor imageDownloadThreadPoolExecutor;
    private final WxMpService wxMpService;



    @Value("${custom.pic-path}")
    private String picPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        fetchSignHistoryData();
    }

    @Scheduled(cron = "0 40-59 16 ? * 1-5", zone = "Asia/Shanghai")
    public void getTodaySignDetail() {
        String day = LocalDate.now().format(DateFormatConstant.STANDARD_FORMAT);
        log.info("---------------------------爬取今日数据开始:{}--------------------------", day);
        startTask();
        log.info("---------------------------爬取今日数据结束:{}--------------------------", day);
    }

    @Scheduled(cron = "0 30-59/5,0-40/10 17,18 ? * 1-5", zone = "Asia/Shanghai")
    public void getTodaySignDetail2() {
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
            if (null != signEntity && StringUtils.isNotEmpty(signEntity.getSignInTime())) {
                return;
            }
            SignEntity entity = (signEntity == null ? saveSign(today) : signEntity);
            List<SignPictureEntity> picList = signPictureService.lambdaQuery().eq(SignPictureEntity::getSignId, entity.getId()).eq(SignPictureEntity::getSpiderStatus, SpiderStatusEnum.TODO.getCode()).list();
            //download images
            if (CollectionUtils.isNotEmpty(picList)) {
                for (SignPictureEntity item : picList) {
                    imageDownloadThreadPoolExecutor.execute(() -> {
                        try {
                            OkHttpUtils.downloadImageSync(item.getSignPictureOrigin(), picPath + "/" + item.getSignPicture());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
            //get meal today
            if (signEntity == null) {
                LocalDate date = LocalDate.parse(entity.getTimeNow(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                int day = date.getDayOfMonth();
                String startMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                SpringUtil.getBean(MealTask.class).dayLoop(startMonth, startMonth, day);
                TimeUnit.SECONDS.sleep(2000);
            }
            //TODO rock push public account

            if (StringUtils.isNotEmpty(entity.getSignInTime())) {
                WxMpAddDraft draft = new WxMpAddDraft();
                WxMpDraftArticles articles = new WxMpDraftArticles();
                articles.setContent("");
                draft.setArticles(Collections.singletonList(articles));
//                wxMpService.getDraftService().addDraft(draft);
                WxMpDraftList draftList = wxMpService.getDraftService().listDraft(0, 1);
                System.out.println(draftList);

            }




        } catch (RuntimeException | InterruptedException | WxErrorException e) {
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
        JSONArray results = JSONObject.parse(dailyResult).getJSONObject("data").getJSONArray("classes");
        String signInTime = null, signOutTime = null, name = "", signInPic = "", signOutPic = "";
        List<String> signInPics = Collections.emptyList(), signOutPics = Collections.emptyList();

        for (Object signDetail : results) {
            JSONObject detailData = JSONObject.parseObject(signDetail.toString());
            name = detailData.getString("name");
            int timeSlotNum = detailData.getInteger("timeSlotNum");
            if (timeSlotNum == 1) {
                signInTime = detailData.getString("signTime");
                signInPics = detailData.getList("signCapture", String.class);
            }
            if (timeSlotNum == 2) {
                signOutTime = StringUtils.isNotEmpty(detailData.getString("signTime")) ? detailData.getString("signTime") : null;
                signOutPics = detailData.getList("signCapture", String.class);
            }
        }
        if (CollectionUtils.isNotEmpty(signInPics)) {
            signInPic = imageName(signInPics.get(0));
        }

        if (CollectionUtils.isNotEmpty(signOutPics)) {
            signInPic = imageName(signOutPics.get(0));
        }
        SignEntity addSign = new SignEntity();
        addSign.setName(name);
        addSign.setSignInTime(signInTime);
        addSign.setSignInPic(signInPic);
        addSign.setSignOutTime(signOutTime);
        addSign.setSignOutPic(signOutPic);
        addSign.setTimeNow(queryDay);
        signService.save(addSign);

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

}
