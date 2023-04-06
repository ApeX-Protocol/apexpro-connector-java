package exchange.apexpro.connector.examples.market;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.trade.FundingRates;

public class GetFundingRate {


    public static void main(String[] args) {
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);
        FundingRates fundingRates = syncRequestClient.getFundingRate("BTC-USDC",100,0l,1l,1l,null);
        System.out.println("FundingRates: "+fundingRates);
    }
}
