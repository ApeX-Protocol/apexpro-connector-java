package exchange.apexpro.connector.model.meta;

import java.util.List;

@lombok.Data
@lombok.Builder(builderMethodName = "newBuilder", builderClassName = "Builder", toBuilder = true, setterPrefix = "set")
public class MultiChain {
    private final String currency; // 充提资产，USDC
    private final String maxWithdraw; // 最大提币量
    private final String minWithdraw; // 最小提币量
    private final String minDeposit; // 最小充币量
    private final List<Chain> chains; //支持的链

    @lombok.Data
    @lombok.Builder(builderMethodName = "newBuilder", builderClassName = "Builder", toBuilder = true, setterPrefix = "set")
    public static class Chain {
        private final String chain; // 主链名
        private final Long chainId; // chainId
        private final String chainIconUrl; //主链图标url
        private final String contractAddress; // 合约地址
        private final Boolean depositGasFeeLess; //是否收充值手续费
        private final Boolean feeLess; // 是否免手续费
        private final String feeRate; // 手续费率
        private final Boolean gasLess; //  是否收手续费
        private final String gasToken; // 主链代币名
        private final String minFee; //提币最小手续费 如果gas+value*fee_rate 小于 min_fee, 就按照min_fee收取
        private final String rpcUrl; // 链的线上节点服务
        private final String webTxUrl; // 交易tx链接
        private final Boolean withdrawGasFeeLess; // 是否收提币手续费
        private final List<MultiChainToken> tokens;
        private final Long txConfirm; // 链上充值确认数
        private final String blockTime; // 区块时间
    }

    @lombok.Data
    @lombok.Builder(builderMethodName = "newBuilder", builderClassName = "Builder", toBuilder = true, setterPrefix = "set")
    public static class MultiChainToken {
        private final String tokenAddress;  //token合约地址
        private final Long decimals; // token精度
        private final String iconUrl; // token图标url
        private final String token; // token name
        private final Boolean pullOff; //是否下架，默认false
    }
}




















