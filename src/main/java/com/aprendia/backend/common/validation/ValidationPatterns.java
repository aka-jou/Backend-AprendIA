package com.aprendia.backend.common.validation;

/** Expresiones regulares de las tablas de campos de la Especificación v2.0. */
public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    /**
     * CURP oficial de 18 caracteres: 4 letras, fecha AAMMDD, sexo (H/M/X), 2 letras de entidad,
     * 3 consonantes internas, homoclave (letra o dígito) y dígito verificador. Acepta minúsculas;
     * el servicio la normaliza a mayúsculas.
     */
    public static final String CURP = "^[A-Za-z]{4}\\d{6}[HMXhmx][A-Za-z]{5}[A-Za-z0-9]\\d$";

    /** Teléfono a 10 dígitos, sin espacios ni guiones. */
    public static final String PHONE_10 = "^\\d{10}$";

    /** Código postal a 5 dígitos. */
    public static final String ZIP_CODE = "^\\d{5}$";

    /**
     * Usuario de mínimo 8 caracteres. La spec dice "alfanuméricos" pero sus propios ejemplos usan
     * punto (alicia.morales), por eso se aceptan además '.', '_' y '-'.
     */
    public static final String USERNAME = "^[A-Za-z0-9._-]{8,50}$";

    /** Contraseña de mínimo 8 caracteres con al menos una mayúscula, un número y un símbolo. */
    public static final String PASSWORD = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,100}$";

    /** Género del módulo de usuarios: H (Hombre), M (Mujer), X. */
    public static final String GENDER_STAFF = "^[HMXhmx]$";

    /** Género del módulo de estudiantes: M (Masculino), F (Femenino). */
    public static final String GENDER_STUDENT = "^[MFmf]$";

    /** Estado de un usuario tal como lo muestra el listado. */
    public static final String USER_STATUS = "^(Activo|Inactivo)$";
}
