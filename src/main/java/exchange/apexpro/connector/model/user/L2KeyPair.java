package exchange.apexpro.connector.model.user;

import lombok.Data;

@Data
public class L2KeyPair {
    private String publicKey;
    private String publicKeyYCoordinate;
    private String privateKey;
}
