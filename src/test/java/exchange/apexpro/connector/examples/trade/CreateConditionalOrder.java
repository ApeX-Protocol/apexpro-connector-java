package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.impl.L2OrderSigner;
import exchange.apexpro.connector.model.enums.OrderSide;
import exchange.apexpro.connector.model.enums.OrderType;
import exchange.apexpro.connector.model.enums.PriceType;
import exchange.apexpro.connector.model.enums.TimeInForce;
import exchange.apexpro.connector.model.meta.ExchangeInfo;
import exchange.apexpro.connector.model.trade.Order;
import exchange.apexpro.connector.model.user.ApiCredential;
import exchange.apexpro.connector.model.user.L2KeyPair;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
public class CreateConditionalOrder {
    public static void main(String[] args) throws IOException {
        //Initialize the exchange configuration information. This is optional because it will be loaded automatically when you call its internal member variables.
        ExchangeInfo.load();


        //Prepare an order;
        String symbol = "BTC-USDT";
        String clientId = UUID.randomUUID().toString();
        BigDecimal size = new BigDecimal("0.002");
        PriceType triggerPriceType = PriceType.ORACLE;
        BigDecimal triggerPrice = new BigDecimal("23300");
        BigDecimal orderPrice = new BigDecimal("23300");
        BigDecimal maxFeeRate = new BigDecimal("0.0005"); //you can get this value through max(Account.takerFeeRate,Account.makerFeeRate)

        //Send conditional order to ApeXPro
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);

        Order order = syncRequestClient.createConditionalOrder(
                symbol,
                OrderSide.BUY,
                OrderType.LIMIT,
                size,
                triggerPrice,
                triggerPriceType,
                orderPrice,
                maxFeeRate,
                TimeInForce.GOOD_TIL_CANCEL,
                clientId,
                false);
        log.info("Created Order:{} ",order);

    }
}