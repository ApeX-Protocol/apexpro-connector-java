package exchange.apexpro.connector.impl.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import exchange.apexpro.connector.model.enums.EventFrequency;
import exchange.apexpro.connector.model.user.ApiCredential;

import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

public abstract class Channels {


    public static String depthChannel(String symbol, EventFrequency frequency, int limit) {
        JSONObject json = new JSONObject();
        json.put("op", "subscribe");
        JSONArray params = new JSONArray();
        params.add("orderBook"+limit+"."+frequency.name()+"." + symbol);
        json.put("args",params);
        return json.toJSONString();
    }

    public static String tradeChannel(String symbol, EventFrequency frequency) {
        JSONObject json = new JSONObject();
        json.put("op", "subscribe");
        JSONArray params = new JSONArray();
        params.add("recentlyTrade."+frequency.name()+"." + symbol);
        json.put("args",params);
        return json.toJSONString();
    }

    public static String tickerChannel(String symbol, EventFrequency frequency) {
        JSONObject json = new JSONObject();
        json.put("op", "subscribe");
        JSONArray params = new JSONArray();
        params.add("instrumentInfo."+frequency.name()+"." + symbol);
        json.put("args",params);
        return json.toJSONString();
    }

    public static String candlestickChannel(String symbol, String interval) {
        JSONObject json = new JSONObject();
        json.put("op", "subscribe");
        JSONArray params = new JSONArray();
        params.add("candle."+interval+"." + symbol);
        json.put("args",params);
        return json.toJSONString();
    }

    public static String allTickersChannel() {
        JSONObject json = new JSONObject();
        json.put("op", "subscribe");
        JSONArray params = new JSONArray();
        params.add("instrumentInfo.all");
        json.put("args",params);
        return json.toJSONString();
    }


    public static String signeChannel(ApiCredential apiCredential) {

        long timestamp = Calendar.getInstance().getTimeInMillis();
        String httpMethod = "GET";
        String requestPath = "/ws/accounts";
        Map<String, Object> data = new TreeMap<>();
        String signature = ApiSignHelper.sign(apiCredential.getSecret(),requestPath,httpMethod,timestamp,data);

        JSONObject json = new JSONObject();
        json.put("op", "login");
        JSONArray params = new JSONArray();


        JSONObject loginJson = new JSONObject();
        JSONArray topics = new JSONArray();
        topics.add("ws_accounts_v2");
        loginJson.put("type","login");
        loginJson.put("topics",topics);

        loginJson.put("httpMethod",httpMethod);
        loginJson.put("requestPath",requestPath);
        loginJson.put("apiKey",apiCredential.getApiKey());
        loginJson.put("passphrase",apiCredential.getPassphrase());
        loginJson.put("timestamp",timestamp);
        loginJson.put("signature",signature);
        params.add(loginJson.toJSONString());

        json.put("args",params);
        return json.toJSONString();
    }



    public static String accountDataChannel() {
        JSONObject json = new JSONObject();
        json.put("op", "subscribe");
        JSONArray params = new JSONArray();
        params.add("ws_accounts_v1");
        json.put("args",params);
        return json.toJSONString();
    }


}