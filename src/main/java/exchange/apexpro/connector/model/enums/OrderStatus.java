package exchange.apexpro.connector.model.enums;

import exchange.apexpro.connector.impl.utils.EnumLookup;

public enum OrderStatus {

  PENDING("PENDING"),
  OPEN("OPEN"),
  FILLED("FILLED"),
  CANCELING("CANCELING"),
  CANCELED("CANCELED"),
  UNTRIGGERED("UNTRIGGERED");


  private final String code;

  OrderStatus(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<OrderStatus> lookup = new EnumLookup<>(OrderStatus.class);

  public static OrderStatus lookup(String name) {
    return lookup.lookup(name);
  }
}
