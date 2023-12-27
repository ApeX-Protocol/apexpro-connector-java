package exchange.apexpro.connector.model.account;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Account
 */
@Data
public class ContractZoneConfig {

    private String contractZone;
    private BigDecimal takerFeeRate;
    private BigDecimal makerFeeRate;
    private BigDecimal minInitialMarginRate;
    private Long createdTime;
}
