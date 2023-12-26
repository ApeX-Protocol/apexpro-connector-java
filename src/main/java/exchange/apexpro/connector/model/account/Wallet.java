package exchange.apexpro.connector.model.account;

import lombok.Data;

/**
 * Wallet
 */
@Data
public class Wallet {

    private String token;

    private String balance;

    private String pendingDepositAmount;

    private String pendingWithdrawAmount;

    private String pendingTransferOutAmount;

    private String pendingTransferInAmount;

}
