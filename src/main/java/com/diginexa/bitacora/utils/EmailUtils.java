package com.diginexa.bitacora.utils;

import org.springframework.stereotype.Component;

@Component
public class EmailUtils {

    public static String extractUsernameFromEmail(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return correo;
        }

        if (correo.contains("@")) {
            return correo.substring(0, correo.indexOf("@"));
        }

        return correo;
    }

    public static boolean isValidEmail(String correo) {
        return correo != null &&
                correo.contains("@") &&
                correo.indexOf("@") > 0 &&
                correo.indexOf("@") < correo.length() - 1;
    }
}