package exchange.apexpro.connector.impl.utils;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;



/**
 * The ApiCredentialHelper class provides functionality to handle operations related to API credentials in a concurrent environment.
 * It provides methods for encrypting and decrypting private keys, as well as for generating HMACs using a shared secret key.
 *
 * Public static methods:
 *
 * 1. encryptSecret(String secret, String passphrase): Encrypts the user's private key. It performs encryption operation asynchronously
 *    using a thread pool executor. The encrypted key is Base64 encoded and returned as a String.
 *
 * 2. decryptSecret(String encrypted_secret, String passphrase) Decrypts the user's private key. This operation is performed asynchronously.
 *    The method expects a Base64 encoded encrypted secret and returns the decrypted secret as a String.
 *
 * 3. createHmac(String secretKey, String data): Generates HMAC (Hash-based Message Authentication Code) using the provided secretKey and data.
 *    This operation is synchronous and the HMAC is returned as a Base64 encoded String.
 *
 * In addition to the public static methods, the class also utilizes a private method, urlBase64Encode(byte[] data), for Base64 encoding operations.
 */


public class ApiCredentialHelper {

    private static ExecutorService fixedThreadPoolExecutor = Executors.newFixedThreadPool(10);


    private static String urlBase64Encode(byte[] data){
        return Base64.getEncoder().encodeToString(data)
                .replaceAll("=", "")
                .replaceAll("\\+", "-")
                .replaceAll("/", "_");
    }

    /**
     * Encrypts the user private key
     * @param secret
     * @param passphrase
     * @return
     */
    public static String encryptSecret(String secret,String passphrase){
        String encryptedSecret = null;
        Future<String> encryptedSecretFuture = fixedThreadPoolExecutor.submit(() -> {
            try {
                SecureRandom random = new SecureRandom();
                byte[] salt = new byte[16];
                random.nextBytes(salt);
                byte[] key = passphrase.getBytes("UTF-8");
                MessageDigest sha = MessageDigest.getInstance("SHA-1");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16);
                SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                cipher.init(Cipher.ENCRYPT_MODE, secretKey);


                AlgorithmParameters params = cipher.getParameters();
                byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
                byte[] encryptedText = cipher.doFinal(secret.getBytes("UTF-8"));

                // concatenate salt + iv + ciphertext
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(salt);
                outputStream.write(iv);
                outputStream.write(encryptedText);
                // properly encode the complete ciphertext
                return DatatypeConverter.printBase64Binary(outputStream.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });

        try {
            encryptedSecret = encryptedSecretFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return encryptedSecret;
    }

    /**
     * Decrypt the user's private key
     * @param encrypted_secret
     * @param passphrase
     * @return
     */
    public static String decryptSecret(String encrypted_secret,String passphrase){

        Future<String> decryptedSecretFuture = fixedThreadPoolExecutor.submit(() -> {
            try {
                byte[] ciphertext = Base64.getDecoder().decode(encrypted_secret);
                if (ciphertext.length < 48) {
                    return null;
                }
                byte[] salt = Arrays.copyOfRange(ciphertext, 0, 16);
                byte[] iv = Arrays.copyOfRange(ciphertext, 16, 32);
                byte[] ct = Arrays.copyOfRange(ciphertext, 32, ciphertext.length);

                byte[] key = passphrase.getBytes("UTF-8");
                MessageDigest sha = MessageDigest.getInstance("SHA-1");
                key = sha.digest(key);
                key = Arrays.copyOf(key, 16);
                SecretKey secretKey = new SecretKeySpec(key, "AES");

                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
                byte[] plaintext = cipher.doFinal(ct);
                String secretResult = new String(plaintext, "UTF-8");
                return secretResult;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });

        String decryptedSecret = null;
        try {
            decryptedSecret = decryptedSecretFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return decryptedSecret;
    }

    /**
     * generate HMAC
     * @param
     * @param data
     * @return
     * @throws Exception
     */
    public static String createHmac(String secretKey, String data){
        byte[] hmacSha256 = null;
        String secretKeyBase64 = Base64.getEncoder().encodeToString(secretKey.getBytes());
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyBase64.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            hmacSha256 = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hmac-sha256", e);
        }
        return Base64.getEncoder().encodeToString(hmacSha256);
    }

}
