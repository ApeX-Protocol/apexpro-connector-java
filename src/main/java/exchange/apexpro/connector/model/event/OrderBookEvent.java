package exchange.apexpro.connector.model.event;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.List;

import exchange.apexpro.connector.model.market.OrderBookEntry;
import org.apache.commons.lang3.builder.ToStringStyle;

@Data
public class OrderBookEvent {

    private String topic; //Same as request args
    private String eventType; //Snapshot or delta

    private Long eventTime;

    private String symbol;
    private Long updateId;

    private List<OrderBookEntry> bids;
    private List<OrderBookEntry> asks;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
                .append("topic",topic)
                .append("eventType", eventType)
                .append("eventTime", eventTime).append("symbol", symbol)
                .append("updateId", updateId)
                .append("bids", bids).append("asks", asks)
                .toString();
    }
}
