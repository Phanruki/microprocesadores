package almacenamiento;

/**
 * Unidad de almacenamiento generica.
 * Representa una memoria organizada en celdas de ancho de palabra fijo.
 */
public class UnidadAlmacenamiento {

    public static final int ANCHO_PALABRA_DEFECTO = 32;

    protected final int anchoPalabra;
    protected final int cantidadCeldas;
    protected final int[] celdas;

    public UnidadAlmacenamiento(int anchoPalabra, int cantidadCeldas) {
        if (anchoPalabra <= 0 || cantidadCeldas <= 0) {
            throw new IllegalArgumentException("Ancho de palabra y cantidad de celdas deben ser > 0");
        }
        this.anchoPalabra = anchoPalabra;
        this.cantidadCeldas = cantidadCeldas;
        this.celdas = new int[cantidadCeldas];
    }

    public UnidadAlmacenamiento() {
        this(ANCHO_PALABRA_DEFECTO, 100);
    }

    private void validarDireccion(int direccion) {
        if (direccion < 0 || direccion >= cantidadCeldas) {
            throw new IndexOutOfBoundsException(
                    "Direccion fuera de rango: " + direccion + " (0.." + (cantidadCeldas - 1) + ")");
        }
    }

    public int leer(int direccion) {
        validarDireccion(direccion);
        return celdas[direccion];
    }

    public void escribir(int direccion, int valor) {
        validarDireccion(direccion);
        celdas[direccion] = valor;
    }

    public void rellenar(int valor) {
        for (int i = 0; i < cantidadCeldas; i++) {
            celdas[i] = valor;
        }
    }

    public void limpiar() {
        rellenar(0);
    }

    public int getAnchoPalabra() {
        return anchoPalabra;
    }

    public int getCantidadCeldas() {
        return cantidadCeldas;
    }

    /** Devuelve una copia de todas las celdas. */
    public int[] obtenerCeldas() {
        return celdas.clone();
    }

    /** Volcado legible de la memoria (para capturas de pantalla). */
    public String volcar(String titulo) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(titulo)
          .append(" | ancho=").append(anchoPalabra)
          .append(" bits | celdas=").append(cantidadCeldas)
          .append(" ===\n");
        for (int i = 0; i < cantidadCeldas; i++) {
            if (celdas[i] != 0) {
                sb.append(String.format("  [%3d] = %11d  (0x%08X)%n", i, celdas[i], celdas[i]));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ancho=" + anchoPalabra + ", celdas=" + cantidadCeldas + "}";
    }
}
