package instruccion;

import cpu.CPU;
import memoria.Registros;

/**
 * Desensamblador de las instrucciones MIPS soportadas por el simulador.
 *
 * mnemonico()   -> texto ensamblador de la instruccion
 * operacion()   -> efecto de la instruccion con los valores actuales
 *                  de los registros y de la memoria
 */
public final class Desensamblador {

    private Desensamblador() {
    }

    // ------------------------------------------------------------------
    // Ensamblador
    // ------------------------------------------------------------------

    public static String mnemonico(int palabra) {
        return mnemonico(palabra, -1);
    }

    /**
     * @param pc direccion (indice de palabra) de la instruccion, o -1 si se
     *           desconoce; se usa para resolver el destino de las ramas.
     */
    public static String mnemonico(int palabra, int pc) {
        Instruccion instr = Decodificador.decodificar(palabra);

        if (instr.esNop()) {
            return "nop";
        }
        if (instr.opcode == null) {
            return String.format("?opcode 0x%02X?", Decodificador.opcode(palabra));
        }
        if (instr.opcode == Opcode.R) {
            return mnemonicoR(instr);
        }
        if (instr.opcode == Opcode.HALT) {
            return Opcode.HALT.mnemonico;
        }

        int imm = instr.inmediatoSinSigno;
        switch (instr.opcode) {
            case ADDI:
                return String.format("addi %s, %s, %d", nom(instr.rt), nom(instr.rs), instr.inmediato);
            case ADDIU:
                return String.format("addiu %s, %s, %d", nom(instr.rt), nom(instr.rs), instr.inmediato);
            case SLTI:
                return String.format("slti %s, %s, %d", nom(instr.rt), nom(instr.rs), instr.inmediato);
            case SLTIU:
                return String.format("sltiu %s, %s, %d", nom(instr.rt), nom(instr.rs), instr.inmediato);
            case ANDI:
                return String.format("andi %s, %s, 0x%04X", nom(instr.rt), nom(instr.rs), imm);
            case ORI:
                return String.format("ori %s, %s, 0x%04X", nom(instr.rt), nom(instr.rs), imm);
            case XORI:
                return String.format("xori %s, %s, 0x%04X", nom(instr.rt), nom(instr.rs), imm);
            case LUI:
                return String.format("lui %s, 0x%04X", nom(instr.rt), imm);
            case LW:
                return String.format("lw %s, %d(%s)", nom(instr.rt), instr.inmediato, nom(instr.rs));
            case SW:
                return String.format("sw %s, %d(%s)", nom(instr.rt), instr.inmediato, nom(instr.rs));
            case BEQ:
                return String.format("beq %s, %s, %s", nom(instr.rs), nom(instr.rt), destino(pc, instr.inmediato));
            case BNE:
                return String.format("bne %s, %s, %s", nom(instr.rs), nom(instr.rt), destino(pc, instr.inmediato));
            case J:
                return String.format("j %d", instr.direccion);
            case JAL:
                return String.format("jal %d", instr.direccion);
            default:
                return String.format("?opcode %s?", instr.opcode.mnemonico);
        }
    }

    private static String mnemonicoR(Instruccion instr) {
        if (instr.funct == null) {
            return String.format("?funct 0x%02X?", Decodificador.funct(instr.palabra));
        }
        switch (instr.funct) {
            case ADD:
                return String.format("add %s, %s, %s", nom(instr.rd), nom(instr.rs), nom(instr.rt));
            case ADDU:
                return String.format("addu %s, %s, %s", nom(instr.rd), nom(instr.rs), nom(instr.rt));
            case SUB:
                return String.format("sub %s, %s, %s", nom(instr.rd), nom(instr.rs), nom(instr.rt));
            case SUBU:
                return String.format("subu %s, %s, %s", nom(instr.rd), nom(instr.rs), nom(instr.rt));
            case AND:
                return String.format("and %s, %s, %s", nom(instr.rd), nom(instr.rs), nom(instr.rt));
            case OR:
                return String.format("or %s, %s, %s", nom(instr.rd), nom(instr.rs), nom(instr.rt));
            case XOR:
                return String.format("xor %s, %s, %s", nom(instr.rd), nom(instr.rs), nom(instr.rt));
            case NOR:
                return String.format("nor %s, %s, %s", nom(instr.rd), nom(instr.rs), nom(instr.rt));
            case SLT:
                return String.format("slt %s, %s, %s", nom(instr.rd), nom(instr.rs), nom(instr.rt));
            case SLTU:
                return String.format("sltu %s, %s, %s", nom(instr.rd), nom(instr.rs), nom(instr.rt));
            case SLL:
                return String.format("sll %s, %s, %d", nom(instr.rd), nom(instr.rt), instr.shamt);
            case SRL:
                return String.format("srl %s, %s, %d", nom(instr.rd), nom(instr.rt), instr.shamt);
            case JR:
                return String.format("jr %s", nom(instr.rs));
            case SYSCALL:
                return "syscall";
            case MFHI:
                return String.format("mfhi %s", nom(instr.rd));
            case MFLO:
                return String.format("mflo %s", nom(instr.rd));
            case MULT:
                return String.format("mult %s, %s", nom(instr.rs), nom(instr.rt));
            case DIV:
                return String.format("div %s, %s", nom(instr.rs), nom(instr.rt));
            default:
                return "?funct?";
        }
    }

