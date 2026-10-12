package almacenamiento;

/**
 * ROM: ancho de palabra de 32 bits y 100 celdas de almacenamiento.
 * Almacena el programa (instrucciones). Puede bloquearse tras la carga
 * para impedir escrituras accidentales.
 */
public class ROM extends UnidadAlmacenamiento {

    public static final int ANCHO_PALABRA = 32;
    public static final int CELDAS = 100;

    private boolean bloqueada;

    public ROM() {
        super(ANCHO_PALABRA, CELDAS);
        this.bloqueada = false;
    }

    @Override
    public void escribir(int direccion, int valor) {
        if (bloqueada) {
            throw new IllegalStateException("ROM bloqueada: no se puede escribir en la direccion " + direccion);
        }
        super.escribir(direccion, valor);
    }

    /** Carga un arreglo de instrucciones en la ROM (desde la direccion base). */
    public void cargarPrograma(int[] programa, int direccionBase) {
        boolean estadoPrevio = bloqueada;
        bloqueada = false;
        try {
            if (direccionBase + programa.length > cantidadCeldas) {
                throw new IllegalArgumentException("El programa excede la capacidad de la ROM");
            }
            for (int i = 0; i < programa.length; i++) {
                super.escribir(direccionBase + i, programa[i]);
            }
        } finally {
            bloqueada = estadoPrevio;
        }
    }

    public void bloquear() {
        bloqueada = true;
    }

    public void desbloquear() {
        bloqueada = false;
    }

    public boolean isBloqueada() {
        return bloqueada;
    }
}
