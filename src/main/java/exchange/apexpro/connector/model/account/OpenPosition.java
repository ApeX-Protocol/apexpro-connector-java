package exchange.apexpro.connector.model.account;

import lombok.Data;

/**
 * Open positions
 */
@Data
public class OpenPosition {

    private String symbol;

    private String side;

    private String size;

    private String entryPrice;

    private String fee;

    private String fundingFee;

    private String createdAt;

    private String updatedTime;

    private String lightNumbers;

}
