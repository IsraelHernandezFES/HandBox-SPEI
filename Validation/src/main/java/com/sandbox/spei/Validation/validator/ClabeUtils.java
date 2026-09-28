package com.sandbox.spei.Validation.validator;


public class ClabeUtils {

    private static final int[] PESOS = {3, 7, 1};

    /**
     * Valida si una CLABE de 18 dígitos cumple con el formato y su dígito verificador.
     */
    public static boolean esClabeValida(String clabe) {
        if (clabe == null || !clabe.matches("^[0-9]{18}$")) {
            return false;
        }

        String primeros17 = clabe.substring(0, 17);
        int digitoControlIngresado = Character.getNumericValue(clabe.charAt(17));

        return calcularDigitoVerificador(primeros17) == digitoControlIngresado;
    }

    /**
     * Calcula el dígito verificador usando el algoritmo de doble módulo 10.
     */
    public static int calcularDigitoVerificador(String primeros17) {
        int suma = 0;
        for (int i = 0; i < 17; i++) {
            int digito = primeros17.charAt(i) - '0';
            suma += (digito * PESOS[i % 3]) % 10;
        }
        return (10 - (suma % 10)) % 10;
    }
}