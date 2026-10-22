package memoria;

/**
 * Unidad de almacenamiento: un segmento de memoria.
 *
 * Cada unidad tiene un nombre, una direccion base dentro del mapa global
 * de memoria y una cantidad de celdas de ancho de palabra fijo. Las
 * celdas se inicializan en 0x00000000.
 */
public class UnidadAlmacenamiento {

    public static final int ANCHO_PALABRA_DEFECTO = 32;

    protected final String nombre;
    protected final int direccionBase;
    protected final int anchoPalabra;
    protected final int cantidadCeldas;
    protected final int[] celdas;

    public UnidadAlmacenamiento(String nombre, int direccionBase, int anchoPalabra, int cantidadCeldas) {
        if (anchoPalabra <= 0 || cantidadCeldas <= 0) {
            throw new IllegalArgumentException("Ancho de palabra y cantidad de celdas deben ser > 0");
        }
        if (direccionBase < 0) {
            throw new IllegalArgumentException("La direccion base no puede ser negativa");
        }
        this.nombre = nombre;
        this.direccionBase = direccionBase;
        this.anchoPalabra = anchoPalabra;
        this.cantidadCeldas = cantidadCeldas;
        this.celdas = new int[cantidadCeldas]; // todas arrancan en 0x00000000
    }

    // ------------------------------------------------------------------
    // Segmento: nombre y direccion
    // ------------------------------------------------------------------

    public String getNombre() {
        return nombre;
    }

    /** Primera direccion global del segmento. */
    public int getDireccionBase() {
        return direccionBase;
    }

    /** Ultima direccion global del segmento. */
    public int getDireccionFinal() {
        return direccionBase + cantidadCeldas - 1;
    }

    /** Direccion global de una celda local. */
    public int direccionAbsoluta(int direccionLocal) {
        return direccionBase + direccionLocal;
    }

    /** true si la direccion global pertenece a este segmento. */
    public boolean contieneAbsoluta(int direccion) {
        return direccion >= direccionBase && direccion <= getDireccionFinal();
    }

    /** Convierte una direccion global en el indice de celda local. */
    public int aLocal(int direccion) {
        if (!contieneAbsoluta(direccion)) {
            throw new IndexOutOfBoundsException(
                    "La direccion " + direccion + " no pertenece al segmento " + nombre
                    + " (" + direccionBase + ".." + getDireccionFinal() + ")");
        }
        return direccion - direccionBase;
    }

    // ------------------------------------------------------------------
    // Lectura y escritura (direcciones locales del segmento)
    // ------------------------------------------------------------------

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

    /** Volcado legible del segmento con sus direcciones (para capturas). */
    public String volcar(String titulo) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(titulo)
          .append(" | segmento=").append(nombre)
          .append(" | direcciones=").append(direccionBase).append("..").append(getDireccionFinal())
          .append(" | ancho=").append(anchoPalabra)
          .append(" bits | celdas=").append(cantidadCeldas)
          .append(" ===\n");
        for (int i = 0; i < cantidadCeldas; i++) {
            sb.append(String.format("  [%3d] dir %3d (0x%08X) = %11d  (0x%08X)%n",
                    i, direccionAbsoluta(i), direccionAbsoluta(i), celdas[i], celdas[i]));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{segmento=" + nombre
                + ", base=" + direccionBase + ", ancho=" + anchoPalabra
                + ", celdas=" + cantidadCeldas + "}";
    }
}
