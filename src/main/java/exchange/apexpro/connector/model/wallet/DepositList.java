package exchange.apexpro.connector.model.wallet;

import lombok.Data;

import java.util.List;

@Data
public class DepositList {

    private List<Deposit> deposits;

    private Integer totalSize;

}
