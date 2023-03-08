package exchange.apexpro.connector.model.meta;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import exchange.apexpro.connector.RequestOptions;
import exchange.apexpro.connector.SyncRequestClient;
import exchange.apexpro.connector.exception.ApexProApiException;
import exchange.apexpro.connector.impl.utils.json.CostBigDecimalAdapter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static exchange.apexpro.connector.exception.ApexProApiException.RUNTIME_ERROR;

@Slf4j
public class ExchangeInfo {

    private static final AtomicReference<Holder> HOLDER_REF = new AtomicReference<>(
            new Holder(Global.newBuilder().build(), ImmutableMap.<String, Currency>of(), ImmutableMap.<String, PerpetualContract>of(), ImmutableMap.<Integer, PerpetualContract>of(), MultiChain.newBuilder().build()));

    private static boolean isLoaded = false;

    public static void load(){
        if (!isLoaded) {
            RequestOptions options = new RequestOptions();
            SyncRequestClient syncRequestClient = SyncRequestClient.create(options);
            String exchangeInfoJson = syncRequestClient.getExchangeInfo();
            loadData(exchangeInfoJson);
            isLoaded = true;
        }
    }

    private static void loadData(String exchangeInfoJsonStr) {
        log.info("[loadMetaData] content=", exchangeInfoJsonStr);
        try {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(BigDecimal.class, new CostBigDecimalAdapter());
            Gson gson = builder.create();

            JsonObject exchangeInfoJson = gson.fromJson(exchangeInfoJsonStr,JsonObject.class);
            List<Currency> currencyList = gson.fromJson(exchangeInfoJson.getAsJsonArray("currency"),new TypeToken<List<Currency>>(){}.getType());
            ImmutableMap<String, Currency> currencyMap = parseCurrency(currencyList);

            List<PerpetualContract> perpetualContractList = gson.fromJson(exchangeInfoJson.getAsJsonArray("perpetualContract"),new TypeToken<List<PerpetualContract>>(){}.getType());
            ImmutableMap<String, PerpetualContract> perpetualContractMap = parsePerpetualContract(perpetualContractList, currencyMap);

            ImmutableMap<Integer, PerpetualContract> crossSymbolIdToPerpetualContractMap = perpetualContractMap.values().stream()
                    .collect(ImmutableMap.toImmutableMap(
                            PerpetualContract::getCrossSymbolId,
                            Function.identity(),
                            (a, b) -> a));
            Global global = gson.fromJson(exchangeInfoJson.getAsJsonObject("global"),Global.class);

            MultiChain multiChain = gson.fromJson(exchangeInfoJson.getAsJsonObject("multiChain"),MultiChain.class);

            HOLDER_REF.set(new Holder(global, currencyMap, perpetualContractMap, crossSymbolIdToPerpetualContractMap, multiChain));
            log.info("[loadMetaData] loaded. global={}", global);
            currencyMap.values().forEach(currency -> log.info("[loadMetaData] finish. currency={}", currency));
            perpetualContractMap.values().forEach(perpetualContract -> log.info("[loadMetaData] finish. perpetualContract={}", perpetualContract));
        } catch (Throwable throwable) {
            log.error("[loadMetaData] error. exchangeInfoJsonStr={}", exchangeInfoJsonStr, throwable);
            throw new Error(throwable);
        }
    }


    private static ImmutableMap<String, Currency> parseCurrency(
            List<Currency> list) {
        return list.stream()
                .map(pb -> {
                    BigDecimal stepSize = pb.getStepSize();
                    return Currency.newBuilder()
                            .setId(pb.getId())
                            .setStepSize(stepSize)
                            .setStepSizeZero(BigDecimal.ZERO.setScale(stepSize.scale(), RoundingMode.UNNECESSARY))
                            .setIconUrl(pb.getIconUrl())
                            .setStarkExAssetId(pb.getStarkExAssetId())
                            .setStarkExResolution(pb.getStarkExResolution() == null ? BigDecimal.ONE : pb.getStarkExResolution())
                            .build();
                })
                .collect(ImmutableMap.toImmutableMap(
                        Currency::getId,
                        Function.identity()));
    }