    private static String destino(int pc, int inmediato) {
        if (pc < 0) {
            return "+" + inmediato;
        }
        return Integer.toString(pc + 1 + inmediato);
    }

    // ------------------------------------------------------------------
    // Operacion (con valores actuales, calculada antes de ejecutar)
    // ------------------------------------------------------------------

    public static String operacion(int palabra, int pc, CPU cpu) {
        try {
            Registros reg = cpu.getRegistros();
            Instruccion instr = Decodificador.decodificar(palabra);

            if (instr.esNop()) {
                return "sin operacion";
            }
            if (instr.opcode == null) {
                return "operacion desconocida (opcode 0x"
                        + Integer.toHexString(Decodificador.opcode(palabra)) + ")";
            }
            if (instr.opcode == Opcode.HALT) {
                return "alto: la CPU se detiene (el programa termino)";
            }
            if (instr.opcode == Opcode.R) {
                return operacionR(instr, pc, cpu, reg);
            }

            int rs = instr.rs;
            int rt = instr.rt;
            int inm = instr.inmediato;
            switch (instr.opcode) {
                case ADDI:
                case ADDIU: {
                    int b = inm;
                    return String.format("%s = %s + %d = %d + %d = %d",
                            nom(rt), nom(rs), b, reg.leer(rs), b, (long) reg.leer(rs) + b);
                }
                case SLTI:
                case SLTIU: {
                    int b = inm;
                    int r = instr.opcode == Opcode.SLTI
                            ? (reg.leer(rs) < b ? 1 : 0)
                            : (Integer.compareUnsigned(reg.leer(rs), b) < 0 ? 1 : 0);
                    return String.format("%s = (%s < %d) = (%d < %d) = %d",
                            nom(rt), nom(rs), b, reg.leer(rs), b, r);
                }
                case ANDI:
                    return String.format("%s = %s AND %s = %s AND %s = %s",
                            nom(rt), nom(rs), h(instr.inmediatoSinSigno), h(reg.leer(rs)),
                            h(instr.inmediatoSinSigno), h(reg.leer(rs) & instr.inmediatoSinSigno));
                case ORI:
                    return String.format("%s = %s OR %s = %s OR %s = %s",
                            nom(rt), nom(rs), h(instr.inmediatoSinSigno), h(reg.leer(rs)),
                            h(instr.inmediatoSinSigno), h(reg.leer(rs) | instr.inmediatoSinSigno));
                case XORI:
                    return String.format("%s = %s XOR %s = %s XOR %s = %s",
                            nom(rt), nom(rs), h(instr.inmediatoSinSigno), h(reg.leer(rs)),
                            h(instr.inmediatoSinSigno), h(reg.leer(rs) ^ instr.inmediatoSinSigno));
                case LUI:
                    return String.format("%s = %s << 16 = %s", nom(rt), h(instr.inmediatoSinSigno),
                            h(instr.inmediatoSinSigno << 16));
                case LW: {
                    int dir = reg.leer(rs) + inm;
                    return String.format("%s = Mem[%s + %d] = Mem[%d] = %d",
                            nom(rt), nom(rs), inm, dir, cpu.getMemoria().leer(dir));
                }
                case SW: {
                    int dir = reg.leer(rs) + inm;
                    return String.format("Mem[%s + %d] = Mem[%d] = %s = %d",
                            nom(rs), inm, dir, nom(rt), reg.leer(rt));
                }
                case BEQ:
                    return rama(reg.leer(rs), reg.leer(rt), nom(rs), nom(rt), pc, inm, true);
                case BNE:
                    return rama(reg.leer(rs), reg.leer(rt), nom(rs), nom(rt), pc, inm, false);
                case J:
                    return String.format("PC = %d", instr.direccion);
                case JAL:
                    return String.format("$ra = %d; PC = %d", pc < 0 ? 0 : pc + 1, instr.direccion);
                default:
                    return "operacion desconocida (opcode " + instr.opcode.mnemonico + ")";
            }
        } catch (RuntimeException e) {
            return "no evaluable (" + e.getMessage() + ")";
        }
    }

