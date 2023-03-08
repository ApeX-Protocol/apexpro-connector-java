package exchange.apexpro.connector.impl;

import exchange.apexpro.connector.SubscriptionOptions;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class WebSocketWatchDog {

    private final CopyOnWriteArrayList<WebSocketConnection> TIME_HELPER = new CopyOnWriteArrayList<>();


    private final SubscriptionOptions options;
    private static final Logger log = LoggerFactory.getLogger(WebSocketConnection.class);

    WebSocketWatchDog(SubscriptionOptions subscriptionOptions) {
        this.options = Objects.requireNonNull(subscriptionOptions);
        long t = 1_000;
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(() -> {
            TIME_HELPER.forEach(connection -> {
                if (connection.getState() == WebSocketConnection.ConnectionState.CONNECTED) {
                    // Check response
                    if (options.isAutoReconnect()) {
                        long ts = System.currentTimeMillis() - connection.getLastReceivedTime();
                        if (ts > options.getReceiveLimitMs()) {
                            log.warn("[Sub][" + connection.getConnectionId() + "] No response from server");
                            connection.reConnect(options.getConnectionDelayOnFailure());
                        }
                    }
                } else if (connection.getState() == WebSocketConnection.ConnectionState.DELAY_CONNECT) {
                    connection.reConnect();
                } else if (connection.getState() == WebSocketConnection.ConnectionState.CLOSED_ON_ERROR) {
                    if (options.isAutoReconnect()) {
                        connection.reConnect(options.getConnectionDelayOnFailure());
                    }
                }
            });
        }, t, t, TimeUnit.MILLISECONDS);

        exec.scheduleAtFixedRate( () ->{
            TIME_HELPER.forEach(connection -> {
                if (connection.getState() == WebSocketConnection.ConnectionState.CONNECTED) {
                    // Check response
                    connection.send("{\"op\":\"pong\",\"args\":[\""+ Calendar.getInstance().getTimeInMillis() +"\"]}");
                }
            });
        },t * 15,t * 15l,TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(exec::shutdown));
    }

    void onConnectionCreated(WebSocketConnection connection) {
        TIME_HELPER.addIfAbsent(connection);
    }

    void onClosedNormally(WebSocketConnection connection) {
        TIME_HELPER.remove(connection);
    }

    public SubscriptionOptions getOptions() {
        return options;
    }

}
