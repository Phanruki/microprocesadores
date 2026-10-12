package programas;

import mips.ProgramaBuilder;

import static mips.ProgramaBuilder.A0;
import static mips.ProgramaBuilder.V0;

/** Secuencias de syscall para imprimir y terminar. */
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

    /** Detiene la ejecucion del procesador. */
    public static void terminar(ProgramaBuilder p) {
        p.cargarConstante(V0, 10);
        p.syscall();
    }
}
