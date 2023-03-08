package exchange.apexpro.connector.model.user;

import lombok.Data;

@Data
public class User {

    private String ethereumAddress;

    private Boolean isRegistered;

    private String email;

    private String username;

    private Boolean isEmailVerified;

    private Boolean emailNotifyGeneralEnable;

    private Boolean emailNotifyTradingEnable;

    private Boolean emailNotifyAccountEnable;

    private Boolean popupNotifyTradingEnable;

}
