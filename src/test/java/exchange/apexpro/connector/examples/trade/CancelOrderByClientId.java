package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;

import java.util.Map;

public class CancelOrderByClientId {
    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        Map<String, String> result = syncRequestClient.cancelOrderByClientId("3a18cfa5-aeb2-4eda-aa47-47c0feb2457c");
        System.out.println(result);
    }
}
