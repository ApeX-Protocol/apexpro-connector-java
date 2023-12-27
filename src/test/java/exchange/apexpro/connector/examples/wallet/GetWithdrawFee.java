package exchange.apexpro.connector.examples.wallet;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.meta.ExchangeInfo;
import exchange.apexpro.connector.model.wallet.WithdrawalFee;

import java.math.BigDecimal;

import static exchange.apexpro.connector.constant.ApiConstants.*;

public class GetWithdrawFee {

    public static void main(String[] args) {
        ExchangeInfo.load();
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);

        ExchangeInfo.multiChain(CONTRACT_ZONE_USDC).getChains().forEach(chain -> {
            WithdrawalFee result = syncRequestClient.getWithdrawalFee(COLLATERAL_ASSET_USDC,new BigDecimal("1000"), chain.getChainId());
            System.out.println("CONTRACT_ZONE_USDC: WithdrawalFee[chainId:"+chain.getChain()+"]:"+result);
        });

        ExchangeInfo.multiChain(CONTRACT_ZONE_USDT).getChains().forEach(chain -> {
            WithdrawalFee result = syncRequestClient.getWithdrawalFee(COLLATERAL_ASSET_USDT,new BigDecimal("1000"), chain.getChainId());
            System.out.println("CONTRACT_ZONE_USDT: WithdrawalFee[chainId:"+chain.getChain()+"]:"+result);
        });


    }
}
