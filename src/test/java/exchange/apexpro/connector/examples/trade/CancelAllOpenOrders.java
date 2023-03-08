package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;

import java.util.Map;

public class CancelAllOpenOrders {
    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        Map<String, String> result = syncRequestClient.cancelAllOpenOrders("BTC-USDC");
        System.out.println(result);
    }
}
