package com.christinewang;

import com.google.common.hash.Hashing;
import org.bouncycastle.crypto.params.KeyParameter;
import org.postgresql.util.Base64;

import javax.crypto.Cipher;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.crypto.macs.HMac;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import static com.christinewang.AdminController.MAX_PRECINCT;
import static com.christinewang.AdminController.MIN_PRECINCT;
import static com.christinewang.HTMLBase.protected_template;
import static com.christinewang.Application.LOG;


//passphrase: "voting is awesome"
/** A bunch of functions to encrypt the admin panel, securing it.
 * */
//TODO make the key into a param, like /1?key=nonsense
    //TODO put the phash in db, NO hardcoding of plaintext.
public class CryptoLib {
    //This is the password for the admin panel.
    private static final String admin_password="voting is awesome";
    //PBKDF2 hashing mode.
    private static final String hashingMode="PBKDF2WithHmacSHA256";
    //AES encryption mode.
    private static final String defaultMode="AES/CBC/PKCS5Padding";

    /*
        DO NOT change the following variables if you want any currently printed QRs to work.
        A change to any of the HMAC prefixes/suffixes WILL render all corresponding
        printed QR codes useless. You will need to reprint all corresponding QR codes.
        Ex: changing a start prefix/suffix would mean that reprints of all start QR codes
        would be required. However, end QR codes wouldn't be impacted.
     */

