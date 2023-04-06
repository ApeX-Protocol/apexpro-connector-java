package exchange.apexpro.connector.model.trade;

import exchange.apexpro.connector.model.enums.OrderSide;
import exchange.apexpro.connector.model.enums.PriceType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderParams {
    private String clientOrderId;
    private OrderSide side;
    private BigDecimal size;
    private BigDecimal price;
    private BigDecimal triggerPrice;
    private PriceType triggerPriceType;
}
