package instruccion;

import memoria.RAM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Construye programas para el procesador directamente con instrucciones
 * MIPS (formatos R, I y J), sin pasar por texto ensamblado.
 *
 * Las etiquetas de codigo se resuelven al generar(); las direcciones de
 * datos se obtienen al registrar cada elemento (palabra, cadena, reserva).
 * Todo programa debe terminar con halt() para que la CPU sepa que acabo.
 */
public class ProgramaBuilder {

    /** Capacidad del segmento de texto (ROM): 100 instrucciones. */
    public static final int ROM_MAX = 100;

    // Registros
    public static final int ZERO = 0;
    public static final int AT = 1;
    public static final int V0 = 2;
    public static final int V1 = 3;
    public static final int A0 = 4;
    public static final int A1 = 5;
    public static final int A2 = 6;
    public static final int A3 = 7;
    public static final int T0 = 8;
    public static final int T1 = 9;
    public static final int T2 = 10;
    public static final int T3 = 11;
    public static final int T4 = 12;
    public static final int T5 = 13;
    public static final int T6 = 14;
    public static final int T7 = 15;
    public static final int S0 = 16;
    public static final int S1 = 17;
    public static final int S2 = 18;
    public static final int S3 = 19;
    public static final int S4 = 20;
    public static final int S5 = 21;
    public static final int S6 = 22;
    public static final int S7 = 23;
    public static final int T8 = 24;
    public static final int T9 = 25;
    public static final int K0 = 26;
    public static final int K1 = 27;
    public static final int GP = 28;
    public static final int SP = 29;
    public static final int FP = 30;
    public static final int RA = 31;

    /** Programa listo para cargar: instrucciones y datos iniciales. */
    public static final class Programa {
        public final int[] codigo;
        public final int[] datos;

        Programa(int[] codigo, int[] datos) {
            this.codigo = codigo;
            this.datos = datos;
        }
    }

    private static final class RamaPendiente {
        final int indice;
        final int opcode;
        final int rs;
        final int rt;
        final String etiqueta;
        final boolean esSalto; // j / jal guardan la direccion directa

        RamaPendiente(int indice, int opcode, int rs, int rt, String etiqueta, boolean esSalto) {
            this.indice = indice;
            this.opcode = opcode;
            this.rs = rs;
            this.rt = rt;
            this.etiqueta = etiqueta;
            this.esSalto = esSalto;
        }
    }

    private final List<Integer> codigo = new ArrayList<>();
    private final List<Integer> datos = new ArrayList<>();
    private final Map<String, Integer> etiquetas = new HashMap<>();
    private final List<RamaPendiente> pendientes = new ArrayList<>();

    /** Reserva una celda con el valor dado y devuelve su direccion. */
    public int palabra(int valor) {
        reservarCeldas(1);
        datos.add(valor);
        return RAM.DIRECCION_BASE + datos.size() - 1;
    }

    /** Reserva una cadena terminada en nulo y devuelve su direccion. */
    public int cadena(String texto) {
        reservarCeldas(texto.length() + 1);
        int direccion = RAM.DIRECCION_BASE + datos.size();
        for (int i = 0; i < texto.length(); i++) {
            datos.add((int) texto.charAt(i));
        }
        datos.add(0);
        return direccion;
    }

    /** Reserva n celdas en cero y devuelve la direccion de la primera. */
    public int reservar(int celdas) {
        reservarCeldas(celdas);
        int direccion = RAM.DIRECCION_BASE + datos.size();
        for (int i = 0; i < celdas; i++) {
            datos.add(0);
        }
        return direccion;
    }

    private void reservarCeldas(int celdas) {
        if (datos.size() + celdas > RAM.CELDAS) {
            throw new IllegalStateException("Los datos exceden la capacidad de la RAM (100 celdas)");
        }
    }

    /** Direccion de una celda dentro de un bloque de datos. */
    public static int celda(int direccionBase, int indice) {
        return direccionBase + indice;
    }

    /** Marca una etiqueta en la posicion actual del codigo. */
    public ProgramaBuilder etiqueta(String nombre) {
        if (etiquetas.put(nombre, codigo.size()) != null) {
            throw new IllegalArgumentException("Etiqueta repetida: " + nombre);
        }
        return this;
    }

