package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.examples.config.PrivateConfig;

import java.util.Map;

public class CancelAllOpenOrders {
    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        Map<String, String> result = syncRequestClient.cancelAllOpenOrders("BTC-USDC", ApiConstants.CONTRACT_ZONE_USDC);
        System.out.println(result);
        result = syncRequestClient.cancelAllOpenOrders("BTC-USDT,ETH-USDT", ApiConstants.CONTRACT_ZONE_USDT);
        System.out.println(result);

    }
}
