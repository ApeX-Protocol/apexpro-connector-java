package exchange.apexpro.connector;

import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.exception.ApexProApiException;

import java.net.URL;

import static exchange.apexpro.connector.constant.ApiConstants.NETWORKID_MAIN;

/**
 * The configuration for the request APIs
 */
public class RequestOptions {

    private String url = ApiConstants.APEX_HTTP_TEST + "/api";

    private int networkId = ApiConstants.NETWORKID_TEST;
    public RequestOptions() {
    }

    public RequestOptions(RequestOptions option) {
        this.url = option.url;
    }


    /**
     * Set the URL for request.
     */
    private void setUrl(String url) {
        try {
            URL u = new URL(url);
            this.url = u.toString();
        } catch (Exception e) {
            throw new ApexProApiException(ApexProApiException.INPUT_ERROR, "The URI is incorrect: " + e.getMessage());
        }
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
        if (this.networkId == NETWORKID_MAIN) {
            this.url = ApiConstants.APEX_HTTP_MAIN + "/api";
        } else {
            this.url = ApiConstants.APEX_HTTP_TEST + "/api";
        }
    }

}
