package exchange.apexpro.connector.impl;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import exchange.apexpro.connector.constant.ApiConstants;
import exchange.apexpro.connector.exception.ApexProApiException;
import exchange.apexpro.connector.impl.utils.JsonWrapper;

public class WebSocketConnection extends WebSocketListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConnection.class);

    private static int connectionCounter = 0;

    public enum ConnectionState {
        IDLE, DELAY_CONNECT, CONNECTED, CLOSED_ON_ERROR
    }

    private WebSocket webSocket = null;

    private volatile long lastReceivedTime = 0;

    private volatile ConnectionState state = ConnectionState.IDLE;
    private int delayInSecond = 0;

    private final WebsocketRequest request;
    private final Request okhttpRequest;
    private final WebSocketWatchDog watchDog;
    private final int connectionId;
    private final boolean autoClose;

    private String subscriptionUrl = ApiConstants.APEX_WS_MAIN;

    WebSocketConnection(WebsocketRequest request,
            WebSocketWatchDog watchDog) {
        this(request, watchDog, false);
    }

    WebSocketConnection(WebsocketRequest request, WebSocketWatchDog watchDog, boolean autoClose) {
        this.connectionId = WebSocketConnection.connectionCounter++;
        this.request = request;
        this.autoClose = autoClose;

        this.okhttpRequest = request.authHandler == null ? new Request.Builder().url(watchDog.getOptions().getUri()).build()
                : new Request.Builder().url(subscriptionUrl).build();
        this.watchDog = watchDog;
        log.info("[Sub] Connection [id: " + this.connectionId + "] created for " + request.name);
    }

    int getConnectionId() {
        return this.connectionId;
    }

    void connect() {
        if (state == ConnectionState.CONNECTED) {
            log.info("[Sub][" + this.connectionId + "] Already connected");
            return;
        }
        log.info("[Sub][" + this.connectionId + "] Connecting...");
        webSocket = RestApiInvoker.createWebSocket(okhttpRequest, this);
    }

    void reConnect(int delayInSecond) {
        log.warn("[Sub][" + this.connectionId + "] Reconnecting after " + delayInSecond + " seconds later");
        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }
        this.delayInSecond = delayInSecond;
        state = ConnectionState.DELAY_CONNECT;
    }

    void reConnect() {
        if (delayInSecond != 0) {
            delayInSecond--;
        } else {
            connect();
        }
    }

    long getLastReceivedTime() {
        return this.lastReceivedTime;
    }

    void send(String str) {
        boolean result = false;
        log.debug("[Send]{}", str);
        if (webSocket != null) {
            result = webSocket.send(str);
        }
        if (!result) {
            log.error("[Sub][" + this.connectionId + "] Failed to send message");
            closeOnError();
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        lastReceivedTime = System.currentTimeMillis();

        log.debug("[On Message]:{}", text);
        try {
            JsonWrapper jsonWrapper = JsonWrapper.parseFromString(text);

            onReceive(jsonWrapper);

        } catch (Exception e) {
            log.error("[On Message][{}]: catch exception:", connectionId, e);
            closeOnError();
        }
    }

    private void onError(String errorMessage, Throwable e) {
        if (request.errorHandler != null) {
            ApexProApiException exception = new ApexProApiException(ApexProApiException.SUBSCRIPTION_ERROR, errorMessage, e);
            request.errorHandler.onError(exception);
        }
        log.error("[Sub][" + this.connectionId + "] " + errorMessage);
    }

    private void onReceiveAndClose(JsonWrapper jsonWrapper) {
        onReceive(jsonWrapper);
        if (autoClose) {
            close();
        }
    }

    @SuppressWarnings("unchecked")
    private void onReceive(JsonWrapper jsonWrapper) {
        Object obj = null;
        try {
            obj = request.jsonParser.parseJson(jsonWrapper);
            if (obj == null)
                return;

        } catch (Exception e) {
            onError("Failed to parse server's response: " + e.getMessage(), e);
        }
        try {
            request.updateCallback.onReceive(obj);
        } catch (Exception e) {
            onError("Process error: " + e.getMessage() + " You should capture the exception in your error handler", e);
        }
    }

    public ConnectionState getState() {
        return state;
    }

    public void close() {
        log.info("[Sub][" + this.connectionId + "] Closing normally");
        webSocket.cancel();
        webSocket = null;
        watchDog.onClosedNormally(this);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        if (state == ConnectionState.CONNECTED) {
            state = ConnectionState.IDLE;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        this.webSocket = webSocket;
        log.info("[Sub][" + this.connectionId + "] Connected to server");
        watchDog.onConnectionCreated(this);
        if (request.connectionHandler != null) {
            request.connectionHandler.handle(this);
        }
        state = ConnectionState.CONNECTED;
        lastReceivedTime = System.currentTimeMillis();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        onError("Unexpected error: "+t.getClass()+ ":" + t.getMessage(), t);
        closeOnError();
    }

    private void closeOnError() {
        if (webSocket != null) {
            this.webSocket.cancel();
            state = ConnectionState.CLOSED_ON_ERROR;
            log.error("[Sub][" + this.connectionId + "] Connection is closing due to error");
        }
    }
}
