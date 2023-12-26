package exchange.apexpro.connector.model.account;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Balance
 */
@Data
public class Balance {

    private String token;
    private BigDecimal totalEquity;
    private BigDecimal available; //Available to withdraw or as margin to open new position.
    private BigDecimal totalInitialMargin;
    private BigDecimal totalMaintenanceMargin;

    public long updatedTime;

}
