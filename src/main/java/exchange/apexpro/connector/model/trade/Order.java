package exchange.apexpro.connector.model.trade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Order {

    private String orderId;

    private String clientOrderId;

    private String accountId;

    private String symbol;

    private String side;

    private BigDecimal price;

    private BigDecimal limitFee;

    private BigDecimal fee;
    private BigDecimal liquidateFee;
    private BigDecimal size;

    private String type;
    private BigDecimal triggerPrice;
    private String triggerPriceType;

    private Long unfillableAt;

    private Long createdTime;

    private Long updatedTime;

    private Long expiresTime;

    private String status;

    private String timeInForce;

    private Boolean postOnly;

    private Boolean reduceOnly;

    private BigDecimal latestMatchFillPrice;

    private BigDecimal cumMatchFillSize;

    private BigDecimal cumMatchFillValue;

    private BigDecimal cumMatchFillFee;

    private BigDecimal cumSuccessFillSize;

    private BigDecimal cumSuccessFillValue;
    private BigDecimal cumSuccessLiquidateFee;

    private BigDecimal cumSuccessFillFee;

    private  Boolean isDeleverage; //True means this is a deleverage order produced by system;
    private  Boolean isLiquidate;  //True means this is a liquidation order produced by system;
    private Boolean isPositionTpsl; //Order is a Take-profit or Stop-Loss on true, otherwise is a normal order;
}
