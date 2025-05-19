package com.something.schedule;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.something.constants.DateFormatConstant;
import com.something.constants.MealTyeEnum;
import com.something.dao.domain.MealEntity;
import com.something.dao.domain.SignEntity;
import com.something.dao.service.IMealService;
import com.something.dao.service.ISignService;
import com.something.utils.AncdaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class MealTask implements ApplicationRunner {

    private static final String strMonth = "2024-04";

    private final IMealService mealService;
    private final ISignService signService;
    private final AncdaUtil ancdaUtil;

    public void fetchSignHistoryData() {
        List<String> months = getMonths();
        SignEntity signEntity = signService.getMaxTimeNow();
        if (null == signEntity) {
            return;
        }
        MealEntity mealEntity = mealService.getMaxTimeNow();
        String lastSignTime = signEntity.getTimeNow();

        if (null != mealEntity && mealEntity.getTimeNow().compareTo(lastSignTime) >= 0) {
            return;
        }

        int day = 0;
        String startMonth = months.get(0);
        if (null != mealEntity) {
            String lastDay = mealEntity.getTimeNow();
            day = Integer.parseInt(lastDay.split("-")[2]);
            startMonth = getLastDayOfMonth(lastDay);

            LocalDate date = LocalDate.now().plusDays(-1);
            String today = date.format(DateFormatConstant.STANDARD_FORMAT);
            if (today.equals(lastDay)) {
                return;
            }
        }
        loop(startMonth, day, months);

    }


    public void loop(String startMonth, int day, List<String> months) {
        LocalDate localDate = LocalDate.now();
        int today = localDate.getDayOfMonth();
        String todayMonth = localDate.format(DateFormatConstant.SHORT_FORMAT);
        for (String month : months) {
            if (month.compareTo(startMonth) < 0) {
                continue;
            }
            if (month.equals(todayMonth) && today <= day) {
                return;
            }
            dayLoop(month, todayMonth, day);
        }
    }

    public void dayLoop(String month, String todayMonth, int day) {
        for (int i = 1; i < 6; i++) {
            if (month.equals(todayMonth) && day != 0) {
                if (i < day / 7) {
                    continue;
                }
            }
            getMeat(month, i);
        }
    }

    public void getMeat(String month, int weekNo) {
        String meatResult = ancdaUtil.getMonthMeat(month, weekNo);
        JSONArray arr = JSONObject.parseObject(meatResult).getJSONArray("list");
        if (arr.size() == 0) {
            return;
        }
        for (Object str : arr) {
            JSONObject jsonObject = JSONObject.parseObject(str.toString());
            String cookDate = jsonObject.getString("cookDate");
            SignEntity signEntity = signService.lambdaQuery().eq(SignEntity::getTimeNow, cookDate).one();
            if (null == signEntity) {
                continue;
            }
            JSONArray arr2 = jsonObject.getJSONArray("dishList");
            if (arr2.size() == 0) {
                continue;
            }
            String breakfast = "", launch = "", launchMiddle = "", fruit = "";
            for (Object item : arr2) {
                JSONObject itemObject = JSONObject.parseObject(item.toString());
                String mealName = itemObject.getString("mealName");
                if (MealTyeEnum.BREAKFAST.getDesc().equals(mealName) || MealTyeEnum.BREAKFAST_MIDDLE.getDesc().equals(mealName)) {
                    breakfast += itemObject.getString("dishName") + "、";
                } else if (MealTyeEnum.LAUNCH.getDesc().equals(mealName)) {
                    launch += itemObject.getString("dishName") + "、";
                } else if (MealTyeEnum.LAUNCH_MIDDLE.getDesc().equals(mealName)) {
                    launchMiddle += itemObject.getString("dishName") + "、";
                } else if (MealTyeEnum.FRUIT.getDesc().equals(mealName)) {
                    fruit += itemObject.getString("dishName") + "、";
                }
            }
            MealEntity queryMealEntity = mealService.lambdaQuery().eq(MealEntity::getTimeNow, cookDate).one();
            if (queryMealEntity != null) {
                continue;
            }
            MealEntity mealEntity = new MealEntity();
            mealEntity.setTimeNow(cookDate);
            mealEntity.setBreakfast(breakfast);
            mealEntity.setLunch(launch);
            mealEntity.setLunchMiddle(launchMiddle);
            mealEntity.setFruit(fruit);
            mealEntity.setSignId(signEntity.getId());
            mealEntity.setOrginData(JSONObject.toJSONString(jsonObject));
            mealService.save(mealEntity);
        }
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        fetchSignHistoryData();
    }

    public static String getLastDayOfMonth(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        int lastDayOfMonth = date.lengthOfMonth();
        return date.getDayOfMonth() == lastDayOfMonth ? date.plusMonths(1L).format(DateFormatConstant.SHORT_FORMAT) : date.format(DateFormatConstant.SHORT_FORMAT);
    }

    public List<String> getMonths(){
        List<String> months = new ArrayList<>();
        // Attention 起始时间起始时间与签到一致
        YearMonth start = YearMonth.parse(strMonth, DateFormatConstant.SHORT_FORMAT);
        // 结束时间（当前年月）
        YearMonth end = YearMonth.now();
        YearMonth current = start;
        while (!current.isAfter(end)) {
            months.add(current.format(DateFormatConstant.SHORT_FORMAT));
            current = current.plusMonths(1);
        }
        return months;
    }


}
