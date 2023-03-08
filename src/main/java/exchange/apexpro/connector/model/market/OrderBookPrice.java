package exchange.apexpro.connector.model.market;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

@Data
public class OrderBookPrice {

    BigDecimal worstPrice;
    BigDecimal bidOnePrice;
    BigDecimal askOnePrice;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
                .append("worstPrice", worstPrice)
                .append("bidOnePrice", bidOnePrice)
                .append("askOnePrice", askOnePrice)
                .toString();
    }
}
