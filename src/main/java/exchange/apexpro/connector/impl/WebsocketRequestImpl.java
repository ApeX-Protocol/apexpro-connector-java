package exchange.apexpro.connector.impl;

import exchange.apexpro.connector.SubscriptionErrorHandler;
import exchange.apexpro.connector.SubscriptionListener;
import exchange.apexpro.connector.impl.utils.Channels;
import exchange.apexpro.connector.impl.utils.JsonWrapper;
import exchange.apexpro.connector.impl.utils.JsonWrapperArray;
import exchange.apexpro.connector.model.account.Position;
import exchange.apexpro.connector.model.account.Wallet;
import exchange.apexpro.connector.model.enums.EventFrequency;
import exchange.apexpro.connector.model.event.*;
import exchange.apexpro.connector.model.market.CandlestickChart;
import exchange.apexpro.connector.model.market.OrderBookEntry;
import exchange.apexpro.connector.model.market.Ticker;
import exchange.apexpro.connector.model.market.Trade;
import exchange.apexpro.connector.model.trade.Order;
import exchange.apexpro.connector.model.trade.OrderFill;
import exchange.apexpro.connector.model.user.ApiCredential;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class WebsocketRequestImpl {

    WebsocketRequestImpl() {
    }


    WebsocketRequest<OrderBookEvent> subscribeDepthEvent(String symbol, int limit, EventFrequency frequency,
                                                         SubscriptionListener<OrderBookEvent> subscriptionListener,
                                                         SubscriptionErrorHandler errorHandler) {
        InputChecker.checker()
                .shouldNotNull(symbol, "symbol")
                .shouldNotNull(subscriptionListener, "listener");
        WebsocketRequest<OrderBookEvent> request = new WebsocketRequest<>(subscriptionListener, errorHandler);

        request.name = "***Depth for " + symbol + "***";
        request.connectionHandler = (connection) -> connection.send(Channels.depthChannel(symbol,frequency,limit));

        request.jsonParser = (jsonWrapper) -> {

            OrderBookEvent result = new OrderBookEvent();
            result.setTopic(jsonWrapper.getString("topic"));
            result.setEventType(jsonWrapper.getString("type"));
            result.setEventTime(jsonWrapper.getLong("ts"));

            JsonWrapper dataJsonWrapper = jsonWrapper.getJsonObject("data");

            result.setSymbol(dataJsonWrapper.getString("s"));
            result.setUpdateId(dataJsonWrapper.getLong("u"));

            //parse bids;
            JsonWrapperArray bidsJsonWrapper = dataJsonWrapper.getJsonArray("b");
            List<OrderBookEntry> bids = new ArrayList<>();
            bidsJsonWrapper.forEachAsArray(item -> {
                OrderBookEntry bid = new OrderBookEntry();
                bid.setPrice(new BigDecimal(item.getStringAt(0)));
                bid.setQty(new BigDecimal(item.getStringAt(1)));
                bids.add(bid);
            });
            result.setBids(bids);

            //parse asks;
            JsonWrapperArray asksJsonWrapper = dataJsonWrapper.getJsonArray("a");
            List<OrderBookEntry> asks = new ArrayList<>();
            asksJsonWrapper.forEachAsArray(item -> {
                OrderBookEntry ask = new OrderBookEntry();
                ask.setPrice(new BigDecimal(item.getStringAt(0)));
                ask.setQty(new BigDecimal(item.getStringAt(1)));
                asks.add(ask);
            });
            result.setAsks(asks);

            return result;
        };
        return request;
    }


    WebsocketRequest<TradeEvent> subscribeTradeEvent(String symbol, EventFrequency frequency,
                                                     SubscriptionListener<TradeEvent> subscriptionListener,
                                                     SubscriptionErrorHandler errorHandler) {
        InputChecker.checker()
                .shouldNotNull(symbol, "symbol")
                .shouldNotNull(subscriptionListener, "listener");
        WebsocketRequest<TradeEvent> request = new WebsocketRequest<>(subscriptionListener, errorHandler);

        request.name = "***Trade subscription for " + symbol + "***";
        request.connectionHandler = (connection) -> connection.send(Channels.tradeChannel(symbol,frequency));

        request.jsonParser = (jsonWrapper) -> {

            TradeEvent result = new TradeEvent();
            result.setTopic(jsonWrapper.getString("topic"));
            result.setEventType(jsonWrapper.getString("type"));
            result.setEventTime(jsonWrapper.getLong("ts"));
            result.setUpdateId(jsonWrapper.getLong("cs"));

            JsonWrapperArray dataJsonWrapper = jsonWrapper.getJsonArray("data");

            //parse trade;
            List<Trade> trades = new ArrayList<>();
            dataJsonWrapper.forEach(item -> {
                Trade trade = new Trade();
                trade.setTime(item.getLong("T"));
                trade.setSymbol(item.getString("s"));
                trade.setIsBuyer(item.getString("S").equals("Buy"));
                trade.setVolume(new BigDecimal(item.getString("v")));
                trade.setPrice(new BigDecimal(item.getString("p")));
                trade.setTickDirection(item.getString("L")); //Tick direction
                trade.setId(item.getString("i"));
                trades.add(trade);
            });
            result.setTrades(trades);

            return result;
        };
        return request;
    }

    WebsocketRequest<TickerEvent> subscribeTickerEvent(String symbol, EventFrequency frequency,
                                                      SubscriptionListener<TickerEvent> subscriptionListener,
                                                      SubscriptionErrorHandler errorHandler) {
        InputChecker.checker()
                .shouldNotNull(symbol, "symbol")
                .shouldNotNull(subscriptionListener, "listener");
        WebsocketRequest<TickerEvent> request = new WebsocketRequest<>(subscriptionListener, errorHandler);

        request.name = "***Ticker subscription for " + symbol + "***";
        request.connectionHandler = (connection) -> connection.send(Channels.tickerChannel(symbol,frequency));

        request.jsonParser = (jsonWrapper) -> {

            TickerEvent result = new TickerEvent();
            result.setTopic(jsonWrapper.getString("topic"));
            result.setEventType(jsonWrapper.getString("type"));
            result.setEventTime(jsonWrapper.getLong("ts"));
            result.setUpdateId(jsonWrapper.getLong("cs"));

            JsonWrapper dataJsonWrapper = jsonWrapper.getJsonObject("data");

            Ticker ticker = new Ticker();
            ticker.setSymbol(dataJsonWrapper.getString("symbol"));

            ticker.setSymbol(dataJsonWrapper.getString("symbol"));
            if (dataJsonWrapper.containKey("price24hPcnt"))
                ticker.setPrice24hChange(new BigDecimal(dataJsonWrapper.getString("price24hPcnt")));
            if (dataJsonWrapper.containKey("lastPrice"))
                ticker.setLastPrice(new BigDecimal(dataJsonWrapper.getString("lastPrice")));
            if (dataJsonWrapper.containKey("highPrice24h"))
                ticker.setHighPrice24h(new BigDecimal(dataJsonWrapper.getString("highPrice24h")));
            if (dataJsonWrapper.containKey("lowPrice24h"))
                ticker.setLowPrice24h(new BigDecimal(dataJsonWrapper.getString("lowPrice24h")));
            if (dataJsonWrapper.containKey("oraclePrice"))
                ticker.setOraclePrice(new BigDecimal(dataJsonWrapper.getString("oraclePrice")));
            if (dataJsonWrapper.containKey("indexPrice"))
                ticker.setIndexPrice(new BigDecimal(dataJsonWrapper.getString("indexPrice")));
            if (dataJsonWrapper.containKey("openInterest"))
                ticker.setOpenInterest(new BigDecimal(dataJsonWrapper.getString("openInterest")));
            if (dataJsonWrapper.containKey("turnover24h"))
                ticker.setTurnover24h(new BigDecimal(dataJsonWrapper.getString("turnover24h")));
            if (dataJsonWrapper.containKey("volume24h"))
                ticker.setVolume24h(new BigDecimal(dataJsonWrapper.getString("volume24h")));
            if (dataJsonWrapper.containKey("fundingRate"))
                ticker.setFundingRate(new BigDecimal(dataJsonWrapper.getString("fundingRate")));
            if (dataJsonWrapper.containKey("predictedFundingRate"))
                ticker.setPredictedFundingRate(new BigDecimal(dataJsonWrapper.getString("predictedFundingRate")));
            if (dataJsonWrapper.containKey("nextFundingTime"))
                ticker.setNextFundingTime(dataJsonWrapper.getString("nextFundingTime"));
            if (dataJsonWrapper.containKey("tradeCount"))
                ticker.setTradeCount(Long.valueOf(dataJsonWrapper.getString("tradeCount")));

            result.setTicker(ticker);

            return result;
        };
        return request;
    }




    WebsocketRequest<CandlestickChartEvent> subscribeCandlestickEvent(String symbol, String interval,
                                                       SubscriptionListener<CandlestickChartEvent> subscriptionListener,
                                                       SubscriptionErrorHandler errorHandler) {
        InputChecker.checker()
                .shouldNotNull(symbol, "symbol")
                .shouldNotNull(subscriptionListener, "listener");
        WebsocketRequest<CandlestickChartEvent> request = new WebsocketRequest<>(subscriptionListener, errorHandler);

        request.name = "***Candlestick subscription for " + symbol + "***";
        request.connectionHandler = (connection) -> connection.send(Channels.candlestickChannel(symbol,interval));

        request.jsonParser = (jsonWrapper) -> {

            CandlestickChartEvent result = new CandlestickChartEvent();
            result.setTopic(jsonWrapper.getString("topic"));
            result.setEventType(jsonWrapper.getString("type"));
            result.setEventTime(jsonWrapper.getLong("ts"));

            JsonWrapperArray dataJsonArrWrapper = jsonWrapper.getJsonArray("data");

            List<CandlestickChart> candlestickCharts = new ArrayList<>();
            dataJsonArrWrapper.forEach(dataJsonWrapper -> {
                CandlestickChart candlestickChart = new CandlestickChart();
                candlestickChart.setStart(dataJsonWrapper.getLong("start"));
                candlestickChart.setEnd(dataJsonWrapper.getLong("end"));
                candlestickChart.setInterval(dataJsonWrapper.getString("interval"));
                candlestickChart.setOpen(new BigDecimal(dataJsonWrapper.getString("open")));
                candlestickChart.setClose(new BigDecimal(dataJsonWrapper.getString("close")));
                candlestickChart.setHigh(new BigDecimal(dataJsonWrapper.getString("high")));
                candlestickChart.setLow(new BigDecimal(dataJsonWrapper.getString("low")));
                candlestickChart.setVolume(new BigDecimal(dataJsonWrapper.getString("volume")));
                candlestickChart.setTurnover(new BigDecimal(dataJsonWrapper.getString("turnover")));
                candlestickChart.setConfirm(dataJsonWrapper.getBoolean("confirm"));
                candlestickChart.setTime(dataJsonWrapper.getLong("time"));
                candlestickCharts.add(candlestickChart);
            });


            result.setCandlestickCharts(candlestickCharts);

            return result;
        };
        return request;
    }


    WebsocketRequest<AllTickersEvent> subscribeAllTickersEvent(
                                                       SubscriptionListener<AllTickersEvent> subscriptionListener,
                                                       SubscriptionErrorHandler errorHandler) {
        InputChecker.checker()
                .shouldNotNull(subscriptionListener, "listener");
        WebsocketRequest<AllTickersEvent> request = new WebsocketRequest<>(subscriptionListener, errorHandler);

        request.name = "***All tickers subscription***";
        request.connectionHandler = (connection) -> connection.send(Channels.allTickersChannel());

        request.jsonParser = (jsonWrapper) -> {

            AllTickersEvent result = new AllTickersEvent();
            result.setTopic(jsonWrapper.getString("topic"));
            result.setEventType(jsonWrapper.getString("type"));
            result.setEventTime(jsonWrapper.getLong("ts"));
            result.setUpdateId(jsonWrapper.getLong("cs"));

            JsonWrapperArray dataJsonAllWrapper = jsonWrapper.getJsonArray("data");
            List<Ticker> tickers = new ArrayList<>();
            dataJsonAllWrapper.forEach(dataJsonWrapper -> {
                Ticker ticker = new Ticker();

                ticker.setSymbol(dataJsonWrapper.getString("s"));
                if (dataJsonWrapper.containKey("p"))
                    ticker.setLastPrice(new BigDecimal(dataJsonWrapper.getString("p")));

                if (dataJsonWrapper.containKey("pr"))
                    ticker.setPrice24hChange(new BigDecimal(dataJsonWrapper.getString("pr")));

                if (dataJsonWrapper.containKey("h"))
                    ticker.setHighPrice24h(new BigDecimal(dataJsonWrapper.getString("h")));
                if (dataJsonWrapper.containKey("l"))
                    ticker.setLowPrice24h(new BigDecimal(dataJsonWrapper.getString("l")));

                if (dataJsonWrapper.containKey("op"))
                    ticker.setOraclePrice(new BigDecimal(dataJsonWrapper.getString("op")));

                if (dataJsonWrapper.containKey("xp"))
                    ticker.setIndexPrice(new BigDecimal(dataJsonWrapper.getString("xp")));

                if (dataJsonWrapper.containKey("to"))
                    ticker.setTurnover24h(new BigDecimal(dataJsonWrapper.getString("to")));

                if (dataJsonWrapper.containKey("v"))
                    ticker.setVolume24h(new BigDecimal(dataJsonWrapper.getString("v")));

                if (dataJsonWrapper.containKey("fr"))
                    ticker.setFundingRate(new BigDecimal(dataJsonWrapper.getString("fr")));

                if (dataJsonWrapper.containKey("o"))
                    ticker.setOpenInterest(new BigDecimal(dataJsonWrapper.getString("o")));

                if (dataJsonWrapper.containKey("tc"))
                    ticker.setTradeCount(Long.valueOf(dataJsonWrapper.getString("tc")));


                if (dataJsonWrapper.containKey("pr"))
                    ticker.setPrice24hChange(new BigDecimal(dataJsonWrapper.getString("pr")));

                tickers.add(ticker);
            });

            result.setTickers(tickers);

            return result;
        };
        return request;
    }




    WebsocketRequest<AccountDataEvent> subscribeAccountDataEvent(
            ApiCredential apiCredential,
            SubscriptionListener<AccountDataEvent> subscriptionListener,
            SubscriptionErrorHandler errorHandler) {
        InputChecker.checker()
                .shouldNotNull(subscriptionListener, "listener");
        WebsocketRequest<AccountDataEvent> request = new WebsocketRequest<>(subscriptionListener, errorHandler);

        request.name = "***Account data subscription***";

        request.connectionHandler = (connection) -> {
            connection.send(Channels.signeChannel(apiCredential));
        };

        request.jsonParser = (jsonWrapper) -> {
            AccountDataEvent result = null;
            if (jsonWrapper.containKey("contents")) {
                result = new AccountDataEvent();
                result.setTopic(jsonWrapper.getString("topic"));
                result.setEventType(jsonWrapper.getString("type"));
                result.setEventTime(jsonWrapper.getLong("ts"));
                result.setUpdateId(jsonWrapper.getLong("cs"));

                //Orders
                JsonWrapperArray jsonOrders =  jsonWrapper.getJsonArray("orders");
                List<Order> orders = new ArrayList<>();
                jsonOrders.forEach(item -> {
                    Order order = new Order();

                    order.setOrderId(item.getString("id"));
                    order.setAccountId(item.getString("accountId"));
                    order.setClientOrderId(item.getString("clientId"));

                    order.setCumSuccessFillFee(new BigDecimal(item.getString("cumSuccessFillFee")));
                    order.setCumSuccessFillSize(new BigDecimal(item.getString("cumSuccessFillSize")));
                    order.setCumSuccessFillValue(new BigDecimal(item.getString("cumSuccessFillValue")));
                    order.setCumSuccessLiquidateFee(new BigDecimal(item.getString("cumSuccessLiquidateFee")));
                    order.setExpiresTime(item.getLong("expiresAt"));

                    order.setIsDeleverage(item.getBoolean("isDeleverage"));
                    order.setIsLiquidate(item.getBoolean("isLiquidate"));
                    order.setIsPositionTpsl(item.getBoolean("isPositionTpsl"));
                    order.setLimitFee(new BigDecimal(item.getString("limitFee")));
                    order.setPrice(new BigDecimal(item.getString("price")));
                    order.setReduceOnly(item.getBoolean("reduceOnly"));
                    order.setSide(item.getString("side"));
                    order.setSize(new BigDecimal(item.getString("size")));
                    order.setStatus(item.getString("status"));
                    order.setSymbol(item.getString("symbol"));
                    order.setTimeInForce(item.getString("timeInForce"));
                    order.setTriggerPrice(new BigDecimal(item.getString("triggerPrice")));
                    order.setTriggerPriceType(item.getString("triggerPriceType"));
                    order.setType(item.getString("type"));

                    order.setCreatedTime(item.getLong("createdAt"));
                    order.setUpdatedTime(item.getLong("updatedAt"));
                    orders.add(order);


                });

                //Order Fills
                JsonWrapperArray jsonOrderFills =  jsonWrapper.getJsonArray("fills");
                List<OrderFill> orderFills = new ArrayList<>();
                jsonOrderFills.forEach(item -> {
                    OrderFill orderFill = new OrderFill();
                    orderFill.setId(item.getString("id"));
                    orderFill.setClientOrderId(item.getString("clientOrderId"));
                    orderFill.setCreatedTime(item.getLong("createdAt"));
                    orderFill.setDirection(item.getString("direction"));
                    orderFill.setFee(new BigDecimal(item.getString("fee")));
                    orderFill.setIsDeleverage(item.getBoolean("isDeleverage"));
                    orderFill.setIsLiquidate(item.getBoolean("isLiquidate"));
                    orderFill.setLiquidateFee(new BigDecimal(item.getString("liquidateFee")));
                    orderFill.setMatchFillId(item.getString("matchFillId"));
                    orderFill.setOrderId(item.getString("orderId"));
                    orderFill.setPrice(new BigDecimal(item.getString("price")));
                    orderFill.setSize(new BigDecimal(item.getString("size")));

                    orderFill.setQuoteAmount(orderFill.getSize().multiply(orderFill.getPrice()));
                    orderFill.setSide(item.getString("side"));

                    orderFill.setStatus(item.getString("status"));
                    orderFill.setSymbol(item.getString("symbol"));
                    orderFill.setUpdatedTime(item.getLong("updatedAt"));
                    orderFills.add(orderFill);
                });
                result.setOrderFills(orderFills);

                //positions
                JsonWrapperArray jsonPositions =  jsonWrapper.getJsonArray("positions");
                List<Position> positions = new ArrayList<>();
                jsonPositions.forEach(item -> {
                    Position position = new Position();
                    position.setAccountId(item.getString("accountId"));
                    position.setClosedTime(item.getLong("closedAt"));
                    position.setCustomInitialMarginRate(new BigDecimal(item.getString("customInitialMarginRate")));
                    position.setEntryPrice(new BigDecimal(item.getString("entryPrice")));
                    position.setExitPrice(new BigDecimal(item.getString("exitPrice")));
                    position.setMaxSize(new BigDecimal(item.getString("maxSize")));
                    position.setFundingFee(new BigDecimal(item.getString("fundingFee")));
                    position.setSide(item.getString("side"));
                    position.setSize(new BigDecimal(item.getString("size")));
                    position.setSumClose(new BigDecimal(item.getString("sumClose")));
                    position.setSumOpen(new BigDecimal(item.getString("sumOpen")));
                    position.setSymbol(item.getString("symbol"));
                    position.setUpdatedTime(item.getLong("updatedAt"));
                    positions.add(position);
                });
                result.setPositions(positions);


                //wallets
                JsonWrapperArray jsonWallets =  jsonWrapper.getJsonArray("wallets");
                List<Wallet> wallets = new ArrayList<>();
                jsonWallets.forEach(item -> {
                    Wallet wallet = new Wallet();
                    wallet.setToken(item.getString("token"));
                    wallet.setBalance(item.getString("balance"));
                    wallet.setPendingDepositAmount(item.getString("pendingDepositAmount"));
                    wallet.setPendingTransferInAmount(item.getString("pendingTransferInAmount"));
                    wallet.setPendingWithdrawAmount(item.getString("pendingWithdrawAmount"));
                    wallet.setPendingTransferOutAmount(item.getString("pendingTransferOutAmount"));
                    wallets.add(wallet);
                });
                result.setWallets(wallets);
            }

            return result;
        };
        return request;
    }


}
