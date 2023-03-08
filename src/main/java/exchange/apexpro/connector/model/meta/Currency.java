package exchange.apexpro.connector.model.meta;

import java.math.BigDecimal;

@lombok.Data
@lombok.Builder(builderMethodName = "newBuilder", builderClassName = "Builder", toBuilder = true, setterPrefix="set")
public class Currency {

    private final String id;  // 货币id。例如：USDT
    private final BigDecimal stepSize; // 最小数量单位
    private final BigDecimal stepSizeZero; // 最小数量单位,0值
    private final String iconUrl; // 货币图标url

    private final String starkExAssetId; // starkex资产id, 默认为空
    private final BigDecimal starkExResolution; // starkex精度，默认为1

}
