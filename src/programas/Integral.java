package programas;

import instruccion.ProgramaBuilder;

import static instruccion.ProgramaBuilder.T0;
import static instruccion.ProgramaBuilder.T1;
import static instruccion.ProgramaBuilder.T2;
import static instruccion.ProgramaBuilder.T3;
import static instruccion.ProgramaBuilder.T4;
import static instruccion.ProgramaBuilder.ZERO;

/**
 * Integral definida de f(x) = x^2 en [1, 5] por rectangulos de ancho 1:
 * 1^2 + 2^2 + 3^2 + 4^2 = 30.
 */
public final class Integral {

    private Integral() {
    }

    public static ProgramaBuilder construir() {
        ProgramaBuilder p = new ProgramaBuilder();

        int dirA = p.palabra(1);
        int dirB = p.palabra(5);
        int dirMsg1 = p.cadena("Integral de x^2 dx desde x=");
        int dirMsg2 = p.cadena(" hasta x=");
        int dirMsg3 = p.cadena(" = ");
        int dirSalto = p.cadena("\n");

        p.lwAbs(T0, dirA);
        p.lwAbs(T1, dirB);

        Impresion.cadena(p, dirMsg1);
        Impresion.entero(p, T0);
        Impresion.cadena(p, dirMsg2);
        Impresion.entero(p, T1);
        Impresion.cadena(p, dirMsg3);

        p.cargarConstante(T2, 0);    // acumulado
        p.mover(T3, T0);             // x = limite inferior

        p.etiqueta("bucle");
        p.slt(T4, T3, T1);           // 1 si x < limite superior
        p.beq(T4, ZERO, "fin_bucle");
        p.mult(T3, T3);
        p.mflo(T4);                  // x * x
        p.addu(T2, T2, T4);
        p.addiu(T3, T3, 1);
        p.j("bucle");

        p.etiqueta("fin_bucle");
        Impresion.entero(p, T2);
        Impresion.cadena(p, dirSalto);
        Impresion.terminar(p);
        return p;
    }
}
