package exchange.apexpro.connector.impl;


import exchange.apexpro.connector.ApexProCredentials;
import exchange.apexpro.connector.RequestOptions;
import exchange.apexpro.connector.exception.ApexProApiException;
import exchange.apexpro.connector.impl.utils.ApiSignHelper;
import exchange.apexpro.connector.impl.utils.JsonWrapper;
import exchange.apexpro.connector.impl.utils.JsonWrapperArray;
import exchange.apexpro.connector.impl.utils.RequestParamsBuilder;
import exchange.apexpro.connector.model.account.*;
import exchange.apexpro.connector.model.enums.*;
import exchange.apexpro.connector.model.market.OrderBookPrice;
import exchange.apexpro.connector.model.market.Ticker;
import exchange.apexpro.connector.model.meta.ExchangeInfo;
import exchange.apexpro.connector.model.meta.MultiChain;
import exchange.apexpro.connector.model.trade.*;
import exchange.apexpro.connector.model.user.L2KeyPair;
import exchange.apexpro.connector.model.wallet.*;
import exchange.apexpro.connector.model.user.ApiCredential;
import exchange.apexpro.connector.model.user.User;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import org.web3j.utils.Strings;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static exchange.apexpro.connector.constant.ApiConstants.*;
import static exchange.apexpro.connector.exception.ApexProApiException.EXEC_ERROR;

@Slf4j
class RestApiRequestImpl {

    private ApiCredential apiCredential;
    private L2KeyPair l2KeyPair;
    private String serverUrl;

    RestApiRequestImpl(ApexProCredentials apexProCredentials, RequestOptions options) {
        if (apexProCredentials != null) {
            this.apiCredential = apexProCredentials.apiCredential;
            this.l2KeyPair = apexProCredentials.l2KeyPair;
        }
        if (options.getUrl() != null && !options.getUrl().equals(""))
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

        request.request = createRequest(serverUrl, "/v2/symbols", builder);

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


    RestApiRequest<ApiCredential> onboard(String ethAddress, String onboardingSignature, String l2PublicKey, String l2KeyYCoordinate,String contractZone) {
        RestApiRequest<ApiCredential> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("ethereumAddress", ethAddress)
                .putToPost("starkKey", l2PublicKey)
                .putToPost("starkKeyYCoordinate", l2KeyYCoordinate)
                .putToPost("walletName", "java-sdk")
                .putToPost("platform", "api")
                .putToPost("token", contractZone)
                .putToHeader("apex-ethereum-address", ethAddress)
                .putToHeader("apex-signature", onboardingSignature);

        request.request = createRequest(serverUrl, "/v2/onboarding", builder);

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
        request.request = createRequest(serverUrl, "/v2/account", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");

            Account account = new Account();
            account.setId(jsonWrapper.getString("id"));
            account.setStarkKey(jsonWrapper.getString("starkKey"));
            account.setPositionId(jsonWrapper.getString("positionId"));

            JsonWrapperArray accountsArray = jsonWrapper.getJsonArray("accounts");
            List<ContractZoneConfig> contractZoneConfigs = new LinkedList<>();
            accountsArray.forEach(item -> {
                ContractZoneConfig contractZoneConfig = new ContractZoneConfig();
                contractZoneConfig.setContractZone(item.getString("token").toUpperCase());
                contractZoneConfig.setTakerFeeRate(new BigDecimal(item.getString("takerFeeRate")));
                contractZoneConfig.setMakerFeeRate(new BigDecimal(item.getString("makerFeeRate")));
                contractZoneConfig.setMinInitialMarginRate(new BigDecimal(item.getString("minInitialMarginRate")));
                contractZoneConfig.setCreatedTime(item.getLong("createdAt"));
                contractZoneConfigs.add(contractZoneConfig);
            });
            account.setContractZoneConfigList(contractZoneConfigs);


            List<Wallet> walletList = new LinkedList<>();
            JsonWrapperArray walletsArray = jsonWrapper.getJsonArray("wallets");
            walletsArray.forEach((item) -> {
                Wallet wallet = new Wallet();
                wallet.setToken(item.getString("asset"));
                wallet.setBalance(item.getString("balance"));
                wallet.setPendingDepositAmount(item.getString("pendingDepositAmount"));
                wallet.setPendingWithdrawAmount(item.getString("pendingWithdrawAmount"));
                wallet.setPendingTransferOutAmount(item.getString("pendingTransferOutAmount"));
                wallet.setPendingTransferInAmount(item.getString("pendingTransferInAmount"));
                walletList.add(wallet);
            });
            account.setWallets(walletList);

            List<Position> openPositionList = new LinkedList<>();
            JsonWrapperArray openPositionsArray = jsonWrapper.getJsonArray("openPositions");
            openPositionsArray.forEach((item) -> {
                Position position = new Position();
                position.setSymbol(item.getString("symbol"));
                position.setSide(item.getString("side"));
                position.setSize(new BigDecimal(item.getString("size")));
                position.setEntryPrice(new BigDecimal(item.getString("entryPrice")));
                position.setFee(new BigDecimal(item.getString("fee")));
                position.setFundingFee(new BigDecimal(item.getString("fundingFee")));
                position.setUpdatedTime(item.getLong("updatedAt"));
                position.setLightNumbers(item.getString("lightNumbers"));
                openPositionList.add(position);
            });
            account.setOpenPositions(openPositionList);
            return account;
        });
        return request;
    }


