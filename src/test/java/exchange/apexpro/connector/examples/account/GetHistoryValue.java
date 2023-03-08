package exchange.apexpro.connector.examples.account;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.account.HistoryValue;
import org.apache.commons.lang.time.DateUtils;

import java.util.Date;

public class GetHistoryValue {

    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        long endTime = System.currentTimeMillis();
        long startTime = DateUtils.addDays(new Date(), -60).getTime();
        HistoryValue historyValue = syncRequestClient.getHistoryValue(startTime, endTime);
        System.out.println(historyValue);
    }
}
