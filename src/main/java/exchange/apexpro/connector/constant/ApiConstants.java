package exchange.apexpro.connector.constant;

import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;

/**
 * Constants used throughout ApexPro's API.
 */
public class ApiConstants {
    public static final String BROKER_NAME = "ApeX";

    // ------------ API URLs ------------
    public static final String APEX_HTTP_MAIN = "https://pro.apex.exchange";
    public static final String APEX_HTTP_TEST = "https://testnet.pro.apex.exchange";

    public static final String APEX_WS_MAIN = "wss://quote.pro.apex.exchange";
    public static final String APEX_WS_TEST = "wss://quote-qa.pro.apex.exchange";

    public static final String URL_SUFFIX = "/api";


    // ------------ network_id ------------
    public static final int NETWORKID_MAIN = 1;
    public static final int NETWORKID_TEST = 5;


    // ------------ Assets ------------
    public static final String ASSET_USDC = "USDC";
    public static final String ASSET_USDT = "USDT";
    public static final String COLLATERAL_ASSET_USDC = ASSET_USDC;
    public static final String COLLATERAL_ASSET_USDT = ASSET_USDT;

    // ------------ Contract Area ------------
    public static String CONTRACT_ZONE_USDC = COLLATERAL_ASSET_USDC;
    public static String CONTRACT_ZONE_USDT = COLLATERAL_ASSET_USDT;


    // ------------ Ethereum Transactions ------------
    public static final Uint256 MAX_SOLIDITY_UINT = new Uint256(new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639935"));
    // ------------ Off-Chain Ethereum-Signed Actions ------------
    public static final String OFF_CHAIN_ONBOARDING_ACTION = BROKER_NAME+" Onboarding"; // action:ApeX Onboarding  onlySignOn:https://pro.apex.exchange nonce:1188491033265307648
    public static final String OFF_CHAIN_KEY_DERIVATION_ACTION = "L2 Key"; //{"name": "ApeX","version": "1.0","envId": 1,"action": "L2 Key","onlySignOn": "https://pro.apex.exchange"}
    public static final String OFF_CHAIN_ONBOARDING_SIGN_DOMAIN = "https://pro.apex.exchange";
    //------------  StarkEx ------------
    public static final long ORDER_SIGNATURE_EXPIRATION_BUFFER_HOURS = 24l * 7l * 4l;


}
