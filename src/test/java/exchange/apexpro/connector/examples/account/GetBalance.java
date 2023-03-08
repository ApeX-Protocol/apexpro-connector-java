package exchange.apexpro.connector.examples.account;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.account.Balance;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetBalance {

    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        Balance balance = syncRequestClient.getBalance();
        System.out.println("Account balance: "+balance);
    }
}