package exchange.apexpro.connector.model.event;

import exchange.apexpro.connector.model.market.Ticker;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@Data
public class AllTickersEvent {

    private String topic; //Same as request args
    private String eventType; //Snapshot or delta

    private Long eventTime;

    private Long updateId;

    List<Ticker> tickers;



    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
                .append("topic",topic)
                .append("eventType", eventType)
                .append("eventTime", eventTime)
                .append("updateId", updateId)
                .append("tickers", tickers)
                .toString();
    }
}
