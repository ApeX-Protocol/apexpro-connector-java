package exchange.apexpro.connector.model.user;

import lombok.Data;

@Data
public class ApiCredential {
    // Key
    private String apiKey;
    // Secret
    private String secret;
    // Passphrase
    private String passphrase;

    //ID
    private Long userId;
    private Long accountId;
    private String address;
    private int networkId;
}
