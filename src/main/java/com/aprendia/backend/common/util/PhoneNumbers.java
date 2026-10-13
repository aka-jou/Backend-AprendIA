package com.aprendia.backend.common.util;

/** Normalización de teléfonos: se almacenan siempre como 10 dígitos sin separadores. */
public final class PhoneNumbers {

    private PhoneNumbers() {
    }

    /** {@code "961 120 4821"} -> {@code "9611204821"}; null o vacío -> null. */
    public static String normalize(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        return phone.replaceAll("\\D", "");
    }
}
