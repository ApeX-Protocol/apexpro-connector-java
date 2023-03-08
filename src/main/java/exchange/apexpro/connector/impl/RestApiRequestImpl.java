package exchange.apexpro.connector.impl;


import exchange.apexpro.connector.RequestOptions;
import exchange.apexpro.connector.impl.utils.ApiSignHelper;
import exchange.apexpro.connector.impl.utils.JsonWrapper;
import exchange.apexpro.connector.impl.utils.JsonWrapperArray;
import exchange.apexpro.connector.impl.utils.RequestParamsBuilder;
import exchange.apexpro.connector.model.account.*;
import exchange.apexpro.connector.model.enums.OrderSide;
import exchange.apexpro.connector.model.enums.OrderStatus;
import exchange.apexpro.connector.model.enums.OrderType;
import exchange.apexpro.connector.model.enums.PositionSide;
import exchange.apexpro.connector.model.market.OrderBookPrice;
import exchange.apexpro.connector.model.meta.ExchangeInfo;
import exchange.apexpro.connector.model.meta.MultiChain;
import exchange.apexpro.connector.model.trade.*;
import exchange.apexpro.connector.model.wallet.*;
import exchange.apexpro.connector.model.user.ApiCredential;
import exchange.apexpro.connector.model.user.User;
import okhttp3.Request;
import org.web3j.utils.Strings;

import java.math.BigDecimal;
import java.util.*;

import static exchange.apexpro.connector.constant.ApiConstants.URL_SUFFIX;

class RestApiRequestImpl {

    private ApiCredential apiCredential;
    private String serverUrl;

    RestApiRequestImpl(ApiCredential credential, RequestOptions options) {
        this.apiCredential = credential;
        this.serverUrl = options.getUrl();
    }



    private Request createRequest(String url, String address, RequestParamsBuilder builder) {
        String requestUrl = url + address;
        System.out.print(requestUrl);
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(requestUrl + builder.buildUrl());

        if (builder.hasPostData()) {
            requestBuilder.post(builder.buildPostBody());
        }

        //Set default headers
        requestBuilder.addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("platform", "api");

        //sign request
        if (apiCredential != null) {
            Long timestamp = Calendar.getInstance().getTimeInMillis();
            //Long timestamp = 1674920517000l;
            String signature;
            if (builder.hasPostData()) {
                signature = ApiSignHelper.sign(this.apiCredential.getSecret(), URL_SUFFIX + address, "POST", timestamp, builder.getPostData());
            } else {
                signature = ApiSignHelper.sign(this.apiCredential.getSecret(), URL_SUFFIX + address, "GET", timestamp, builder.getGetData());
            }

            builder.putToHeader("APEX-API-KEY", this.apiCredential.getApiKey());
            builder.putToHeader("APEX-PASSPHRASE", this.apiCredential.getPassphrase());
            builder.putToHeader("APEX-SIGNATURE", signature);
            builder.putToHeader("APEX-TIMESTAMP", String.valueOf(timestamp));
        }

        if (builder.hasHeader()) {
            requestBuilder.headers(builder.buildHeaders());
        }

        return requestBuilder.build();
    }

    RestApiRequest<String> getExchangeInfo() {
        RestApiRequest<String> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build();

        request.request = createRequest(serverUrl, "/v1/symbols", builder);

        request.jsonParser = (jsonWrapper -> jsonWrapper.getString("data"));
        return request;
    }


    RestApiRequest<Long> generateNonce(String ethAddress, int chainId, String l2PublicKey) {
        RestApiRequest<Long> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("ethAddress", ethAddress)
                .putToPost("starkKey", l2PublicKey)
                .putToPost("chainId", Long.valueOf(chainId));

        request.request = createRequest(serverUrl, "/v1/generate-nonce", builder);

        request.jsonParser = (jsonWrapper -> {
            JsonWrapper jsonObject = jsonWrapper.getJsonObject("data");
            return Long.valueOf(jsonObject.getString("nonce"));
        });
        return request;
    }


    RestApiRequest<ApiCredential> onboard(String ethAddress, String onboardingSignature, String l2PublicKey, String l2KeyYCoordinate) {
        RestApiRequest<ApiCredential> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("ethereumAddress", ethAddress)
                .putToPost("starkKey", l2PublicKey)
                .putToPost("starkKeyYCoordinate", l2KeyYCoordinate)
                .putToPost("walletName", "java-sdk")
                .putToPost("platform", "api")

                .putToHeader("apex-ethereum-address", ethAddress)
                .putToHeader("apex-signature", onboardingSignature);

        request.request = createRequest(serverUrl, "/v1/onboarding", builder);

        request.jsonParser = (jsonWrapper -> {
            JsonWrapper jsonData = jsonWrapper.getJsonObject("data");

            JsonWrapper jsonUser = jsonData.getJsonObject("user");
            ApiCredential apiCredential = new ApiCredential();
            apiCredential.setUserId(Long.valueOf(jsonUser.getString("id")));

            JsonWrapper jsonApiKey = jsonData.getJsonObject("apiKey");
            apiCredential.setSecret(jsonApiKey.getString("secret"));
            apiCredential.setApiKey(jsonApiKey.getString("key"));
            apiCredential.setPassphrase(jsonApiKey.getString("passphrase"));

            JsonWrapper jsonAccount = jsonData.getJsonObject("account");
            apiCredential.setAccountId(Long.valueOf(jsonAccount.getString("id")));

            return apiCredential;
        });
        return request;
    }

