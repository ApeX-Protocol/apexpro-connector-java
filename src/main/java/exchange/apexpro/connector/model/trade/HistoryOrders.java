package exchange.apexpro.connector.model.trade;

import lombok.Data;

import java.util.List;

@Data
public class HistoryOrders {

    private List<Order> orders;

    Integer totalSize;

}
