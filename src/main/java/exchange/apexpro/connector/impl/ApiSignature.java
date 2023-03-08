package exchange.apexpro.connector.impl;

import com.alibaba.fastjson.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

class ApiSignature {

    public String createSignature(String secret, String path, String method, Long timestamp, JSONObject data) {
        StringBuffer messageString = new StringBuffer();
        StringBuffer bodyString = new StringBuffer();
        for (Map.Entry entry : data.entrySet()) {
            bodyString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        String body = null;
        if (data.size() > 0) {
            body = bodyString.toString();
            body = body.substring(0, body.length() - 1);
            messageString
                    .append(timestamp)
                    .append(method)
                    .append(path)
                    .append(body);
        } else {
            messageString
                    .append(timestamp)
                    .append(method)
                    .append(path);
        }
        return createHmac(secret, messageString.toString());
    }

    public static String createHmac(String secretKey, String data) {
        byte[] hmacSha256 = null;
        String secretKeyBase64 = Base64.getEncoder().encodeToString(secretKey.getBytes());
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBase64.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            hmacSha256 = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hmac-sha256", e);
        }
        return Base64.getEncoder().encodeToString(hmacSha256);
    }


}
