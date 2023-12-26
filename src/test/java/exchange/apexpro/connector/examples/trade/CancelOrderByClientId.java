package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;

import java.util.Map;

public class CancelOrderByClientId {
    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        Map<String, String> result = syncRequestClient.cancelOrderByClientId("8c7db6a3-7492-4c3f-8f8f-69f9381329e1");
        System.out.println(result);
    }
}
