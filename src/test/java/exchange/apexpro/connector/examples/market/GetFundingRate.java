package exchange.apexpro.connector.examples.market;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.trade.FundingRates;

public class GetFundingRate {


    public static void main(String[] args) {
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.loadConfig().apiCredential);
        FundingRates fundingRates = syncRequestClient.getFundingRate("BTC-USDC",100,0l,1l,1l,null);
        System.out.println("FundingRates: "+fundingRates);
    }
}
