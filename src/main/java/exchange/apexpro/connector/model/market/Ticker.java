package exchange.apexpro.connector.model.market;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

@Data
/**
 * This topic only utilizes the update field. Both the delete and insert fields are null.
 * If a key is not found in the update field, its value has not changed.
 */
public class Ticker {


    private String symbol;
    private BigDecimal price24hChange;
    private BigDecimal lastPrice;
    private BigDecimal highPrice24h;
    private BigDecimal lowPrice24h;
    private BigDecimal oraclePrice;
    private BigDecimal indexPrice;
    private BigDecimal openInterest;
    private BigDecimal turnover24h;
    private BigDecimal volume24h;
    private BigDecimal fundingRate;
    private BigDecimal predictedFundingRate;
    private String nextFundingTime;
    private Long tradeCount;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
                .append("symbol",symbol)
                .append("price24hPcnt", price24hChange)
                .append("lastPrice",lastPrice)
                .append("highPrice24h",highPrice24h)
                .append("lowPrice24h",lowPrice24h)
                .append("oraclePrice",oraclePrice)
                .append("indexPrice",indexPrice)
                .append("openInterest",openInterest)
                .append("turnover24h",turnover24h)
                .append("volume24h",volume24h)
                .append("fundingRate",fundingRate)
                .append("predictedFundingRate",predictedFundingRate)
                .append("nextFundingTime",nextFundingTime)
                .append("tradeCount",tradeCount)
                .toString();
    }
}
