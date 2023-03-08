package exchange.apexpro.connector.impl;

import exchange.apexpro.connector.SubscriptionClient;
import exchange.apexpro.connector.SubscriptionErrorHandler;
import exchange.apexpro.connector.SubscriptionListener;
import exchange.apexpro.connector.SubscriptionOptions;
import exchange.apexpro.connector.model.enums.EventFrequency;
import exchange.apexpro.connector.model.event.*;
import exchange.apexpro.connector.model.user.ApiCredential;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class WebSocketStreamClientImpl implements SubscriptionClient {

    private final SubscriptionOptions options;
    private WebSocketWatchDog watchDog;

    private final WebsocketRequestImpl requestImpl;

    private final List<WebSocketConnection> connections = new LinkedList<>();

    WebSocketStreamClientImpl(SubscriptionOptions options) {
        this.watchDog = null;
        this.options = Objects.requireNonNull(options);

        this.requestImpl = new WebsocketRequestImpl();
    }

    private <T> void createConnection(WebsocketRequest<T> request, boolean autoClose) {
        if (watchDog == null) {
            watchDog = new WebSocketWatchDog(options);
        }
        WebSocketConnection connection = new WebSocketConnection(request, watchDog, autoClose);
        if (autoClose == false) {
            connections.add(connection);
        }
        connection.connect();
    }

    private <T> void createConnection(WebsocketRequest<T> request) {
        createConnection(request, false);
    }

    @Override
    public void unsubscribeAll() {
        for (WebSocketConnection connection : connections) {
            watchDog.onClosedNormally(connection);
            connection.close();
        }
        connections.clear();
    }


    @Override
    public void subscribeDepthEvent(String symbol, int limit, EventFrequency frequency,
            SubscriptionListener<OrderBookEvent> subscriptionListener, 
            SubscriptionErrorHandler errorHandler) {
        createConnection(
                requestImpl.subscribeDepthEvent(symbol, limit, frequency, subscriptionListener, errorHandler));
    }


    @Override
    public void subscribeTradeEvent(String symbol, EventFrequency frequency,
                                    SubscriptionListener<TradeEvent> subscriptionListener,
                                    SubscriptionErrorHandler errorHandler) {
        createConnection(
                requestImpl.subscribeTradeEvent(symbol, frequency, subscriptionListener, errorHandler));
    }


    @Override
    public void subscribeTickerEvent(String symbol, EventFrequency frequency,
                              SubscriptionListener<TickerEvent> subscriptionListener,
                              SubscriptionErrorHandler errorHandler) {
        createConnection(
                requestImpl.subscribeTickerEvent(symbol, frequency, subscriptionListener, errorHandler));

    }

    @Override
    public void subscribeCandlestickEvent(String symbol,String interval,
                                   SubscriptionListener<CandlestickChartEvent> subscriptionListener,
                                   SubscriptionErrorHandler errorHandler) {

        createConnection(
                requestImpl.subscribeCandlestickEvent(symbol, interval, subscriptionListener, errorHandler));
    }

    @Override
    public void subscribeAllTickersEvent(
                                  SubscriptionListener<AllTickersEvent> subscriptionListener,
                                  SubscriptionErrorHandler errorHandler) {
        createConnection(
                requestImpl.subscribeAllTickersEvent(subscriptionListener, errorHandler));

    }

    @Override
    public void subscribeAccountDataEvent(ApiCredential apiCredential, SubscriptionListener<AccountDataEvent> subscriptionListener,
                                   SubscriptionErrorHandler errorHandler) {
        createConnection(
                requestImpl.subscribeAccountDataEvent(apiCredential,subscriptionListener, errorHandler));
    }




}
