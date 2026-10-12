package mips;

import almacenamiento.ContadorPrograma;
import almacenamiento.DiscoDuro;
import almacenamiento.RAM;
import almacenamiento.ROM;
import almacenamiento.Registros;
import alu.ALU;
import alu.OperacionALU;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Procesador MIPS basico: ciclo busqueda-decodificacion-ejecucion.
 */
public class ProcesadorMIPS {

    // Codigos de syscall (convencion MIPS)
    public static final int SYSCALL_PRINT_INT = 1;
    public static final int SYSCALL_PRINT_STRING = 4;
    public static final int SYSCALL_READ_INT = 5;
    public static final int SYSCALL_PRINT_CHAR = 11;
    public static final int SYSCALL_EXIT = 10;

    private final ROM rom;
    private final RAM ram;
    private final DiscoDuro disco;
    private final Registros registros;
    private final ContadorPrograma pc;
    private final ALU alu;
    private final MemoriaSistema memoria;

    private long hi;
    private long lo;
    private boolean detenido;
    private long ciclos;
    private final List<String> consola = new ArrayList<>();
    private ByteArrayOutputStream bufferSalida;
    private PrintStream salidaOriginal;
    private int[] entradaEntera = new int[0];
    private int indiceEntrada;

    public ProcesadorMIPS() {
        this.rom = new ROM();
        this.ram = new RAM();
        this.disco = new DiscoDuro();
        this.registros = new Registros();
        this.pc = new ContadorPrograma();
        this.alu = new ALU();
        this.memoria = new MemoriaSistema(rom, ram, disco);
        reiniciar();
    }

    public void reiniciar() {
        rom.limpiar();
        rom.desbloquear();
        ram.limpiar();
        registros.limpiar();
        pc.reiniciar();
        hi = 0;
        lo = 0;
        detenido = false;
        ciclos = 0;
        consola.clear();
        indiceEntrada = 0;
    }

    public void cargarPrograma(int[] programa) {
        rom.cargarPrograma(programa, 0);
        rom.bloquear();
        pc.set(0);
    }

    public void fijarEntradas(int... valores) {
        this.entradaEntera = valores.clone();
        this.indiceEntrada = 0;
    }

    /** Ejecuta un ciclo (una instruccion). */
    public void ciclo() {
        if (detenido) {
            return;
        }
        int direccionPc = pc.get();
        if (direccionPc < 0 || direccionPc >= rom.getCantidadCeldas()) {
            throw new IllegalStateException("PC fuera de la ROM: " + direccionPc);
        }
        int instr = rom.leer(direccionPc);
        pc.incrementar(); // durante la busqueda el PC apunta a la siguiente instruccion
        ejecutar(instr);
        ciclos++;
    }

    /** Ejecuta el programa completo (hasta syscall exit o fin de programa). */
    public void ejecutar() {
        int limite = longitudPrograma();
        while (!detenido) {
            int pcActual = pc.get();
            if (pcActual < 0 || pcActual >= rom.getCantidadCeldas()) {
                detenido = true;
                break;
            }
            int instr = rom.leer(pcActual);
            if (instr == 0 && pcActual >= limite) {
                detenido = true;
                break;
            }
            ciclo();
        }
    }

    /** Longitud util del programa en la ROM (ultima celda no nula + 1). */
    public int longitudPrograma() {
        int n = 0;
        for (int i = 0; i < rom.getCantidadCeldas(); i++) {
            if (rom.leer(i) != 0) {
                n = i + 1;
            }
        }
        return n;
    }

