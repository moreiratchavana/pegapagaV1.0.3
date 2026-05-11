package heis.berg.pega_paga.util;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class ReferenceGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String ALPHANUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final SecureRandom secureRandom = new SecureRandom();

    public String accountNumber() {
        return "ACC" + randomDigits(10);
    }

    public String pointOfSaleCode() {
        return "POS" + randomDigits(6);
    }

    public String invoiceNumber() {
        return "INV" + LocalDateTime.now().format(DATE_FORMATTER) + randomDigits(4);
    }

    public String transactionReference() {
        return "TX" + LocalDateTime.now().format(DATE_FORMATTER) + randomDigits(3);
    }

    public String qrToken() {
        StringBuilder builder = new StringBuilder("QR");
        for (int index = 0; index < 18; index++) {
            builder.append(ALPHANUMERIC.charAt(secureRandom.nextInt(ALPHANUMERIC.length())));
        }
        return builder.toString();
    }

    private String randomDigits(int size) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < size; index++) {
            builder.append(secureRandom.nextInt(10));
        }
        return builder.toString();
    }
}
