package exchange.apexpro.connector.model.account;

import lombok.Data;

/**
 * User Historial Profit and Loss
 */
@Data
public class HistoryPnlEntry {

    private String symbol;

    private String size;

    private String totalPnl;

    private String price;

    private Long createdAt;

    private Boolean isLiquidate;

    private Boolean isDeleverage;

}