    public ProgramaBuilder add(int rd, int rs, int rt) {
        return r(Funct.ADD, rd, rs, rt, 0);
    }

    public ProgramaBuilder addu(int rd, int rs, int rt) {
        return r(Funct.ADDU, rd, rs, rt, 0);
    }

    public ProgramaBuilder sub(int rd, int rs, int rt) {
        return r(Funct.SUB, rd, rs, rt, 0);
    }

    public ProgramaBuilder subu(int rd, int rs, int rt) {
        return r(Funct.SUBU, rd, rs, rt, 0);
    }

    public ProgramaBuilder and(int rd, int rs, int rt) {
        return r(Funct.AND, rd, rs, rt, 0);
    }

    public ProgramaBuilder or(int rd, int rs, int rt) {
        return r(Funct.OR, rd, rs, rt, 0);
    }

    public ProgramaBuilder xor(int rd, int rs, int rt) {
        return r(Funct.XOR, rd, rs, rt, 0);
    }

    public ProgramaBuilder nor(int rd, int rs, int rt) {
        return r(Funct.NOR, rd, rs, rt, 0);
    }

    /** rd = 1 si rs < rt (con signo), 0 en otro caso. */
    public ProgramaBuilder slt(int rd, int rs, int rt) {
        return r(Funct.SLT, rd, rs, rt, 0);
    }

    /** rd = 1 si rs < rt (sin signo), 0 en otro caso. */
    public ProgramaBuilder sltu(int rd, int rs, int rt) {
        return r(Funct.SLTU, rd, rs, rt, 0);
    }

    public ProgramaBuilder sll(int rd, int rt, int shamt) {
        emitir(Instruccion.codificarR(Opcode.R.codigo, 0, rt, rd, shamt, Funct.SLL.codigo));
        return this;
    }

    public ProgramaBuilder srl(int rd, int rt, int shamt) {
        emitir(Instruccion.codificarR(Opcode.R.codigo, 0, rt, rd, shamt, Funct.SRL.codigo));
        return this;
    }

    public ProgramaBuilder mult(int rs, int rt) {
        return r(Funct.MULT, 0, rs, rt, 0);
    }

    public ProgramaBuilder div(int rs, int rt) {
        return r(Funct.DIV, 0, rs, rt, 0);
    }

    public ProgramaBuilder mflo(int rd) {
        emitir(Instruccion.codificarR(Opcode.R.codigo, 0, 0, rd, 0, Funct.MFLO.codigo));
        return this;
    }

    public ProgramaBuilder mfhi(int rd) {
        emitir(Instruccion.codificarR(Opcode.R.codigo, 0, 0, rd, 0, Funct.MFHI.codigo));
        return this;
    }

    public ProgramaBuilder jr(int rs) {
        return r(Funct.JR, 0, rs, 0, 0);
    }

    public ProgramaBuilder syscall() {
        return r(Funct.SYSCALL, 0, 0, 0, 0);
    }

    /** Instruccion de alto: le indica a la CPU que el programa termino. */
    public ProgramaBuilder halt() {
        emitir(Instruccion.codificarHalt());
        return this;
    }

    public ProgramaBuilder addi(int rt, int rs, int imm) {
        return i(Opcode.ADDI, rs, rt, imm);
    }

    public ProgramaBuilder addiu(int rt, int rs, int imm) {
        return i(Opcode.ADDIU, rs, rt, imm);
    }

    public ProgramaBuilder andi(int rt, int rs, int imm) {
        return i(Opcode.ANDI, rs, rt, imm);
    }

    public ProgramaBuilder ori(int rt, int rs, int imm) {
        return i(Opcode.ORI, rs, rt, imm);
    }

    public ProgramaBuilder xori(int rt, int rs, int imm) {
        return i(Opcode.XORI, rs, rt, imm);
    }

    public ProgramaBuilder slti(int rt, int rs, int imm) {
        return i(Opcode.SLTI, rs, rt, imm);
    }

    public ProgramaBuilder sltiu(int rt, int rs, int imm) {
        return i(Opcode.SLTIU, rs, rt, imm);
    }

    public ProgramaBuilder lui(int rt, int imm) {
        return i(Opcode.LUI, 0, rt, imm);
    }