    //HMAC start key prefix
    private static final String START_HMAC_K_PREFIX = "szmK8NywT/Cj3jwYGFAYddO2rCiGxwnYqo9w/KICdq6lUl+R83WeHFu1Pipn7yni5tttXSp+MNyl6aLWo2Y7ETHC6iNQ0wA553wj8Vb/vnjIgQyDWyJUNRlfbp25qt1n4Q065Q==";
    //HMAC start key suffix
    private static final String START_HMAC_K_SUFFIX = "RFqSJUXvqRrq5VcxNDUQnlOCVQ/xkzqMQP7yyze9doGI6JqwFQ25iKHRWUr3ePUT5rMn89k2HzIM08IDGbU7khfyfwx1mpjUb3jjsthQFYf95Rmz+xvdyp0x9wE6FVv/iE3+Tw==";
    //HMAC end key prefix
    private static final String END_HMAC_K_PREFIX = "mv2VTkJQMAIz2MCPjxtok97EwMPzk75lIPESsjNGJRw8cQJLie6aq0DKU+l9LeTV/51BsTso6gW02TpnvRhHudNd5q1o6KGwo+4Y91IXNxGdARhgZtTMhb0Pg5xsajWmzRZmgQ==";
    //HMAC end key suffix
    private static final String END_HMAC_K_SUFFIX = "lOkQc5m+3vPuafPazLXGigvY0r9Vutkxhh0AahbIXN6rcI3az3Rwdl1DwPdcKzW5egs8Ui6nmItX16ehOrnp2JQFoweymfdmEw4miwfZwtM/ULnB2lwTSt0ugJZNbkfe2+TKOg==";
    //HMAC start value prefix
    private static final String START_HMAC_PREFIX = "wjddGAzMlwZnDAQtZcvPH+mGasRFC0+r0nm/72hSxSMMJiRKq6vubU/6uYjc+Y8FsKp4Xr2BYPPTXYonZ6IjnzqWkOkFcF9kW8IeK01EBv71vno/TUL0eqVavVnaCjF+Z11GgA==";
    //HMAC start value suffix
    private static final String START_HMAC_SUFFIX = "hO+8uGGlxc5RHYjuP556FvPAyjX3S+1wcXa3aKx+yCgEOdpwAOmkfNYETMD8PgFyGakPmL0a5MmNtzkLHdIZgIqc/8R5/vQUcKGxbbgsDgVSfcLAB0SJFd0wNSimlsH+oI3/6w==";
    //HMAC end value prefix
    private static final String END_HMAC_PREFIX = "SejYxv4ESEAot6V4s7dwIappgfyMVw1f9aQIS3TlGWCjQ3KjiPVxnRnJ3XbTaL+/YSdO/FcbyhIrRzm9e6GvnGp7/DLzEe1z5zAp2b0BagNy4fallRbav29byhIby4+M10+M7w==";
    //HMAC end value suffix
    private static final String END_HMAC_SUFFIX = "VmHtEMGSUWnfaqhSr6OsLHTFTjLZu296xHvIDZP7m/zUp7/oMa7VaS89j82kSA/N1x3YB1brxcKxNqfQc+ipBCHlohkKeTCSU0Jdsh3fzJErwlX3dKtognZCaVEdmWJCcmORcg==";


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
     * @param keyLength The size of hash to return (in bytes).
     * @param mode The hashing mode to use. Always "PBKDF2WithHmacSHA256".
     * @return A byte[], containing the password hash.
     * */
    public static byte[] pbkdf2(String password, byte[] salt, int iterations, int keyLength, String mode)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        //Get a char-arr version of the password.
        char[] chars = password.toCharArray();
        //Make a key specification, with the password chars, the salt,
        //the output length*8 for bits, and the number of iterations.
        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, keyLength*8);
        //Make a hashing object.
        SecretKeyFactory skf = SecretKeyFactory.getInstance(mode);
        //And get a hash conforming to the spec.
        byte[] hash = skf.generateSecret(spec).getEncoded();
        //We're done!
        return hash;
    }

    /** Gets the SHA256 HMAC for a key and message.
     * @param key The secret for HMAC to use.
     * @param message The message to hash.
     * @return A byte[], containing the hash.
     * Should have length 32.
     * */
    public static byte[] HMAC_SHA_256(byte[] key, byte[] message) {
        //Let's get our hashing algorithm.
        SHA3Digest algo = new SHA3Digest(256);
        //We make a nice Hmac object.
        HMac hmac = new HMac(algo);
        //And a key to go along with it.
        KeyParameter secretKeySpec = new KeyParameter(key);
        //Put the key in.
        hmac.init(secretKeySpec);
        //Put the message in, with 0 offset, up to the max length.
        hmac.update(message, 0, message.length);
        //Make a holder for our output.
        byte[] hash = new byte[hmac.getMacSize()];
        //And hash our message, output with 0 offset.
        hmac.doFinal(hash, 0);
        //We're done!
        return hash;
    }

    /** Gets a single url-safe b64'd start path.
     * @param precinct The precinct the path should be for.
     * @return The start url, with no leading or trailing '/' chars.
     * */
    public static String getStart_Hmac_b64(int precinct) {
        //Format the key/value pair nicely
        String keyString = START_HMAC_K_PREFIX+"_starting vote_"+START_HMAC_K_SUFFIX;
        String valueString = START_HMAC_PREFIX+String.format("_%d_",precinct)+START_HMAC_SUFFIX;
        //Hash the strings, for some randomness.
        byte[] key = Hashing.sha256().hashBytes(keyString.getBytes()).asBytes();
        byte[] value = Hashing.sha256().hashBytes(valueString.getBytes()).asBytes();
        //And feed them to HMAC.
        byte[] hash = HMAC_SHA_256(key, value);
        //Then, encode that with url-safe base64.
        String url = java.util.Base64.getUrlEncoder().encodeToString(hash);
        //And return. We're done!
        return url;
    }

    /** Gets a single url-safe b64'd end path.
     * @param precinct The precinct the path should be for.
     * @return The end url, with no leading or trailing '/' chars.
     * */
    public static String getEnd_Hmac_b64(int precinct) {
        //Format the key/value pair nicely
        String keyString = END_HMAC_K_PREFIX+"_ending vote_"+END_HMAC_K_SUFFIX;
        String valueString = END_HMAC_PREFIX+String.format("_%d_",precinct)+END_HMAC_SUFFIX;
        //Hash the strings, for some randomness.
        byte[] key = Hashing.sha256().hashBytes(keyString.getBytes()).asBytes();
        byte[] value = Hashing.sha256().hashBytes(valueString.getBytes()).asBytes();
        //And feed them to HMAC.
        byte[] hash = HMAC_SHA_256(key, value);
        //Then, encode that with url-safe base64.
        String url = java.util.Base64.getUrlEncoder().encodeToString(hash);
        //And return. We're done!
        return url;
    }

    /** Gets all the start paths between MIN_PRECINCT
     * and MAX_PRECINCT.
     * @return A List of all the start paths, each
     * having no leading or trailing '/' chars.
     * */
    public static List<String> get_StartURLs() {
        ArrayList<String> urls = new ArrayList<String>();
        for (int i=MIN_PRECINCT;i<=MAX_PRECINCT; i++) {
            urls.add(getStart_Hmac_b64(i));
        }
        return urls;
    }

    /** Gets all the end paths between MIN_PRECINCT
     * and MAX_PRECINCT.
     * @return A List of all the end paths, each
     * having no leading or trailing '/' chars.
     * */
    public static List<String> get_EndURLs() {
        ArrayList<String> urls = new ArrayList<String>();
        for (int i=MIN_PRECINCT;i<=MAX_PRECINCT; i++) {
            urls.add(getEnd_Hmac_b64(i));
        }
        return urls;
    }
}
