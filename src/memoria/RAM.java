package memoria;

/**
 * RAM: segmento de datos del simulador.
 *
 * Ancho de palabra de 32 bits y 100 celdas, en las direcciones
 * 100..199 del mapa de memoria. Todas las celdas arrancan en
 * 0x00000000 (que ademas se interpreta como NOP).
 */
public class RAM extends UnidadAlmacenamiento {

    public static final int ANCHO_PALABRA = 32;
    public static final int CELDAS = 100;

    /** Segmento de datos: aqui viven las variables del programa. */
    public static final String NOMBRE = "datos";
    public static final int DIRECCION_BASE = 100;

    public RAM() {
        super(NOMBRE, DIRECCION_BASE, ANCHO_PALABRA, CELDAS);
    }
}
