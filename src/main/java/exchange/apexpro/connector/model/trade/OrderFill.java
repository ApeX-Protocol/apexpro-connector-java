package exchange.apexpro.connector.model.trade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderFill {

    private String id;
    private String orderId;
    private String clientOrderId;

    private BigDecimal fee;
    private Boolean isDeleverage;
    private Boolean isLiquidate;
    private BigDecimal liquidateFee;
    private String matchFillId;
    private BigDecimal price;
    private BigDecimal quoteAmount;
    private String side;
    private String orderType;
    private String direction;
    private BigDecimal size;
    private String status;
    private String symbol;

    private Long createdTime;
    private Long updatedTime;

}
