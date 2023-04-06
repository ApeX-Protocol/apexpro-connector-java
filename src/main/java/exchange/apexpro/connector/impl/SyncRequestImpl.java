package exchange.apexpro.connector.impl;

import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.model.account.*;
import exchange.apexpro.connector.model.enums.*;
import exchange.apexpro.connector.model.market.OrderBookPrice;
import exchange.apexpro.connector.model.market.Ticker;
import exchange.apexpro.connector.model.meta.ExchangeInfo;
import exchange.apexpro.connector.model.trade.*;
import exchange.apexpro.connector.model.wallet.*;
import exchange.apexpro.connector.model.user.ApiCredential;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SyncRequestImpl implements SyncRequestClient {

    private final RestApiRequestImpl requestImpl;

    SyncRequestImpl(RestApiRequestImpl requestImpl) {
        this.requestImpl = requestImpl;
    }

    @Override
    public Long generateNonce(String ethAddress, int chainId, String l2PublicKey) {
        return RestApiInvoker.callSync(requestImpl.generateNonce(ethAddress, chainId, l2PublicKey));
    }

    @Override
    public String getExchangeInfo() {
        return RestApiInvoker.callSync(requestImpl.getExchangeInfo());
    }

    @Override
    public ApiCredential onboard(String ethAddress, String onboardingSignature, String l2PublicKey, String l2KeyYCoordinate) {
        return RestApiInvoker.callSync(requestImpl.onboard(ethAddress, onboardingSignature, l2PublicKey, l2KeyYCoordinate));
    }


    @Override
    public Account getAccount() {
        return RestApiInvoker.callSync(requestImpl.getAccount());
    }

    @Override
    public Balance getBalance() {
        return RestApiInvoker.callSync(requestImpl.getBalance());
    }

    @Override
    public HistoryPnl getHistoryPnl(Long beginTimeInclusive, Long endTimeExclusive, String symbol, Long page, Integer limit) {
        return RestApiInvoker.callSync(requestImpl.getHistoryPnl(beginTimeInclusive, endTimeExclusive, symbol, page, limit));
    }

    @Override
    public YesterdayPnl getYesterdayPnl() {
        return RestApiInvoker.callSync(requestImpl.getYesterdayPnl());
    }

    @Override
    public HistoryValue getHistoryValue(Long startTime, Long endTime) {
        return RestApiInvoker.callSync(requestImpl.getHistoryValue(startTime, endTime));
    }

    @Override
    public OrderFills getFills(String symbol, Long beginTimeInclusive, Long endTimeExclusive, Integer page, Integer limit) {
        return RestApiInvoker.callSync(requestImpl.getFills( symbol, beginTimeInclusive,  endTimeExclusive,  page,  limit));
    }

    @Override
    public Order createOrder(String symbol, OrderSide side, OrderType type, BigDecimal size, BigDecimal price, BigDecimal maxFeeRate, TimeInForce timeInForce, String clientOrderId, boolean reduceOnly) {
        return RestApiInvoker.callSync(requestImpl.createOrderWithTPSL(symbol, side, type, size, price, maxFeeRate, timeInForce, clientOrderId, reduceOnly,null,null));
    }

    @Override
    public Order createOrderWithTPSL(String symbol, OrderSide side, OrderType type, BigDecimal size, BigDecimal price, BigDecimal maxFeeRate, TimeInForce timeInForce, String clientOrderId, boolean reduceOnly,OrderParams withTakeProfit, OrderParams withStopLoss) {
        return RestApiInvoker.callSync(requestImpl.createOrderWithTPSL(symbol, side, type, size, price, maxFeeRate, timeInForce, clientOrderId, reduceOnly,withTakeProfit,withStopLoss));
    }

    @Override
    public Order createConditionalOrder(String symbol, OrderSide side, OrderType type, BigDecimal size, BigDecimal triggerPrice,PriceType triggerPriceType, BigDecimal orderPrice, BigDecimal maxFeeRate,TimeInForce timeInForce, String clientOrderId, boolean reduceOnly) {
        return RestApiInvoker.callSync(requestImpl.createConditionalOrder(symbol, side, type, size, triggerPrice, triggerPriceType,orderPrice, maxFeeRate, timeInForce, clientOrderId, reduceOnly));
    }


    @Override
    public Map<String, String> cancelOrder(String id) {
        return RestApiInvoker.callSync(requestImpl.cancelOrder(id));
    }

    @Override
    public Map<String, String> cancelOrderByClientId(String id) {
        return RestApiInvoker.callSync(requestImpl.cancelOrderByClientId(id));
    }

    @Override
    public OpenOrders getOpenOrders() {
        return RestApiInvoker.callSync(requestImpl.getOpenOrders());
    }

    @Override
    public Map<String, String> cancelAllOpenOrders(String symbol) {
        return RestApiInvoker.callSync(requestImpl.cancelAllOpenOrders(symbol));
    }

    @Override
    public HistoryOrders getHistoryOrders(String symbol, OrderStatus status, OrderSide side, OrderType orderType, Long beginTimeInclusive, Long endTimeExclusive, Integer limit, Integer page) {
        return RestApiInvoker.callSync(requestImpl.getHistoryOrders(symbol, status, side, orderType, beginTimeInclusive, endTimeExclusive, limit, page));
    }

    @Override
    public Order getOrder(String id) {
        return RestApiInvoker.callSync(requestImpl.getOrder(id));
    }

    @Override
    public Order getOrderByClientOrderId(String id) {
        return RestApiInvoker.callSync(requestImpl.getOrderByClientOrderId(id));
    }

    @Override
    public DepositList getDepositList(Integer limit, Long page, String currencyId, Long beginTimeInclusive, Long endTimeExclusive, List<String> chainIds){
        return RestApiInvoker.callSync(requestImpl.getDepositList(limit, page, currencyId, beginTimeInclusive, endTimeExclusive, chainIds));
    }

    @Override
    public WithdrawalList getWithdrawList(Integer limit, Long page, Long beginTimeInclusive, Long endTimeExclusive) {
        return RestApiInvoker.callSync(requestImpl.getWithdrawList(limit, page, beginTimeInclusive, endTimeExclusive));
    }

    @Override
    public WithdrawalResult createWithdrawalOrder(BigDecimal amount, String clientId, Long expiration, String currency, String address,String signature) {
        return RestApiInvoker.callSync(requestImpl.createWithdrawalOrder(amount, clientId, expiration, currency,address, signature));
    }

    @Override
    public WithdrawalResult createFastWithdrawalOrder(BigDecimal amount, String clientId, Long expiration, String currencyId, String signature, String address, BigDecimal fee, Long chainId) {
        return RestApiInvoker.callSync(requestImpl.fastWithdraw(amount, clientId, expiration, currencyId, signature, address, fee, chainId, String.valueOf(ExchangeInfo.global().getFastWithdrawAccountId())));
    }

    @Override
    public WithdrawalResult createCrossChainWithdrawalOrder(BigDecimal amount, String clientId, Long expiration, String currencyId, String signature, String address, BigDecimal fee, Long chainId) {
        return RestApiInvoker.callSync(requestImpl.crossChainWithdraw(amount, clientId, expiration, currencyId, signature, address, fee, chainId,String.valueOf(ExchangeInfo.global().getCrossChainAccountId())));
    }

    @Override
    public WithdrawalFee getWithdrawalFee(BigDecimal amount, long chainId) {
        return RestApiInvoker.callSync(requestImpl.getWithdrawalFee(amount, chainId));
    }

    public OrderBookPrice getWorstPrice(String symbol, BigDecimal size, OrderSide side) {
        return RestApiInvoker.callSync(requestImpl.getWorstPrice(symbol, size,side));
    }


    public FundingRates getFundingRate(String symbol, Integer limit, Long page, Long beginTimeInclusive, Long endTimeExclusive, PositionSide positionSide) {
        return RestApiInvoker.callSync(requestImpl.getFundingRate( symbol,  limit,  page,  beginTimeInclusive,  endTimeExclusive,  positionSide));
    }

    public Ticker getTicker(String symbol) {
        return RestApiInvoker.callSync(requestImpl.getTicker( symbol));
    }

}