    private void ejecutar(int instr) {
        int opcode = Instruccion.opcode(instr);

        if (opcode == Instruccion.OP_R) {
            ejecutarTipoR(instr);
            return;
        }

        switch (opcode) {
            case Instruccion.OP_ADDI: {
                int rs = Instruccion.rs(instr);
                int rt = Instruccion.rt(instr);
                int imm = Instruccion.extensionSigno16(Instruccion.inmediatoSinSigno(instr));
                registros.escribir(rt, alu.operar(OperacionALU.ADD, registros.leer(rs), imm, 0));
                break;
            }
            case Instruccion.OP_ADDIU: {
                int rs = Instruccion.rs(instr);
                int rt = Instruccion.rt(instr);
                int imm = Instruccion.extensionSigno16(Instruccion.inmediatoSinSigno(instr));
                registros.escribir(rt, alu.operar(OperacionALU.ADDU, registros.leer(rs), imm, 0));
                break;
            }
            case Instruccion.OP_ANDI: {
                int rs = Instruccion.rs(instr);
                int rt = Instruccion.rt(instr);
                int imm = Instruccion.inmediatoSinSigno(instr);
                registros.escribir(rt, alu.operar(OperacionALU.AND, registros.leer(rs), imm, 0));
                break;
            }
            case Instruccion.OP_ORI: {
                int rs = Instruccion.rs(instr);
                int rt = Instruccion.rt(instr);
                int imm = Instruccion.inmediatoSinSigno(instr);
                registros.escribir(rt, alu.operar(OperacionALU.OR, registros.leer(rs), imm, 0));
                break;
            }
            case Instruccion.OP_XORI: {
                int rs = Instruccion.rs(instr);
                int rt = Instruccion.rt(instr);
                int imm = Instruccion.inmediatoSinSigno(instr);
                registros.escribir(rt, alu.operar(OperacionALU.XOR, registros.leer(rs), imm, 0));
                break;
            }
            case Instruccion.OP_SLTI: {
                int rs = Instruccion.rs(instr);
                int rt = Instruccion.rt(instr);
                int imm = Instruccion.extensionSigno16(Instruccion.inmediatoSinSigno(instr));
                registros.escribir(rt, alu.operar(OperacionALU.SLT, registros.leer(rs), imm, 0));
                break;
            }
            case Instruccion.OP_SLTIU: {
                int rs = Instruccion.rs(instr);
                int rt = Instruccion.rt(instr);
                int imm = Instruccion.extensionSigno16(Instruccion.inmediatoSinSigno(instr));
                registros.escribir(rt, alu.operar(OperacionALU.SLTU, registros.leer(rs), imm, 0));
                break;
            }
            case Instruccion.OP_LUI: {
                int rt = Instruccion.rt(instr);
                int imm = Instruccion.inmediatoSinSigno(instr);
                registros.escribir(rt, imm << 16);
                break;
            }
            case Instruccion.OP_LW: {
                int rs = Instruccion.rs(instr);
                int rt = Instruccion.rt(instr);
                int offset = Instruccion.extensionSigno16(Instruccion.inmediatoSinSigno(instr));
                int direccion = registros.leer(rs) + offset;
                registros.escribir(rt, memoria.leer(direccion));
                break;
            }
            case Instruccion.OP_SW: {
                int rs = Instruccion.rs(instr);
                int rt = Instruccion.rt(instr);
                int offset = Instruccion.extensionSigno16(Instruccion.inmediatoSinSigno(instr));
                int direccion = registros.leer(rs) + offset;
                memoria.escribir(direccion, registros.leer(rt));
                break;
            }
            case Instruccion.OP_BEQ: {
                int rs = Instruccion.rs(instr);
                int rt = Instruccion.rt(instr);
                int offset = Instruccion.extensionSigno16(Instruccion.inmediatoSinSigno(instr));
                if (registros.leer(rs) == registros.leer(rt)) {
                    pc.set(pc.get() + offset);
                }
                break;
            }
            case Instruccion.OP_BNE: {
                int rs = Instruccion.rs(instr);
                int rt = Instruccion.rt(instr);
                int offset = Instruccion.extensionSigno16(Instruccion.inmediatoSinSigno(instr));
                if (registros.leer(rs) != registros.leer(rt)) {
                    pc.set(pc.get() + offset);
                }
                break;
            }
            case Instruccion.OP_J: {
                pc.set(Instruccion.direccionJ(instr));
                break;
            }
            case Instruccion.OP_JAL: {
                registros.escribir(31, pc.get()); // $ra = direccion de la siguiente instruccion
                pc.set(Instruccion.direccionJ(instr));
                break;
            }
            default:
                throw new UnsupportedOperationException(
                        "Opcode no soportado: 0x" + Integer.toHexString(opcode)
                        + " en PC=" + (pc.get() - 1));
        }
    }

