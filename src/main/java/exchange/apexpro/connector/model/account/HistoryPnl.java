package exchange.apexpro.connector.model.account;

import lombok.Data;

import java.util.List;

/**
 * User Historial Profit and Loss
 */
@Data
public class HistoryPnl {

    private List<HistoryPnlEntry> historicalPnl;

    private Integer totalSize;

}

