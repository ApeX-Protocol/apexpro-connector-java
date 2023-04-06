package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;

import java.util.Map;

public class CancelAllOpenOrders {
    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        Map<String, String> result = syncRequestClient.cancelAllOpenOrders("BTC-USDC");
        System.out.println(result);
    }
}