    private void ejecutarTipoR(int instr) {
        int rs = Instruccion.rs(instr);
        int rt = Instruccion.rt(instr);
        int rd = Instruccion.rd(instr);
        int shamt = Instruccion.shamt(instr);
        int funct = Instruccion.funct(instr);

        switch (funct) {
            case Instruccion.FUNCT_ADD:
                registros.escribir(rd, alu.operar(OperacionALU.ADD, registros.leer(rs), registros.leer(rt), 0));
                break;
            case Instruccion.FUNCT_ADDU:
                registros.escribir(rd, alu.operar(OperacionALU.ADDU, registros.leer(rs), registros.leer(rt), 0));
                break;
            case Instruccion.FUNCT_SUB:
                registros.escribir(rd, alu.operar(OperacionALU.SUB, registros.leer(rs), registros.leer(rt), 0));
                break;
            case Instruccion.FUNCT_SUBU:
                registros.escribir(rd, alu.operar(OperacionALU.SUBU, registros.leer(rs), registros.leer(rt), 0));
                break;
            case Instruccion.FUNCT_AND:
                registros.escribir(rd, alu.operar(OperacionALU.AND, registros.leer(rs), registros.leer(rt), 0));
                break;
            case Instruccion.FUNCT_OR:
                registros.escribir(rd, alu.operar(OperacionALU.OR, registros.leer(rs), registros.leer(rt), 0));
                break;
            case Instruccion.FUNCT_XOR:
                registros.escribir(rd, alu.operar(OperacionALU.XOR, registros.leer(rs), registros.leer(rt), 0));
                break;
            case Instruccion.FUNCT_NOR:
                registros.escribir(rd, alu.operar(OperacionALU.NOR, registros.leer(rs), registros.leer(rt), 0));
                break;
            case Instruccion.FUNCT_SLT:
                registros.escribir(rd, alu.operar(OperacionALU.SLT, registros.leer(rs), registros.leer(rt), 0));
                break;
            case Instruccion.FUNCT_SLTU:
                registros.escribir(rd, alu.operar(OperacionALU.SLTU, registros.leer(rs), registros.leer(rt), 0));
                break;
            case Instruccion.FUNCT_SLL:
                registros.escribir(rd, alu.operar(OperacionALU.SLL, registros.leer(rt), 0, shamt));
                break;
            case Instruccion.FUNCT_SRL:
                registros.escribir(rd, alu.operar(OperacionALU.SRL, registros.leer(rt), 0, shamt));
                break;
            case Instruccion.FUNCT_JR:
                pc.set(registros.leer(rs));
                break;
            case Instruccion.FUNCT_MULT: {
                long producto = (long) registros.leer(rs) * (long) registros.leer(rt);
                lo = (int) producto;
                hi = (int) (producto >>> 32);
                break;
            }
            case Instruccion.FUNCT_DIV: {
                int a = registros.leer(rs);
                int b = registros.leer(rt);
                if (b == 0) {
                    throw new ArithmeticException("Division por cero");
                }
                lo = a / b;
                hi = a % b;
                break;
            }
            case Instruccion.FUNCT_MFHI:
                registros.escribir(rd, (int) hi);
                break;
            case Instruccion.FUNCT_MFLO:
                registros.escribir(rd, (int) lo);
                break;
            case Instruccion.FUNCT_SYSCALL:
                syscall();
                break;
            default:
                throw new UnsupportedOperationException("Funct no soportado: 0x" + Integer.toHexString(funct));
        }
    }

    private void syscall() {
        int servicio = registros.leer(2); // $v0
        switch (servicio) {
            case SYSCALL_PRINT_INT:
                imprimir(Integer.toString(registros.leer(4))); // $a0
                break;
            case SYSCALL_PRINT_STRING: {
                int dir = registros.leer(4);
                StringBuilder sb = new StringBuilder();
                while (true) {
                    int c = memoria.leer(dir);
                    if (c == 0) {
                        break;
                    }
                    sb.append((char) c);
                    dir++;
                }
                imprimir(sb.toString());
                break;
            }
            case SYSCALL_PRINT_CHAR:
                imprimir(String.valueOf((char) (registros.leer(4) & 0xFF)));
                break;
            case SYSCALL_READ_INT: {
                int valor = 0;
                if (indiceEntrada < entradaEntera.length) {
                    valor = entradaEntera[indiceEntrada++];
                }
                registros.escribir(2, valor);
                break;
            }
            case SYSCALL_EXIT:
                detenido = true;
                break;
            default:
                throw new UnsupportedOperationException("Servicio syscall no soportado: " + servicio);
        }
    }

    private void imprimir(String texto) {
        consola.add(texto);
        System.out.print(texto);
    }

    /** Redirige la salida del sistema al buffer interno (para pruebas). */
    public void capturarSalida() {
        if (salidaOriginal == null) {
            salidaOriginal = System.out;
            bufferSalida = new ByteArrayOutputStream();
            System.setOut(new PrintStream(bufferSalida));
        }
    }

    public void restaurarSalida() {
        if (salidaOriginal != null) {
            System.setOut(salidaOriginal);
            salidaOriginal = null;
        }
    }

    public String obtenerSalidaCapturada() {
        return bufferSalida == null ? "" : bufferSalida.toString();
    }

    public String obtenerConsola() {
        StringBuilder sb = new StringBuilder();
        for (String s : consola) {
            sb.append(s);
        }
        return sb.toString();
    }

    public boolean isDetenido() {
        return detenido;
    }

    public void detener() {
        detenido = true;
    }

    public long getCiclos() {
        return ciclos;
    }

    public ROM getRom() {
        return rom;
    }

    public RAM getRam() {
        return ram;
    }

    public DiscoDuro getDisco() {
        return disco;
    }

    public Registros getRegistros() {
        return registros;
    }

    public ContadorPrograma getPc() {
        return pc;
    }

    public ALU getAlu() {
        return alu;
    }

    public MemoriaSistema getMemoria() {
        return memoria;
    }

    public long getHi() {
        return hi;
    }

    public long getLo() {
        return lo;
    }
}
