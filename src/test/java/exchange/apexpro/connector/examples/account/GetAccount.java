package exchange.apexpro.connector.examples.account;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.account.Account;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GetAccount {

    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        Account account = syncRequestClient.getAccount();
        System.out.println("Account info: "+account);
    }
}