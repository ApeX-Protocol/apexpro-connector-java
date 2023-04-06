package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;

import java.util.Map;

public class CancelOrderByClientId {
    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        Map<String, String> result = syncRequestClient.cancelOrderByClientId("9dbdcc36-3311-46bf-95fc-b02c059fb8d2");
        System.out.println(result);
    }
}