    public ProgramaBuilder lw(int rt, int base, int offset) {
        return i(Opcode.LW, base, rt, offset);
    }

    public ProgramaBuilder sw(int rt, int base, int offset) {
        return i(Opcode.SW, base, rt, offset);
    }

    /** lw con direccion absoluta (base $zero). */
    public ProgramaBuilder lwAbs(int rt, int direccion) {
        return lw(rt, ZERO, direccion);
    }

    /** sw con direccion absoluta (base $zero). */
    public ProgramaBuilder swAbs(int rt, int direccion) {
        return sw(rt, ZERO, direccion);
    }

    public ProgramaBuilder beq(int rs, int rt, String destino) {
        pendientes.add(new RamaPendiente(codigo.size(), Opcode.BEQ.codigo, rs, rt, destino, false));
        emitir(Instruccion.codificarI(Opcode.BEQ.codigo, rs, rt, 0));
        return this;
    }

    public ProgramaBuilder bne(int rs, int rt, String destino) {
        pendientes.add(new RamaPendiente(codigo.size(), Opcode.BNE.codigo, rs, rt, destino, false));
        emitir(Instruccion.codificarI(Opcode.BNE.codigo, rs, rt, 0));
        return this;
    }

    public ProgramaBuilder j(String destino) {
        pendientes.add(new RamaPendiente(codigo.size(), Opcode.J.codigo, 0, 0, destino, true));
        emitir(Instruccion.codificarJ(Opcode.J.codigo, 0));
        return this;
    }

    public ProgramaBuilder jal(String destino) {
        pendientes.add(new RamaPendiente(codigo.size(), Opcode.JAL.codigo, 0, 0, destino, true));
        emitir(Instruccion.codificarJ(Opcode.JAL.codigo, 0));
        return this;
    }

    /** Carga una constante de 32 bits usando addiu / ori / lui segun haga falta. */
    public ProgramaBuilder cargarConstante(int rt, int valor) {
        if (valor >= Short.MIN_VALUE && valor <= Short.MAX_VALUE) {
            addiu(rt, ZERO, valor);
        } else if (valor >= 0 && valor <= 0xFFFF) {
            ori(rt, ZERO, valor);
        } else {
            int alto = (valor >>> 16) & 0xFFFF;
            int bajo = valor & 0xFFFF;
            lui(rt, alto);
            if (bajo != 0) {
                ori(rt, rt, bajo);
            }
        }
        return this;
    }

    /** Copia un registro en otro (addu rd, rs, $zero). */
    public ProgramaBuilder mover(int rd, int rs) {
        return addu(rd, rs, ZERO);
    }

    public Programa generar() {
        for (RamaPendiente p : pendientes) {
            Integer destino = etiquetas.get(p.etiqueta);
            if (destino == null) {
                throw new IllegalArgumentException("Etiqueta sin definir: " + p.etiqueta);
            }
            int cod;
            if (p.esSalto) {
                cod = Instruccion.codificarJ(p.opcode, destino);
            } else {
                int offset = destino - (p.indice + 1);
                cod = Instruccion.codificarI(p.opcode, p.rs, p.rt, offset);
            }
            codigo.set(p.indice, cod);
        }

        int[] cod = new int[codigo.size()];
        for (int i = 0; i < cod.length; i++) {
            cod[i] = codigo.get(i);
        }
        int[] dat = new int[datos.size()];
        for (int i = 0; i < dat.length; i++) {
            dat[i] = datos.get(i);
        }
        return new Programa(cod, dat);
    }

    private ProgramaBuilder r(Funct funct, int rd, int rs, int rt, int shamt) {
        emitir(Instruccion.codificarR(Opcode.R.codigo, rs, rt, rd, shamt, funct.codigo));
        return this;
    }

    private ProgramaBuilder i(Opcode opcode, int rs, int rt, int imm) {
        emitir(Instruccion.codificarI(opcode.codigo, rs, rt, imm));
        return this;
    }

    private void emitir(int instr) {
        if (codigo.size() >= ROM_MAX) {
            throw new IllegalStateException("El programa excede la capacidad de la ROM (100 instrucciones)");
        }
        codigo.add(instr);
    }
}