    public RestApiRequest<User> getUser() {
        RestApiRequest<User> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build();
        request.request = createRequest(serverUrl, "/v1/user", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            User result = new User();
            result.setEthereumAddress(jsonWrapper.getString("ethereumAddress"));
            result.setIsRegistered(jsonWrapper.getBoolean("isRegistered"));
            result.setEmail(jsonWrapper.getString("email"));
            result.setUsername(jsonWrapper.getString("username"));
            result.setIsEmailVerified(jsonWrapper.getBoolean("isEmailVerified"));
            result.setEmailNotifyGeneralEnable(jsonWrapper.getBoolean("emailNotifyGeneralEnable"));
            result.setEmailNotifyTradingEnable(jsonWrapper.getBoolean("emailNotifyTradingEnable"));
            result.setEmailNotifyAccountEnable(jsonWrapper.getBoolean("emailNotifyAccountEnable"));
            result.setPopupNotifyTradingEnable(jsonWrapper.getBoolean("popupNotifyTradingEnable"));
            return result;
        });
        return request;
    }

    public RestApiRequest<User> modifyUser(String userData, String email, String username, String isSharingUsername, String isSharingAddress, String country, String emailNotifyGeneralEnable, String emailNotifyTradingEnable, String emailNotifyAccountEnable, String popupNotifyTradingEnable) {
        RestApiRequest<User> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("userData", userData)
                .putToPost("email", email)
                .putToPost("username", username)
                .putToPost("isSharingUsername", isSharingUsername)
                .putToPost("isSharingAddress", isSharingAddress)
                .putToPost("country", country)
                .putToPost("emailNotifyGeneralEnable", emailNotifyGeneralEnable)
                .putToPost("emailNotifyTradingEnable", emailNotifyTradingEnable)
                .putToPost("emailNotifyAccountEnable", emailNotifyAccountEnable)
                .putToPost("popupNotifyTradingEnable", popupNotifyTradingEnable);
        request.request = createRequest(serverUrl, "/v1/modify-user", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            User result = new User();
            result.setEthereumAddress(jsonWrapper.getString("ethereumAddress"));
            result.setIsRegistered(jsonWrapper.getBoolean("isRegistered"));
            result.setEmail(jsonWrapper.getString("email"));
            result.setUsername(jsonWrapper.getString("username"));
            result.setIsEmailVerified(jsonWrapper.getBoolean("isEmailVerified"));
            result.setEmailNotifyGeneralEnable(jsonWrapper.getBoolean("emailNotifyGeneralEnable"));
            result.setEmailNotifyTradingEnable(jsonWrapper.getBoolean("emailNotifyTradingEnable"));
            result.setEmailNotifyAccountEnable(jsonWrapper.getBoolean("emailNotifyAccountEnable"));
            result.setPopupNotifyTradingEnable(jsonWrapper.getBoolean("popupNotifyTradingEnable"));
            return result;
        });
        return request;
    }

    public RestApiRequest<Account> getAccount() {
        RestApiRequest<Account> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build();
        request.request = createRequest(serverUrl, "/v1/account", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            Account account = new Account();
            account.setId(jsonWrapper.getString("id"));
            account.setStarkKey(jsonWrapper.getString("starkKey"));
            account.setPositionId(jsonWrapper.getString("positionId"));
            account.setTakerFeeRate(new BigDecimal(jsonWrapper.getString("takerFeeRate")));
            account.setMakerFeeRate(new BigDecimal(jsonWrapper.getString("makerFeeRate")));
            account.setCreatedTime(jsonWrapper.getLong("createdAt"));
            List<Wallet> walletList = new LinkedList<>();
            JsonWrapperArray walletsArray = jsonWrapper.getJsonArray("wallets");
            walletsArray.forEach((item) -> {
                Wallet wallet = new Wallet();
                wallet.setAsset(item.getString("asset"));
                wallet.setBalance(item.getString("balance"));
                wallet.setPendingDepositAmount(item.getString("pendingDepositAmount"));
                wallet.setPendingWithdrawAmount(item.getString("pendingWithdrawAmount"));
                wallet.setPendingTransferOutAmount(item.getString("pendingTransferOutAmount"));
                wallet.setPendingTransferInAmount(item.getString("pendingTransferInAmount"));
                walletList.add(wallet);
            });
            account.setWallets(walletList);

            List<OpenPosition> openPositionList = new LinkedList<>();
            JsonWrapperArray openPositionsArray = jsonWrapper.getJsonArray("openPositions");
            openPositionsArray.forEach((item) -> {
                OpenPosition position = new OpenPosition();
                position.setSymbol(item.getString("symbol"));
                position.setSide(item.getString("side"));
                position.setSize(item.getString("size"));
                position.setEntryPrice(item.getString("entryPrice"));
                position.setFee(item.getString("fee"));
                position.setFundingFee(item.getString("fundingFee"));
                position.setCreatedAt(item.getString("createdAt"));
                position.setUpdatedTime(item.getString("updatedTime"));
                position.setLightNumbers(item.getString("lightNumbers"));
                openPositionList.add(position);
            });
            account.setOpenPositions(openPositionList);
            return account;
        });
        return request;
    }


    public RestApiRequest<Balance> getBalance() {
        RestApiRequest<Balance> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build();
        request.request = createRequest(serverUrl, "/v1/account-balance", builder);
        request.jsonParser = (jsonWrapper -> {
            JsonWrapper data = jsonWrapper.getJsonObject("data");
            Balance balance = new Balance();

            balance.setAvailable(new BigDecimal(data.getString("availableBalance")));
            balance.setTotalEquity(new BigDecimal(data.getString("totalEquityValue")));
            balance.setTotalInitialMargin(new BigDecimal(data.getString("initialMargin")));
            balance.setTotalMaintenanceMargin(new BigDecimal(data.getString("maintenanceMargin")));
            balance.setUpdatedTime(Calendar.getInstance().getTimeInMillis());
            return balance;
        });
        return request;
    }


    public RestApiRequest<HistoryPnl> getHistoryPnl(Long beginTimeInclusive, Long endTimeExclusive, String symbol, Long page, Integer limit) {
        RestApiRequest<HistoryPnl> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("beginTimeInclusive", String.valueOf(beginTimeInclusive))
                .putToUrl("endTimeExclusive", String.valueOf(endTimeExclusive))
                .putToUrl("type", "CLOSE_POSITION")
                .putToUrl("symbol", symbol)
                .putToUrl("page", String.valueOf(page))
                .putToUrl("limit", String.valueOf(limit));
        request.request = createRequest(serverUrl, "/v1/historical-pnl", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            HistoryPnl result = new HistoryPnl();
            List<HistoryPnlEntry> historyPnlEntryList = new LinkedList<>();
            JsonWrapperArray historyValuesArray = jsonWrapper.getJsonArray("historicalPnl");
            historyValuesArray.forEach((item) -> {
                HistoryPnlEntry element = new HistoryPnlEntry();
                element.setSymbol(item.getString("symbol"));
                element.setSize(item.getString("size"));
                element.setTotalPnl(item.getString("totalPnl"));
                element.setPrice(item.getString("price"));
                element.setCreatedAt(item.getLong("createdAt"));
                element.setIsLiquidate(item.getBoolean("isLiquidate"));
                element.setIsDeleverage(item.getBoolean("isDeleverage"));
                historyPnlEntryList.add(element);
            });
            result.setHistoricalPnl(historyPnlEntryList);
            result.setTotalSize(jsonWrapper.getInteger("totalSize"));
            return result;
        });
        return request;
    }

    public RestApiRequest<YesterdayPnl> getYesterdayPnl() {
        RestApiRequest<YesterdayPnl> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build();
        request.request = createRequest(serverUrl, "/v1/yesterday-pnl", builder);
        request.jsonParser = (jsonWrapper -> {
            YesterdayPnl result = new YesterdayPnl();
            result.setPnl(new BigDecimal(jsonWrapper.getString("data")));
            return result;
        });
        return request;
    }

    public RestApiRequest<HistoryValue> getHistoryValue(Long startTime, Long endTime) {
        RestApiRequest<HistoryValue> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("endTime", String.valueOf(endTime))
                .putToUrl("startTime", String.valueOf(startTime));
        request.request = createRequest(serverUrl, "/v1/history-value", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            HistoryValue result = new HistoryValue();
            List<HistoryValueEntry> historyValueList = new LinkedList<>();
            JsonWrapperArray historyValuesArray = jsonWrapper.getJsonArray("historyValues");
            historyValuesArray.forEach((item) -> {
                HistoryValueEntry element = new HistoryValueEntry();
                element.setAccountTotalValue(item.getString("accountTotalValue"));
                element.setDateTime(item.getLong("dateTime"));
                historyValueList.add(element);
            });
            result.setHistoryValues(historyValueList);
            return result;
        });
        return request;
    }

    public RestApiRequest<OrderFills> getFills(String symbol, Long beginTimeInclusive, Long endTimeExclusive, Integer page, Integer limit) {
        RestApiRequest<OrderFills> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("page", String.valueOf(page))
                .putToUrl("limit", String.valueOf(limit));
                if (beginTimeInclusive > 0)
                    builder.putToUrl("beginTimeInclusive", String.valueOf(beginTimeInclusive));
                if (endTimeExclusive > 0)
                    builder.putToUrl("endTimeExclusive", String.valueOf(endTimeExclusive));

        request.request = createRequest(serverUrl, "/v1/fills", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            OrderFills result = new OrderFills();
            List<OrderFill> fills = new ArrayList<>();
            JsonWrapperArray ordersArray = jsonWrapper.getJsonArray("orders");

            ordersArray.forEach((item) -> {
                OrderFill orderFill = new OrderFill();
                orderFill.setId(item.getString("id"));
                orderFill.setClientOrderId(item.getString("clientId"));
                orderFill.setOrderId(item.getString("orderId"));
                orderFill.setAccountId(item.getString("accountId"));
                orderFill.setSymbol(item.getString("symbol"));

                orderFill.setPrice(new BigDecimal(item.getString("price")));
                orderFill.setSize(new BigDecimal(item.getString("size")));
                orderFill.setQuoteAmount(orderFill.getSize().multiply(orderFill.getPrice()));
                orderFill.setSide(item.getString("side"));
                orderFill.setFee(new BigDecimal(item.getString("fee")));
                orderFill.setStatus(item.getString("status"));
                orderFill.setDirection(item.getString("direction"));
                orderFill.setCreatedTime(item.getLong("createdAt"));
                orderFill.setMatchFillId(item.getString("matchFillId"));
                orderFill.setLiquidateFee(new BigDecimal(item.getString("liquidateFee")));
                orderFill.setUpdatedTime(item.getLong("updatedTime"));
                orderFill.setIsLiquidate(item.containKey("isLiquidate") ? item.getBoolean("isLiquidate") : false);
                orderFill.setIsDeleverage(item.containKey("isDeleverage") ? item.getBoolean("isDeleverage") : false);
                orderFill.setOrderType(item.getString("orderType"));
                fills.add(orderFill);
            });

            result.setOrderFills(fills);
            result.setTotalSize(jsonWrapper.getInteger("totalSize"));
            return result;
        });
        return request;
    }

    public RestApiRequest<Order> createOrder(String symbol, OrderSide side, OrderType type, BigDecimal size, BigDecimal price, BigDecimal limitFee, long expiration, OrderType timeInForce, BigDecimal triggerPrice, BigDecimal trailingPercent, String clientOrderId, String signature, boolean reduceOnly) {
        RestApiRequest<Order> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("symbol", symbol)
                .putToPost("side", side.name())
                .putToPost("type", type.name())
                .putToPost("size", size.toPlainString())
                .putToPost("price", price.toPlainString())
                .putToPost("limitFee", limitFee.toPlainString())
                .putToPost("expiration", String.valueOf(expiration))
                .putToPost("timeInForce", timeInForce.name())
                .putToPost("clientId", clientOrderId)
                .putToPost("signature", signature)
                .putToPost("reduceOnly", String.valueOf(reduceOnly));
        if (triggerPrice != null && triggerPrice.compareTo(new BigDecimal(0)) != 0)
            builder.putToPost("triggerPrice", triggerPrice.toPlainString());
        if (trailingPercent != null && trailingPercent.compareTo(new BigDecimal(0)) != 0)
            builder.putToPost("trailingPercent", trailingPercent.toPlainString());

        request.request = createRequest(serverUrl, "/v1/create-order", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            Order order = new Order();
            order.setOrderId(jsonWrapper.getString("orderId"));
            order.setClientOrderId(jsonWrapper.getString("clientOrderId"));
            order.setAccountId(jsonWrapper.getString("accountId"));
            order.setSymbol(jsonWrapper.getString("symbol"));
            order.setSide(jsonWrapper.getString("side"));
            order.setPrice(new BigDecimal(jsonWrapper.getString("price")));
            order.setLimitFee(new BigDecimal(jsonWrapper.getString("limitFee")));
            order.setFee(new BigDecimal(!jsonWrapper.getString("fee").equals("") ? jsonWrapper.getString("fee") : "0"));
            order.setLiquidateFee(new BigDecimal(!jsonWrapper.getString("liquidateFee").equals("") ? jsonWrapper.getString("liquidateFee") : "0"));
            order.setTriggerPrice(new BigDecimal(jsonWrapper.getString("triggerPrice")));
            order.setSize(new BigDecimal(jsonWrapper.getString("size")));
            order.setType(jsonWrapper.getString("type"));
            order.setStatus(jsonWrapper.getString("status"));
            order.setTimeInForce(jsonWrapper.getString("timeInForce"));
            order.setPostOnly(jsonWrapper.getBoolean("postOnly"));
            order.setReduceOnly(jsonWrapper.getBoolean("reduceOnly"));
            order.setLatestMatchFillPrice(new BigDecimal(jsonWrapper.getString("latestMatchFillPrice")));
            order.setCumMatchFillSize(new BigDecimal(jsonWrapper.getString("cumMatchFillSize")));
            order.setCumMatchFillValue(new BigDecimal(jsonWrapper.getString("cumMatchFillValue")));
            order.setCumMatchFillFee(new BigDecimal(jsonWrapper.getString("cumMatchFillFee")));
            order.setCumSuccessFillSize(new BigDecimal(jsonWrapper.getString("cumSuccessFillSize")));
            order.setCumSuccessFillValue(new BigDecimal(jsonWrapper.getString("cumSuccessFillValue")));
            order.setCumSuccessFillFee(new BigDecimal(jsonWrapper.getString("cumSuccessFillFee")));
            order.setIsPositionTpsl(jsonWrapper.getBoolean("isPositionTpsl"));
            order.setExpiresTime(jsonWrapper.getLong("expiresAt"));

            order.setCreatedTime(jsonWrapper.getLong("createdAt"));
            order.setUpdatedTime(jsonWrapper.getLong("updatedTime"));
            return order;
        });
        return request;
    }

    public RestApiRequest<Map<String, String>> cancelOrder(String id) {
        RestApiRequest<Map<String, String>> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("id", id);
        request.request = createRequest(serverUrl, "/v1/delete-order", builder);
        request.jsonParser = (jsonWrapper -> {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("data", jsonWrapper.getString("data"));
            return dataMap;
        });
        return request;
    }

    public RestApiRequest<Map<String, String>> cancelOrderByClientId(String id) {
        RestApiRequest<Map<String, String>> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("id", id);
        request.request = createRequest(serverUrl, "/v1/delete-connector-order-id", builder);
        request.jsonParser = (jsonWrapper -> {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("data", jsonWrapper.getString("data"));
            return dataMap;
        });
        return request;
    }

    public RestApiRequest<OpenOrders> getOpenOrders() {
        RestApiRequest<OpenOrders> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build();
        request.request = createRequest(serverUrl, "/v1/open-orders", builder);
        request.jsonParser = (jsonWrapper -> {
            OpenOrders result = new OpenOrders();
            List<Order> orders = new LinkedList<>();
            JsonWrapperArray array = jsonWrapper.getJsonArray("data");
            array.forEach((item) -> {
                Order order = new Order();
                order.setOrderId(item.getString("id"));
                order.setClientOrderId(item.getString("clientOrderId"));
                order.setAccountId(item.getString("accountId"));
                order.setSymbol(item.getString("symbol"));
                order.setSide(item.getString("side"));
                order.setPrice(new BigDecimal(item.getString("price")));
                order.setLimitFee(new BigDecimal(item.getString("limitFee")));
                order.setFee(new BigDecimal(!item.getString("fee").equals("") ? item.getString("fee") : "0"));
                order.setLiquidateFee(new BigDecimal(!item.getString("liquidateFee").equals("") ? item.getString("liquidateFee") : "0"));

                order.setIsPositionTpsl(item.getBoolean("isPositionTpsl"));
                order.setTriggerPrice(new BigDecimal(item.getString("triggerPrice")));
                order.setSize(new BigDecimal(item.getString("size")));
                order.setType(item.getString("type"));
                order.setCreatedTime(item.getLong("createdAt"));
                order.setUpdatedTime(item.getLong("updatedTime"));
                order.setExpiresTime(item.getLong("expiresAt"));
                order.setStatus(item.getString("status"));
                order.setTimeInForce(item.getString("timeInForce"));
                order.setPostOnly(item.getBoolean("postOnly"));
                order.setReduceOnly(item.getBoolean("reduceOnly"));
                order.setLatestMatchFillPrice(new BigDecimal(item.getString("latestMatchFillPrice")));
                order.setCumMatchFillSize(new BigDecimal(item.getString("cumMatchFillSize")));
                order.setCumMatchFillValue(new BigDecimal(item.getString("cumMatchFillValue")));
                order.setCumMatchFillFee(new BigDecimal(item.getString("cumMatchFillFee")));
                order.setCumSuccessFillSize(new BigDecimal(item.getString("cumSuccessFillSize")));
                order.setCumSuccessFillValue(new BigDecimal(item.getString("cumSuccessFillValue")));
                order.setCumSuccessFillFee(new BigDecimal(item.getString("cumSuccessFillFee")));
                orders.add(order);
            });
            result.setOrders(orders);
            return result;
        });
        return request;
    }

    public RestApiRequest<Map<String, String>> cancelAllOpenOrders(String symbol) {
        RestApiRequest<Map<String, String>> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("symbol", symbol);
        request.request = createRequest(serverUrl, "/v1/delete-open-orders", builder);
        request.jsonParser = (jsonWrapper -> new HashMap<>());
        return request;
    }

    public RestApiRequest<HistoryOrders> getHistoryOrders(String symbol, OrderStatus status, OrderSide side, OrderType orderType, Long beginTimeInclusive, Long endTimeExclusive, Integer limit, Integer page) {
        RestApiRequest<HistoryOrders> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("status", status != null ? status.name() : "")
                .putToUrl("side", side != null ? side.name() : "")
                .putToUrl("type", orderType != null ? orderType.name() : "")
                .putToUrl("limit", String.valueOf(limit))
                .putToUrl("beginTimeInclusive", beginTimeInclusive > 0 ? String.valueOf(beginTimeInclusive) : "")
                .putToUrl("endTimeExclusive", endTimeExclusive > 0 ? String.valueOf(endTimeExclusive) : "")
                .putToUrl("page", String.valueOf(page));
        request.request = createRequest(serverUrl, "/v1/history-orders", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            HistoryOrders result = new HistoryOrders();
            List<Order> orders = new LinkedList<>();
            JsonWrapperArray ordersArray = jsonWrapper.getJsonArray("orders");
            ordersArray.forEach((item) -> {
                Order order = new Order();
                order.setOrderId(item.getString("id"));
                order.setClientOrderId(item.getString("clientOrderId"));
                order.setAccountId(item.getString("accountId"));
                order.setSymbol(item.getString("symbol"));
                order.setSide(item.getString("side"));
                order.setPrice(new BigDecimal(item.getString("price")));
                order.setLimitFee(new BigDecimal(item.getString("limitFee")));
                order.setFee(new BigDecimal(!item.getString("fee").equals("") ? item.getString("fee") : "0"));
                order.setLiquidateFee(new BigDecimal(!item.getString("liquidateFee").equals("") ? item.getString("liquidateFee") : "0"));
                order.setTriggerPrice(new BigDecimal(item.getString("triggerPrice")));

                order.setIsPositionTpsl(item.getBoolean("isPositionTpsl"));
                order.setSize(new BigDecimal(item.getString("size")));
                order.setType(item.getString("type"));
                order.setCreatedTime(item.getLong("createdAt"));
                order.setUpdatedTime(item.getLong("updatedTime"));
                order.setExpiresTime(item.getLong("expiresAt"));
                order.setStatus(item.getString("status"));
                order.setTimeInForce(item.getString("timeInForce"));
                order.setPostOnly(item.getBoolean("postOnly"));
                order.setReduceOnly(item.getBoolean("reduceOnly"));
                order.setLatestMatchFillPrice(new BigDecimal(item.getString("latestMatchFillPrice")));
                order.setCumMatchFillSize(new BigDecimal(item.getString("cumMatchFillSize")));
                order.setCumMatchFillValue(new BigDecimal(item.getString("cumMatchFillValue")));
                order.setCumMatchFillFee(new BigDecimal(item.getString("cumMatchFillFee")));
                order.setCumSuccessFillSize(new BigDecimal(item.getString("cumSuccessFillSize")));
                order.setCumSuccessFillValue(new BigDecimal(item.getString("cumSuccessFillValue")));
                order.setCumSuccessFillFee(new BigDecimal(item.getString("cumSuccessFillFee")));
                orders.add(order);
            });
            result.setOrders(orders);
            result.setTotalSize(jsonWrapper.getInteger("totalSize"));
            return result;
        });
        return request;
    }

    public RestApiRequest<Order> getOrder(String id) {
        RestApiRequest<Order> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("id", id);
        request.request = createRequest(serverUrl, "/v1/get-order", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            Order order = new Order();
            order.setOrderId(jsonWrapper.getString("id"));
            order.setClientOrderId(jsonWrapper.getString("clientOrderId"));
            order.setAccountId(jsonWrapper.getString("accountId"));
            order.setSymbol(jsonWrapper.getString("symbol"));
            order.setSide(jsonWrapper.getString("side"));
            order.setPrice(new BigDecimal(jsonWrapper.getString("price")));
            order.setLimitFee(new BigDecimal(jsonWrapper.getString("limitFee")));
            order.setFee(new BigDecimal(!jsonWrapper.getString("fee").equals("") ? jsonWrapper.getString("fee") : "0"));
            order.setLiquidateFee(new BigDecimal(!jsonWrapper.getString("liquidateFee").equals("") ? jsonWrapper.getString("liquidateFee") : "0"));
            order.setIsPositionTpsl(jsonWrapper.getBoolean("isPositionTpsl"));
            order.setTriggerPrice(new BigDecimal(jsonWrapper.getString("triggerPrice")));
            order.setSize(new BigDecimal(jsonWrapper.getString("size")));
            order.setType(jsonWrapper.getString("type"));
            order.setCreatedTime(jsonWrapper.getLong("createdAt"));
            order.setUpdatedTime(jsonWrapper.getLong("updatedTime"));
            order.setExpiresTime(jsonWrapper.getLong("expiresAt"));
            order.setStatus(jsonWrapper.getString("status"));
            order.setTimeInForce(jsonWrapper.getString("timeInForce"));
            order.setPostOnly(jsonWrapper.getBoolean("postOnly"));
            order.setReduceOnly(jsonWrapper.getBoolean("reduceOnly"));
            order.setLatestMatchFillPrice(new BigDecimal(jsonWrapper.getString("latestMatchFillPrice")));
            order.setCumMatchFillSize(new BigDecimal(jsonWrapper.getString("cumMatchFillSize")));
            order.setCumMatchFillValue(new BigDecimal(jsonWrapper.getString("cumMatchFillValue")));
            order.setCumMatchFillFee(new BigDecimal(jsonWrapper.getString("cumMatchFillFee")));
            order.setCumSuccessFillSize(new BigDecimal(jsonWrapper.getString("cumSuccessFillSize")));
            order.setCumSuccessFillValue(new BigDecimal(jsonWrapper.getString("cumSuccessFillValue")));
            order.setCumSuccessFillFee(new BigDecimal(jsonWrapper.getString("cumSuccessFillFee")));
            return order;
        });
        return request;
    }

    public RestApiRequest<Order> getOrderByClientOrderId(String id) {
        RestApiRequest<Order> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("id", id);
        request.request = createRequest(serverUrl, "/v1/order-by-connector-order-id", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            Order order = new Order();
            order.setOrderId(jsonWrapper.getString("id"));
            order.setClientOrderId(jsonWrapper.getString("clientOrderId"));
            order.setAccountId(jsonWrapper.getString("accountId"));
            order.setSymbol(jsonWrapper.getString("symbol"));
            order.setSide(jsonWrapper.getString("side"));
            order.setPrice(new BigDecimal(jsonWrapper.getString("price")));
            order.setLimitFee(new BigDecimal(jsonWrapper.getString("limitFee")));
            order.setFee(new BigDecimal(!jsonWrapper.getString("fee").equals("") ? jsonWrapper.getString("fee") : "0"));
            order.setLiquidateFee(new BigDecimal(!jsonWrapper.getString("liquidateFee").equals("") ? jsonWrapper.getString("liquidateFee") : "0"));
            order.setIsPositionTpsl(jsonWrapper.getBoolean("isPositionTpsl"));
            order.setTriggerPrice(new BigDecimal(jsonWrapper.getString("triggerPrice")));
            order.setSize(new BigDecimal(jsonWrapper.getString("size")));
            order.setType(jsonWrapper.getString("type"));
            order.setCreatedTime(jsonWrapper.getLong("createdAt"));
            order.setUpdatedTime(jsonWrapper.getLong("updatedTime"));
            order.setExpiresTime(jsonWrapper.getLong("expiresAt"));
            order.setStatus(jsonWrapper.getString("status"));
            order.setTimeInForce(jsonWrapper.getString("timeInForce"));
            order.setPostOnly(jsonWrapper.containKey("postOnly") ? jsonWrapper.getBoolean("postOnly"):false);
            order.setReduceOnly(jsonWrapper.getBoolean("reduceOnly"));
            order.setLatestMatchFillPrice(new BigDecimal(jsonWrapper.getString("latestMatchFillPrice")));
            order.setCumMatchFillSize(new BigDecimal(jsonWrapper.getString("cumMatchFillSize")));
            order.setCumMatchFillValue(new BigDecimal(jsonWrapper.getString("cumMatchFillValue")));
            order.setCumMatchFillFee(new BigDecimal(jsonWrapper.getString("cumMatchFillFee")));
            order.setCumSuccessFillSize(new BigDecimal(jsonWrapper.getString("cumSuccessFillSize")));
            order.setCumSuccessFillValue(new BigDecimal(jsonWrapper.getString("cumSuccessFillValue")));
            order.setCumSuccessFillFee(new BigDecimal(jsonWrapper.getString("cumSuccessFillFee")));
            return order;
        });
        return request;
    }

    public RestApiRequest<DepositList> getDepositList(Integer limit, Long page, String currencyId, Long beginTimeInclusive, Long endTimeExclusive, List<String> chainIds) {
        RestApiRequest<DepositList> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("limit", String.valueOf(limit))
                .putToUrl("page", String.valueOf(page))
                .putToUrl("currencyId", currencyId)
                .putToUrl("beginTimeInclusive", beginTimeInclusive!=null?String.valueOf(beginTimeInclusive):"")
                .putToUrl("endTimeExclusive", endTimeExclusive!=null?String.valueOf(endTimeExclusive):"")
                .putToUrl("chainIds", chainIds!=null?Strings.join(chainIds,","):"")
                .putToUrl("transferType", "DEPOSIT,CROSS_DEPOSIT");
        request.request = createRequest(serverUrl, "/v1/transfers", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            DepositList result = new DepositList();
            List<Deposit> transfers = new LinkedList<>();
            JsonWrapperArray array = jsonWrapper.getJsonArray("transfers");
            array.forEach((item) -> {
                Deposit deposit = new Deposit();
                deposit.setId(item.getString("id"));
                deposit.setType(item.getString("type"));
                deposit.setCurrencyId(item.getString("currencyId"));
                deposit.setAmount(new BigDecimal(item.getString("amount")));
                deposit.setTransactionHash(item.getString("transactionHash"));
                deposit.setStatus(item.getString("status"));
                deposit.setCreatedTime(item.getLong("createdAt"));
                deposit.setConfirmedTime(item.getLong("confirmedAt"));
                deposit.setUpdatedTime(item.getLong("updatedTime"));
                deposit.setConfirmedCount(item.getInteger("confirmedCount"));
                deposit.setRequiredCount(item.getInteger("requiredCount"));
                deposit.setClientId(item.getString("clientId"));
                deposit.setOrderId(item.getString("orderId"));
                deposit.setChainId(item.getString("chainId"));
                transfers.add(deposit);
            });
            result.setDeposits(transfers);
            result.setTotalSize(jsonWrapper.getInteger("totalSize"));
            return result;
        });
        return request;
    }

    public RestApiRequest<WithdrawalList> getWithdrawList(Integer limit, Long page, Long beginTimeInclusive, Long endTimeExclusive) {
        RestApiRequest<WithdrawalList> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("limit", String.valueOf(limit))
                .putToUrl("page", String.valueOf(page))
                .putToUrl("beginTimeInclusive", beginTimeInclusive != null ? String.valueOf(beginTimeInclusive) : "")
                .putToUrl("endTimeExclusive", endTimeExclusive != null ? String.valueOf(endTimeExclusive) : "")
                .putToUrl("transferType", "WITHDRAW,FAST_WITHDRAW,CROSS_WITHDRAW");
        request.request = createRequest(serverUrl, "/v1/transfers", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            WithdrawalList result = new WithdrawalList();
            List<Withdrawal> withdrawals = new LinkedList<>();
            JsonWrapperArray array = jsonWrapper.getJsonArray("transfers");
            array.forEach((item) -> {
                Withdrawal withdrawal = new Withdrawal();
                withdrawal.setId(item.getString("id"));
                withdrawal.setType(item.getString("type"));
                withdrawal.setCurrencyId(item.getString("currencyId"));
                withdrawal.setAmount(new BigDecimal(item.getString("amount")));
                withdrawal.setTransactionHash(item.getString("transactionHash"));
                withdrawal.setStatus(item.getString("status"));
                withdrawal.setCreatedTime(item.getLong("createdAt"));
                withdrawal.setConfirmedTime(item.getLong("confirmedAt"));
                withdrawal.setUpdatedTime(item.getLong("updatedTime"));
                withdrawal.setClientId(item.getString("clientId"));
                withdrawal.setChainId(item.getString("chainId"));
                withdrawal.setFee(item.containKey("fee") ? new BigDecimal(item.getString("fee")): new BigDecimal(0));
                withdrawals.add(withdrawal);
            });
            result.setWithdrawals(withdrawals);
            result.setTotalSize(jsonWrapper.getInteger("totalSize"));
            return result;
        });
        return request;
    }

    public RestApiRequest<WithdrawalResult> createWithdrawalOrder(BigDecimal amount, String clientId, Long expiration, String currencyId, String address,String signature) {
        RestApiRequest<WithdrawalResult> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("amount", amount)
                .putToPost("clientId", clientId)
                .putToPost("expiration", expiration)
                .putToPost("asset", currencyId)
                .putToPost("ethAddress",address)
                .putToPost("signature", signature);
        request.request = createRequest(serverUrl, "/v1/create-withdrawal-to-address", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            JsonWrapper withdrawJsonWrapper = jsonWrapper.getJsonObject("withdraw");
            WithdrawalResult result = new WithdrawalResult();
            result.setId(withdrawJsonWrapper.getString("id"));
            result.setType(withdrawJsonWrapper.getString("type"));
            return result;
        });
        return request;
    }

    public RestApiRequest<WithdrawalResult> fastWithdraw(BigDecimal amount, String clientId, Long expiration, String currencyId, String signature, String address, BigDecimal fee, Long chainId, String lpAccountId) {

        Optional< MultiChain.Chain> chain = ExchangeInfo.multiChain().getChains().stream().filter(f->f.getChainId() == chainId).findAny();
        MultiChain.MultiChainToken multiChainToken = chain.get().getTokens().stream().filter(t -> t.getToken().equals(currencyId)).findAny().get();


        RestApiRequest<WithdrawalResult> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("amount", amount)
                .putToPost("asset", currencyId)
                .putToPost("expiration", expiration)
                .putToPost("ethAddress", address)
                .putToPost("erc20Address", multiChainToken.getTokenAddress())
                .putToPost("clientId", clientId)
                .putToPost("signature", signature)
                .putToPost("fee", fee)
                .putToPost("chainId", chainId)
                .putToPost("lpAccountId", lpAccountId);
        request.request = createRequest(serverUrl, "/v1/fast-withdraw", builder);
        request.jsonParser = (jsonWrapper -> {

            jsonWrapper = jsonWrapper.getJsonObject("data");
            JsonWrapper withdrawJsonWrapper = jsonWrapper.getJsonObject("withdraw");
            WithdrawalResult result = new WithdrawalResult();
            result.setId(withdrawJsonWrapper.getString("id"));
            result.setType(withdrawJsonWrapper.getString("type"));

            return result;
        });
        return request;
    }

    public RestApiRequest<WithdrawalResult> crossChainWithdraw(BigDecimal amount, String clientId, Long expiration, String currencyId, String signature, String address, BigDecimal fee, Long chainId, String lpAccountId) {

        Optional< MultiChain.Chain> chain = ExchangeInfo.multiChain().getChains().stream().filter(f->f.getChainId() == chainId).findAny();
        MultiChain.MultiChainToken multiChainToken = chain.get().getTokens().stream().filter(t -> t.getToken().equals(currencyId)).findAny().get();


        RestApiRequest<WithdrawalResult> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("amount", amount)
                .putToPost("asset", currencyId)
                .putToPost("expiration", expiration)
                .putToPost("ethAddress", address)
                .putToPost("erc20Address", multiChainToken.getTokenAddress())
                .putToPost("clientId", clientId)
                .putToPost("signature", signature)
                .putToPost("fee", fee)
                .putToPost("chainId", chainId)
                .putToPost("lpAccountId", lpAccountId);
        request.request = createRequest(serverUrl, "/v1/cross-chain-withdraw", builder);
        request.jsonParser = (jsonWrapper -> {

            jsonWrapper = jsonWrapper.getJsonObject("data");
            JsonWrapper withdrawJsonWrapper = jsonWrapper.getJsonObject("withdraw");
            WithdrawalResult result = new WithdrawalResult();
            result.setId(withdrawJsonWrapper.getString("id"));
            result.setType(withdrawJsonWrapper.getString("type"));

            return result;
        });
        return request;
    }

    public RestApiRequest<WithdrawalFee> getWithdrawalFee(BigDecimal amount, long chainId) {
        RestApiRequest<WithdrawalFee> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("amount", amount.toPlainString())
                .putToUrl("chainId", String.valueOf(chainId));
        request.request = createRequest(serverUrl, "/v1/uncommon-withdraw-fee", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            WithdrawalFee result = new WithdrawalFee();
            result.setWithdrawalFee(new BigDecimal(jsonWrapper.getString("fee")));
            result.setPoolAvailableAmount(new BigDecimal(jsonWrapper.getString("poolAvailableAmount")));
            return result;
        });
        return request;
    }


    public RestApiRequest<OrderBookPrice> getWorstPrice(String symbol, BigDecimal size, OrderSide side) {
        RestApiRequest<OrderBookPrice> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("side", side.name())
                .putToUrl("size", size.toPlainString());
        request.request = createRequest(serverUrl, "/v1/get-worst-price", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");

            OrderBookPrice result = new OrderBookPrice();
            result.setWorstPrice(new BigDecimal(jsonWrapper.getString("worstPrice")));
            result.setAskOnePrice(new BigDecimal(jsonWrapper.getString("askOnePrice")));
            result.setBidOnePrice(new BigDecimal(jsonWrapper.getString("bidOnePrice")));
            return result;
        });
        return request;
    }


    public RestApiRequest<FundingRates> getFundingRate(String symbol, Integer limit, Long page, Long beginTimeInclusive, Long endTimeExclusive, PositionSide positionSide) {
        RestApiRequest<FundingRates> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("limit", limit != null? String.valueOf(limit) : "")
                .putToUrl("page", page != null ? String.valueOf(page) : "")
                .putToUrl("beginTimeInclusive",beginTimeInclusive !=null ? String.valueOf(beginTimeInclusive) : "")
                .putToUrl("endTimeExclusive",endTimeExclusive != null ? String.valueOf(endTimeExclusive) : "")
                .putToUrl("positionSide",positionSide != null ? positionSide.name() : "");

        request.request = createRequest(serverUrl, "/v1/funding", builder);
        request.jsonParser = (jsonWrapper -> {
            FundingRates fundingRates = new FundingRates();

            JsonWrapperArray jsonWrapperArray = jsonWrapper.getJsonObject("data").getJsonArray("fundingValues");
            fundingRates.setTotalSize(jsonWrapper.getJsonObject("data").getLong("totalSize"));

            List<FundingRate> fundingRateList = new ArrayList<>();
            jsonWrapperArray.forEach(item -> {
                FundingRate fundingRate = new FundingRate();
                fundingRate.setId(item.getString("id"));
                fundingRate.setSymbol(item.getString("symbol"));
                fundingRate.setFundingValue(new BigDecimal(item.getString("fundingValue")));
                fundingRate.setRate(new BigDecimal(item.getString("rate")));
                fundingRate.setPositionSize(new BigDecimal(item.getString("positionSize")));
                fundingRate.setPrice(new BigDecimal(item.getString("price")));
                fundingRate.setPositionSide(item.getString("side"));
                fundingRate.setFundingTime(item.getLong("fundingTime"));
                fundingRate.setStatus(item.getString("status"));
                fundingRate.setTransactionId(item.getString("transactionId"));
                fundingRateList.add(fundingRate);
            });

            fundingRates.setFundingRates(fundingRateList);

            return fundingRates;
        });
        return request;
    }
}
