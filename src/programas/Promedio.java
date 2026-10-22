package programas;

import instruccion.ProgramaBuilder;

import static instruccion.ProgramaBuilder.T0;
import static instruccion.ProgramaBuilder.T1;
import static instruccion.ProgramaBuilder.T2;
import static instruccion.ProgramaBuilder.T4;
import static instruccion.ProgramaBuilder.T5;
import static instruccion.ProgramaBuilder.T6;
import static instruccion.ProgramaBuilder.T7;
import static instruccion.ProgramaBuilder.ZERO;

/**
 * Promedio entero de un vector fijo.
 * {4, 8, 15, 16, 23} -> 66 / 5 = 13.
 */
public final class Promedio {

    private Promedio() {
    }

    public static ProgramaBuilder construir() {
        ProgramaBuilder p = new ProgramaBuilder();

        int dirVec = p.palabra(4);
        p.palabra(8);
        p.palabra(15);
        p.palabra(16);
        p.palabra(23);
        int dirN = p.palabra(5);
        int dirMsgVec = p.cadena("Vector: ");
        int dirMsgProm = p.cadena("Promedio = ");
        int dirEspacio = p.cadena(" ");
        int dirSalto = p.cadena("\n");

        p.lwAbs(T0, dirN);           // cantidad de elementos
        p.cargarConstante(T1, 0);    // indice
        p.cargarConstante(T2, 0);    // suma
        p.cargarConstante(T4, dirVec);

        Impresion.cadena(p, dirMsgVec);

        p.etiqueta("imprimir");
        p.slt(T5, T1, T0);
        p.beq(T5, ZERO, "fin_imprimir");
        p.addu(T6, T4, T1);
        p.lw(T6, T6, 0);
        Impresion.entero(p, T6);
        Impresion.cadena(p, dirEspacio);
        p.addiu(T1, T1, 1);
        p.j("imprimir");

        p.etiqueta("fin_imprimir");
        Impresion.cadena(p, dirSalto);

        p.cargarConstante(T1, 0);
        p.cargarConstante(T2, 0);

        p.etiqueta("sumar");
        p.slt(T5, T1, T0);
        p.beq(T5, ZERO, "fin_suma");
        p.addu(T6, T4, T1);
        p.lw(T6, T6, 0);
        p.addu(T2, T2, T6);
        p.addiu(T1, T1, 1);
        p.j("sumar");

        p.etiqueta("fin_suma");
        p.div(T2, T0);
        p.mflo(T7);                  // cociente entero

        Impresion.cadena(p, dirMsgProm);
        Impresion.entero(p, T7);
        Impresion.cadena(p, dirSalto);
        Impresion.terminar(p);
        return p;
    }
}
