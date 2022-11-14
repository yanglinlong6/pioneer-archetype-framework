package com.glsx.plat.common.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class MD5Util {

    private static final String hexDigits[] = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }

        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static String encode(String origin) {
        return encode(origin, "UTF-8");
    }

    public static String encode(String origin, String charsetname) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charsetname == null || "".equals(charsetname))
                resultString = byteArrayToHexString(md.digest(resultString
                        .getBytes()));
            else
                resultString = byteArrayToHexString(md.digest(resultString
                        .getBytes(charsetname)));
        } catch (Exception exception) {
        }
        return resultString;
    }

    /**
     * md5加密方法
     *
     * @param str
     * @return
     */
    public static String encrypt(String str) {
        try {
            byte b[] = Coder.encryptMD5(str.getBytes(StandardCharsets.UTF_8));
            StringBuffer buf = new StringBuffer();
            String stmp = "";
            for (int n = 0; n < b.length; n++) {
                stmp = Integer.toHexString(b[n] & 0XFF);
                if (stmp.length() == 1)
                    buf.append("0").append(stmp);
                else
                    buf.append(stmp);
            }
            return buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String MD516(String sourceStr) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try {
                md.update(sourceStr.getBytes("GBK"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
            result = result.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
        }
        return result;
    }

    /**
     * 获取MD5加密的sign
     *
     * @param params
     * @return
     */
    public static String getParamsMD5Sign(Map<Object, Object> params) {
        StringBuffer paramsStr = new StringBuffer();
        for (Map.Entry<Object, Object> param : params.entrySet()) {
            paramsStr.append(param.getKey()).append(param.getValue());
        }
        return MD5Util.encrypt(paramsStr.toString()).toUpperCase();
    }

    /**
     * 32位MD5加密算法
     *
     * @param s
     * @return
     */
    public static String convert32(String s) {
        char hexChars[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            bytes = md.digest();
            int j = bytes.length;
            char[] chars = new char[j * 2];
            int k = 0;
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                chars[k++] = hexChars[b >>> 4 & 0xf];
                chars[k++] = hexChars[b & 0xf];
            }
            return new String(chars);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 16位MD5加密算法
     *
     * @param s
     * @return
     */
    public static String convert16(String s) {
        char hexChars[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] bytes = s.getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            bytes = md.digest();
            int j = bytes.length;
            char[] chars = new char[j * 2];
            int k = 0;
            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                chars[k++] = hexChars[b >>> 4 & 0xf];
                chars[k++] = hexChars[b & 0xf];
            }
            return new String(chars).substring(8, 24);
        } catch (Exception e) {
            return null;
        }
    }

}
