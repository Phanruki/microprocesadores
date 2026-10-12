package almacenamiento;

/**
 * RAM: ancho de palabra de 32 bits y 100 celdas de almacenamiento.
 */
public class RAM extends UnidadAlmacenamiento {

    public static final int ANCHO_PALABRA = 32;
    public static final int CELDAS = 100;

    public RAM() {
        super(ANCHO_PALABRA, CELDAS);
    }
}
