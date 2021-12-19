package unito.prog3.utils;

public class Security {

    public static String encryptSHA(String data) {
        if (data == null)
            throw new IllegalArgumentException();

        return org.apache.commons.codec.digest.DigestUtils.sha1Hex(data);
    }
}
