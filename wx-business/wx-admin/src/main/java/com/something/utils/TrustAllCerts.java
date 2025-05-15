package com.something.utils;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class TrustAllCerts implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
        // 信任客户端证书
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
        // 信任服务端证书
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }


}
