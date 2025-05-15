package com.something.constants;

import lombok.Getter;

@Getter
public enum MealTyeEnum {

    BREAKFAST( "早餐"),
    BREAKFAST_MIDDLE( "早点"),
    LAUNCH("午餐"),
    LAUNCH_MIDDLE("午点"),
    FRUIT( "水果");

    private final String desc;

    MealTyeEnum(String desc) {
        this.desc = desc;
    }
}
