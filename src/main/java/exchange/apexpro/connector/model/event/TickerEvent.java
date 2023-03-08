package exchange.apexpro.connector.model.event;

import exchange.apexpro.connector.model.market.Ticker;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
public class TickerEvent {

    private String topic; //Same as request args
    private String eventType; //Snapshot or delta

    private Long eventTime;

    private Long updateId;

    Ticker ticker;



    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
                .append("topic",topic)
                .append("eventType", eventType)
                .append("eventTime", eventTime)
                .append("updateId", updateId)
                .append("ticker", ticker)
                .toString();
    }
}
