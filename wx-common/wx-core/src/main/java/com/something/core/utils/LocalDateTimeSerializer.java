package com.something.core.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @Date 2023/5/19
 * @Version 1.0
 * 格式: yyyy年MM月dd日 HH:mm:ss
 */
public class LocalDateTimeSerializer extends JsonSerializer<Long> {
    @Override
    public void serialize(Long date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (ObjectUtils.isEmpty(date) || date == 0) {
            jsonGenerator.writeString("");
        } else {
            jsonGenerator.writeString(new Date(date).toInstant().atZone(ZoneOffset.of("+8")).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));
        }
    }
}