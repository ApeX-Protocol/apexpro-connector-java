package exchange.apexpro.connector.model.trade;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundingRate {

    private String id;
    private String symbol;
    private BigDecimal fundingValue;
    private BigDecimal rate;
    private BigDecimal positionSize;
    private BigDecimal price;
    private String positionSide;
    private String status;
    private Long fundingTime;
    private String transactionId;

}
