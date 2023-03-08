package exchange.apexpro.connector.examples.websocket;

import exchange.apexpro.connector.SubscriptionClient;
import exchange.apexpro.connector.SubscriptionOptions;
import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.model.enums.EventFrequency;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MarketSubscription {


    public static void main(String[] args) throws InterruptedException {
        SubscriptionOptions subscriptionOptions = new SubscriptionOptions();
        subscriptionOptions.setUri(ApiConstants.APEX_WS_TEST+"/realtime_public");
        SubscriptionClient client = SubscriptionClient.create(subscriptionOptions);

        //depth subscription;
        client.subscribeDepthEvent("BTCUSDC",200, EventFrequency.H, ((event) -> {
            log.info("message:"+event);
        }), exception -> {
            log.error("exception:"+exception.getMessage());
        });

        //trades subscription
        client.subscribeTradeEvent("BTCUSDC", EventFrequency.H, ((event) -> {
            log.info("message:"+event);
        }), exception -> {
            log.error("exception:"+exception.getMessage());
        });

        //ticker subscription
        client.subscribeTickerEvent("BTCUSDC", EventFrequency.H, ((event) -> {
            log.info("message:"+event);
        }), exception -> {
            log.error("exception:"+exception.getMessage());
        });
        //candlestick chart subscription
        client.subscribeCandlestickEvent("BTCUSDC", "3", ((event) -> {
            log.info("message:"+event);
        }), exception -> {
            log.error("exception:"+exception.getMessage());
        });

        //all tickers subscription
        client.subscribeAllTickersEvent(((event) -> {
            log.info("message:"+event);
        }), exception -> {
            log.error("exception:"+exception.getMessage());
        });

        Thread.sleep(1000l * 30l);
        client.unsubscribeAll();
    }

}
