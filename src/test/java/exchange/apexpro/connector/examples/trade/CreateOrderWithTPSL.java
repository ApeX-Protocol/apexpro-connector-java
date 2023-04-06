package exchange.apexpro.connector.examples.trade;

import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.examples.config.PrivateConfig;
import exchange.apexpro.connector.model.enums.OrderSide;
import exchange.apexpro.connector.model.enums.OrderType;
import exchange.apexpro.connector.model.enums.PriceType;
import exchange.apexpro.connector.model.enums.TimeInForce;
import exchange.apexpro.connector.model.meta.ExchangeInfo;
import exchange.apexpro.connector.model.trade.Order;
import exchange.apexpro.connector.model.trade.OrderParams;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
public class CreateOrderWithTPSL {
    public static void main(String[] args) throws IOException {
        //Initialize the exchange configuration information. This is optional because it will be loaded automatically when you call its internal member variables.
        ExchangeInfo.load();


        //Prepare an order;
        String symbol = "BTC-USDC";
        String clientId = UUID.randomUUID().toString();
        BigDecimal size = new BigDecimal("0.002");
        BigDecimal price = new BigDecimal("23300");
        BigDecimal maxFeeRate = new BigDecimal("0.0005"); //you can get this value through max(Account.takerFeeRate,Account.makerFeeRate)
        OrderSide side = OrderSide.BUY;
        OrderType orderType = OrderType.LIMIT;


        // with take-profit order;
        OrderParams takeProfitOrder = new OrderParams();
        takeProfitOrder.setTriggerPrice(new BigDecimal(23500));
        takeProfitOrder.setTriggerPriceType(PriceType.INDEX);
        takeProfitOrder.setPrice(new BigDecimal(23500));
        takeProfitOrder.setSize(size);
        takeProfitOrder.setSide(OrderSide.SELL);

        // with stop-loss order
        OrderParams stopLossOrder = new OrderParams();
        stopLossOrder.setTriggerPrice(new BigDecimal(23000));
        stopLossOrder.setTriggerPriceType(PriceType.ORACLE);
        stopLossOrder.setPrice(new BigDecimal(23000));
        stopLossOrder.setSize(size);
        stopLossOrder.setSide(OrderSide.SELL);


        //Send conditional order to ApeXPro
        ApexProCredentials apexProCredentials = PrivateConfig.loadConfig().getApexProCredentials(); //Load the credentials
        SyncRequestClient syncRequestClient = SyncRequestClient.create(apexProCredentials);

        Order order = syncRequestClient.createOrderWithTPSL(symbol,
                side,
                orderType,
                size,
                price,
                maxFeeRate,
                TimeInForce.GOOD_TIL_CANCEL,
                clientId,  false,takeProfitOrder,stopLossOrder);


        log.info("Created TPSL-Order:{} ",order);

    }
}