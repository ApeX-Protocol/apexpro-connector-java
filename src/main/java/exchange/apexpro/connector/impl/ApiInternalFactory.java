package exchange.apexpro.connector.impl;

import exchange.apexpro.connector.RequestOptions;
import exchange.apexpro.connector.SubscriptionClient;
import exchange.apexpro.connector.SubscriptionOptions;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.model.user.ApiCredential;

public final class ApiInternalFactory {

    private static final ApiInternalFactory instance = new ApiInternalFactory();

    public static ApiInternalFactory getInstance() {
        return instance;
    }

    private ApiInternalFactory() {
    }

    public SyncRequestClient createSyncRequestClient(ApiCredential credential, RequestOptions options) {
        RequestOptions requestOptions = new RequestOptions(options);
        RestApiRequestImpl requestImpl = new RestApiRequestImpl(credential, requestOptions);
        return new SyncRequestImpl(requestImpl);
    }

    public SubscriptionClient createSubscriptionClient(SubscriptionOptions options) {
        SubscriptionOptions subscriptionOptions = new SubscriptionOptions(options);
        SubscriptionClient webSocketStreamClient = new WebSocketStreamClientImpl(subscriptionOptions);
        return webSocketStreamClient;
    }

}
