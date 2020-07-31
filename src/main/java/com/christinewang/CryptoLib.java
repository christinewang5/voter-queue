package com.christinewang;

import org.postgresql.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import static com.christinewang.HTMLBase.protected_template;
import static com.christinewang.Application.LOG;


//passphrase: "voting is awesome"
/** A bunch of functions to encrypt the admin panel, securing it.
 * */
public class CryptoLib {
    //This is the password for the admin panel.
    private static final String admin_password="voting is awesome";
    //PBKDF2 hashing mode.
    private static final String hashingMode="PBKDF2WithHmacSHA256";
    //AES encryption mode.
    private static final String defaultMode="AES/CBC/PKCS5Padding";

    /** A function to get the secured admin page.
     * @param upsalt The filename to use for uploading. Should be randomized, so that
     *               an attacker cannot change the precinct names by guessing the filename
     *               and submitting their own POST request.
     * @param csvpath A secret random string, made so that only authorized people
     *                can download the csv data.
     * @return The secured page, in html format.
     * */
    public static String get_adminpage(String upsalt, String csvpath) throws Exception {
        //Make an empty initialization vector.
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        //And fill it with random bytes. The iv MUST be random.
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        //Get the base64 version of that, to feed to the javascript later.
        String iv_b64 = Base64.encodeBytes(iv);
        //A salt with unprintable characters resulted in some problems, so we're only using printables here.
        byte[] salt=getRandomString(" !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~",
                32).getBytes();
        //We need a base64 version, to throw at the javascript.
        String salt_b64 = Base64.encodeBytes(salt);
        //Generate the pbkdf2 hash, 50k iterations should be fine.
        byte[] dec_key = pbkdf2(admin_password,salt,50000,32, hashingMode);
        //Replace the upload filename and csvpath.
        String replaced = HTMLBase.admin
                .replace("REPLACEME",upsalt)
                .replace("REPLACETHIS",csvpath);
        //Then encrypt the admin page.
        byte[] encrypted = encrypt(replaced,dec_key,iv,defaultMode);
        //And get the base64 version of the encrypted admin page.
        String encrypted_b64 = java.util.Base64.getEncoder().encodeToString(encrypted);
        //Assemble that into a nice JSON, for injection into the template javascript decryptor.
        String enc_json = String.format("{\"salt\":\"%s\",\"iv\":\"%s\",\"data\":\"%s\"}",
                salt_b64,iv_b64,encrypted_b64);
        //And throw it in. We're done.
        String concat = protected_template.replace("/*ENCRYPTED_PAYLOAD}}*/\"\"",enc_json);
        return concat;
    }

    /** An nice method to AES-encrypt a string.
     * Sourced from <a href=https://gist.github.com/itarato/abef95871756970a9dad>here</a>, with some minor modifications.
     * @param plainText The text to be encrypted.
     * @param key The AES key. Should have size 32 bytes (256 bits).
     * @param iv The AES init vector. Use a different one every time (randomly generated), of size 16 bytes.
     * @param mode The AES mode to use (AES/ECB/... or AES/CBC/...).
     *             Because ECB has some insecurities, please use CBC.
     *             Also, the JS decryptor uses CBC, so use CBC.
     * @return The encrypted bytes.
     * */
    public static byte[] encrypt(String plainText, byte[] key, byte[] iv, String mode) throws Exception {
        //Turn the plaintext into bytes.
        byte[] clean = plainText.getBytes();
        //Put the IV in a format that the cipher will be able to use.
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        //Put the key in a format that the cipher will be able to use.
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        //Make a new cipher, using the mode specified as an input.
        Cipher cipher = Cipher.getInstance(mode);
        //Give the cipher the key and iv, and tell it to encrypt.
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        //Now give it the plaintext (in bytes), and let it encrypt.
        byte[] encrypted = cipher.doFinal(clean);
        //It's encrypted!
        return encrypted;
    }

    /** Gets a (cryptographically secure) random String.
     * @param optionchars A String containing the characters to choose from.
     * @param length The length of String to generate.
     * @return A nicely randomized String.
     * */
    public static String getRandomString(String optionchars, int length) {
        //Get a cryptographically secure pseudorandom number generator.
        SecureRandom rand = new SecureRandom();
        //Make a stringbuilder to hold our assembled string.
        StringBuilder retvar = new StringBuilder();
        //Run the loop "length" times.
        for (int i=0;i<length;i++) {
            //Add a randomly-determined character from optionchars each time around the loop.
            retvar.append(optionchars.charAt((int) (optionchars.length() * rand.nextFloat())));
        }
        //And return our wonderful random string.
        return retvar.toString();
    }

    /** A function that hashes a password with an arbitrary algorithm.
     * It's named pbkdf2 because that's all that it's used for.
     * @param password The password to hash.
     * @param salt The salt to use for the password hash.
     * @param iterations The number of times to hash it.
     * @param keyLength The size of hash to return.
     * @param mode The hashing mode to use. Always "PBKDF2WithHmacSHA256".
     * @return A byte[], containing the password hash.
     * */
    public static byte[] pbkdf2(String password, byte[] salt, int iterations, int keyLength, String mode)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        char[] chars = password.toCharArray();

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, keyLength*8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(mode);
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return hash; //toHex(hash);
    }
}
