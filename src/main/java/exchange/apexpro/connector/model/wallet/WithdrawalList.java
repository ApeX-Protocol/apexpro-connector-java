package exchange.apexpro.connector.model.wallet;

import lombok.Data;

import java.util.List;

@Data
public class WithdrawalList {

    private List<Withdrawal> withdrawals;

    private Integer totalSize;

}
