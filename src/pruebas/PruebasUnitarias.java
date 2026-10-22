package pruebas;

import cpu.PC;
import memoria.DiscoDuro;
import memoria.RAM;
import memoria.ROM;
import memoria.Registros;
import memoria.UnidadAlmacenamiento;
import alu.ALU;
import alu.OperacionALU;
import instruccion.Decodificador;
import instruccion.FormatoInstruccion;
import instruccion.Funct;
import instruccion.Instruccion;
import instruccion.Opcode;
import instruccion.ProgramaBuilder;
import instruccion.ProgramaBuilder.Programa;
import memoria.MemoriaSistema;

import static instruccion.ProgramaBuilder.T0;
import static instruccion.ProgramaBuilder.T1;
import static instruccion.ProgramaBuilder.V0;
import static instruccion.ProgramaBuilder.ZERO;

/**
 * Pruebas unitarias del emulador del procesador MIPS.
 * Ejecucion: java -cp out pruebas.PruebasUnitarias
 */
public class PruebasUnitarias {

    public static void main(String[] args) {
        System.out.println("=== PRUEBAS UNITARIAS - Emulador MIPS ===");
        System.out.println();

        probarUnidadAlmacenamiento();
        probarSegmentosDeMemoria();
        probarMemorias();
        probarDiscoDuro();
        probarRegistros();
        probarContadorPrograma();
        probarALU();
        probarCodificacionInstrucciones();
        probarHalto();
        probarProgramaBuilder();

        int fallidas = MiniTest.resumen("RESUMEN PRUEBAS UNITARIAS");
        System.exit(fallidas == 0 ? 0 : 1);
    }

    private static void probarUnidadAlmacenamiento() {
        System.out.println("-- UnidadAlmacenamiento --");
        UnidadAlmacenamiento u = new UnidadAlmacenamiento("prueba", 0, 32, 100);
        MiniTest.assertEquals("ancho de palabra = 32 bits", 32, u.getAnchoPalabra());
        MiniTest.assertEquals("cantidad de celdas = 100", 100, u.getCantidadCeldas());
        MiniTest.assertEquals("nombre del segmento", "prueba", u.getNombre());

        u.escribir(0, 42);
        u.escribir(99, -1);
        MiniTest.assertEquals("lectura celda 0", 42, u.leer(0));
        MiniTest.assertEquals("lectura celda 99", -1, u.leer(99));

        MiniTest.assertThrows("escritura fuera de rango (100)",
                () -> u.escribir(100, 1), IndexOutOfBoundsException.class);
        MiniTest.assertThrows("lectura fuera de rango (-1)",
                () -> u.leer(-1), IndexOutOfBoundsException.class);

        u.limpiar();
        MiniTest.assertEquals("limpiar pone celda 0 a 0", 0, u.leer(0));

        MiniTest.assertThrows("constructor rechaza celdas <= 0",
                () -> new UnidadAlmacenamiento("prueba", 0, 32, 0), IllegalArgumentException.class);
    }

    private static void probarSegmentosDeMemoria() {
        System.out.println("-- Segmentos de memoria --");
        RAM ram = new RAM();
        ROM rom = new ROM();
        DiscoDuro disco = new DiscoDuro();

        MiniTest.assertEquals("ROM = segmento de texto en la direccion 0", 0, rom.getDireccionBase());
        MiniTest.assertEquals("RAM = segmento de datos en la direccion 100", 100, ram.getDireccionBase());
        MiniTest.assertEquals("disco = segmento de almacenamiento en la direccion 200", 200,
                disco.getDireccionBase());

        MiniTest.assertTrue("la direccion 100 pertenece a la RAM", ram.contieneAbsoluta(100));
        MiniTest.assertFalse("la direccion 100 no pertenece a la ROM", rom.contieneAbsoluta(100));
        MiniTest.assertEquals("direccion absoluta -> local en RAM", 0, ram.aLocal(100));
        MiniTest.assertEquals("direccion absoluta -> local en disco", 50, disco.aLocal(250));
        MiniTest.assertThrows("direccion de otro segmento rechazada",
                () -> ram.aLocal(250), IndexOutOfBoundsException.class);

        MemoriaSistema mapa = new MemoriaSistema(rom, ram, disco);
        MiniTest.assertEquals("el mapa tiene 300 celdas", 300, mapa.totalCeldas());
        MiniTest.assertEquals("leer direccion global 105 en la RAM", 0, mapa.leer(105));
        mapa.escribir(105, 777);
        MiniTest.assertEquals("escribir/leer direccion global 105", 777, mapa.leer(105));
        MiniTest.assertEquals("escribir en 105 llega a la celda local 5 de la RAM", 777, ram.leer(5));
        MiniTest.assertThrows("direccion fuera del mapa (300)",
                () -> mapa.leer(300), IndexOutOfBoundsException.class);
    }

