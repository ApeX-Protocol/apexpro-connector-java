package exchange.apexpro.connector;

import exchange.apexpro.connector.impl.ApiInternalFactory;
import exchange.apexpro.connector.model.enums.EventFrequency;
import exchange.apexpro.connector.model.event.*;
import exchange.apexpro.connector.model.user.ApiCredential;

/***
 * The subscription connector interface, it is used for subscribing any market data
 * update and account change, it is asynchronous, so you must implement the
 * SubscriptionListener interface. The server will push any update to the
 * connector. if connector get the update, the onReceive method will be called.
 */
public interface SubscriptionClient {
    /**
     * Create the subscription connector to subscribe the update from server.
     *
     * @return The instance of synchronous connector.
     */
    static SubscriptionClient create() {
        return create(new SubscriptionOptions());
    }

    /**
     * Create the subscription connector to subscribe the update from server.
     *
     * @param subscriptionOptions The option of subscription connection, see
     *                            {@link SubscriptionOptions}
     * @return The instance of synchronous connector.
     */
    static SubscriptionClient create(SubscriptionOptions subscriptionOptions) {
        return ApiInternalFactory.getInstance().createSubscriptionClient(subscriptionOptions);
    }

    /**
     * Unsubscribe all subscription.
     */
    void unsubscribeAll();

    /**
     * Subscribe diff depth event. If the book depth is updated,
     * server will send the data to connector and onReceive in callback will be called.
     *
     * @param symbol       The symbol, like "BTCUSDC".
     * @param limit        25 or 200
     * @param frequency    H means High frequency, M means middle frequency
     * @param subscriptionListener     The implementation is required. onReceive will be called
     *                     if receive server's update.
     * @param errorHandler The error handler will be called if subscription failed
     *                     or error happen between connector and ApexPro server.
     */
    void subscribeDepthEvent(String symbol, int limit, EventFrequency frequency,
                                         SubscriptionListener<OrderBookEvent> subscriptionListener,
                                         SubscriptionErrorHandler errorHandler);



    /**
     * Subscribe trade event. If there has any trade produced,
     * server will send the data to connector and onReceive in callback will be called.
     *
     * @param symbol       The symbol, like "BTCUSDC".
     * @param frequency    H means High frequency, M means middle frequency
     * @param subscriptionListener     The implementation is required. onReceive will be called
     *                     if receive server's update.
     * @param errorHandler The error handler will be called if subscription failed
     *                     or error happen between connector and ApexPro server.
     */
    void subscribeTradeEvent(String symbol, EventFrequency frequency,
                             SubscriptionListener<TradeEvent> subscriptionListener,
                             SubscriptionErrorHandler errorHandler);



    /**
     * Subscribe ticker event, If there has any information for the symbol that has been updated,
     * server will send the data to connector and onReceive in callback will be called.
     *
     * @param symbol       The symbol, like "BTCUSDC".
     * @param frequency    H means High frequency, M means middle frequency
     * @param subscriptionListener     The implementation is required. onReceive will be called
     *                     if receive server's update.
     * @param errorHandler The error handler will be called if subscription failed
     *                     or error happen between connector and ApexPro server.
     */
    void subscribeTickerEvent(String symbol, EventFrequency frequency,
                             SubscriptionListener<TickerEvent> subscriptionListener,
                             SubscriptionErrorHandler errorHandler);



    /**
     * Subscribe candlestick event.  If there has any information for the symbol that has been updated,
     * server will send the data to connector and onReceive in callback will be called.
     *
     * @param symbol      The symbol, like "BTCUSDC".
     * @param interval    The candlestick interval, Numbers represent minutes, D for Days, M for Month and W for Week — 1 5 15 30 60 120 240 360 720 "D" "M" "W"
     * @param subscriptionListener     The implementation is required. onReceive will be called
     *                     if receive server's update.
     * @param errorHandler The error handler will be called if subscription failed
     *                     or error happen between connector and Binance server.
     */
    void subscribeCandlestickEvent(String symbol,String interval,
                              SubscriptionListener<CandlestickChartEvent> subscriptionListener,
                              SubscriptionErrorHandler errorHandler);

    /**
     * Subscribe all tickers event. If the candlestick is updated,
     * server will send the data to connector and onReceive in callback will be called.
     *
     * @param subscriptionListener     The implementation is required. onReceive will be called
     *                     if receive server's update.
     * @param errorHandler The error handler will be called if subscription failed
     *                     or error happen between connector and Binance server.
     */
    void subscribeAllTickersEvent(SubscriptionListener<AllTickersEvent> subscriptionListener,
                                  SubscriptionErrorHandler errorHandler);



    /**
     * Subscribe user data event. If the user account data is updated,
     * server will send the data to connector and onReceive in callback will be called.
     *
     * @param apiCredential The api credential for identity authentication.
     * @param subscriptionListener     The implementation is required. onReceive will be called
     *                     if receive server's update.
     * @param errorHandler The error handler will be called if subscription failed
     *                     or error happen between connector and Binance server.
     */

    void subscribeAccountDataEvent(ApiCredential apiCredential, SubscriptionListener<AccountDataEvent> subscriptionListener,
                                   SubscriptionErrorHandler errorHandler);


}
