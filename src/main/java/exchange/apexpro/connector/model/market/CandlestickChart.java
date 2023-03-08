package exchange.apexpro.connector.model.market;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

@Data
public class CandlestickChart {

    private Long start;
    private Long end;
    private String interval;
    private BigDecimal open;
    private BigDecimal close;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal volume;
    private BigDecimal turnover;
    private Boolean confirm; //If it is the last tick of this candle
    private Long time;


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
                .append("start",start)
                .append("end",end)
                .append("interval",interval)
                .append("open",open)
                .append("close",close)
                .append("high",high)
                .append("low",low)
                .append("volume",volume)
                .append("turnover",turnover)
                .append("confirm",confirm)
                .append("time",time)
                .toString();
    }
}
