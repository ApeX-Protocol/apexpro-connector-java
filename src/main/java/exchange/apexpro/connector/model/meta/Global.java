package exchange.apexpro.connector.model.meta;

import java.math.BigDecimal;

@lombok.Data
@lombok.Builder(builderMethodName = "newBuilder", builderClassName = "Builder", toBuilder = true, setterPrefix="set")
public class Global {

    private final long feeAccountId; // 手续费账户id
    private final String feeAccountL2Key; // 手续费账户l2Key
    private final long fastWithdrawAccountId; // 快速提现账户id
    private final String fastWithdrawL2Key; //快速提现账户l2Key
    private final BigDecimal fastWithdrawMaxAmount; // 快速提现最大额度
    private final String fastWithdrawFactRegisterAddress; // L1 Fact注册的合约地址
    private final long crossChainAccountId; // 跨链冲提lp账户id
    private final String crossChainL2Key; // 跨链冲提lp账户key

    private final String starkExCollateralCurrencyId; // starkex 支持的抵押品货币id
    private final Currency starkExCollateralCurrency; // starkex 支持的抵押品货币
    private final BigDecimal starkExMaxFundingRate; // 最大资金费用
    private final int starkExOrdersTreeHeight; // 订单树高度
    private final int starkExPositionsTreeHeight; // position数高度
    private final int starkExFundingValidityPeriod;
    private final int starkExPriceValidityPeriod;

    private final String operationAccountId;// 活动奖励账户id

    private final String operationL2Key;// 活动奖励账户l2key

    private final long registerEnvId;
    private final String starkExContractAddress;
}
