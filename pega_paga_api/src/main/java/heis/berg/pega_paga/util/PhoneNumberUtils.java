package heis.berg.pega_paga.util;

public final class PhoneNumberUtils {

    private PhoneNumberUtils() {
    }

    public static String normalize(String phoneNumber) {
        return phoneNumber == null ? null : phoneNumber.replaceAll("\\s+", "").trim();
    }
}
