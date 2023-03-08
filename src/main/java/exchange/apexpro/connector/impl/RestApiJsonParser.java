package exchange.apexpro.connector.impl;

import exchange.apexpro.connector.impl.utils.JsonWrapper;

@FunctionalInterface
public interface RestApiJsonParser<T> {

  T parseJson(JsonWrapper json);
}
