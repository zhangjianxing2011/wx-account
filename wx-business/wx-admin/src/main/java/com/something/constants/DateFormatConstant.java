package com.something.constants;

import java.time.format.DateTimeFormatter;

public class DateFormatConstant {
    public final static DateTimeFormatter SHORT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");
    public final static DateTimeFormatter STANDARD_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
}
