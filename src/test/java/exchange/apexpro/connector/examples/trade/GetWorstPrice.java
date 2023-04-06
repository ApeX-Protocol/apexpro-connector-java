package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.enums.OrderSide;
import exchange.apexpro.connector.model.market.OrderBookPrice;

import java.math.BigDecimal;

public class GetWorstPrice {

    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        String symbol = "BTC-USDC";
        BigDecimal orderSize = new BigDecimal(0.5);
        OrderSide orderSide = OrderSide.SELL;
        OrderBookPrice orderBookPrice = syncRequestClient.getWorstPrice(symbol,orderSize,orderSide);
        System.out.println("OrderBookPrice: "+orderBookPrice);
    }


}
