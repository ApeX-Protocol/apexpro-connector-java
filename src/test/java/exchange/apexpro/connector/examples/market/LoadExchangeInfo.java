package exchange.apexpro.connector.examples.market;

import exchange.apexpro.connector.model.meta.ExchangeInfo;
import lombok.extern.slf4j.Slf4j;

import static exchange.apexpro.connector.constant.ApiConstants.CONTRACT_AREA_USDC;
import static exchange.apexpro.connector.constant.ApiConstants.CONTRACT_AREA_USDT;

@Slf4j
public class LoadExchangeInfo {


    public static void main(String[] args) {
        ExchangeInfo.load();
        log.info("ExchangeInfo.global:{} ",ExchangeInfo.global(CONTRACT_AREA_USDC));
        log.info("ExchangeInfo.currencies: {}",ExchangeInfo.currencyMap(CONTRACT_AREA_USDC));
        log.info("ExchangeInfo.perpetualContracts: {}", ExchangeInfo.perpetualContractMap(CONTRACT_AREA_USDC));
    }
}
