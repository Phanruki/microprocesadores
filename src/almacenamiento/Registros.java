package almacenamiento;

/**
 * Banco de registros del procesador: 32 registros de 32 bits.
 * El registro $0 (zero) siempre lee 0 y las escrituras se ignoran.
 */
public class Registros {

    public static final int CANTIDAD = 32;
    public static final int ANCHO = 32;

    private static final String[] NOMBRES = {
        "$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
        "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
        "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
        "$t8", "$t9", "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"
    };

    private final int[] registros = new int[CANTIDAD];

    /** Nombre convencional del registro con indice indicado. */
    public static String nombre(int indice) {
        if (indice < 0 || indice >= CANTIDAD) {
            return "$" + indice;
        }
        return NOMBRES[indice];
    }

    public int leer(int indice) {
        validar(indice);
        return indice == 0 ? 0 : registros[indice];
    }

    public void escribir(int indice, int valor) {
        validar(indice);
        if (indice != 0) {
            registros[indice] = valor;
        }
    }

    private void validar(int indice) {
        if (indice < 0 || indice >= CANTIDAD) {
            throw new IndexOutOfBoundsException("Indice de registro invalido: " + indice);
        }
    }

    public void limpiar() {
        for (int i = 0; i < CANTIDAD; i++) {
            registros[i] = 0;
        }
    }

    public int[] obtenerCopia() {
        return registros.clone();
    }

    public String volcar() {
        StringBuilder sb = new StringBuilder("=== REGISTROS (32 bits) ===\n");
        for (int i = 0; i < CANTIDAD; i++) {
            sb.append(String.format("  %2d %-5s = %11d  (0x%08X)%n", i, NOMBRES[i], registros[i], registros[i]));
        }
        return sb.toString();
    }
}
