package pruebas;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Mini-framework de pruebas sin dependencias externas.
 * Cuenta pruebas pasadas/fallidas y permite resumen final.
 */
public final class MiniTest {

    private static int pasadas = 0;
    private static int fallidas = 0;
    private static final StringBuilder detalleFallas = new StringBuilder();

    private MiniTest() {
    }

    public static void assertTrue(String nombre, boolean condicion) {
        if (condicion) {
            pasadas++;
            System.out.println("  [PASS] " + nombre);
        } else {
            fallidas++;
            detalleFallas.append("  [FAIL] ").append(nombre).append('\n');
            System.out.println("  [FAIL] " + nombre);
        }
    }

    public static void assertFalse(String nombre, boolean condicion) {
        assertTrue(nombre, !condicion);
    }

    public static void assertEquals(String nombre, int esperado, int obtenido) {
        if (esperado == obtenido) {
            pasadas++;
            System.out.println("  [PASS] " + nombre);
        } else {
            fallidas++;
            detalleFallas.append("  [FAIL] ").append(nombre)
                    .append(" | esperado=").append(esperado)
                    .append(" obtenido=").append(obtenido).append('\n');
            System.out.println("  [FAIL] " + nombre + " | esperado=" + esperado + " obtenido=" + obtenido);
        }
    }

    public static void assertEquals(String nombre, String esperado, String obtenido) {
        if (esperado == null ? obtenido == null : esperado.equals(obtenido)) {
            pasadas++;
            System.out.println("  [PASS] " + nombre);
        } else {
            fallidas++;
            detalleFallas.append("  [FAIL] ").append(nombre)
                    .append(" | esperado=\"").append(esperado)
                    .append("\" obtenido=\"").append(obtenido).append("\"\n");
            System.out.println("  [FAIL] " + nombre + " | esperado=\"" + esperado + "\" obtenido=\"" + obtenido + "\"");
        }
    }

    public static void assertThrows(String nombre, Runnable accion, Class<? extends Throwable> tipo) {
        try {
            accion.run();
            fallidas++;
            detalleFallas.append("  [FAIL] ").append(nombre)
                    .append(" | se esperaba ").append(tipo.getSimpleName())
                    .append(" pero no se lanzo\n");
            System.out.println("  [FAIL] " + nombre + " | se esperaba " + tipo.getSimpleName());
        } catch (Throwable t) {
            if (tipo.isInstance(t)) {
                pasadas++;
                System.out.println("  [PASS] " + nombre);
            } else {
                fallidas++;
                detalleFallas.append("  [FAIL] ").append(nombre)
                        .append(" | excepcion incorrecta: ").append(t).append('\n');
                System.out.println("  [FAIL] " + nombre + " | excepcion incorrecta: " + t);
            }
        }
    }

    /** Captura la salida de System.out mientras ejecuta la accion. */
    public static String capturarSalida(Runnable accion) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true));
        try {
            accion.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }

    public static void reiniciar() {
        pasadas = 0;
        fallidas = 0;
        detalleFallas.setLength(0);
    }

    public static int resumen(String titulo) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println(" " + titulo);
        System.out.println(" Pasadas: " + pasadas + "   Fallidas: " + fallidas);
        if (fallidas > 0) {
            System.out.println(" Detalle de fallas:");
            System.out.print(detalleFallas);
        }
        System.out.println("==================================================");
        return fallidas;
    }

    public static int getFallidas() {
        return fallidas;
    }

    public static int getPasadas() {
        return pasadas;
    }
}
