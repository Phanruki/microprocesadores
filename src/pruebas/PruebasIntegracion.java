package pruebas;

import mips.MemoriaSistema;
import mips.ProcesadorMIPS;
import mips.ProgramaBuilder;
import mips.ProgramaBuilder.Programa;

import static mips.ProgramaBuilder.A0;
import static mips.ProgramaBuilder.RA;
import static mips.ProgramaBuilder.T0;
import static mips.ProgramaBuilder.T1;
import static mips.ProgramaBuilder.T2;
import static mips.ProgramaBuilder.T3;
import static mips.ProgramaBuilder.T4;
import static mips.ProgramaBuilder.T9;
import static mips.ProgramaBuilder.V0;
import static mips.ProgramaBuilder.ZERO;

/**
 * Pruebas de integracion del simulador del procesador MIPS.
 * Verifican el flujo completo: programa -> ROM -> CPU -> salida.
 * Ejecucion: java -cp out pruebas.PruebasIntegracion
 */
public class PruebasIntegracion {

    public static void main(String[] args) {
        System.out.println("=== PRUEBAS DE INTEGRACION - Simulador MIPS ===");
        System.out.println();

        probarCargaEnRom();
        probarArismeticaBasica();
        probarDesbordamientoConSigno();
        probarRamasCondicionales();
        probarBucleYAcumulador();
        probarMemoriaDatos();
        probarSyscallImpresion();
        probarSubrutinaJalJr();
        probarMultiplicacionYDivision();

        int fallidas = MiniTest.resumen("RESUMEN PRUEBAS DE INTEGRACION");
        System.exit(fallidas == 0 ? 0 : 1);
    }

    private static ProcesadorMIPS cargar(ProgramaBuilder pb) {
        Programa prog = pb.generar();
        ProcesadorMIPS cpu = new ProcesadorMIPS();
        cpu.cargarPrograma(prog.codigo);
        for (int i = 0; i < prog.datos.length; i++) {
            cpu.getMemoria().escribir(MemoriaSistema.BASE_RAM + i, prog.datos[i]);
        }
        return cpu;
    }

    private static ProcesadorMIPS ejecutar(ProgramaBuilder pb) {
        ProcesadorMIPS cpu = cargar(pb);
        cpu.ejecutar();
        return cpu;
    }

    private static ProcesadorMIPS ejecutarCapturando(ProgramaBuilder pb) {
        ProcesadorMIPS cpu = cargar(pb);
        cpu.capturarSalida();
        try {
            cpu.ejecutar();
        } finally {
            cpu.restaurarSalida();
        }
        return cpu;
    }

    /** Termina el programa con syscall exit ($v0 = 10). */
    private static void fin(ProgramaBuilder p) {
        p.cargarConstante(V0, 10);
        p.syscall();
    }

    private static void probarCargaEnRom() {
        System.out.println("-- Integracion: carga en ROM --");
        ProgramaBuilder pb = new ProgramaBuilder();
        pb.addiu(T0, ZERO, 1);
        pb.addiu(T1, ZERO, 2);
        pb.addu(T2, T0, T1);
        fin(pb);
        Programa prog = pb.generar();
        ProcesadorMIPS cpu = new ProcesadorMIPS();
        cpu.cargarPrograma(prog.codigo);

        MiniTest.assertEquals("ROM tiene 5 instrucciones", 5, cpu.longitudPrograma());
        MiniTest.assertTrue("ROM bloqueada despues de cargar", cpu.getRom().isBloqueada());
        MiniTest.assertEquals("PC inicia en 0", 0, cpu.getPc().get());
        MiniTest.assertEquals("instruccion 0 en ROM == generada",
                prog.codigo[0], cpu.getRom().leer(0));
    }

