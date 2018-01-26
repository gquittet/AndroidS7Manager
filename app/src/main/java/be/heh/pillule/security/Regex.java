package be.heh.pillule.security;

import java.util.regex.Pattern;

/**
 * Created by gquittet on 12/9/17.
 */

public class Regex {

    private static final String DIGIT = "^[0-9]{1,1}$";
    private static final String NAME = "^[A-Z]{1,1}[a-zéèàù]{1,10}([\\s\\-'][A-zéèàù]{1,10}){0,4}$";
    private static final String PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[`~!@#$%^&*()\\-_+=i\\[\\]{}\\\\|;:'\"/?,.<>€©äåé®þüúíóöáßðœøæx©ñµ¡²³¤€¼½¾‘’¥×«»¬¶'¿ç˙])[A-Za-z\\d`~!@#$%^&*()\\-_+=\\[\\]{}\\\\|;:'\"/?,.<>€©äåé®þüúíóöáßðœøæx©ñµ¡²³¤€¼½¾‘’¥×«»¬¶'¿ç˙]{8,255}$";

    private static boolean compare(String regex, String s) {
        if (regex == null) {
            throw new NullPointerException("Please give a regex.");
        }
        if (s == null) {
            throw new NullPointerException("Please give a name.");
        }
        return Pattern.compile(regex).matcher(s).matches();
    }

    public static boolean isDigit(String s) {
        return compare(DIGIT, s);
    }

    public static boolean isName(String s) {
        return compare(NAME, s);
    }

    public static boolean isPassword(String s) {
        return compare(PASSWORD, s);
    }
}
