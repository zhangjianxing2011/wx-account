package com.something.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Created by bootvue@gmail.com
 * Date 2019-04-16 14:24
 * <p>
 * AES加密 解密
 * CBC模式, 构造方法要提供16位的key与iv
 */
public class AesUtil {
    private static final String key = "8AcOuVAyVtbDG05F";
    private static final String iv = "2loMHb8sbZS0XXvc";
    // 加密
    public static String encrypt(String origin, String key, String iv) throws Exception {
        return aes(origin, key, iv, Cipher.ENCRYPT_MODE);
    }

    // 解密
    public static String decrypt(String cipher, String key, String iv) throws Exception {
        return aes(cipher, key, iv, Cipher.DECRYPT_MODE);
    }

    private static String aes(String content, String key, String iv, int type) throws Exception {
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv_ = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(type, skeySpec, iv_);
        if (Cipher.ENCRYPT_MODE == type) {
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return new String(Base64.encodeBase64(encrypted)); // 此处使用BASE64做转码。
        }
        byte[] original = cipher.doFinal(Base64.decodeBase64(content));
        return new String(original, StandardCharsets.UTF_8);
    }


    public static void main(String[] args) throws Exception {
        String encrypt = AesUtil.encrypt("-----BEGIN PRIVATE KEY-----\n" +
                "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQg37p5rWnaN27bJuS3\n" +
                "wuc6x3Qin3fxkjtK45KWXwEywdegCgYIKoZIzj0DAQehRANCAARzM4xIlk2Tvv+T\n" +
                "WmOHSi3kkM/XIJNyiQBn6X3dPOIQa2IgSCEXgVqJGYSAaEc/ZDve0/SsUB5HFAqf\n" +
                "E481fZt8\n" +
                "-----END PRIVATE KEY-----",key,  iv);

        System.out.println(encrypt);
        System.out.println(AesUtil.decrypt("4RB2sV5xSrgbTeedoyaNvO8uI5J8UlqbhtsUzrvF0V3TlsolqfalMimaUpBk4ofNPxOigFKjkFU7fgQve5SZj/k+33UDKDTBzQUZh6L8ADynyA35MSo5lgY/591meLOK5ret3bYHkyE0KlxFC5Je+eE8557nAE0OKH5IjrN8UqE5BsI1BC1Y0QH0NfjSTyCVilCd7/oGoBJXavJiN4tfzdMbzAjgh9iFrC3MZeQjfFTydZh9jTYH6xKwj8+gdZ8ZzbRr1J5RpxcSKiU5ieK6FHE5vU4eLmLUa9oy59qJeJ09awgNNXqoUNRtBVCa3oj8fQeJ4bNBANUf+89JhoZFm4y8rz/N+Ce7dWLKbnX4Id0=", key, iv));
    }
}
