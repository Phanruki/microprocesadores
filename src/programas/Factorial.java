package programas;

import instruccion.ProgramaBuilder;

import static instruccion.ProgramaBuilder.T0;
import static instruccion.ProgramaBuilder.T1;
import static instruccion.ProgramaBuilder.T2;
import static instruccion.ProgramaBuilder.T3;
import static instruccion.ProgramaBuilder.ZERO;

/**
 * Factorial de un numero.
 * Con n = 7 el resultado es 5040.
 */
public final class Factorial {

    private Factorial() {
    }

    public static ProgramaBuilder construir() {
        ProgramaBuilder p = new ProgramaBuilder();

        int dirN = p.palabra(7);
        int dirMsg1 = p.cadena("Factorial de ");
        int dirMsg2 = p.cadena(" = ");
        int dirSalto = p.cadena("\n");

        p.lwAbs(T0, dirN);

        Impresion.cadena(p, dirMsg1);
        Impresion.entero(p, T0);
        Impresion.cadena(p, dirMsg2);

        p.cargarConstante(T1, 1);    // acumulado, arranca en 1
        p.cargarConstante(T2, 1);    // contador i

        p.etiqueta("bucle");
        p.slt(T3, T0, T2);            // 1 si n < i
        p.bne(T3, ZERO, "fin_bucle"); // sale cuando i pasa n
        p.mult(T1, T2);
        p.mflo(T1);                   // acumulado *= i
        p.addiu(T2, T2, 1);
        p.j("bucle");

        p.etiqueta("fin_bucle");
        Impresion.entero(p, T1);
        Impresion.cadena(p, dirSalto);
        Impresion.terminar(p);
        return p;
    }
}
