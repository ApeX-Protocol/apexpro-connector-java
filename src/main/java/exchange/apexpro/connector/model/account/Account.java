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
    private String starkKey; // This is an alias of L2Key.
    private String positionId;
    private List<ContractZoneConfig> contractZoneConfigList;
    private Long createdTime;
    private List<Wallet> wallets;
    private List<Position> openPositions;

}
