package exchange.apexpro.connector.examples.wallet;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.meta.ExchangeInfo;
import exchange.apexpro.connector.model.wallet.WithdrawalFee;

import java.math.BigDecimal;

public class GetWithdrawFee {

    public static void main(String[] args) {
        ExchangeInfo.load();
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);

        ExchangeInfo.multiChain().getChains().forEach(chain -> {
            WithdrawalFee result = syncRequestClient.getWithdrawalFee(new BigDecimal("1000"), chain.getChainId());
            System.out.println("WithdrawalFee[chainId:"+chain.getChain()+"]:"+result);
        });
    }
}
