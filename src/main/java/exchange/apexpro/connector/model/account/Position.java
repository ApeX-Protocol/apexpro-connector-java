package exchange.apexpro.connector.model.account;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Position {

    private String symbol;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal size;
    private String side;
    private BigDecimal customInitialMarginRate;
    private BigDecimal maxSize;
    private BigDecimal sumOpen;
    private BigDecimal sumClose;
    private BigDecimal netFunding;
    private String accountId;
    private Long updatedTime;
    private Long closedTime;

}
