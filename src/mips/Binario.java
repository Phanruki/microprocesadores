package mips;

import java.util.ArrayList;
import java.util.List;

/**
 * Conversion entre enteros de 32 bits y su representacion binaria,
 * mas el analisis del texto escrito por el usuario en la ventana.
 */
public final class Binario {

    private Binario() {
    }

    /** Devuelve la cadena binaria de 32 bits (con ceros a la izquierda). */
    public static String a32(int valor) {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 31; i >= 0; i--) {
            sb.append((valor >>> i) & 1);
        }
        return sb.toString();
    }

    /**
     * Convierte el texto de la ventana en instrucciones de 32 bits.
     *
     * Se aceptan palabras binarias de hasta 32 caracteres separadas por
     * espacios, saltos de linea, comas o punto y coma; tambien hexadecimal
     * con prefijo 0x. Todo lo que siga a # o // se ignora (comentarios).
     *
     * @throws IllegalArgumentException si alguna palabra no es valida
     */
    public static int[] parsear(String texto) {
        if (texto == null) {
            return new int[0];
        }
        String limpio = texto.replaceAll("(?m)#.*$", "").replaceAll("(?m)//.*$", "").trim();
        if (limpio.isEmpty()) {
            return new int[0];
        }
        String[] tokens = limpio.split("[\\s,;|()\\[\\]]+");
        List<Integer> palabras = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isEmpty()) {
                palabras.add(convertir(token));
            }
        }
        int[] resultado = new int[palabras.size()];
        for (int i = 0; i < resultado.length; i++) {
            resultado[i] = palabras.get(i);
        }
        return resultado;
    }

    private static int convertir(String token) {
        String t = token.replace("_", "");
        if (t.startsWith("0b") || t.startsWith("0B")) {
            t = t.substring(2);
        }
        if (t.startsWith("0x") || t.startsWith("0X")) {
            String hex = t.substring(2);
            if (hex.isEmpty() || hex.length() > 8 || !hex.matches("[0-9a-fA-F]+")) {
                throw new IllegalArgumentException("Valor hexadecimal invalido: " + token);
            }
            return (int) Long.parseLong(hex, 16);
        }
        if (t.length() > 32) {
            throw new IllegalArgumentException(
                    "La palabra " + token + " tiene mas de 32 bits (" + t.length() + ")");
        }
        if (t.isEmpty() || !t.matches("[01]+")) {
            throw new IllegalArgumentException(
                    "No es una instruccion binaria valida: " + token
                    + " (se esperan solo 0 y 1, hasta 32 bits)");
        }
        int valor = 0;
        for (int i = 0; i < t.length(); i++) {
            valor = (valor << 1) | (t.charAt(i) - '0');
        }
        return valor;
    }
}
