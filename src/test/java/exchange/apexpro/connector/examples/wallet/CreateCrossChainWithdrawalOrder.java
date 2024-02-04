package exchange.apexpro.connector.examples.wallet;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.impl.L2OrderSigner;
import exchange.apexpro.connector.model.user.ApiCredential;
import exchange.apexpro.connector.model.user.L2KeyPair;
import exchange.apexpro.connector.model.wallet.WithdrawalFee;
import exchange.apexpro.connector.model.wallet.WithdrawalResult;

import java.math.BigDecimal;

import static exchange.apexpro.connector.constant.ApiConstants.COLLATERAL_ASSET_USDC;
import static exchange.apexpro.connector.constant.ApiConstants.ORDER_SIGNATURE_EXPIRATION_BUFFER_HOURS;

public class CreateCrossChainWithdrawalOrder {

    public static void main(String[] args) {

        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials

        L2KeyPair l2KeyPair = apexProCredentials.l2KeyPair;
        ApiCredential apiCredential = apexProCredentials.apiCredential;
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);

        BigDecimal amount = new BigDecimal("10");
        String clientId = String.valueOf(System.currentTimeMillis());
        long expireTimeInHour = (System.currentTimeMillis() / (60L * 60L * 1000L)) + ORDER_SIGNATURE_EXPIRATION_BUFFER_HOURS;
        long expireTime = expireTimeInHour * 3600L * 1000L;

        String currency = COLLATERAL_ASSET_USDC;
        String address = apexProCredentials.getAddress();
        Long chainId = 97l;
        WithdrawalFee withdrawalFee = syncRequestClient.getWithdrawalFee(currency,amount,chainId);
        BigDecimal fee = withdrawalFee.getWithdrawalFee();

        String signature = L2OrderSigner.signCrossChainWithdraw(l2KeyPair, apiCredential.getAccountId(), amount, clientId, expireTimeInHour, currency, address, fee, chainId);
        WithdrawalResult result = syncRequestClient.createCrossChainWithdrawalOrder(amount, clientId, expireTime, currency, signature, address, fee, chainId);
        System.out.println(result);

    }
}