    public RestApiRequest<List<Balance>> getBalance() {
        RestApiRequest<List<Balance>> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build();
        request.request = createRequest(serverUrl, "/v2/account-balance", builder);
        request.jsonParser = (jsonWrapper -> {
            List<Balance> balances = new ArrayList<>();
            jsonWrapper = jsonWrapper.getJsonObject("data");
            if (jsonWrapper.containKey("usdtBalance")) {
                JsonWrapper data = jsonWrapper.getJsonObject("usdtBalance");
                Balance balance = new Balance();
                balance.setToken(COLLATERAL_ASSET_USDT);
                balance.setAvailable(new BigDecimal(data.getString("availableBalance")));
                balance.setTotalEquity(new BigDecimal(data.getString("totalEquityValue")));
                balance.setTotalInitialMargin(new BigDecimal(data.getString("initialMargin")));
                balance.setTotalMaintenanceMargin(new BigDecimal(data.getString("maintenanceMargin")));
                balance.setUpdatedTime(Calendar.getInstance().getTimeInMillis());
                balances.add(balance);
            }

            if (jsonWrapper.containKey("usdcBalance")) {
                JsonWrapper data = jsonWrapper.getJsonObject("usdcBalance");
                Balance balance = new Balance();
                balance.setToken(COLLATERAL_ASSET_USDC);
                balance.setAvailable(new BigDecimal(data.getString("availableBalance")));
                balance.setTotalEquity(new BigDecimal(data.getString("totalEquityValue")));
                balance.setTotalInitialMargin(new BigDecimal(data.getString("initialMargin")));
                balance.setTotalMaintenanceMargin(new BigDecimal(data.getString("maintenanceMargin")));
                balance.setUpdatedTime(Calendar.getInstance().getTimeInMillis());
                balances.add(balance);
            }

            return balances;
        });
        return request;
    }


