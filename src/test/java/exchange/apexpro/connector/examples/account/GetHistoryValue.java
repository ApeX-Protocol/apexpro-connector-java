package exchange.apexpro.connector.examples.account;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.account.HistoryValue;
import org.apache.commons.lang.time.DateUtils;

import java.util.Date;

public class GetHistoryValue {

    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        long endTime = System.currentTimeMillis();
        long startTime = DateUtils.addDays(new Date(), -60).getTime();
        HistoryValue historyValue = syncRequestClient.getHistoryValue(startTime, endTime);
        System.out.println(historyValue);
    }
}
