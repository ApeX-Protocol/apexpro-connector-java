package exchange.apexpro.connector;

import exchange.apexpro.connector.exception.ApexProApiException;

/**
 * The error handler for the subscription.
 */
@FunctionalInterface
public interface SubscriptionErrorHandler {

  void onError(ApexProApiException exception);
}
