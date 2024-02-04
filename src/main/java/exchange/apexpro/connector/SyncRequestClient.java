package exchange.apexpro.connector;

import exchange.apexpro.connector.exception.ApexProApiException;
import exchange.apexpro.connector.impl.ApiInternalFactory;
import exchange.apexpro.connector.model.account.*;
import exchange.apexpro.connector.model.enums.*;
import exchange.apexpro.connector.model.market.OrderBookPrice;
import exchange.apexpro.connector.model.market.Ticker;
import exchange.apexpro.connector.model.trade.*;
import exchange.apexpro.connector.model.wallet.*;
import exchange.apexpro.connector.model.user.ApiCredential;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Synchronous request interface, invoking ApexPro RestAPI via synchronous
 * method.<br>
 * All methods in this interface will be blocked until the RestAPI response.
 * <p>
 * If the invoking failed or timeout, the
 * {@link ApexProApiException} will be thrown.
 */
public interface SyncRequestClient {

    /**
     * Create the synchronous connector. All interfaces defined in synchronous connector
     * are implemented by synchronous mode.
     *
     * @return The instance of synchronous connector.
     */
    static SyncRequestClient create() {
        return create(null, new RequestOptions());
    }


    /**
     * Create the synchronous connector. All interfaces defined in synchronous connector
     * are implemented by synchronous mode.
     *
     * @return The instance of synchronous connector.
     */
    static SyncRequestClient create(RequestOptions options) {
        return create(null, options);
    }

    /**
     * Create the synchronous connector. All interfaces defined in synchronous connector
     * are implemented by synchronous mode.
     *
     * @return The instance of synchronous connector.
     */
    static SyncRequestClient create(ApexProCredentials apexProCredentials, RequestOptions options) {
        return ApiInternalFactory.getInstance().createSyncRequestClient(apexProCredentials, options);
    }

