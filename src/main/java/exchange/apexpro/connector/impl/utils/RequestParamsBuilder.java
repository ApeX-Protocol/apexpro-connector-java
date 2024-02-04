package exchange.apexpro.connector.impl.utils;

import exchange.apexpro.connector.exception.ApexProApiException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.RequestBody;

public class RequestParamsBuilder {

    class ParamsMap {

        final Map<String, Object> postMap = new LinkedHashMap<>();
        final Map<String, Object> urlParams = new HashMap<>();
        final Map<String, String> headers = new HashMap<>();

        void putToPost(String name, Object value) {

            if (name == null) {
                throw new ApexProApiException(ApexProApiException.RUNTIME_ERROR, "[URL] Key can not be null");
            }
            if (value == null) {
                return;
            }

            this.postMap.put(name, value);
        }

        void putToUrlParam(String name, Object value) {
            if (name == null) {
                throw new ApexProApiException(ApexProApiException.RUNTIME_ERROR, "[URL] Key can not be null");
            }
            if (value == null) {
                return;
            }
            this.urlParams.put(name, value);
        }

        void putToHeader(String name, String value) {
            this.headers.put(name, value);
        }

    }


    private final ParamsMap paramsMap = new ParamsMap();


    public static RequestParamsBuilder build() {
        return new RequestParamsBuilder();
    }

    private RequestParamsBuilder() {
    }


    public RequestParamsBuilder putToUrl(String name, String value) {
        this.paramsMap.putToUrlParam(name, value);
        return this;
    }

    public RequestParamsBuilder putToHeader(String name, String value) {
        paramsMap.headers.put(name, value);
        return this;
    }
    public boolean hasPostData() {
        if (paramsMap.postMap.size() > 0)
            return true;
        return false;
    }

    public Map<String,Object> getPostData() {
            return paramsMap.postMap;
    }

    public boolean hasGetData() {
        if (paramsMap.urlParams.size() > 0)
            return true;
        return false;
    }

    public Map<String,Object> getGetData() {
        return paramsMap.urlParams;
    }


    public boolean hasHeader(){
        if (paramsMap.headers.size() > 0)
            return true;
        return false;
    }

    public RequestParamsBuilder putToPost(String name, Object value) {
        paramsMap.putToPost(name, value);
        return this;
    }

    public String buildUrl() {
        StringBuilder builder = new StringBuilder("");

        Map<String, Object> orderedMap = new TreeMap<>();
        orderedMap.putAll(paramsMap.urlParams);
        orderedMap.forEach((k,v) -> {
            if (v instanceof List) {
                ((List<?>) v).forEach(i -> {
                    builder.append(k).append("[]").append("=").append(v).append("&");
                });
            }else {
                builder.append(k).append("=").append(v).append("&");
            }
        } );
        String params = builder.toString();
        if (params.equals(""))
            return "";
        return "?" + (params.endsWith("&") ? params.substring(0,params.length() - 1) : params );
    }



    public RequestBody buildPostBody() {

        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        paramsMap.postMap.forEach((k,v) -> {

            if (v instanceof List) {
                ((List<?>) v).forEach(i -> formBodyBuilder.add(k+"[]",v.toString()));
            }else {
                formBodyBuilder.add(k,v.toString());
            }
        });

        return formBodyBuilder.build();
    }

    public Headers buildHeaders() {
        Headers headers = Headers.of(this.paramsMap.headers);
        return headers;
    }

    /**
     * Encode using standard URL Encode. Note that unlike the JDK default, whitespace is encoded as %20 instead of +.
     *
     * @param s String as input
     * @return URL encoded string
     */
    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new ApexProApiException(ApexProApiException.RUNTIME_ERROR, "[URL] UTF-8 encoding not supported!");
        }
    }
}
