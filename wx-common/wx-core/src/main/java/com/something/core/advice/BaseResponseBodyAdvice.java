package com.something.core.advice;

import com.something.core.constant.ResultMsg;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @Date 2023/5/19
 * @Version 1.0
 * @ref https://juejin.cn/post/7023751970531835918
 */

@RestControllerAdvice
public class BaseResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return !methodParameter.getDeclaringClass().getName().contains("springfox");
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        // 遇到feign接口之类的请求, 不应该再次包装,应该直接返回
        // 上述问题的解决方案: 可以在feign拦截器中,给feign请求头中添加一个标识字段, 表示是feign请求
        // 在此处拦截到feign标识字段, 则直接放行 返回body.
        if (body instanceof ResultMsg) {
            return body;
        } else if (body instanceof String) {
            return new ResultMsg<>(body.toString());
        } else {
            return new ResultMsg<>(body);
        }
    }
}
