package exchange.apexpro.connector.model.account;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Account
 */
@Data
public class ContractAreaConfig {

    private String contractArea;
    private BigDecimal takerFeeRate;
    private BigDecimal makerFeeRate;
    private BigDecimal minInitialMarginRate;
    private Long createdTime;
}
