package memoria;

/**
 * ROM: segmento de texto del simulador.
 *
 * Guarda el programa (instrucciones). Ancho de palabra de 32 bits y
 * 100 celdas, en las direcciones 0..99 del mapa de memoria. Se bloquea
 * tras la carga para impedir escrituras accidentales.
 */
public class ROM extends UnidadAlmacenamiento {

    public static final int ANCHO_PALABRA = 32;
    public static final int CELDAS = 100;

    /** Segmento de texto: la CPU busca las instrucciones aqui. */
    public static final String NOMBRE = "texto";
    public static final int DIRECCION_BASE = 0;

    private boolean bloqueada;

    public ROM() {
        super(NOMBRE, DIRECCION_BASE, ANCHO_PALABRA, CELDAS);
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
