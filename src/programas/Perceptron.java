package programas;

import mips.ProgramaBuilder;

import static mips.ProgramaBuilder.S0;
import static mips.ProgramaBuilder.S1;
import static mips.ProgramaBuilder.T0;
import static mips.ProgramaBuilder.T1;
import static mips.ProgramaBuilder.T2;
import static mips.ProgramaBuilder.T3;
import static mips.ProgramaBuilder.T4;
import static mips.ProgramaBuilder.T5;
import static mips.ProgramaBuilder.T7;
import static mips.ProgramaBuilder.T8;
import static mips.ProgramaBuilder.T9;
import static mips.ProgramaBuilder.ZERO;

/**
 * Perceptron de 3 entradas con pesos y bias fijos:
 * z = 2*1 + 2*1 + (-3)*0 + (-1) = 3, y = 1 porque z >= 0.
 */
public final class Perceptron {

    private Perceptron() {
    }

    public static ProgramaBuilder construir() {
        ProgramaBuilder p = new ProgramaBuilder();

        int dirX = p.palabra(1);
        p.palabra(1);
        p.palabra(0);
        int dirW = p.palabra(2);
        p.palabra(2);
        p.palabra(-3);
        int dirBias = p.palabra(-1);
        int dirN = p.palabra(3);
        int dirMsgZ = p.cadena("z = ");
        int dirMsgY = p.cadena("  y = ");
        int dirSalto = p.cadena("\n");

        p.lwAbs(T0, dirN);
        p.cargarConstante(T1, 0);    // indice
        p.cargarConstante(T2, 0);    // acumulado z
        p.cargarConstante(S0, dirX);
        p.cargarConstante(S1, dirW);

        p.etiqueta("bucle");
        p.slt(T3, T1, T0);
        p.beq(T3, ZERO, "fin_bucle");
        p.addu(T4, S0, T1);
        p.lw(T4, T4, 0);             // x[i]
        p.addu(T5, S1, T1);
        p.lw(T5, T5, 0);             // w[i]
        p.mult(T5, T4);
        p.mflo(T7);
        p.addu(T2, T2, T7);
        p.addiu(T1, T1, 1);
        p.j("bucle");

        p.etiqueta("fin_bucle");
        p.lwAbs(T8, dirBias);
        p.addu(T2, T2, T8);

        Impresion.cadena(p, dirMsgZ);
        Impresion.entero(p, T2);

        p.cargarConstante(T9, 0);    // y = 0 por defecto
        p.slt(T3, T2, ZERO);         // 1 si z < 0
        p.bne(T3, ZERO, "imprimir"); // z negativo: y se queda en 0
        p.cargarConstante(T9, 1);    // z >= 0

        p.etiqueta("imprimir");
        Impresion.cadena(p, dirMsgY);
        Impresion.entero(p, T9);
        Impresion.cadena(p, dirSalto);
        Impresion.terminar(p);
        return p;
    }
}
