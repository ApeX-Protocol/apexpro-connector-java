package exchange.apexpro.connector.model.account;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Position {

    private String symbol;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal size;
    private BigDecimal fee;
    private String side;
    private BigDecimal customInitialMarginRate;
    private BigDecimal maxSize;
    private BigDecimal sumOpen;
    private BigDecimal sumClose;

    private BigDecimal fundingFee;

    private String lightNumbers;
    private String accountId;
    private Long updatedTime;
    private Long closedTime;

}
