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

import static exchange.apexpro.connector.constant.ApiConstants.*;
import static exchange.apexpro.connector.exception.ApexProApiException.RUNTIME_ERROR;

@Slf4j
public class ExchangeInfo {

    private static final AtomicReference<Holder> HOLDER_REF_USDC = new AtomicReference<>(
            new Holder(Global.newBuilder().build(), ImmutableMap.<String, Currency>of(), ImmutableMap.<String, PerpetualContract>of(), ImmutableMap.<Integer, PerpetualContract>of(), MultiChain.newBuilder().build()));

    private static final AtomicReference<Holder> HOLDER_REF_USDT = new AtomicReference<>(
            new Holder(Global.newBuilder().build(), ImmutableMap.<String, Currency>of(), ImmutableMap.<String, PerpetualContract>of(), ImmutableMap.<Integer, PerpetualContract>of(), MultiChain.newBuilder().build()));

    private static boolean isLoaded = false;

    public static void load(){
        if (!isLoaded) {
            RequestOptions options = new RequestOptions();
            SyncRequestClient syncRequestClient = SyncRequestClient.create(options);
            String exchangeInfoJsonStr = syncRequestClient.getExchangeInfo();

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(BigDecimal.class, new CostBigDecimalAdapter());
            Gson gson = builder.create();

            JsonObject exchangeInfoJson = gson.fromJson(exchangeInfoJsonStr,JsonObject.class);
            JsonObject usdcConfig = exchangeInfoJson.getAsJsonObject("usdcConfig");
            JsonObject usdtConfig = exchangeInfoJson.getAsJsonObject("usdtConfig");
            loadData(usdcConfig,HOLDER_REF_USDC);
            loadData(usdtConfig,HOLDER_REF_USDT);
            isLoaded = true;
        }
    }