    private static void probarMemorias() {
        System.out.println("-- RAM / ROM --");
        RAM ram = new RAM();
        MiniTest.assertEquals("RAM ancho 32 bits", 32, ram.getAnchoPalabra());
        MiniTest.assertEquals("RAM 100 celdas", 100, ram.getCantidadCeldas());
        MiniTest.assertEquals("RAM arranca en 0x00000000 (celda 0)", 0, ram.leer(0));
        MiniTest.assertEquals("RAM arranca en 0x00000000 (celda 99)", 0, ram.leer(99));
        ram.escribir(50, 12345);
        MiniTest.assertEquals("RAM escritura/lectura", 12345, ram.leer(50));

        ROM rom = new ROM();
        MiniTest.assertEquals("ROM ancho 32 bits", 32, rom.getAnchoPalabra());
        MiniTest.assertEquals("ROM 100 celdas", 100, rom.getCantidadCeldas());
        rom.bloquear();
        MiniTest.assertThrows("ROM bloqueada rechaza escritura",
                () -> rom.escribir(0, 1), IllegalStateException.class);
        rom.desbloquear();
        rom.escribir(0, 0xAAAAAAAA);
        MiniTest.assertEquals("ROM desbloqueada permite escritura", 0xAAAAAAAA, rom.leer(0));

        int[] prog = {1, 2, 3, 4};
        rom.cargarPrograma(prog, 0);
        MiniTest.assertEquals("cargarPrograma celda 0", 1, rom.leer(0));
        MiniTest.assertEquals("cargarPrograma celda 3", 4, rom.leer(3));
        rom.bloquear();
        MiniTest.assertThrows("ROM bloqueada tras cargarPrograma",
                () -> rom.escribir(1, 9), IllegalStateException.class);
    }

    private static void probarDiscoDuro() {
        System.out.println("-- DiscoDuro --");
        DiscoDuro disco = new DiscoDuro();
        MiniTest.assertEquals("DiscoDuro ancho 32 bits", 32, disco.getAnchoPalabra());
        MiniTest.assertEquals("DiscoDuro 100 celdas", 100, disco.getCantidadCeldas());

        disco.escribir(0, 111);
        disco.escribir(1, -222);

        String ruta = System.getProperty("java.io.tmpdir") + "/disco_prueba.txt";
        try {
            disco.guardar(ruta);
            DiscoDuro copia = new DiscoDuro();
            copia.cargar(ruta);
            MiniTest.assertEquals("guardar/cargar celda 0", 111, copia.leer(0));
            MiniTest.assertEquals("guardar/cargar celda 1", -222, copia.leer(1));
        } catch (Exception e) {
            MiniTest.assertTrue("persistencia en disco (" + e.getMessage() + ")", false);
        }

        RAM ram = new RAM();
        ram.escribir(10, 777);
        DiscoDuro d2 = new DiscoDuro();
        d2.importar(ram);
        MiniTest.assertEquals("importar desde RAM", 777, d2.leer(10));
        RAM ram2 = new RAM();
        d2.exportar(ram2);
        MiniTest.assertEquals("exportar a RAM", 777, ram2.leer(10));
    }

    private static void probarRegistros() {
        System.out.println("-- Registros --");
        Registros r = new Registros();
        MiniTest.assertEquals("cantidad de registros = 32", 32, Registros.CANTIDAD);
        r.escribir(8, 55);
        MiniTest.assertEquals("escritura/lectura $t0", 55, r.leer(8));
        r.escribir(0, 99);
        MiniTest.assertEquals("$zero siempre es 0", 0, r.leer(0));
        MiniTest.assertThrows("indice de registro invalido",
                () -> r.leer(32), IndexOutOfBoundsException.class);
        r.limpiar();
        MiniTest.assertEquals("limpiar registros", 0, r.leer(8));
    }

