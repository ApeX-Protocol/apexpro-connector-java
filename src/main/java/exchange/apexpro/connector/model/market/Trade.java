package exchange.apexpro.connector.model.market;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

@Data
public class Trade {


    private String symbol;
    private BigDecimal price;

    private BigDecimal volume;


    private Long time;

    private Boolean isBuyer;

    private String tickDirection;
    private String id;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
                .append("id", id)
                .append("price", price)
                .append("volume", volume)
                .append("time", time)
                .append("isBuyer", isBuyer)
                .append("tickDirection", tickDirection)
                .toString();
    }
}
