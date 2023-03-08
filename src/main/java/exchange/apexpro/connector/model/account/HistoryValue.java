package exchange.apexpro.connector.model.account;

import lombok.Data;

import java.util.List;

/**
 * Historical Asset Value
 */
@Data
public class HistoryValue {

    private List<HistoryValueEntry> historyValues;

}
