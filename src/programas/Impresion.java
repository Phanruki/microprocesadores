package programas;

import instruccion.ProgramaBuilder;

import static instruccion.ProgramaBuilder.A0;
import static instruccion.ProgramaBuilder.V0;

/** Secuencias de syscall para imprimir y la instruccion HALT para terminar. */
public final class Impresion {

    private Impresion() {
    }

    /** Imprime la cadena terminada en nulo ubicada en la direccion dada. */
    public static void cadena(ProgramaBuilder p, int direccion) {
        p.cargarConstante(V0, 4);
        p.cargarConstante(A0, direccion);
        p.syscall();
    }

    /** Imprime el entero guardado en el registro indicado. */
    public static void entero(ProgramaBuilder p, int registro) {
        p.mover(A0, registro);
        p.cargarConstante(V0, 1);
        p.syscall();
    }

    /** Termina el programa: la instruccion HALT le avisa a la CPU. */
    public static void terminar(ProgramaBuilder p) {
        p.halt();
    }
}
