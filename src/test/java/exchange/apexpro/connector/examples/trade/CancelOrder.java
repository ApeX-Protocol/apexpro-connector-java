package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;

import java.util.Map;

public class CancelOrder {
    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        Map<String, String> result = syncRequestClient.cancelOrder("415687649278296337");
        System.out.println("Delete order result: "+result);
    }
}