    private static void probarContadorPrograma() {
        System.out.println("-- Contador de programa (PC) --");
        PC pc = new PC();
        MiniTest.assertEquals("PC inicia en 0", 0, pc.get());
        pc.incrementar();
        MiniTest.assertEquals("PC incrementa en 1 (siguiente instruccion)", 1, pc.get());
        pc.incrementar();
        pc.incrementar();
        MiniTest.assertEquals("PC tras 3 incrementos", 3, pc.get());
        pc.set(50);
        MiniTest.assertEquals("PC set manual", 50, pc.get());
        pc.reiniciar();
        MiniTest.assertEquals("PC reiniciar", 0, pc.get());
        MiniTest.assertEquals("ancho del PC = 32 bits", 32, PC.ANCHO);
    }

    private static void probarALU() {
        System.out.println("-- ALU --");
        ALU alu = new ALU();

        MiniTest.assertEquals("ADD 2+3", 5, alu.operar(OperacionALU.ADD, 2, 3, 0));
        MiniTest.assertEquals("ADD negativos -5+(-3)", -8, alu.operar(OperacionALU.ADD, -5, -3, 0));
        MiniTest.assertThrows("ADD con desbordamiento detiene la ejecucion",
                () -> alu.operar(OperacionALU.ADD, Integer.MAX_VALUE, 1, 0),
                ArithmeticException.class);
        MiniTest.assertEquals("ADDU MAX+1 envuelve a MIN",
                Integer.MIN_VALUE, alu.operar(OperacionALU.ADDU, Integer.MAX_VALUE, 1, 0));

        MiniTest.assertEquals("SUB 10-4", 6, alu.operar(OperacionALU.SUB, 10, 4, 0));
        MiniTest.assertThrows("SUB con desbordamiento detiene la ejecucion",
                () -> alu.operar(OperacionALU.SUB, Integer.MIN_VALUE, 1, 0),
                ArithmeticException.class);
        MiniTest.assertEquals("SUBU MIN-1 envuelve a MAX",
                Integer.MAX_VALUE, alu.operar(OperacionALU.SUBU, Integer.MIN_VALUE, 1, 0));

        MiniTest.assertEquals("AND 0xF0 & 0x3C", 0x30, alu.operar(OperacionALU.AND, 0xF0, 0x3C, 0));
        MiniTest.assertEquals("OR 0xF0 | 0x0F", 0xFF, alu.operar(OperacionALU.OR, 0xF0, 0x0F, 0));
        MiniTest.assertEquals("XOR 0xFF ^ 0x0F", 0xF0, alu.operar(OperacionALU.XOR, 0xFF, 0x0F, 0));
        MiniTest.assertEquals("NOR 0 | 0 = ~0", -1, alu.operar(OperacionALU.NOR, 0, 0, 0));

        MiniTest.assertEquals("SLT 3 < 5", 1, alu.operar(OperacionALU.SLT, 3, 5, 0));
        MiniTest.assertEquals("SLT 5 < 3", 0, alu.operar(OperacionALU.SLT, 5, 3, 0));
        MiniTest.assertEquals("SLT -1 < 1", 1, alu.operar(OperacionALU.SLT, -1, 1, 0));
        MiniTest.assertEquals("SLTU -1 < 1 vale 0 (-1 es 4294967295 sin signo)",
                0, alu.operar(OperacionALU.SLTU, -1, 1, 0));
        MiniTest.assertEquals("SLTU 1 < -1 vale 1 (sin signo)",
                1, alu.operar(OperacionALU.SLTU, 1, -1, 0));

        MiniTest.assertEquals("SLL 1 << 4", 16, alu.operar(OperacionALU.SLL, 1, 0, 4));
        MiniTest.assertEquals("SRL 32 >>> 3", 4, alu.operar(OperacionALU.SRL, 32, 0, 3));
    }

