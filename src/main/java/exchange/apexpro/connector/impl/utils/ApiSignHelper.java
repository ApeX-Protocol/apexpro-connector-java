package exchange.apexpro.connector.impl.utils;

import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

public class ApiSignHelper {

    //private static ExecutorService fixedThreadPoolExecutor = Executors.newFixedThreadPool(10);

    public static String urlBase64Encode(byte[] data){
        return Base64.getEncoder().encodeToString(data)
                .replaceAll("=", "")
                .replaceAll("\\+", "-")
                .replaceAll("/", "_");
    }



    public static String sign(String secret, String path, String method, Long timestamp, Map<String, Object> data) {
        StringBuffer messageString = new StringBuffer();
        StringBuffer bodyString = new StringBuffer();

        Map<String, Object> orderedMap = new TreeMap<>();
        orderedMap.putAll(data);

        for (Map.Entry entry : orderedMap.entrySet()) {
            bodyString.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        if (bodyString.length() > 1)
            bodyString.deleteCharAt(bodyString.length() - 1);

        String body;

        if (data.size() > 0) {
            body = bodyString.toString();
            messageString.append(timestamp).append(method);
            messageString.append(path);

            if (method.equals("POST"))
                messageString.append(body);
            else
                messageString.append("?").append(body);
        } else {
            messageString
                    .append(timestamp)
                    .append(method)
                    .append(path);
        }
        return ApiCredentialHelper.createHmac(secret, messageString.toString());
    }

}
