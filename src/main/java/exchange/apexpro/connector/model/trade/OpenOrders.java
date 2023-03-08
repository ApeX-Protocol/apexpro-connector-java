package exchange.apexpro.connector.model.trade;

import lombok.Data;

import java.util.List;

@Data
public class OpenOrders {

    private List<Order> orders;

}