    private static void loadData(JsonObject usdcConfig,AtomicReference<Holder> holderAtomicReference) {
        log.info("[loadMetaData] content=", usdcConfig.toString());
        try {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(BigDecimal.class, new CostBigDecimalAdapter());
            Gson gson = builder.create();


            List<Currency> currencyList = gson.fromJson(usdcConfig.getAsJsonArray("currency"),new TypeToken<List<Currency>>(){}.getType());
            ImmutableMap<String, Currency> currencyMap = parseCurrency(currencyList);

            List<PerpetualContract> perpetualContractList = gson.fromJson(usdcConfig.getAsJsonArray("perpetualContract"),new TypeToken<List<PerpetualContract>>(){}.getType());
            ImmutableMap<String, PerpetualContract> perpetualContractMap = parsePerpetualContract(perpetualContractList, currencyMap);

            ImmutableMap<Integer, PerpetualContract> crossSymbolIdToPerpetualContractMap = perpetualContractMap.values().stream()
                    .collect(ImmutableMap.toImmutableMap(
                            PerpetualContract::getCrossSymbolId,
                            Function.identity(),
                            (a, b) -> a));
            Global global = gson.fromJson(usdcConfig.getAsJsonObject("global"),Global.class);

            MultiChain multiChain = gson.fromJson(usdcConfig.getAsJsonObject("multiChain"),MultiChain.class);

            holderAtomicReference.set(new Holder(global, currencyMap, perpetualContractMap, crossSymbolIdToPerpetualContractMap, multiChain));
            log.info("[loadMetaData] loaded. global={}", global);
            currencyMap.values().forEach(currency -> log.info("[loadMetaData] finish. currency={}", currency));
            perpetualContractMap.values().forEach(perpetualContract -> log.info("[loadMetaData] finish. perpetualContract={}", perpetualContract));
        } catch (Throwable throwable) {
            log.error("[loadMetaData] error. exchangeInfoJsonStr={}", usdcConfig.toString(), throwable);
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

    public static Global global(String contractZone) {
        if (!isLoaded)
            load();
        if (contractZone.equals(CONTRACT_ZONE_USDT))
            return HOLDER_REF_USDT.get().global;
        if (contractZone.equals(CONTRACT_ZONE_USDC))
            return HOLDER_REF_USDC.get().global;
        return null;
    }

    public static MultiChain multiChain(String contractZone) {
        if (!isLoaded)
            load();

        if (contractZone.equals(CONTRACT_ZONE_USDT))
            return HOLDER_REF_USDT.get().multiChain;
        if (contractZone.equals(CONTRACT_ZONE_USDC))
            return HOLDER_REF_USDC.get().multiChain;

        return null;
    }

    public static Currency currency(String currencyId,String contractZone) {
        if (!isLoaded)
            load();

        Currency currency = null;
        if (contractZone.equals(CONTRACT_ZONE_USDT))
            currency = HOLDER_REF_USDT.get().currencyMap.get(currencyId);
        if (contractZone.equals(CONTRACT_ZONE_USDC))
            currency = HOLDER_REF_USDC.get().currencyMap.get(currencyId);

        if (currency == null) {
            throw new ApexProApiException(RUNTIME_ERROR,"invalid currencyId: " + currencyId);
        }
        return currency;
    }

    public static ImmutableMap<String, Currency> currencyMap(String contractZone) {
        if (!isLoaded)
            load();

        if (contractZone.equals(CONTRACT_ZONE_USDT))
            return HOLDER_REF_USDT.get().currencyMap;
        if (contractZone.equals(CONTRACT_ZONE_USDC))
            return HOLDER_REF_USDC.get().currencyMap;

        return null;
    }

    public static PerpetualContract perpetualContract(String symbol) {
        if (!isLoaded)
            load();
        if (HOLDER_REF_USDC.get().perpetualContractMap.containsKey(symbol))
            return HOLDER_REF_USDC.get().perpetualContractMap.get(symbol);

        if (HOLDER_REF_USDT.get().perpetualContractMap.containsKey(symbol))
            return HOLDER_REF_USDT.get().perpetualContractMap.get(symbol);

            throw new ApexProApiException(RUNTIME_ERROR,"invalid symbol: " + symbol);
    }

    public static ImmutableMap<String, PerpetualContract> perpetualContractMap(String contractZone) {
        if (!isLoaded)
            load();

        if (contractZone.equals(CONTRACT_ZONE_USDT))
            return HOLDER_REF_USDT.get().perpetualContractMap;
        if (contractZone.equals(CONTRACT_ZONE_USDC))
            return HOLDER_REF_USDC.get().perpetualContractMap;


        return null;
    }

    public static ImmutableMap<Integer, PerpetualContract> crossSymbolIdToPerpetualContractMap(String contractZone) {
        if (!isLoaded)
            load();
        if (contractZone.equals(CONTRACT_ZONE_USDT))
            return HOLDER_REF_USDT.get().crossSymbolIdToPerpetualContractMap;
        if (contractZone.equals(CONTRACT_ZONE_USDC))
            return HOLDER_REF_USDC.get().crossSymbolIdToPerpetualContractMap;

        return null;
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

    public static String getContractZone(String collateralToken) {
        if (collateralToken.toUpperCase().equals(COLLATERAL_ASSET_USDC))
            return CONTRACT_ZONE_USDC;
        if (collateralToken.toUpperCase().equals(COLLATERAL_ASSET_USDT))
            return CONTRACT_ZONE_USDT;
        return null;
    }

    public static String getContractZoneBySymbol(String symbol) {
        ImmutableMap<String, PerpetualContract> perpetualContractImmutableMap = perpetualContractMap(COLLATERAL_ASSET_USDC);
        if (perpetualContractImmutableMap != null && perpetualContractImmutableMap.containsKey(symbol))
            return COLLATERAL_ASSET_USDC;

        perpetualContractImmutableMap = perpetualContractMap(COLLATERAL_ASSET_USDT);
        if (perpetualContractImmutableMap != null && perpetualContractImmutableMap.containsKey(symbol))
            return COLLATERAL_ASSET_USDT;

        log.error("Symbol is in valid");
        return null;
    }
}
