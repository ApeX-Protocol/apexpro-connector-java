package exchange.apexpro.connector.impl.utils;

@FunctionalInterface
public interface Handler<T> {

  void handle(T t);
}
