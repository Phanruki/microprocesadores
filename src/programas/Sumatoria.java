package programas;

import mips.ProgramaBuilder;

import static mips.ProgramaBuilder.T0;
import static mips.ProgramaBuilder.T1;
import static mips.ProgramaBuilder.T2;
import static mips.ProgramaBuilder.T3;
import static mips.ProgramaBuilder.T4;
import static mips.ProgramaBuilder.ZERO;

/**
 * Sumatoria desde un numero inicial a uno final.
 * Con inicial = 3 y final = 10 el resultado es 52.
 */
public final class Sumatoria {

    private Sumatoria() {
    }

    public static ProgramaBuilder construir() {
        ProgramaBuilder p = new ProgramaBuilder();

        int dirInicial = p.palabra(3);
        int dirFinal = p.palabra(10);
        int dirMsg1 = p.cadena("Sumatoria de ");
        int dirMsg2 = p.cadena(" a ");
        int dirMsg3 = p.cadena(" = ");
        int dirSalto = p.cadena("\n");

        p.lwAbs(T0, dirInicial);
        p.lwAbs(T1, dirFinal);

        Impresion.cadena(p, dirMsg1);
        Impresion.entero(p, T0);
        Impresion.cadena(p, dirMsg2);
        Impresion.entero(p, T1);
        Impresion.cadena(p, dirMsg3);

        p.cargarConstante(T2, 0);
        p.mover(T3, T0);

        p.etiqueta("bucle");
        p.slt(T4, T1, T3);           // 1 si final < i
        p.bne(T4, ZERO, "fin_bucle"); // sale cuando i pasa el final
        p.addu(T2, T2, T3);
        p.addiu(T3, T3, 1);
        p.j("bucle");

        p.etiqueta("fin_bucle");
        Impresion.entero(p, T2);
        Impresion.cadena(p, dirSalto);
        Impresion.terminar(p);
        return p;
    }
}
