package com.something.utils;

import com.something.core.exception.MyException;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.io.CharStreams;
import okhttp3.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class OkHttpUtils {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final MediaType FORM = MediaType.get("application/x-www-form-urlencoded");

    private static final OkHttpClient client;


    static {
        // 初始化 OkHttpClient，配置超时、连接池等
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(6, 1, TimeUnit.MINUTES))
                .build();
    }


    /**
     * 发送 GET 请求
     *
     * @param url 请求地址
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        return execute(request);
    }

    /**
     * 发送 GET 请求（带请求头）
     *
     * @param url     请求地址
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String getWithHeaders(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        Request request = builder.build();
        return execute(request);
    }

    /**
     * 发送 GET 请求（带请求头）
     *
     * @param url     请求地址
     * @param headers 请求头
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String getWithHeadersAndUnzipBody(String url, Map<String, String> headers) {
        Request.Builder builder = new Request.Builder().url(url);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        Request request = builder.build();
        return getUnzipBody(request);
    }

    /**
     * 发送 POST 请求（JSON 格式）
     *
     * @param url  请求地址
     * @param json JSON 请求体
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String postJson(String url, JSONObject json) {
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return execute(request);
    }

    /**
     * 发送 POST 请求（表单格式）
     *
     * @param url    请求地址
     * @param params 表单参数
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String postForm(String url, Map<String, String> params) {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody body = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        return execute(request);
    }

    /**
     * 发送 PUT 请求（JSON 格式）
     *
     * @param url  请求地址
     * @param json JSON 请求体
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String putJson(String url, JSONObject json) {
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        return execute(request);
    }

    /**
     * 发送 DELETE 请求
     *
     * @param url 请求地址
     * @return 响应结果
     * @throws IOException 网络异常
     */
    public static String delete(String url) {
        Request request = new Request.Builder()
                .url(url)
                .delete()
                .build();
        return execute(request);
    }

    /**
     * 异步发送请求（GET 示例）
     *
     * @param url      请求地址
     * @param callback 回调处理
     */
    public static void asyncGet(String url, Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(callback);
    }

    /**
     * 同步下载图片
     *
     * @param imageUrl
     * @param savePath
     * @throws IOException
     */
    public static void downloadImageSync(String imageUrl, String savePath) throws IOException {
        Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }


            // 写入文件
            File file = new File(savePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs(); // 创建父目录
            }

            try (InputStream inputStream = body.byteStream();
                 FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            System.out.println("Image saved to: " + savePath);
        }
    }

    /**
     * 异步下载图片
     *
     * @param imageUrl
     * @param savePath
     * @param callback
     */
    public static void downloadImageAsync(String imageUrl, String savePath, Callback callback) {
        Request request = new Request.Builder()
                .url(imageUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }


                ResponseBody body = response.body();
                if (body == null) {
                    throw new IOException("Empty response body");
                }

                File file = new File(savePath);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                try (InputStream inputStream = body.byteStream();
                     FileOutputStream outputStream = new FileOutputStream(file)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    callback.onResponse(call, response);
                }
            }
        });
    }


    /**
     * 执行请求并返回响应结果
     *
     * @param request 请求对象
     * @return 响应结果
     * @throws IOException 网络异常
     */
    private static String execute(Request request) {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Empty response body");
            }
            return new String(body.bytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            //todo something
            throw new RuntimeException(e);
        }
    }

    private static String getUnzipBody(Request request) {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            ResponseBody body = response.body();
            // 手动解压 GZIP
            try (InputStream inputStream = new GZIPInputStream(Objects.requireNonNull(body).byteStream());) {
                return CharStreams.toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new MyException(500, "读取body失败");
            }
        } catch (MyException e) {
            throw new MyException(e.getCode(), e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