    private static void probarCodificacionInstrucciones() {
        System.out.println("-- Codificacion de instrucciones --");

        int add = Instruccion.codificarR(Opcode.R.codigo, 9, 10, 8, 0, Funct.ADD.codigo);
        MiniTest.assertEquals("opcode de R-type", Opcode.R.codigo, Decodificador.opcode(add));
        MiniTest.assertEquals("rs de add", 9, Decodificador.rs(add));
        MiniTest.assertEquals("rt de add", 10, Decodificador.rt(add));
        MiniTest.assertEquals("rd de add", 8, Decodificador.rd(add));
        MiniTest.assertEquals("funct de add", Funct.ADD.codigo, Decodificador.funct(add));
        MiniTest.assertEquals("decodificar: opcode ADD", "R",
                Decodificador.decodificar(add).opcode.name());
        MiniTest.assertEquals("decodificar: funct ADD", "ADD",
                Decodificador.decodificar(add).funct.name());
        MiniTest.assertEquals("decodificar: formato R", "R",
                Decodificador.decodificar(add).formato.name());

        int addu = Instruccion.codificarR(Opcode.R.codigo, 9, 10, 8, 0, Funct.ADDU.codigo);
        MiniTest.assertEquals("funct de addu", Funct.ADDU.codigo, Decodificador.funct(addu));
        int subu = Instruccion.codificarR(Opcode.R.codigo, 9, 10, 8, 0, Funct.SUBU.codigo);
        MiniTest.assertEquals("funct de subu", Funct.SUBU.codigo, Decodificador.funct(subu));
        int sltu = Instruccion.codificarR(Opcode.R.codigo, 9, 10, 8, 0, Funct.SLTU.codigo);
        MiniTest.assertEquals("funct de sltu", Funct.SLTU.codigo, Decodificador.funct(sltu));

        int addi = Instruccion.codificarI(Opcode.ADDI.codigo, 0, 8, 100);
        MiniTest.assertEquals("opcode de addi", Opcode.ADDI.codigo, Decodificador.opcode(addi));
        MiniTest.assertEquals("inmediato de addi", 100, Decodificador.inmediatoSinSigno(addi));
        MiniTest.assertEquals("decodificar: opcode ADDI", "ADDI",
                Decodificador.decodificar(addi).opcode.name());

        int addiu = Instruccion.codificarI(Opcode.ADDIU.codigo, 0, 8, -3);
        MiniTest.assertEquals("opcode de addiu", Opcode.ADDIU.codigo, Decodificador.opcode(addiu));
        MiniTest.assertEquals("extension de signo de -3", -3, Decodificador.extensionSigno16(
                Decodificador.inmediatoSinSigno(addiu)));

        int sltiu = Instruccion.codificarI(Opcode.SLTIU.codigo, 9, 8, 5);
        MiniTest.assertEquals("opcode de sltiu", Opcode.SLTIU.codigo, Decodificador.opcode(sltiu));

        int j = Instruccion.codificarJ(Opcode.J.codigo, 50);
        MiniTest.assertEquals("opcode de j", Opcode.J.codigo, Decodificador.opcode(j));
        MiniTest.assertEquals("direccion de j", 50, Decodificador.direccionJ(j));
        MiniTest.assertEquals("decodificar: direccion del salto J", 50,
                Decodificador.decodificar(j).direccion);

        int sys = Instruccion.codificarR(Opcode.R.codigo, 0, 0, 0, 0, Funct.SYSCALL.codigo);
        MiniTest.assertEquals("funct de syscall", Funct.SYSCALL.codigo, Decodificador.funct(sys));

        MiniTest.assertEquals("la palabra 0 es NOP", Instruccion.PALABRA_NOP, 0);
        MiniTest.assertTrue("decodificar palabra 0 -> nop", Decodificador.decodificar(0).esNop());
    }

    private static void probarHalto() {
        System.out.println("-- Instruccion HALT --");

        int halt = Instruccion.codificarHalt();
        MiniTest.assertEquals("opcode de halt = 0x3F", 0x3F, Decodificador.opcode(halt));
        Instruccion instr = Decodificador.decodificar(halt);
        MiniTest.assertEquals("decodificar: opcode HALT", "HALT", instr.opcode.name());
        MiniTest.assertTrue("esHalt() es true", instr.esHalt());
        MiniTest.assertFalse("halt no es NOP", instr.esNop());

        ProgramaBuilder p = new ProgramaBuilder();
        p.addiu(T0, ZERO, 7);
        p.halt();
        Programa prog = p.generar();
        MiniTest.assertEquals("halt ocupa una instruccion", 2, prog.codigo.length);
        MiniTest.assertEquals("la ultima palabra es HALT", halt, prog.codigo[1]);
    }