    private static void probarArismeticaBasica() {
        System.out.println("-- Integracion: aritmetica --");
        ProgramaBuilder p = new ProgramaBuilder();
        p.addiu(T0, ZERO, 10);
        p.addiu(T1, ZERO, 32);
        p.addu(T2, T0, T1);
        p.subu(T3, T1, T0);
        fin(p);
        ProcesadorMIPS cpu = ejecutar(p);
        MiniTest.assertEquals("$t0 = 10", 10, cpu.getRegistros().leer(8));
        MiniTest.assertEquals("$t1 = 32", 32, cpu.getRegistros().leer(9));
        MiniTest.assertEquals("$t2 = 10+32 = 42", 42, cpu.getRegistros().leer(10));
        MiniTest.assertEquals("$t3 = 32-10 = 22", 22, cpu.getRegistros().leer(11));
        MiniTest.assertTrue("programa detenido por syscall exit", cpu.isDetenido());

        ProgramaBuilder p2 = new ProgramaBuilder();
        p2.cargarConstante(T0, 0xFF);
        p2.andi(T1, T0, 0x0F);
        p2.ori(T2, ZERO, 0xF0);
        p2.xor(T3, T1, T2);
        fin(p2);
        ProcesadorMIPS cpu2 = ejecutar(p2);
        MiniTest.assertEquals("andi 0xFF & 0x0F = 0x0F", 0x0F, cpu2.getRegistros().leer(9));
        MiniTest.assertEquals("ori", 0xF0, cpu2.getRegistros().leer(10));
        MiniTest.assertEquals("xor 0x0F ^ 0xF0 = 0xFF", 0xFF, cpu2.getRegistros().leer(11));
    }

    private static void probarDesbordamientoConSigno() {
        System.out.println("-- Integracion: desbordamiento --");
        ProgramaBuilder p = new ProgramaBuilder();
        p.cargarConstante(T0, Integer.MAX_VALUE);
        p.addi(T1, T0, 1);
        fin(p);
        MiniTest.assertThrows("addi con desbordamiento detiene la ejecucion",
                () -> ejecutar(p), ArithmeticException.class);

        ProgramaBuilder p2 = new ProgramaBuilder();
        p2.cargarConstante(T0, Integer.MAX_VALUE);
        p2.addiu(T1, T0, 1);
        fin(p2);
        ProcesadorMIPS cpu = ejecutar(p2);
        MiniTest.assertEquals("addiu MAX+1 envuelve sin excepcion",
                Integer.MIN_VALUE, cpu.getRegistros().leer(9));
    }

    private static void probarRamasCondicionales() {
        System.out.println("-- Integracion: ramas --");
        ProgramaBuilder p = new ProgramaBuilder();
        p.addiu(T0, ZERO, 5);
        p.addiu(T1, ZERO, 3);
        p.slt(T2, T1, T0);
        p.addiu(T3, ZERO, 0);
        p.beq(T2, ZERO, "salto");
        p.addiu(T3, ZERO, 99);
        p.etiqueta("salto");
        fin(p);
        ProcesadorMIPS cpu = ejecutar(p);
        MiniTest.assertEquals("slt 3<5 = 1", 1, cpu.getRegistros().leer(10));
        MiniTest.assertEquals("beq no tomado: $t3 = 99", 99, cpu.getRegistros().leer(11));

        ProgramaBuilder p2 = new ProgramaBuilder();
        p2.addiu(T0, ZERO, 1);
        p2.addiu(T3, ZERO, 0);
        p2.bne(T0, ZERO, "ir");
        p2.addiu(T3, ZERO, 111);
        p2.j("fin");
        p2.etiqueta("ir");
        p2.addiu(T3, ZERO, 55);
        p2.etiqueta("fin");
        fin(p2);
        ProcesadorMIPS cpu2 = ejecutar(p2);
        MiniTest.assertEquals("bne tomado: $t3 = 55", 55, cpu2.getRegistros().leer(11));
    }

    private static void probarBucleYAcumulador() {
        System.out.println("-- Integracion: bucle --");
        ProgramaBuilder p = new ProgramaBuilder();
        p.cargarConstante(T0, 5);
        p.cargarConstante(T1, 0);
        p.cargarConstante(T2, 1);
        p.etiqueta("bucle");
        p.slt(T3, T0, T2);
        p.bne(T3, ZERO, "fin");
        p.addu(T1, T1, T2);
        p.addiu(T2, T2, 1);
        p.j("bucle");
        p.etiqueta("fin");
        fin(p);
        ProcesadorMIPS cpu = ejecutar(p);
        MiniTest.assertEquals("suma 1..5 = 15", 15, cpu.getRegistros().leer(9));
        MiniTest.assertTrue("ciclos ejecutados > 10", cpu.getCiclos() > 10);
    }

