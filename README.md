# ApexPro Public API connector Java

This is a lightweight library that enables Java developers to easily connect to [ApeX-Protocol](pro.apex.exchange)


This library is a wrapper around the [ApexPro API](https://api-docs.pro.apex.exchange/#introduction).

## Installation

Replace `LATEST_VERSION` with the latest version number and paste the snippet below in `pom.xml`
```
<dependency>
  <groupId>io.github.apex-protocol</groupId>
  <artifactId>apexpro-connector-java</artifactId>
  <version>LATEST_VERSION</version>
</dependency>
```
Run `mvn install` where `pom.xml` is located to install the dependency.

## Run Example

The examples are located under src/test/java/examples. Before running the examples, you only need set up your ethereum private key ETH_PRIVATE_KEY in PrivateConfig.java. This configuration file is only used for examples, you should rebuild your own PrivateConfig when using the library.


### Market Endpoint: Get Exchange Information

```
//Load configration information from server side;
ExchangeInfo.load();

log.info("ExchangeInfo.global:{} ",ExchangeInfo.global());
log.info("ExchangeInfo.currencies: {}",ExchangeInfo.currencyMap());
log.info("ExchangeInfo.perpetualContracts: {}", ExchangeInfo.perpetualContractMap());
```

### Onboarding Endpoint: Onboard to ApexPro with an ethereum private key;

```
String ETH_PRIVATE_KEY = "Your ethereum private key";
ApexProCredentials apexProCredentials = ApexProCredentials.create(ETH_PRIVATE_KEY,ApiConstants.NETWORKID_TEST);

log.info("apexProCredentials:{} ",apexProCredentials);
```


### Trade Endpoint: Testing a new order

```
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

//Send order to ApexPro
SyncRequestClient syncRequestClient = SyncRequestClient.create(apiCredential);
Order order = syncRequestClient.createOrder(symbol, OrderSide.BUY, OrderType.LIMIT, size, price, limitFee, expireTime, OrderType.LIMIT, null, null, clientId, signature, false);
log.info("Created Order:{} ",order);
```

### Websocket: Market subscription

```
SubscriptionOptions subscriptionOptions = new SubscriptionOptions();
subscriptionOptions.setUri(ApiConstants.APEX_WS_TEST+"/realtime_public");
SubscriptionClient client = SubscriptionClient.create(subscriptionOptions);

//depth subscription;
client.subscribeDepthEvent("BTCUSDC",200, EventFrequency.H, ((event) -> {
    log.info("message:"+event);
}), exception -> {
    log.error("exception:"+exception.getMessage());
});

//trades subscription
client.subscribeTradeEvent("BTCUSDC", EventFrequency.H, ((event) -> {
    log.info("message:"+event);
}), exception -> {
    log.error("exception:"+exception.getMessage());
});

//ticker subscription
client.subscribeTickerEvent("BTCUSDC", EventFrequency.H, ((event) -> {
    log.info("message:"+event);
}), exception -> {
    log.error("exception:"+exception.getMessage());
});
//candlestick chart subscription
client.subscribeCandlestickEvent("BTCUSDC", "3", ((event) -> {
    log.info("message:"+event);
}), exception -> {
    log.error("exception:"+exception.getMessage());
});

//all tickers subscription
client.subscribeAllTickersEvent(((event) -> {
    log.info("message:"+event);
}), exception -> {
    log.error("exception:"+exception.getMessage());
});

Thread.sleep(1000l * 30l);
client.unsubscribeAll();

```

### Websocket: account data steam subscription


```
SubscriptionOptions subscriptionOptions = new SubscriptionOptions();
subscriptionOptions.setUri(ApiConstants.APEX_WS_TEST+"/realtime_private");
SubscriptionClient client = SubscriptionClient.create(subscriptionOptions);

ApiCredential apiCredential = PrivateConfig.loadConfig().apiCredential;
client.subscribeAccountDataEvent(apiCredential,((event) -> {
    log.info("message:{}",event);
}), exception -> {
    log.error("exception:{}",exception.getMessage());
});
```

## Contributing

Contributions are welcome.
If you've found a bug within this project, please open an issue to discuss what you would like to change.


