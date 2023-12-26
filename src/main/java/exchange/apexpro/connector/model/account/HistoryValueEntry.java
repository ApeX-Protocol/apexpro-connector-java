package exchange.apexpro.connector.model.account;

import lombok.Data;

/**
 * Historical Asset Value
 */
@Data
public class HistoryValueEntry {

    private String accountTotalValue;

    private Long dateTime;

    private String collateralToken;
    
}