    private static String operacionR(Instruccion instr, int pc, CPU cpu, Registros reg) {
        if (instr.funct == null) {
            return "operacion desconocida (funct 0x"
                    + Integer.toHexString(Decodificador.funct(instr.palabra)) + ")";
        }
        int rs = instr.rs;
        int rt = instr.rt;
        int rd = instr.rd;
        int shamt = instr.shamt;
        int a = reg.leer(rs);
        int b = reg.leer(rt);
        switch (instr.funct) {
            case ADD:
                return String.format("%s = %s + %s = %d + %d = %d",
                        nom(rd), nom(rs), nom(rt), a, b, (long) a + b);
            case ADDU:
                return String.format("%s = %s + %s = %d + %d = %d (sin signo)",
                        nom(rd), nom(rs), nom(rt), a, b, Integer.toUnsignedLong(a + b));
            case SUB:
                return String.format("%s = %s - %s = %d - %d = %d",
                        nom(rd), nom(rs), nom(rt), a, b, (long) a - b);
            case SUBU:
                return String.format("%s = %s - %s = %d - %d = %d (sin signo)",
                        nom(rd), nom(rs), nom(rt), a, b, Integer.toUnsignedLong(a - b));
            case AND:
                return String.format("%s = %s AND %s = %s AND %s = %s",
                        nom(rd), nom(rs), nom(rt), h(a), h(b), h(a & b));
            case OR:
                return String.format("%s = %s OR %s = %s OR %s = %s",
                        nom(rd), nom(rs), nom(rt), h(a), h(b), h(a | b));
            case XOR:
                return String.format("%s = %s XOR %s = %s XOR %s = %s",
                        nom(rd), nom(rs), nom(rt), h(a), h(b), h(a ^ b));
            case NOR:
                return String.format("%s = %s NOR %s = %s NOR %s = %s",
                        nom(rd), nom(rs), nom(rt), h(a), h(b), h(~(a | b)));
            case SLT:
                return String.format("%s = (%s < %s) = (%d < %d) = %d",
                        nom(rd), nom(rs), nom(rt), a, b, a < b ? 1 : 0);
            case SLTU:
                return String.format("%s = (%s < %s sin signo) = (%d < %d) = %d",
                        nom(rd), nom(rs), nom(rt),
                        Integer.toUnsignedLong(a), Integer.toUnsignedLong(b),
                        Integer.compareUnsigned(a, b) < 0 ? 1 : 0);
            case SLL:
                return String.format("%s = %s << %d = %d << %d = %d",
                        nom(rd), nom(rt), shamt, reg.leer(rt), shamt, reg.leer(rt) << (shamt & 0x1F));
            case SRL:
                return String.format("%s = %s >> %d = %d >> %d = %d",
                        nom(rd), nom(rt), shamt, reg.leer(rt), shamt, reg.leer(rt) >>> (shamt & 0x1F));
            case JR:
                return String.format("PC = %s = %d", nom(rs), a);
            case SYSCALL:
                return "syscall: " + servicio(reg.leer(2), reg.leer(4));
            case MFHI:
                return String.format("%s = HI = %d", nom(rd), (int) cpu.getHi());
            case MFLO:
                return String.format("%s = LO = %d", nom(rd), (int) cpu.getLo());
            case MULT: {
                long producto = (long) a * (long) b;
                return String.format("LO:HI = %s * %s = %d * %d -> LO = %d, HI = %d",
                        nom(rs), nom(rt), a, b, (int) producto, (int) (producto >>> 32));
            }
            case DIV:
                if (b == 0) {
                    return String.format("%s / %s = %d / 0 -> division por cero", nom(rs), nom(rt), a);
                }
                return String.format("LO:HI = %s / %s = %d / %d -> LO = %d, HI = %d",
                        nom(rs), nom(rt), a, b, a / b, a % b);
            default:
                return "operacion desconocida (funct " + instr.funct.mnemonico + ")";
        }
    }

    private static String rama(int a, int b, String na, String nb, int pc, int inmediato, boolean igual) {
        boolean condicion = igual ? a == b : a != b;
        String comparacion = String.format("%s %s %s (%d %s %d)", na,
                igual ? "==" : "!=", nb, a, igual ? "==" : "!=", b);
        if (pc < 0) {
            return comparacion + (condicion ? " -> se salta" : " -> no se salta");
        }
        if (condicion) {
            return String.format("%s -> se salta, PC = %d", comparacion, pc + 1 + inmediato);
        }
        return String.format("%s -> no se salta, PC = %d", comparacion, pc + 1);
    }

    private static String servicio(int v0, int a0) {
        ServicioSyscall s = ServicioSyscall.porCodigo(v0);
        if (s == null) {
            return "servicio " + v0;
        }
        switch (s) {
            case IMPRIMIR_ENTERO:
                return "imprimir entero ($a0 = " + a0 + ")";
            case IMPRIMIR_CADENA:
                return "imprimir cadena (direccion " + a0 + ")";
            case LEER_ENTERO:
                return "leer entero -> $v0";
            case IMPRIMIR_CARACTER:
                return "imprimir caracter (" + (char) (a0 & 0xFF) + ")";
            case TERMINAR:
                return "terminar programa";
            default:
                return s.descripcion;
        }
    }

    private static String nom(int indice) {
        return Registros.nombre(indice);
    }

    private static String h(int valor) {
        return String.format("0x%08X", valor);
    }
}