    private static void probarMemoriaDatos() {
        System.out.println("-- Integracion: memoria de datos --");
        ProgramaBuilder p = new ProgramaBuilder();
        int dirVar = p.palabra(0);
        int dirVal = p.palabra(123);
        p.lwAbs(T0, dirVal);
        p.swAbs(T0, dirVar);
        p.lwAbs(T1, dirVar);
        fin(p);
        ProcesadorMIPS cpu = ejecutar(p);
        MiniTest.assertEquals("lw lee val = 123", 123, cpu.getRegistros().leer(8));
        MiniTest.assertEquals("sw escribe y lw relee = 123", 123, cpu.getRegistros().leer(9));
        MiniTest.assertEquals("RAM[var] = 123", 123, cpu.getMemoria().leer(dirVar));

        ProgramaBuilder p2 = new ProgramaBuilder();
        int dirArr = p2.palabra(10);
        p2.palabra(20);
        p2.palabra(30);
        p2.cargarConstante(T0, dirArr);
        p2.lw(T1, T0, 0);
        p2.lw(T2, T0, 1);
        p2.lw(T3, T0, 2);
        p2.addu(T4, T1, T2);
        p2.addu(T4, T4, T3);
        fin(p2);
        ProcesadorMIPS cpu2 = ejecutar(p2);
        MiniTest.assertEquals("arr[0] = 10", 10, cpu2.getRegistros().leer(9));
        MiniTest.assertEquals("arr[1] = 20", 20, cpu2.getRegistros().leer(10));
        MiniTest.assertEquals("arr[2] = 30", 30, cpu2.getRegistros().leer(11));
        MiniTest.assertEquals("suma arr = 60", 60, cpu2.getRegistros().leer(12));
    }

    private static void probarSyscallImpresion() {
        System.out.println("-- Integracion: syscall --");
        ProgramaBuilder p = new ProgramaBuilder();
        int dirHola = p.cadena("Hola MIPS");
        p.cargarConstante(V0, 4);
        p.cargarConstante(A0, dirHola);
        p.syscall();
        p.cargarConstante(V0, 1);
        p.cargarConstante(A0, 42);
        p.syscall();
        p.cargarConstante(V0, 10);
        p.syscall();
        ProcesadorMIPS cpu = ejecutarCapturando(p);
        MiniTest.assertEquals("salida de syscall = \"Hola MIPS42\"", "Hola MIPS42",
                cpu.obtenerConsola());
        MiniTest.assertTrue("exit detiene la CPU", cpu.isDetenido());
    }

    private static void probarSubrutinaJalJr() {
        System.out.println("-- Integracion: subrutina --");
        ProgramaBuilder p = new ProgramaBuilder();
        p.jal("doble");
        p.addiu(T9, T0, 0);
        p.j("fin");
        p.etiqueta("doble");
        p.addiu(T0, ZERO, 10);
        p.addu(T0, T0, T0);
        p.jr(RA);
        p.etiqueta("fin");
        fin(p);
        ProcesadorMIPS cpu = ejecutar(p);
        MiniTest.assertEquals("subrutina doble(10) = 20", 20, cpu.getRegistros().leer(8));
        MiniTest.assertEquals("$t9 = 20 tras retorno", 20, cpu.getRegistros().leer(25));
    }

    private static void probarMultiplicacionYDivision() {
        System.out.println("-- Integracion: mult/div --");
        ProgramaBuilder p = new ProgramaBuilder();
        p.addiu(T0, ZERO, 7);
        p.addiu(T1, ZERO, 6);
        p.mult(T0, T1);
        p.mflo(T2);
        p.div(T0, T1);
        p.mflo(T3);
        p.mfhi(T4);
        fin(p);
        ProcesadorMIPS cpu = ejecutar(p);
        MiniTest.assertEquals("7*6 = 42", 42, cpu.getRegistros().leer(10));
        MiniTest.assertEquals("7/6 cociente = 1", 1, cpu.getRegistros().leer(11));
        MiniTest.assertEquals("7%6 resto = 1", 1, cpu.getRegistros().leer(12));
        MiniTest.assertEquals("LO = 1 tras div", 1, (int) cpu.getLo());
        MiniTest.assertEquals("HI = 1 tras div", 1, (int) cpu.getHi());
    }
}
