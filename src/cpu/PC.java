package cpu;

/**
 * Contador de programa (PC) de 32 bits.
 * Contiene la direccion de la siguiente instruccion a ejecutar.
 * Durante la busqueda de instrucciones el PC se incrementa para apuntar
 * a la siguiente instruccion.
 */
public class PC {

    public static final int ANCHO = 32;

    private int valor;

    public PC() {
        this.valor = 0;
    }

    public PC(int valorInicial) {
        this.valor = valorInicial;
    }

    public int get() {
        return valor;
    }

    public void set(int nuevaDireccion) {
        this.valor = nuevaDireccion;
    }

    /** Incrementa el PC en una palabra (1 posicion de instruccion). */
    public void incrementar() {
        this.valor = this.valor + 1;
    }

    /** El PC arranca siempre en 0 al encender o reiniciar la CPU. */
    public void reiniciar() {
        this.valor = 0;
    }

    @Override
    public String toString() {
        return String.format("PC = %d (0x%08X)", valor, valor);
    }
}
