package com.christinewang;

import org.postgresql.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

import static com.christinewang.HTMLBase.protected_template;
import static com.christinewang.Application.LOG;


//passphrase: "voting is awesome"
/** A bunch of functions to encrypt the admin panel, securing it.
 * */
public class CryptoLib {
    //This is a b64 of a PBKDF2-hash of the password "voting is awesome". The java implementation
    //of PBKDF2 wasn't working as expected, so I hashed it in python, and copied it here.
    //If you need it changed, see CHANGEPASS.txt.

    //base64(32 byte PBKDF2 hash of "voting is awesome", salted with salt (see below), with 100 iterations.)
    //unbase64'd version is used as encryption key for admin panel.
    private static final String key="pqG9ShMtGLJf2zRHONuB7o8/b5+74G8hx67NtM82jaY=";
    //base64(random 32 bytes)
    private static final String salt="zPojvGlGnA365QF0vRURviCDqWaQyhPfde4jUh+rNFE=";
    //AES encryption mode.
    private static final String defaultMode="AES/CBC/NoPadding";

    /** A function to get the secured admin page.
     * @param upsalt The filename to use for uploading. Should be randomized, so that
     *               an attacker cannot change the precinct names by guessing the filename
     *               and submitting their own POST request.
     * @return The secured page, in html format.
     * */
    public static String get_adminpage(String upsalt, String csvpath) throws Exception {
        //LOG.info(upsalt);
        //Make an empty initialization vector.
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        //And fill it with random bytes. The IV MUST be random.
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        //Get the base64 version of that, to feed to the javascript later.
        String iv_b64 = Base64.encodeBytes(iv);
        //Get the non-base64 version of the key.
        byte[] dec_key = Base64.decode(key);
        //Encrypt the admin page, remembering to replace the upload filename.
        String replaced = HTMLBase.admin
                .replace("REPLACEME",upsalt)
                .replace("REPLACETHIS",csvpath);
        //LOG.info(replaced);
        byte[] encrypted = encrypt(replaced,dec_key,iv,defaultMode);
        //And get the base64 version of the encrypted admin page.
        String encrypted_b64 = java.util.Base64.getEncoder().encodeToString(encrypted);
        //Assemble that into a nice JSON, for injection into the template javascript decryptor.
        String enc_json = String.format("{\"salt\":\"%s\",\"iv\":\"%s\",\"data\":\"%s\"}",salt,iv_b64,encrypted_b64);
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
        //Check how much padding we need to do.
        int numtoadd=16-Math.floorMod(clean.length,16);
        //Allocate a new byte[] to hold that.
        byte[] newclean = new byte[clean.length+numtoadd];
        //Copy in what we have.
        System.arraycopy(clean,0,newclean,0,clean.length);
        //And pad what's left with null bytes.
        for (int i=clean.length;i<newclean.length;i++) {
            //Default byte value should be 0, so this might not be needed.
            //Doesn't hurt to make sure, though.
            newclean[i]=(byte)0;
        }
        //Put the IV in a format that the cipher will be able to use.
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        //Put the key in a format that the cipher will be able to use.
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

        //Make a new cipher, using the mode specified as an input.
        Cipher cipher = Cipher.getInstance(mode);
        //Give the cipher the key and iv, and tell it to encrypt.
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        //Now give it the plaintext (in bytes), and let it encrypt.
        byte[] encrypted = cipher.doFinal(newclean);
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
}