    /**
     * Create the synchronous connector. All interfaces defined in synchronous connector
     * are implemented by synchronous mode.
     *
     * @return The instance of synchronous connector.
     */
    static SyncRequestClient create(ApexProCredentials apexProCredentials) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setNetworkId(apexProCredentials.apiCredential.getNetworkId());
        return ApiInternalFactory.getInstance().createSyncRequestClient(apexProCredentials, requestOptions);
    }


    Long generateNonce(String ethAddress, int chainId, String l2PublicKey);

    String getExchangeInfo();

    ApiCredential onboard(String ethAddress, String onboardingSignature, String l2PublicKey, String l2KeyYCoordinate);

    /**
     * GET Retrieve User Account Data
     * GET /v2/account
     *
     * @return account
     */
    Account getAccount();


    /**
     * GET Account's total equity and available balance
     * GET /v2/account
     *
     * @return list of collateral assets
     */
    List<Balance> getBalance();

    /**
     * GET User Historial Profit and Loss
     * GET /v2/historical-pnl
     * @param beginTimeInclusive StartTime
     * @param endTimeExclusive   EndTime
     * @param symbol             Symbol
     * @param page               Page numbers start from 0
     * @param limit              Default at 100
     * @return historyPnl
     */
    HistoryPnl getHistoryPnl(Long beginTimeInclusive, Long endTimeExclusive, String symbol, Long page, Integer limit);

    /**
     * GET Yesterday's Profit and Loss
     * GET /v2/yesterday-pnl
     * @param contractZone ApiConstants.CONTRACT_ZONE_USDC | ApiConstants.CONTRACT_ZONE_USDT
     *
     * @return yesterdayPnl
     */
    YesterdayPnl getYesterdayPnl(String contractZone);

    /**
     * GET Historical Asset Value
     * GET /v2/history-value
     *
     * @param startTime Start time
     * @param endTime   End time
     * @return history value
     */
    HistoryValue getHistoryValue(Long startTime, Long endTime);


    /**
     * GET Trade History
     * GET /v2/fills
     *
     * @param symbol             Symbol
     * @param limit              default at 100
     * @param beginTimeInclusive Start time
     * @param endTimeExclusive   End time
     * @param page               Page numbers start from 0
     * @return order fills
     */
    OrderFills getFills(String symbol, Long beginTimeInclusive, Long endTimeExclusive, Integer page, Integer limit);


    /**
     * POST Creating Orders
     * POST /v2/create-order
     *
     * @param symbol          Symbol
     * @param side            BUY or SELL
     * @param type            Only "LIMIT", "MARKET" can be accepted.
     * @param size            Size
     * @param price           Price
     * @param maxFeeRate      Maximum trading fee rate, you can get it by max(taker_fee,maker_fee), taker_fee/maker_fee can be fetched from GET /v1/account
     * @param timeInForce     "GOOD_TIL_CANCEL", "FILL_OR_KILL", "IMMEDIATE_OR_CANCEL", "POST_ONLY"
     * @param clientOrderId   Client order id
     * @param reduceOnly      Reduce-only
     * @return order
     */
    Order createOrder(String symbol, OrderSide side, OrderType type, BigDecimal size, BigDecimal price, BigDecimal maxFeeRate, TimeInForce timeInForce, String clientOrderId, boolean reduceOnly);


    /**
     * POST Creating Orders with TPSL(Take-profit/Stop-loss)
     * POST /v2/create-order
     *
     * @param symbol          Symbol
     * @param side            BUY or SELL
     * @param type            Only "LIMIT", "MARKET" can be accepted.
     * @param size            Size
     * @param price           Price
     * @param maxFeeRate      Maximum trading fee rate, you can get it by max(taker_fee,maker_fee), taker_fee/maker_fee can be fetched from GET /v1/account
     * @param timeInForce     "GOOD_TIL_CANCEL", "FILL_OR_KILL", "IMMEDIATE_OR_CANCEL", "POST_ONLY"
     * @param clientOrderId   Client order id
     * @param reduceOnly      Reduce-only
     * @param withTakeProfit  if you want to place an order with some take profit parameters, you can pass it here.
     * @param withStopLoss    if you want to place an order with some stop loss parameters, you can pass it here.
     * @return order
     */
    Order createOrderWithTPSL(String symbol, OrderSide side, OrderType type, BigDecimal size, BigDecimal price, BigDecimal maxFeeRate, TimeInForce timeInForce, String clientOrderId, boolean reduceOnly,OrderParams withTakeProfit, OrderParams withStopLoss);


    /**
     * POST Creating Conditional Order
     * POST /v2/create-order
     *
     * @param symbol          Symbol
     * @param side            BUY or SELL
     * @param type            "LIMIT", "MARKET"
     * @param size            Size
     * @param triggerPrice    Trigger price
     * @param triggerPriceType ORACLE, INDEX, MARKET
     * @param orderPrice      Order price, only valid on LIMIT order
     * @param maxFeeRate      Maximum trading fee rate, you can get it by max(taker_fee,maker_fee), taker_fee/maker_fee can be fetched from GET /v1/account
     * @param timeInForce     "GOOD_TIL_CANCEL", "FILL_OR_KILL", "IMMEDIATE_OR_CANCEL", "POST_ONLY"
     * @param clientOrderId   Order id generated by client
     * @param reduceOnly      Reduce-only
     * @return order
     */
    Order createConditionalOrder(String symbol, OrderSide side, OrderType type, BigDecimal size, BigDecimal triggerPrice,PriceType triggerPriceType, BigDecimal orderPrice,BigDecimal maxFeeRate,TimeInForce timeInForce, String clientOrderId, boolean reduceOnly);





    /**
     * POST Cancel Order
     * POST /v2/delete-order
     *
     * @param id order id
     * @return result map
     */
    Map<String, String> cancelOrder(String id);

    /**
     * POST Cancel Order By ClientOrderId
     * POST /v2/delete-client-order-id
     *
     * @param id connector id
     * @return result map
     */
    Map<String, String> cancelOrderByClientId(String id);

    /**
     * GET Open Orders
     * GET /v2/open-orders
     * @param contractZone valid param is the one of [ApiConstants.CONTRACT_ZONE_USDC,ApiConstants/CONTRACT_ZONE_USDT]
     */
    OpenOrders getOpenOrders(String contractZone);


    /**
     * POST Cancel all Open Orders
     * POST /v2/delete-open-orders
     *
     * @param symbol "BTC-USDC,ETH-USDC", Cancel all orders if none
     * @param contractZone only orders in this contract area will be canceled. valid param is the one of [ApiConstants.CONTRACT_ZONE_USDC,ApiConstants/CONTRACT_ZONE_USDT]
     * @return empty
     */
    Map<String, String> cancelAllOpenOrders(String symbol,String contractZone);

    /**
     * GET All Order History
     * GET /v2/history-orders
     *
     * @param symbol trading pair
     * @param status PENDING,OPEN,FILLED,CANCELING,CANCELED,UNTRIGGERED
     * @param side   BUY or SELL
     * @param orderType   "LIMIT", "MARKET","STOP_LIMIT", "STOP_MARKET", "TAKE_PROFIT_LIMIT","TAKE_PROFIT_MARKET"
     * @param beginTimeInclusive the time to begin with;
     * @param endTimeExclusive   end time;
     * @param limit              default 100
     * @param page               Page numbers start from 0
     * @return history orders
     */
    HistoryOrders getHistoryOrders(String symbol, OrderStatus status, OrderSide side, OrderType orderType, Long beginTimeInclusive, Long endTimeExclusive, Integer limit, Integer page);

    /**
     * GET Order ID
     * GET /v2/get-order
     *
     * @param id order id
     * @return order
     */
    Order getOrder(String id);

    /**
     * GET Order by clientOrderId
     * GET /v2/order-by-client-order-id
     *
     * @param id connector order id
     * @param contractZone valid param is the one of [ApiConstants.CONTRACT_ZONE_USDC,ApiConstants/CONTRACT_ZONE_USDT]

     * @return order
     */
    Order getOrderByClientOrderId(String id,String contractZone);

    /**
     * GET Retrieve User Deposit Data
     * GET /v1/transfers
     *
     * @param limit              Page limit default at 100
     * @param page               Page numbers start from 0
     * @param currencyId         USDT|USDC
     * @param beginTimeInclusive Start time
     * @param endTimeExclusive   End time
     * @param chainIds           Check for multiple chainID records
     * @return wallet result
     */
    DepositList getDepositList(Integer limit, Long page, String currencyId, Long beginTimeInclusive, Long endTimeExclusive, List<String> chainIds);

    /**
     * GET Retrieve User Withdrawal List
     * GET /v1/withdraw-list
     *
     * @param limit              Page limit default at 100;
     * @param page               Page numbers start from 0;
     * @param beginTimeInclusive Start time;
     * @param endTimeExclusive   End time;
     * @param currencyId valid in [USDT,USDC]
     * @return wallet records
     */
    WithdrawalList getWithdrawList(String currencyId,Integer limit, Long page, Long beginTimeInclusive, Long endTimeExclusive);

    /**
     * POST User Withdrawal
     * POST /v2/create-withdrawal
     *
     * @param amount     Amount
     * @param clientId   Unique id.
     * @param expiration Date and time at which the withdrawal expires if it has not been completed. Expiration must be at least seven days in the future.
     * @param currencyId      Asset (in USDC | USDT) being withdrawn.
     * @param address    Your ethereum address only registered on ApexPro.
     * @param signature  The signature for the wallet, signed with the account's STARK private key.
     * @return withdrawal result
     */
    WithdrawalResult createWithdrawalOrder(BigDecimal amount, String clientId, Long expiration, String currencyId, String address,String signature);


    /**
     * POST Create Fast Withdrawal Order
     * POST /v2/fast-withdraw
     *
     * @param amount       Amount
     * @param clientId     Unique id.
     * @param expiration   Date and time at which the withdrawal expires if it has not been completed. Expiration must be at least seven days in the future.
     * @param currencyId        Asset (in USDC/USDT) being withdrawn.
     * @param signature    The signature for the wallet, signed with the account's STARK private key.
     * @param address      eth address
     * @param fee          Fees
     * @param chainId      chainId
     * @return withdrawal result
     */
    WithdrawalResult createFastWithdrawalOrder(BigDecimal amount, String clientId, Long expiration, String currencyId, String signature, String address, BigDecimal fee, Long chainId);

    /**
     * POST Cross-Chain Withdrawals
     * POST /v2/cross-chain-withdraw
     *
     * @param amount       Amount
     * @param clientId     Unique id.
     * @param expiration   Date and time at which the withdrawal expires if it has not been completed. Expiration must be at least seven days in the future.
     * @param currencyId        Asset (in USDC) being withdrawn.
     * @param signature    The signature for the wallet, signed with the account's STARK private key.
     * @param address      Ethereum address registered on ApeX
     * @param fee          Fees
     * @param chainId      chainId
     * @return withdrawal result
     */
    WithdrawalResult createCrossChainWithdrawalOrder(BigDecimal amount, String clientId, Long expiration, String currencyId, String signature, String address, BigDecimal fee, Long chainId);

    /**
     * Returns calculated withdrawal fee for Fast and Cross-Chain withdrawal and total available fund pool amount to withdraw;
     * GET /v2/uncommon-withdraw-fee
     *
     * @param collateralToken  valid params in [ApiConstants.COLLATERAL_ASSET_USDC,ApiConstants.COLLATERAL_ASSET_USDT]
     * @param chainId chainId
     * @return WithdrawalFee
     */
    WithdrawalFee getWithdrawalFee(String collateralToken,BigDecimal amount, long chainId);


    /**
     * Get Worst price and bidOne price and askOne price from orderbook;
     * GET /v2/get-worst-price
     * @param side BUY or SELL order
     * @param size the size of Order placing you want
     * @return OrderBookPrice
     */
    OrderBookPrice getWorstPrice(String symbol, BigDecimal size, OrderSide side);


    /**
     * GET Funding Rate
     * @param symbol
     * @param limit
     * @param page Page numbers start from 0
     * @param beginTimeInclusive Start time
     * @param endTimeExclusive  End time
     * @param positionSide position side ,if null both wil be returned
     * @return
     */
    FundingRates getFundingRate(String symbol, Integer limit, Long page, Long beginTimeInclusive, Long endTimeExclusive, PositionSide positionSide);


    /**
     * GET Ticker Data
     * @param symbol
     * @return
     */
    Ticker getTicker(String symbol);
}