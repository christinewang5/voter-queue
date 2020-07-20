package com.christinewang;

import org.postgresql.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import static com.christinewang.HTMLBase.protected_template;


//passphrase: "voting is awesome"

public class CryptoLib {
    private static final String key="pqG9ShMtGLJf2zRHONuB7o8/b5+74G8hx67NtM82jaY=";
    private static final String salt="zPojvGlGnA365QF0vRURviCDqWaQyhPfde4jUh+rNFE=";
    private static final String defaultMode="AES/CBC/NoPadding";

    public static String get_adminpage(String upsalt) throws Exception {
        // Generating IV.
        int ivSize = 16;
        byte[] iv = new byte[ivSize];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        String iv_b64 = Base64.encodeBytes(iv);
        byte[] dec_key = Base64.decode(key);
        byte[] encrypted = encrypt(HTMLBase.admin.replace("REPLACEME",upsalt),dec_key,iv,defaultMode);
        String encrypted_b64 = java.util.Base64.getEncoder().encodeToString(encrypted);
        String enc_json = String.format("{\"salt\":\"%s\",\"iv\":\"%s\",\"data\":\"%s\"}",salt,iv_b64,encrypted_b64);
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
    //https://gist.github.com/itarato/abef95871756970a9dad
    public static byte[] encrypt(String plainText, byte[] key, byte[] iv, String mode) throws Exception {
        byte[] clean = plainText.getBytes();
        int numtoadd=16-Math.floorMod(clean.length,16);
        byte[] newclean = new byte[clean.length+numtoadd];
        System.arraycopy(clean,0,newclean,0,clean.length);
        for (int i=clean.length;i<newclean.length;i++) {
            newclean[i]=(byte)0;
        }

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        /*/ Hashing key.
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(key);
        byte[] keyBytes = new byte[32];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);*/
        SecretKeySpec secretKeySpec = new SecretKeySpec(key/*keyBytes*/, "AES");

        // Encrypt.
        Cipher cipher = Cipher.getInstance(mode);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encrypted = cipher.doFinal(newclean);
        return encrypted;
    }

    public static String getRandomString(String optionchars, int length) {
        SecureRandom rand = new SecureRandom();
        StringBuilder retvar = new StringBuilder();
        for (int i=0;i<length;i++) {
            retvar.append(optionchars.charAt((int) (optionchars.length() * rand.nextFloat())));
        }
        return retvar.toString();
    }
}
