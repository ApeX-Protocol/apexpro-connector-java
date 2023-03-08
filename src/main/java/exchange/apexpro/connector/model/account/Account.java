package exchange.apexpro.connector.model.account;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Account
 */
@Data
public class Account {

    private String id;

    private String starkKey;

    private String positionId;

    private BigDecimal takerFeeRate;

    private BigDecimal makerFeeRate;

    private Long createdTime;

    private List<Wallet> wallets;

    private List<OpenPosition> openPositions;

}
