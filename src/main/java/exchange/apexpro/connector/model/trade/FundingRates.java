package exchange.apexpro.connector.model.trade;

import lombok.Data;

import java.util.List;

@Data
public class FundingRates {

    private List<FundingRate> fundingRates;
    Long totalSize;
}
