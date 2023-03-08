package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.trade.OrderFills;
import org.apache.commons.lang.time.DateUtils;

import java.util.Date;

public class GetFills {
    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        long endTime = System.currentTimeMillis();
        long startTime = DateUtils.addDays(new Date(), -60).getTime();
        OrderFills result = syncRequestClient.getFills("BTC-USDC",  startTime, endTime, 0,100);
        System.out.println("Order fills:" +result);
    }
}
