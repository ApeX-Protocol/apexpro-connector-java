package exchange.apexpro.connector.model.wallet;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalFee {

    private BigDecimal withdrawalFee; //Fees calculated
    private BigDecimal poolAvailableAmount; //Available balance in pool to withdraw

}
