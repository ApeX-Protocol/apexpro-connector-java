package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.impl.L2OrderSigner;
import exchange.apexpro.connector.model.enums.OrderSide;
import exchange.apexpro.connector.model.enums.OrderType;
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
public class CreateOrder {
    public static void main(String[] args) throws IOException {
        //Initialize the exchange configuration information. This is optional because it will be loaded automatically when you call its internal member variables.
        ExchangeInfo.load();

        //Load the private credential
        PrivateConfig privateConfig = PrivateConfig.loadConfig();
        L2KeyPair l2KeyPair = privateConfig.l2KeyPair;
        ApiCredential apiCredential = privateConfig.apiCredential;

        //Prepare an order;
        String symbol = "BTC-USDC";
        BigDecimal takerFeeRate = new BigDecimal("0.0005"); //you can get this value through max(Account.takerFeeRate,Account.makerFeeRate)
        String clientId = UUID.randomUUID().toString();
        BigDecimal size = new BigDecimal("0.002");
        BigDecimal price = new BigDecimal("23300");
        BigDecimal limitFee = takerFeeRate.multiply(size).multiply(price).setScale(Math.max(0, takerFeeRate.stripTrailingZeros().scale()), RoundingMode.UP);
        long expireTime = System.currentTimeMillis() + 18 * 24 * 60 * 60 * 1000;

        //Sign the order with L2KeyPair
        String signature = L2OrderSigner.signOrder(l2KeyPair, apiCredential.getAccountId(), symbol, size, price, limitFee, expireTime, clientId);

        //Send order to ApeXPro
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apiCredential);
        Order order = syncRequestClient.createOrder(symbol, OrderSide.BUY, OrderType.LIMIT, size, price, limitFee, expireTime, OrderType.LIMIT, null, null, clientId, signature, false);
        log.info("Created Order:{} ",order);

    }
}