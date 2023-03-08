package exchange.apexpro.connector.model.event;

import exchange.apexpro.connector.model.account.Account;
import exchange.apexpro.connector.model.account.Position;
import exchange.apexpro.connector.model.account.Wallet;
import exchange.apexpro.connector.model.trade.Order;
import exchange.apexpro.connector.model.trade.OrderFill;
import exchange.apexpro.connector.model.wallet.Deposit;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;

@Data
public class AccountDataEvent {

    private String topic; //Same as request args
    private String eventType; //Snapshot or delta

    private Long eventTime;

    private Long updateId;

    List<Deposit> transfers;
    List<Order> orders;
    List<Position> positions;
    List<Wallet> wallets;
    Account account;
    List<OrderFill> orderFills;


    @Override
    public String toString() {

        return new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE)
                .append("topic",topic)
                .append("eventType", eventType)
                .append("eventTime", eventTime)
                .append("updateId", updateId)
                .append("orderFills", orderFills)
                .append("transfers", transfers)
                .append("orders", orders)
                .append("positions", positions)
                .append("account", account)
                .toString();
    }
}