    public RestApiRequest<HistoryPnl> getHistoryPnl(Long beginTimeInclusive, Long endTimeExclusive, String symbol, Long page, Integer limit) {

        String contractZone = ExchangeInfo.getContractZoneBySymbol(symbol);


        RestApiRequest<HistoryPnl> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("beginTimeInclusive", String.valueOf(beginTimeInclusive))
                .putToUrl("endTimeExclusive", String.valueOf(endTimeExclusive))
                .putToUrl("type", "CLOSE_POSITION")
                .putToUrl("symbol", symbol)
                .putToUrl("token",contractZone)
                .putToUrl("page", String.valueOf(page))
                .putToUrl("limit", String.valueOf(limit));
        request.request = createRequest(serverUrl, "/v2/historical-pnl", builder);
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

    public RestApiRequest<YesterdayPnl> getYesterdayPnl(String contractZone) {
        RestApiRequest<YesterdayPnl> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("token",contractZone);

        request.request = createRequest(serverUrl, "/v2/yesterday-pnl", builder);
        request.jsonParser = (jsonWrapper -> {
            YesterdayPnl result = new YesterdayPnl();
            String value = jsonWrapper.getString("data");
            result.setPnl(new BigDecimal(Strings.isEmpty(value)? "0" : value));
            return result;
        });
        return request;
    }

    public RestApiRequest<HistoryValue> getHistoryValue(Long startTime, Long endTime) {
        RestApiRequest<HistoryValue> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("endTime", String.valueOf(endTime))
                .putToUrl("startTime", String.valueOf(startTime));
        request.request = createRequest(serverUrl, "/v2/history-value", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            HistoryValue result = new HistoryValue();
            List<HistoryValueEntry> historyValueList = new LinkedList<>();
            JsonWrapper jsonWrapperUsdc = jsonWrapper.getJsonObject("usdcHistoryValues");
            JsonWrapper jsonWrapperUsdt = jsonWrapper.getJsonObject("usdtHistoryValues");
            JsonWrapperArray historyValuesArray = jsonWrapperUsdc.getJsonArray("historyValues");
            historyValuesArray.forEach((item) -> {
                HistoryValueEntry element = new HistoryValueEntry();
                element.setCollateralToken(COLLATERAL_ASSET_USDC);
                element.setAccountTotalValue(item.getString("accountTotalValue"));
                element.setDateTime(item.getLong("dateTime"));
                historyValueList.add(element);
            });

            historyValuesArray = jsonWrapperUsdt.getJsonArray("historyValues");
            historyValuesArray.forEach((item) -> {
                HistoryValueEntry element = new HistoryValueEntry();
                element.setCollateralToken(COLLATERAL_ASSET_USDT);
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
                .putToUrl("token",ExchangeInfo.getContractZoneBySymbol(symbol))
                .putToUrl("page", String.valueOf(page))
                .putToUrl("limit", String.valueOf(limit));
                if (beginTimeInclusive > 0)
                    builder.putToUrl("beginTimeInclusive", String.valueOf(beginTimeInclusive));
                if (endTimeExclusive > 0)
                    builder.putToUrl("endTimeExclusive", String.valueOf(endTimeExclusive));

        request.request = createRequest(serverUrl, "/v2/fills", builder);
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

    public RestApiRequest<Order> createOrderWithTPSL(String symbol, OrderSide side, OrderType type, BigDecimal size, BigDecimal price, BigDecimal maxFeeRate, TimeInForce timeInForce, String clientOrderId, boolean reduceOnly,OrderParams takeProfitOrder, OrderParams stopLossOrder) {

        //Sign the order with L2KeyPair
        String signature;
        BigDecimal limitFee = maxFeeRate.multiply(size).multiply(price).setScale(Math.max(0, maxFeeRate.stripTrailingZeros().scale()), RoundingMode.UP);
        long expireTime = System.currentTimeMillis() + 18 * 24 * 60 * 60 * 1000;
        try {
            signature = L2OrderSigner.signOrder(l2KeyPair, apiCredential.getAccountId(), symbol, size, price, limitFee, expireTime, clientOrderId,side);
        }catch (IOException e) {
            throw new ApexProApiException(EXEC_ERROR,"An error occurred when signing an order with l2KeyPair");
        }

        RestApiRequest<Order> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("symbol", symbol)
                .putToPost("side", side.name())
                .putToPost("type", type.name())
                .putToPost("size", size.toPlainString())
                .putToPost("price", price.toPlainString())
                .putToPost("limitFee", limitFee.toPlainString())
                .putToPost("expiration", String.valueOf(expireTime))
                .putToPost("timeInForce", timeInForce.name())
                .putToPost("clientId", clientOrderId)
                .putToPost("signature", signature)
                .putToPost("reduceOnly", String.valueOf(reduceOnly))
                ;

        // with taker profit
        if (takeProfitOrder != null) {
            //Sign the order with L2KeyPair
            if (Strings.isEmpty(takeProfitOrder.getClientOrderId()))
                takeProfitOrder.setClientOrderId("tp"+clientOrderId);

            BigDecimal tpLimitFee = maxFeeRate.multiply(takeProfitOrder.getSize()).multiply(takeProfitOrder.getPrice()).setScale(Math.max(0, maxFeeRate.stripTrailingZeros().scale()), RoundingMode.UP);
            String tpSignature;
            try {
                tpSignature = L2OrderSigner.signOrder(l2KeyPair, apiCredential.getAccountId(), symbol, takeProfitOrder.getSize(), takeProfitOrder.getPrice(), tpLimitFee, expireTime, takeProfitOrder.getClientOrderId(),takeProfitOrder.getSide());
            }catch (IOException e) {
                throw new ApexProApiException(EXEC_ERROR,"An error occurred when signing tp-order with l2KeyPair");
            }
            builder.putToPost("isSetOpenTp",true).putToPost("isOpenTpslOrder",true)
                    .putToPost("tpClientOrderId",takeProfitOrder.getClientOrderId())
                    .putToPost("tpExpiration",String.valueOf(expireTime))
                    .putToPost("tpLimitFee",tpLimitFee.toPlainString())
                    .putToPost("tpPrice",takeProfitOrder.getPrice().toPlainString())
                    .putToPost("tpSize",takeProfitOrder.getSize().toPlainString())
                    .putToPost("tpSide",takeProfitOrder.getSide().name())
                    .putToPost("tpTriggerPrice",takeProfitOrder.getTriggerPrice().toPlainString())
                    .putToPost("tpTriggerPriceType",takeProfitOrder.getTriggerPriceType().name())
                    .putToPost("tpSignature",tpSignature);
        }

        // with stop loss
        if (stopLossOrder != null) {

            //Sign the order with L2KeyPair
            String slSignature;
            if (Strings.isEmpty(stopLossOrder.getClientOrderId())) {
                stopLossOrder.setClientOrderId("sl"+clientOrderId);
            }
            BigDecimal slLimitFee = maxFeeRate.multiply(stopLossOrder.getSize()).multiply(stopLossOrder.getPrice()).setScale(Math.max(0, maxFeeRate.stripTrailingZeros().scale()), RoundingMode.UP);

            try {
                slSignature = L2OrderSigner.signOrder(l2KeyPair, apiCredential.getAccountId(), symbol, stopLossOrder.getSize(), stopLossOrder.getPrice(), slLimitFee, expireTime, stopLossOrder.getClientOrderId(),stopLossOrder.getSide());
            }catch (IOException e) {
                throw new ApexProApiException(EXEC_ERROR,"An error occurred when signing sl-order with l2KeyPair");
            }
            builder.putToPost("isSetOpenSl",true).putToPost("isOpenTpslOrder",true)
                    .putToPost( "slClientOrderId",stopLossOrder.getClientOrderId())
                    .putToPost("slExpiration",expireTime)
                    .putToPost("slLimitFee",slLimitFee)
                    .putToPost("slPrice",stopLossOrder.getPrice())
                    .putToPost("slSize",stopLossOrder.getSize())
                    .putToPost("slSide",stopLossOrder.getSide())
                    .putToPost("slTriggerPrice",stopLossOrder.getTriggerPrice())
                    .putToPost("slTriggerPriceType",stopLossOrder.getTriggerPriceType())
                    .putToPost("slSignature",slSignature);
        }
        log.info("request.post:{}",builder.getPostData());
        request.request = createRequest(serverUrl, "/v2/create-order", builder);
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

            if (takeProfitOrder != null)
                order.setTakeProfitOrder(takeProfitOrder);
            if (stopLossOrder != null)
                order.setStopLossOrder(stopLossOrder);

            return order;
        });
        return request;
    }


    public RestApiRequest<Order> createConditionalOrder(String symbol, OrderSide side, OrderType type, BigDecimal size, BigDecimal triggerPrice,PriceType triggerPriceType, BigDecimal orderPrice,BigDecimal maxFeeRate,TimeInForce timeInForce, String clientOrderId, boolean reduceOnly) {
        long expireTime = System.currentTimeMillis() + 18 * 24 * 60 * 60 * 1000;

        BigDecimal limitFee = maxFeeRate.multiply(size).multiply(orderPrice).setScale(Math.max(0, maxFeeRate.stripTrailingZeros().scale()), RoundingMode.UP);

        Ticker ticker = RestApiInvoker.callSync(this.getTicker( symbol ));
        BigDecimal currentPrice = null;
        if (triggerPriceType == PriceType.INDEX)
            currentPrice = ticker.getIndexPrice();
        else if (triggerPriceType == PriceType.ORACLE)
            currentPrice =  ticker.getOraclePrice();
        else
            currentPrice = ticker.getLastPrice();


        //Sign the order with L2KeyPair

        String signature;
        try {
            if (type == OrderType.MARKET) {
                BigDecimal maxMarketPriceRange = ExchangeInfo.perpetualContract(symbol).getMaxMarketPriceRange();
                if (side == OrderSide.BUY ) {
                    type = currentPrice.compareTo(triggerPrice) < 0 ? OrderType.STOP_MARKET : OrderType.TAKE_PROFIT_MARKET;
                    orderPrice  = triggerPrice.multiply(new BigDecimal(1).add(maxMarketPriceRange));
                } else {
                    type = currentPrice.compareTo(triggerPrice) > 0 ? OrderType.STOP_MARKET : OrderType.TAKE_PROFIT_MARKET;
                    orderPrice  = triggerPrice.multiply(new BigDecimal(1).subtract(maxMarketPriceRange));
                }
            } else if (type == OrderType.LIMIT) {
                if (side == OrderSide.BUY ) {
                    type = currentPrice.compareTo(triggerPrice) < 0 ? OrderType.STOP_LIMIT : OrderType.TAKE_PROFIT_LIMIT;
                } else {
                    type = currentPrice.compareTo(triggerPrice) > 0 ? OrderType.STOP_LIMIT : OrderType.TAKE_PROFIT_LIMIT;
                }
            }

            signature = L2OrderSigner.signOrder(this.l2KeyPair, apiCredential.getAccountId(), symbol, size, orderPrice, limitFee, expireTime, clientOrderId,side);
        }catch (IOException e) {
            throw new ApexProApiException(EXEC_ERROR,"An error occurred when signing an order with l2KeyPair");
        }

        RestApiRequest<Order> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("symbol", symbol)
                .putToPost("side", side.name())
                .putToPost("type", type.name())
                .putToPost("size", size.toPlainString())
                .putToPost("price", orderPrice.toPlainString())
                .putToPost("limitFee", limitFee.toPlainString())
                .putToPost("expiration", String.valueOf(expireTime))
                .putToPost("triggerPrice",String.valueOf(triggerPrice))
                .putToPost("triggerPriceType",triggerPriceType)
                .putToPost("timeInForce", timeInForce.name())
                .putToPost("clientId", clientOrderId)
                .putToPost("signature", signature)
                .putToPost("reduceOnly", String.valueOf(reduceOnly));

        request.request = createRequest(serverUrl, "/v2/create-order", builder);
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
            order.setTriggerPriceType(jsonWrapper.getString("triggerPriceType"));
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
        request.request = createRequest(serverUrl, "/v2/delete-order", builder);
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
        request.request = createRequest(serverUrl, "/v2/delete-client-order-id", builder);
        request.jsonParser = (jsonWrapper -> {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("data", jsonWrapper.getString("data"));
            return dataMap;
        });
        return request;
    }

    public RestApiRequest<OpenOrders> getOpenOrders(String contractZone) {
        RestApiRequest<OpenOrders> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build().putToUrl("token",contractZone);
        request.request = createRequest(serverUrl, "/v2/open-orders", builder);
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

    public RestApiRequest<Map<String, String>> cancelAllOpenOrders(String symbol,String contractZone) {

        RestApiRequest<Map<String, String>> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToPost("symbol", symbol)
                .putToPost("token",contractZone);
        request.request = createRequest(serverUrl, "/v2/delete-open-orders", builder);
        request.jsonParser = (jsonWrapper -> new HashMap<>());
        return request;
    }

    public RestApiRequest<HistoryOrders> getHistoryOrders(String symbol, OrderStatus status, OrderSide side, OrderType orderType, Long beginTimeInclusive, Long endTimeExclusive, Integer limit, Integer page) {
        RestApiRequest<HistoryOrders> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("token",ExchangeInfo.getContractZoneBySymbol(symbol))
                .putToUrl("status", status != null ? status.name() : "")
                .putToUrl("side", side != null ? side.name() : "")
                .putToUrl("type", orderType != null ? orderType.name() : "")
                .putToUrl("limit", String.valueOf(limit))
                .putToUrl("beginTimeInclusive", beginTimeInclusive > 0 ? String.valueOf(beginTimeInclusive) : "")
                .putToUrl("endTimeExclusive", endTimeExclusive > 0 ? String.valueOf(endTimeExclusive) : "")
                .putToUrl("page", String.valueOf(page));
        request.request = createRequest(serverUrl, "/v2/history-orders", builder);
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
        request.request = createRequest(serverUrl, "/v2/get-order", builder);
        request.jsonParser = (jsonWrapper -> {
            jsonWrapper = jsonWrapper.getJsonObject("data");
            Order order = new Order();
            order.setOrderId(jsonWrapper.getString("id"));

            if (Strings.isEmpty(order.getOrderId())) {
                throw new ApexProApiException(EXEC_ERROR,"order not found by given id="+id);
            }

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

            JsonWrapper openTpParam = jsonWrapper.getJsonObject("openTpParam");
            if (openTpParam != null && openTpParam.containKey("clientOrderId")) {
                OrderParams tpOrder = new OrderParams();
                tpOrder.setSize(new BigDecimal(openTpParam.getString("size")));
                tpOrder.setSide(OrderSide.valueOf(openTpParam.getString("side")));
                tpOrder.setPrice(new BigDecimal(openTpParam.getString("price")));
                tpOrder.setTriggerPrice(new BigDecimal(openTpParam.getString("triggerPrice")));
                tpOrder.setTriggerPriceType(PriceType.valueOf(openTpParam.getString("triggerPriceType")));
                tpOrder.setClientOrderId(openTpParam.getString("clientOrderId"));
                order.setTakeProfitOrder(tpOrder);
            }


            JsonWrapper openSlParam = jsonWrapper.getJsonObject("openSlParam");
            if (openSlParam != null && openSlParam.containKey("clientOrderId")) {
                OrderParams slOrder = new OrderParams();
                slOrder.setSize(new BigDecimal(openSlParam.getString("size")));
                slOrder.setSide(OrderSide.valueOf(openSlParam.getString("side")));
                slOrder.setPrice(new BigDecimal(openSlParam.getString("price")));
                slOrder.setTriggerPrice(new BigDecimal(openSlParam.getString("triggerPrice")));
                slOrder.setTriggerPriceType(PriceType.valueOf(openSlParam.getString("triggerPriceType")));
                slOrder.setClientOrderId(openSlParam.getString("clientOrderId"));
                order.setStopLossOrder(slOrder);
            }



            return order;
        });
        return request;
    }

    public RestApiRequest<Order> getOrderByClientOrderId(String id,String contractZone) {
        RestApiRequest<Order> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("id", id)
                .putToUrl("token",contractZone);
        request.request = createRequest(serverUrl, "/v2/order-by-client-order-id", builder);
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

    public RestApiRequest<WithdrawalList> getWithdrawList(String currencyId,Integer limit, Long page, Long beginTimeInclusive, Long endTimeExclusive) {
        RestApiRequest<WithdrawalList> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("limit", String.valueOf(limit))
                .putToUrl("page", String.valueOf(page))
                .putToUrl("currencyId", currencyId)
                .putToUrl("beginTimeInclusive", beginTimeInclusive != null ? String.valueOf(beginTimeInclusive) : "")
                .putToUrl("endTimeExclusive", endTimeExclusive != null ? String.valueOf(endTimeExclusive) : "")
                .putToUrl("transferType", "WITHDRAW,FAST_WITHDRAW,CROSS_WITHDRAW");
        request.request = createRequest(serverUrl, "/v2/transfers", builder);
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
        request.request = createRequest(serverUrl, "/v2/create-withdrawal-to-address", builder);
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

        Optional< MultiChain.Chain> chain = ExchangeInfo.multiChain(ExchangeInfo.getContractZone(currencyId)).getChains().stream().filter(f->f.getChainId() == chainId).findAny();
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
        request.request = createRequest(serverUrl, "/v2/fast-withdraw", builder);
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

        Optional< MultiChain.Chain> chain = ExchangeInfo.multiChain(ExchangeInfo.getContractZone(currencyId)).getChains().stream().filter(f->f.getChainId() == chainId).findAny();
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
        request.request = createRequest(serverUrl, "/v2/cross-chain-withdraw", builder);
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

    public RestApiRequest<WithdrawalFee> getWithdrawalFee(String collateralToken,BigDecimal amount, long chainId) {
        RestApiRequest<WithdrawalFee> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("amount", amount.toPlainString())
                .putToUrl("chainId", String.valueOf(chainId))
                .putToUrl("token",collateralToken)
                ;
        request.request = createRequest(serverUrl, "/v2/uncommon-withdraw-fee", builder);
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
        request.request = createRequest(serverUrl, "/v2/get-worst-price", builder);
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
        String contractZone = ExchangeInfo.getContractZoneBySymbol(symbol);
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("symbol", symbol)
                .putToUrl("limit", limit != null? String.valueOf(limit) : "")
                .putToUrl("page", page != null ? String.valueOf(page) : "")
                .putToUrl("token",contractZone)
                .putToUrl("beginTimeInclusive",beginTimeInclusive !=null ? String.valueOf(beginTimeInclusive) : "")
                .putToUrl("endTimeExclusive",endTimeExclusive != null ? String.valueOf(endTimeExclusive) : "")
                .putToUrl("positionSide",positionSide != null ? positionSide.name() : "");

        request.request = createRequest(serverUrl, "/v2/funding", builder);
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


    public RestApiRequest<Ticker> getTicker(String symbol) {
        RestApiRequest<Ticker> request = new RestApiRequest<>();
        RequestParamsBuilder builder = RequestParamsBuilder.build()
                .putToUrl("symbol", symbol.replace("-",""));

        request.request = createRequest(serverUrl, "/v1/ticker", builder);
        request.jsonParser = (jsonWrapper -> {
            JsonWrapperArray jsonWrapperArray = jsonWrapper.getJsonArray("data");

            JsonWrapper wrapper = jsonWrapperArray.getJsonObjectAt(0);
            Ticker ticker = new Ticker();
            ticker.setSymbol(wrapper.getString("symbol"));
            ticker.setPrice24hChange(new BigDecimal(wrapper.getString("price24hPcnt")));
            ticker.setLastPrice(new BigDecimal(wrapper.getString("lastPrice")));
            ticker.setHighPrice24h(new BigDecimal(wrapper.getString("highPrice24h")));
            ticker.setLowPrice24h(new BigDecimal(wrapper.getString("lowPrice24h")));
            ticker.setOraclePrice(new BigDecimal(wrapper.getString("oraclePrice")));
            ticker.setIndexPrice(new BigDecimal(wrapper.getString("indexPrice")));
            ticker.setOpenInterest(new BigDecimal(wrapper.getString("openInterest")));
            ticker.setTurnover24h(new BigDecimal(wrapper.getString("turnover24h")));
            ticker.setVolume24h(new BigDecimal(wrapper.getString("volume24h")));
            ticker.setFundingRate(new BigDecimal(wrapper.getString("fundingRate")));
            ticker.setPredictedFundingRate(new BigDecimal(wrapper.getString("predictedFundingRate")));
            ticker.setNextFundingTime(wrapper.getString("nextFundingTime"));
            ticker.setTradeCount(wrapper.getLong("tradeCount"));

            return ticker;
        });
        return request;
    }
}
