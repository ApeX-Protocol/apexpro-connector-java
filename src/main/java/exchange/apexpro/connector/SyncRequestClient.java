package exchange.apexpro.connector;

import exchange.apexpro.connector.exception.ApexProApiException;
import exchange.apexpro.connector.impl.ApiInternalFactory;
import exchange.apexpro.connector.model.account.*;
import exchange.apexpro.connector.model.enums.OrderSide;
import exchange.apexpro.connector.model.enums.OrderStatus;
import exchange.apexpro.connector.model.enums.OrderType;
import exchange.apexpro.connector.model.enums.PositionSide;
import exchange.apexpro.connector.model.market.OrderBookPrice;
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
    static SyncRequestClient create(ApiCredential apiCredential, RequestOptions options) {
        return ApiInternalFactory.getInstance().createSyncRequestClient(apiCredential, options);
    }

    /**
     * Create the synchronous connector. All interfaces defined in synchronous connector
     * are implemented by synchronous mode.
     *
     * @return The instance of synchronous connector.
     */
    static SyncRequestClient create(ApiCredential apiCredential) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setNetworkId(apiCredential.getNetworkId());
        return ApiInternalFactory.getInstance().createSyncRequestClient(apiCredential, requestOptions);
    }


    Long generateNonce(String ethAddress, int chainId, String l2PublicKey);

    String getExchangeInfo();

    ApiCredential onboard(String ethAddress, String onboardingSignature, String l2PublicKey, String l2KeyYCoordinate);

    /**
     * GET Retrieve User Account Data
     * GET /v1/account
     *
     * @return account
     */
    Account getAccount();


    /**
     * GET Account's total equity & available balance
     * GET /v1/account
     *
     * @return account
     */
    Balance getBalance();

    /**
     * GET User Historial Profit and Loss
     * GET /v1/historical-pnl
     *
     * @param beginTimeInclusive StartTime
     * @param endTimeExclusive   EndTime
     * @param symbol             Symbol
     * @param page               Page numbers start from 0
     * @param limit              Default at 100
     * @return historyPnl
     */
    HistoryPnl getHistoryPnl(Long beginTimeInclusive, Long endTimeExclusive, String symbol, Long page, Integer limit);

    /**
     * GET Yesterday's Profit & Loss
     * GET /v1/yesterday-pnl
     *
     * @return yesterdayPnl
     */
    YesterdayPnl getYesterdayPnl();

    /**
     * GET Historical Asset Value
     * GET /v1/history-value
     *
     * @param startTime Start time
     * @param endTime   End time
     * @return history value
     */
    HistoryValue getHistoryValue(Long startTime, Long endTime);


    /**
     * GET Trade History
     * GET /v1/fills
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
     * POST /v1/create-order
     *
     * @param symbol          Symbol
     * @param side            BUY or SELL
     * @param type            "LIMIT", "MARKET","STOP_LIMIT", "STOP_MARKET", "TAKE_PROFIT_LIMIT", "TAKE_PROFIT_MARKET"
     * @param size            Size
     * @param price           Price
     * @param limitFee        limitFee = price * size * takerFeeRate( from GET /v1/account)
     * @param expiration      Order expiry time
     * @param timeInForce     "GOOD_TIL_CANCEL", "FILL_OR_KILL", "IMMEDIATE_OR_CANCEL", "POST_ONLY"
     * @param triggerPrice    Trigger price
     * @param trailingPercent Conditional order trailing-stop
     * @param clientOrderId   Randomized connector id
     * @param signature       l2Key signature
     * @param reduceOnly      Reduce-only
     * @return order
     */
    Order createOrder(String symbol, OrderSide side, OrderType type, BigDecimal size, BigDecimal price, BigDecimal limitFee, long expiration, OrderType timeInForce, BigDecimal triggerPrice, BigDecimal trailingPercent, String clientOrderId, String signature, boolean reduceOnly);

    /**
     * POST Cancel Order
     * POST /v1/delete-order
     *
     * @param id order id
     * @return result map
     */
    Map<String, String> cancelOrder(String id);

    /**
     * POST Cancel Order By ClientOrderId
     * POST /v1/delete-connector-order-id
     *
     * @param id connector id
     * @return result map
     */
    Map<String, String> cancelOrderByClientId(String id);

    /**
     * GET Open Orders
     * GET /v1/open-orders
     */
    OpenOrders getOpenOrders();


    /**
     * POST Cancel all Open Orders
     * POST /v1/delete-open-orders
     *
     * @param symbol "BTC-USDC,ETH-USDC", Cancel all orders if none
     * @return empty
     */
    Map<String, String> cancelAllOpenOrders(String symbol);

    /**
     * GET All Order History
     * GET /v1/history-orders
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
     * GET /v1/get-order
     *
     * @param id order id
     * @return order
     */
    Order getOrder(String id);

    /**
     * GET Order by clientOrderId
     * GET /v1/order-by-connector-order-id
     *
     * @param id connector order id
     * @return order
     */
    Order getOrderByClientOrderId(String id);

    /**
     * GET Retrieve User Deposit Data
     * GET /v1/transfers
     *
     * @param limit              Page limit default at 100
     * @param page               Page numbers start from 0
     * @param currencyId         Filter to show only currency ID, all will be searched if the field is empty
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
     * @return wallet records
     */
    WithdrawalList getWithdrawList(Integer limit, Long page, Long beginTimeInclusive, Long endTimeExclusive);

    /**
     * POST User Withdrawal
     * POST /v1/create-withdrawal
     *
     * @param amount     Amount
     * @param clientId   Unique id of the connector associated with the withdrawal. Must be <= 40 characters. When using the connector, if not included, will be randomly generated by the connector.
     * @param expiration Date and time at which the withdrawal expires if it has not been completed. Expiration must be at least seven days in the future.
     * @param currencyId      Asset (in USDC) being withdrawn.
     * @param address    Your ethereum address only registered on ApexPro.
     * @param signature  The signature for the wallet, signed with the account's STARK private key.
     * @return withdrawal result
     */
    WithdrawalResult createWithdrawalOrder(BigDecimal amount, String clientId, Long expiration, String currencyId, String address,String signature);


    /**
     * POST Create Fast Withdrawal Order
     * POST /v1/fast-withdraw
     *
     * @param amount       Amount
     * @param clientId     Unique id of the connector associated with the withdrawal. Must be <= 40 characters. When using the connector, if not included, will be randomly generated by the connector.
     * @param expiration   Date and time at which the withdrawal expires if it has not been completed. Expiration must be at least seven days in the future.
     * @param currencyId        Asset (in USDC) being withdrawn.
     * @param signature    The signature for the wallet, signed with the account's STARK private key.
     * @param address      eth address
     * @param fee          Fees
     * @param chainId      chainId
     * @return withdrawal result
     */
    WithdrawalResult createFastWithdrawalOrder(BigDecimal amount, String clientId, Long expiration, String currencyId, String signature, String address, BigDecimal fee, Long chainId);

    /**
     * POST Cross-Chain Withdrawals
     * POST /v1/cross-chain-withdraw
     *
     * @param amount       Amount
     * @param clientId     Unique id of the connector associated with the withdrawal. Must be <= 40 characters. When using the connector, if not included, will be randomly generated by the connector.
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
     * Returns calculated withdrawal fee for Fast & Cross-Chain withdrawal & total available fund pool amount to withdraw;
     * GET /v1/uncommon-withdraw-fee
     *
     * @param amount  USDC
     * @param chainId chainId
     * @return WithdrawalFee
     */
    WithdrawalFee getWithdrawalFee(BigDecimal amount, long chainId);


    /**
     * Get Worst price & bidOne price & askOne price from orderbook;
     * GET /v1/get-worst-price
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

}