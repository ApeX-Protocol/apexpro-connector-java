package exchange.apexpro.connector.examples.websocket;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SubscriptionClient;
import exchange.apexpro.connector.SubscriptionOptions;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.user.ApiCredential;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AccountDataStream {


    public static void main(String[] args) {

        SubscriptionOptions subscriptionOptions = new SubscriptionOptions();
        subscriptionOptions.setUri(ApiConstants.APEX_WS_TEST+"/realtime_private");
        SubscriptionClient client = SubscriptionClient.create(subscriptionOptions);

        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials

        client.subscribeAccountDataEvent(apexProCredentials.apiCredential,((event) -> {
            log.info("message:{}",event);
        }), exception -> {
            log.error("exception:{}",exception.getMessage());
        });

    }

}
