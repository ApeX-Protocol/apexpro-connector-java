package exchange.apexpro.connector.model.trade;

import lombok.Data;

import java.util.List;

@Data
public class OrderFills {

    private List<OrderFill> orderFills;

    Integer totalSize;

}
