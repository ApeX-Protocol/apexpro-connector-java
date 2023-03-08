package exchange.apexpro.connector.model.meta;


import java.math.BigDecimal;

@lombok.Data
@lombok.Builder(builderMethodName = "newBuilder", builderClassName = "Builder", toBuilder = true, setterPrefix="set")
public class PerpetualContract {

    /* 标识信息 */
    private final String symbol; // 永续合约币对标识，例如: BTC-USDC
    private final String symbolDisplayName; // 永续合约币对展示名称(不要用来做存储字段)，例如: BTCUSDC
    private final String settleCurrencyId; // 结算货币id，例如：USDC
    private final Currency settleCurrency; // 结算货币
    private final String underlyingCurrencyId; // 标的货币id，例如：BTC
    private final Currency underlyingCurrency; // 标的货币

    /* 各种数量限制 */
    private final BigDecimal tickSize; // 最小报价单位 (settle_currency)
    private final BigDecimal tickSizeZero; // 报价0值 (settle_currency)
    private final BigDecimal stepSize; // 最小数量单位 (underlying_currency)
    private final BigDecimal stepSizeZero; // 数量0值 (underlying_currency)
    private final BigDecimal valueSize; // 最小仓位价值单位 (settle_currency)
    private final BigDecimal valueSizeZero; // 仓位价值0值 (settle_currency)
    private final BigDecimal minOrderSize; // 最小委托单数量 (underlying_currency)
    private final BigDecimal maxOrderSize; // 最大委托单数据 (underlying_currency)
    private final BigDecimal maxPositionSize; // 最大仓位数量 (underlying_currency)
    private final BigDecimal maxMarketPriceRange; // 最差市价成交范围 (max_market_price_range)

    /* 保证金率 */
    private final BigDecimal initialMarginRate; // 初始保证金率
    private final BigDecimal maintenanceMarginRate; // 维持保证金率
    private final BigDecimal baselinePositionValue; // 基准仓位价值 (settle_currency)
    private final BigDecimal incrementalPositionValue; // 阶梯计算时，每档仓位价值增量 (settle_currency)
    private final BigDecimal incrementalInitialMarginRate; // 初始保证金率每档的增加量
    private final BigDecimal incrementalMaintenanceMarginRate; // 维持保证金率每档的增加量
    private final BigDecimal maxMaintenanceMarginRate; // 维持保证金最大值。即分档计算后的最大值，不能超过此值
    private final BigDecimal maxPositionValue; // 分档计算中最大仓位数量

    /* 各种开关 */
    private final boolean enableTrade; // 是否可以交易. true: 可以交易, false: 不可交易
    private final boolean enableDisplay; // 是否可以展示. true: 可以展示, false: 隐藏
    private final boolean enableOpenPosition; // 是否可以开仓. true: 可以开仓和平仓，false: 不可以开仓仅可平仓
    private final boolean enableFundingSettlement; // 是否可以进行资金费率结算. true: 可以结算，false: 不进行结算

    /* 其他设置 */
    private final String digitMerge; // 深度合并. 例如: "1,0.1,0.001"
    private final int crossId; // 当前合约所属crossId
    private final int crossSymbolId; // 对接cross使用的币对id
    private final String crossSymbolName; // 对接cross使用的币对名称
    private final BigDecimal fundingInterestRate; // 综合利率默认值, decimal. 例如: 0.0003
    private final BigDecimal fundingImpactMarginNotional; // 深度加权买卖价计算数量, decimal. 例如: 8000
    private final BigDecimal displayMaxLeverage; // 展示杠杆最大倍数, decimal. 例如: 20
    private final BigDecimal displayMinLeverage; // 展示杠杆最小倍数, decimal. 例如: 1
    private final int indexPriceDecimals; // 指数价格精度
    private final BigDecimal indexPriceVarRate; // 指数价格波动比例
    private final BigDecimal openPositionOiLimitRate; // 当前币对Open Interest（OI）限制比例，单账户高于比例限制下单
    private final BigDecimal openPositionOiLimitMin; // 当前币对Open Interest（OI）低于此额度，OI限制无效

    /* starkex对接信息 */
    private final String starkExSyntheticAssetId; // 当前币对对应的混合资产id, hex str.
    private final BigDecimal starkExResolution; // 当前币对持有数量处理精度, decimal.
    private final long starkExRiskFactor;
    private final BigDecimal starkExRiskRate; // starkEx风险比率 starkExRiskFactor / 2^32 得到，精确值
    private final String starkExOraclePriceQuorum;

}
