package exchange.apexpro.connector.examples.account;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.account.HistoryPnl;
import org.apache.commons.lang.time.DateUtils;

import java.util.Date;

public class GetHistoryPnl {

    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);

        long endTime = System.currentTimeMillis();
        long startTime = DateUtils.addDays(new Date(), -60).getTime();
        HistoryPnl historyPnl = syncRequestClient.getHistoryPnl(startTime, endTime,  "BTC-USDC", 0l, 100);
        System.out.println("History PNL Records:"+historyPnl);
    }

}
