package exchange.apexpro.connector.examples.account;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.account.Balance;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetBalance {

    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        Balance balance = syncRequestClient.getBalance();
        System.out.println("Account balance: "+balance);
    }
}