package be.heh.pillule.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by gquittet on 12/8/17.
 */

public class Encrypt {

    private static String salt1 = "fjsdkhfkjKJL:LJK&843749837489KJJJK]][][r84r8(*)fudkjfdjfLJFDK";
    private static String salt2 = "LTRUEYURrkldjfjk78&78789&*(&)*&&(*FLFJD?DFJFdkfjdyf87r38rhdsf";
    private static String salt3 = "&(*)&*(*(&DFhdfdjskfadjLGKK%RYFCVBV BVNBB&&*&&(YBN GY&**fkjds";

    public static String hash(String text) {
        String hashString;
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        hashString = salt1 + new String(messageDigest.digest(text.getBytes(StandardCharsets.UTF_8)));
        try {
            messageDigest = MessageDigest.getInstance("SHA-384");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        hashString += salt2 + new String(messageDigest.digest(hashString.getBytes(StandardCharsets.UTF_8))) + salt3;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        hashString = new String(messageDigest.digest(hashString.getBytes(StandardCharsets.UTF_8)));
        return hashString;
    }
}
