package com.aprendia.backend.common.security;

/**
 * Convención de nombres de rol.
 *
 * En base de datos y en Spring Security los roles llevan el prefijo {@code ROLE_}
 * (requisito de {@code hasRole(...)}). La API los expone sin prefijo, como en la
 * Especificación v2.0: {@code ROLE_ADMINISTRADOR} se lee y se escribe como {@code "ADMINISTRADOR"}.
 */
public final class RoleNames {

    public static final String PREFIX = "ROLE_";

    public static final String ADMINISTRADOR = "ROLE_ADMINISTRADOR";
    public static final String STUDENT = "ROLE_STUDENT";

    private RoleNames() {
    }

    /** {@code ROLE_ADMINISTRADOR} -> {@code ADMINISTRADOR}. */
    public static String toApi(String authority) {
        if (authority == null) {
            return null;
        }
        return authority.startsWith(PREFIX) ? authority.substring(PREFIX.length()) : authority;
    }

    /** {@code administrador} / {@code ADMINISTRADOR} / {@code ROLE_ADMINISTRADOR} -> {@code ROLE_ADMINISTRADOR}. */
    public static String toAuthority(String apiName) {
        if (apiName == null) {
            return null;
        }
        String upper = apiName.trim().toUpperCase(java.util.Locale.ROOT);
        return upper.startsWith(PREFIX) ? upper : PREFIX + upper;
    }
}