    private static ImmutableMap<String, PerpetualContract> parsePerpetualContract(
            List<PerpetualContract> list,
            ImmutableMap<String, Currency> currencyMap) {
        return list.stream()
                .map(pb -> {
                    Currency settleCurrency = currencyMap.get(pb.getSettleCurrencyId());
                    if (settleCurrency == null) {
                        throw new RuntimeException("invalid settleCurrencyId: " + pb.getSettleCurrencyId());
                    }
                    Currency underlyingCurrency = currencyMap.get(pb.getUnderlyingCurrencyId());
                    if (underlyingCurrency == null) {
                        throw new RuntimeException("invalid underlyingCurrencyId: " + pb.getUnderlyingCurrencyId());
                    }

                    BigDecimal tickSize = pb.getTickSize();
                    BigDecimal stepSize = pb.getStepSize();
                    BigDecimal valueSize = tickSize.multiply(stepSize);
                    return PerpetualContract.newBuilder()
                            .setSymbol(pb.getSymbol())
                            .setSymbolDisplayName(pb.getSymbolDisplayName())
                            .setSettleCurrencyId(pb.getSettleCurrencyId())
                            .setSettleCurrency(settleCurrency)
                            .setUnderlyingCurrencyId(pb.getUnderlyingCurrencyId())
                            .setUnderlyingCurrency(underlyingCurrency)
                            .setTickSize(tickSize)
                            .setTickSizeZero(BigDecimal.ZERO.setScale(tickSize.scale(), RoundingMode.UNNECESSARY))
                            .setStepSize(stepSize)
                            .setStepSizeZero(BigDecimal.ZERO.setScale(stepSize.scale(), RoundingMode.UNNECESSARY))
                            .setValueSize(valueSize)
                            .setValueSizeZero(BigDecimal.ZERO.setScale(valueSize.scale(), RoundingMode.UNNECESSARY))
                            .setMinOrderSize(pb.getMinOrderSize())
                            .setMaxOrderSize(pb.getMaxOrderSize())
                            .setMaxPositionSize(pb.getMaxPositionSize())
                            .setMaxMarketPriceRange(pb.getMaxMarketPriceRange())
                            .setInitialMarginRate(pb.getInitialMarginRate())
                            .setMaintenanceMarginRate(pb.getMaintenanceMarginRate())
                            .setBaselinePositionValue(pb.getBaselinePositionValue())
                            .setIncrementalPositionValue(pb.getIncrementalPositionValue())
                            .setIncrementalInitialMarginRate(pb.getIncrementalInitialMarginRate())
                            .setIncrementalMaintenanceMarginRate(pb.getIncrementalMaintenanceMarginRate())
                            .setMaxMaintenanceMarginRate(pb.getMaxMaintenanceMarginRate())
                            .setMaxPositionValue(pb.getMaxPositionValue())
                            .setEnableTrade(pb.isEnableTrade())
                            .setEnableDisplay(pb.isEnableDisplay())
                            .setEnableOpenPosition(pb.isEnableOpenPosition())
                            .setEnableFundingSettlement(pb.isEnableFundingSettlement())
                            .setDigitMerge(pb.getDigitMerge())
                            .setCrossId(pb.getCrossId())
                            .setCrossSymbolId(pb.getCrossSymbolId())
                            .setCrossSymbolName(pb.getCrossSymbolName())
                            .setFundingInterestRate(pb.getFundingInterestRate())
                            .setFundingImpactMarginNotional(pb.getFundingImpactMarginNotional())
                            .setDisplayMaxLeverage(pb.getDisplayMaxLeverage())
                            .setDisplayMinLeverage(pb.getDisplayMinLeverage())
                            .setIndexPriceDecimals(pb.getIndexPriceDecimals())
                            .setIndexPriceVarRate(pb.getIndexPriceVarRate())
                            .setOpenPositionOiLimitRate(pb.getOpenPositionOiLimitRate())
                            .setOpenPositionOiLimitMin(pb.getOpenPositionOiLimitMin())
                            .setStarkExSyntheticAssetId(pb.getStarkExSyntheticAssetId())
                            .setStarkExResolution(pb.getStarkExResolution())
                            .setStarkExRiskFactor(pb.getStarkExRiskFactor())
                            .setStarkExRiskRate(new BigDecimal(pb.getStarkExRiskFactor()).divide(new BigDecimal(1L << 32)))
                            .build();
                })
                .collect(ImmutableMap.toImmutableMap(
                        PerpetualContract::getSymbol,
                        Function.identity()));
    }

    public static Global global() {
        if (!isLoaded)
            load();
        return HOLDER_REF.get().global;
    }

    public static MultiChain multiChain() {
        if (!isLoaded)
            load();

        return HOLDER_REF.get().multiChain;
    }

    public static Currency currency(String currencyId) {
        if (!isLoaded)
            load();

        Currency currency = HOLDER_REF.get().currencyMap.get(currencyId);
        if (currency == null) {
            throw new ApexProApiException(RUNTIME_ERROR,"invalid currencyId: " + currencyId);
        }
        return currency;
    }

    public static ImmutableMap<String, Currency> currencyMap() {
        if (!isLoaded)
            load();

        return HOLDER_REF.get().currencyMap;
    }

    public static PerpetualContract perpetualContract(String symbol) {
        if (!isLoaded)
            load();

        PerpetualContract perpetualContract = HOLDER_REF.get().perpetualContractMap.get(symbol);
        if (perpetualContract == null) {
            throw new ApexProApiException(RUNTIME_ERROR,"invalid symbol: " + symbol);
        }
        return perpetualContract;
    }

    public static ImmutableMap<String, PerpetualContract> perpetualContractMap() {
        if (!isLoaded)
            load();

        return HOLDER_REF.get().perpetualContractMap;
    }

    public static ImmutableMap<Integer, PerpetualContract> crossSymbolIdToPerpetualContractMap() {
        if (!isLoaded)
            load();

        return HOLDER_REF.get().crossSymbolIdToPerpetualContractMap;
    }

    private static class Holder {
        private final Global global;
        private final ImmutableMap<String, Currency> currencyMap;
        private final ImmutableMap<String, PerpetualContract> perpetualContractMap;
        private final ImmutableMap<Integer, PerpetualContract> crossSymbolIdToPerpetualContractMap;
        private final MultiChain multiChain;

        Holder(Global global,
               ImmutableMap<String, Currency> currencyMap,
               ImmutableMap<String, PerpetualContract> perpetualContractMap,
               ImmutableMap<Integer, PerpetualContract> crossSymbolIdToPerpetualContractMap,
               MultiChain multiChain) {
            this.global = global;
            this.currencyMap = currencyMap;
            this.perpetualContractMap = perpetualContractMap;
            this.crossSymbolIdToPerpetualContractMap = crossSymbolIdToPerpetualContractMap;
            this.multiChain = multiChain;
        }
    }

    private static BigDecimal parseDecimal(String str, BigDecimal defaultValue) {
        return (str == null || str.equals("")) ? defaultValue : new BigDecimal(str);
    }
}
