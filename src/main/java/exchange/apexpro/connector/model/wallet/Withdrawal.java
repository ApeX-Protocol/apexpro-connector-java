package exchange.apexpro.connector.model.wallet;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Withdrawal {

    private String id;

    private String type;

    private String currencyId;

    private BigDecimal amount;

    private String transactionHash;

    private String status;

    private Long createdTime;

    private Long updatedTime;

    private Long confirmedTime;

    private String clientId;

    private String chainId;

    private BigDecimal fee;
}
