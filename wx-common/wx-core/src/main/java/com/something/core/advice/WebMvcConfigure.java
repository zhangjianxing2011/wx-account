package com.something.core.advice;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @ClassName WebMvcConfigure
 * @Version 1.0
 */
@Configuration
public class WebMvcConfigure implements WebMvcConfigurer {

    /**
     * 解决跨域问题
     * <p>
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowCredentials(false)
                .allowedMethods("*")
                .maxAge(3600);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 第一种方式是将 json 处理的转换器放到第一位，使得先让 json 转换器处理返回值，这样 String转换器就处理不了了。
//        converters.add(0, new MappingJackson2HttpMessageConverter());
        // 第二种就是把String类型的转换器去掉，不使用String类型的转换器
        // 添加 XML 转换器
        converters.add(new Jaxb2RootElementHttpMessageConverter());
        converters.add(new MappingJackson2XmlHttpMessageConverter());
        converters.removeIf(httpMessageConverter -> httpMessageConverter.getClass() == StringHttpMessageConverter.class);
    }

    /**
     * mvc 静态资源路径访问权限配置
     * 例如:registry.addResourceHandler("/upload/**").addResourceLocations("classpath:/upload/");
     * /upload/ 路径下的静态资源可以访问
     * <p>
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //所有目录都可以访问
        registry.addResourceHandler("/static/**").
                addResourceLocations("file:/usr/share/nginx/images/");

        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
    }

}