    private static void probarProgramaBuilder() {
        System.out.println("-- ProgramaBuilder --");

        ProgramaBuilder p = new ProgramaBuilder();
        p.cargarConstante(T0, 42);
        p.addu(T1, T0, ZERO);
        p.syscall();
        Programa prog = p.generar();
        MiniTest.assertEquals("3 instrucciones generadas", 3, prog.codigo.length);
        MiniTest.assertEquals("cargarConstante(42) usa addiu", Opcode.ADDIU.codigo,
                Decodificador.opcode(prog.codigo[0]));
        MiniTest.assertEquals("inmediato 42", 42, Decodificador.extensionSigno16(
                Decodificador.inmediatoSinSigno(prog.codigo[0])));
        MiniTest.assertEquals("opcode de addu", Opcode.R.codigo, Decodificador.opcode(prog.codigo[1]));
        MiniTest.assertEquals("funct de addu", Funct.ADDU.codigo, Decodificador.funct(prog.codigo[1]));

        ProgramaBuilder p2 = new ProgramaBuilder();
        p2.cargarConstante(V0, 0x12345678);
        Programa prog2 = p2.generar();
        MiniTest.assertEquals("constante de 32 bits ocupa lui + ori", 2, prog2.codigo.length);
        MiniTest.assertEquals("lui carga la parte alta", Opcode.LUI.codigo,
                Decodificador.opcode(prog2.codigo[0]));
        MiniTest.assertEquals("ori carga la parte baja", 0x5678,
                Decodificador.inmediatoSinSigno(prog2.codigo[1]));

        ProgramaBuilder p3 = new ProgramaBuilder();
        int dir = p3.palabra(5);
        p3.palabra(6);
        p3.palabra(7);
        int dirTexto = p3.cadena("Hola");
        p3.lwAbs(T0, dir);
        Programa prog3 = p3.generar();
        MiniTest.assertEquals("los datos empiezan en la celda 100", RAM.DIRECCION_BASE, dir);
        MiniTest.assertEquals("segunda palabra", 6, prog3.datos[1]);
        MiniTest.assertEquals("la cadena ocupa 5 celdas (4 + nulo)", 5,
                prog3.datos.length - 3);
        MiniTest.assertEquals("direccion de la cadena", 103, dirTexto);
        MiniTest.assertEquals("lwAbs con direccion absoluta", dir,
                Decodificador.inmediatoSinSigno(prog3.codigo[0]));
        MiniTest.assertEquals("opcode de lw", Opcode.LW.codigo,
                Decodificador.opcode(prog3.codigo[0]));

        ProgramaBuilder p4 = new ProgramaBuilder();
        p4.addiu(T0, ZERO, 1);
        p4.bne(T0, ZERO, "salir");
        p4.addiu(T1, ZERO, 2);
        p4.etiqueta("salir");
        p4.syscall();
        Programa prog4 = p4.generar();
        MiniTest.assertEquals("offset de bne hacia adelante", 1,
                Decodificador.extensionSigno16(Decodificador.inmediatoSinSigno(prog4.codigo[1])));
        MiniTest.assertEquals("opcode de bne", Opcode.BNE.codigo,
                Decodificador.opcode(prog4.codigo[1]));

        ProgramaBuilder p5 = new ProgramaBuilder();
        p5.j("destino");
        p5.syscall();
        p5.etiqueta("destino");
        p5.syscall();
        Programa prog5 = p5.generar();
        MiniTest.assertEquals("direccion de j resuelta", 2,
                Decodificador.direccionJ(prog5.codigo[0]));

        ProgramaBuilder p6 = new ProgramaBuilder();
        p6.beq(ZERO, ZERO, "noexiste");
        MiniTest.assertThrows("etiqueta sin definir al generar",
                () -> p6.generar(), IllegalArgumentException.class);

        ProgramaBuilder p7 = new ProgramaBuilder();
        for (int i = 0; i < 100; i++) {
            p7.syscall();
        }
        MiniTest.assertThrows("la ROM no admite mas de 100 instrucciones",
                () -> p7.syscall(), IllegalStateException.class);
    }
}
