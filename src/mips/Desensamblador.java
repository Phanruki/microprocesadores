package mips;

import almacenamiento.Registros;

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

    public static String mnemonico(int instr) {
        return mnemonico(instr, -1);
    }

    /**
     * @param pc direccion (indice de palabra) de la instruccion, o -1 si se
     *           desconoce; se usa para resolver el destino de las ramas.
     */
    public static String mnemonico(int instr, int pc) {
        int opcode = Instruccion.opcode(instr);
        if (opcode == Instruccion.OP_R) {
            return mnemonicoR(instr);
        }
        int rs = Instruccion.rs(instr);
        int rt = Instruccion.rt(instr);
        int imm = Instruccion.inmediatoSinSigno(instr);
        switch (opcode) {
            case Instruccion.OP_ADDI:
                return String.format("addi %s, %s, %d", nom(rt), nom(rs), Instruccion.extensionSigno16(imm));
            case Instruccion.OP_ADDIU:
                return String.format("addiu %s, %s, %d", nom(rt), nom(rs), Instruccion.extensionSigno16(imm));
            case Instruccion.OP_SLTI:
                return String.format("slti %s, %s, %d", nom(rt), nom(rs), Instruccion.extensionSigno16(imm));
            case Instruccion.OP_SLTIU:
                return String.format("sltiu %s, %s, %d", nom(rt), nom(rs), Instruccion.extensionSigno16(imm));
            case Instruccion.OP_ANDI:
                return String.format("andi %s, %s, 0x%04X", nom(rt), nom(rs), imm);
            case Instruccion.OP_ORI:
                return String.format("ori %s, %s, 0x%04X", nom(rt), nom(rs), imm);
            case Instruccion.OP_XORI:
                return String.format("xori %s, %s, 0x%04X", nom(rt), nom(rs), imm);
            case Instruccion.OP_LUI:
                return String.format("lui %s, 0x%04X", nom(rt), imm);
            case Instruccion.OP_LW:
                return String.format("lw %s, %d(%s)", nom(rt), Instruccion.extensionSigno16(imm), nom(rs));
            case Instruccion.OP_SW:
                return String.format("sw %s, %d(%s)", nom(rt), Instruccion.extensionSigno16(imm), nom(rs));
            case Instruccion.OP_BEQ:
                return String.format("beq %s, %s, %s", nom(rs), nom(rt), destino(pc, imm));
            case Instruccion.OP_BNE:
                return String.format("bne %s, %s, %s", nom(rs), nom(rt), destino(pc, imm));
            case Instruccion.OP_J:
                return String.format("j %d", Instruccion.direccionJ(instr));
            case Instruccion.OP_JAL:
                return String.format("jal %d", Instruccion.direccionJ(instr));
            default:
                return String.format("?opcode 0x%02X?", opcode);
        }
    }

    private static String mnemonicoR(int instr) {
        if (instr == 0) {
            return "nop";
        }
        int rs = Instruccion.rs(instr);
        int rt = Instruccion.rt(instr);
        int rd = Instruccion.rd(instr);
        int shamt = Instruccion.shamt(instr);
        int funct = Instruccion.funct(instr);
        switch (funct) {
            case Instruccion.FUNCT_ADD:
                return String.format("add %s, %s, %s", nom(rd), nom(rs), nom(rt));
            case Instruccion.FUNCT_ADDU:
                return String.format("addu %s, %s, %s", nom(rd), nom(rs), nom(rt));
            case Instruccion.FUNCT_SUB:
                return String.format("sub %s, %s, %s", nom(rd), nom(rs), nom(rt));
            case Instruccion.FUNCT_SUBU:
                return String.format("subu %s, %s, %s", nom(rd), nom(rs), nom(rt));
            case Instruccion.FUNCT_AND:
                return String.format("and %s, %s, %s", nom(rd), nom(rs), nom(rt));
            case Instruccion.FUNCT_OR:
                return String.format("or %s, %s, %s", nom(rd), nom(rs), nom(rt));
            case Instruccion.FUNCT_XOR:
                return String.format("xor %s, %s, %s", nom(rd), nom(rs), nom(rt));
            case Instruccion.FUNCT_NOR:
                return String.format("nor %s, %s, %s", nom(rd), nom(rs), nom(rt));
            case Instruccion.FUNCT_SLT:
                return String.format("slt %s, %s, %s", nom(rd), nom(rs), nom(rt));
            case Instruccion.FUNCT_SLTU:
                return String.format("sltu %s, %s, %s", nom(rd), nom(rs), nom(rt));
            case Instruccion.FUNCT_SLL:
                return String.format("sll %s, %s, %d", nom(rd), nom(rt), shamt);
            case Instruccion.FUNCT_SRL:
                return String.format("srl %s, %s, %d", nom(rd), nom(rt), shamt);
            case Instruccion.FUNCT_JR:
                return String.format("jr %s", nom(rs));
            case Instruccion.FUNCT_SYSCALL:
                return "syscall";
            case Instruccion.FUNCT_MFHI:
                return String.format("mfhi %s", nom(rd));
            case Instruccion.FUNCT_MFLO:
                return String.format("mflo %s", nom(rd));
            case Instruccion.FUNCT_MULT:
                return String.format("mult %s, %s", nom(rs), nom(rt));
            case Instruccion.FUNCT_DIV:
                return String.format("div %s, %s", nom(rs), nom(rt));
            default:
                return String.format("?funct 0x%02X?", funct);
        }
    }

    private static String destino(int pc, int imm) {
        int desplazamiento = Instruccion.extensionSigno16(imm);
        if (pc < 0) {
            return "+" + desplazamiento;
        }
        return Integer.toString(pc + 1 + desplazamiento);
    }

    // ------------------------------------------------------------------
    // Operacion (con valores actuales, calculada antes de ejecutar)
    // ------------------------------------------------------------------

    public static String operacion(int instr, int pc, ProcesadorMIPS cpu) {
        try {
            Registros reg = cpu.getRegistros();
            int opcode = Instruccion.opcode(instr);
            if (opcode == Instruccion.OP_R) {
                return operacionR(instr, pc, cpu, reg);
            }
            int rs = Instruccion.rs(instr);
            int rt = Instruccion.rt(instr);
            int imm = Instruccion.inmediatoSinSigno(instr);
            switch (opcode) {
                case Instruccion.OP_ADDI:
                case Instruccion.OP_ADDIU: {
                    int b = Instruccion.extensionSigno16(imm);
                    return String.format("%s = %s + %d = %d + %d = %d",
                            nom(rt), nom(rs), b, reg.leer(rs), b, (long) reg.leer(rs) + b);
                }
                case Instruccion.OP_SLTI:
                case Instruccion.OP_SLTIU: {
                    int b = Instruccion.extensionSigno16(imm);
                    int r = opcode == Instruccion.OP_SLTI
                            ? (reg.leer(rs) < b ? 1 : 0)
                            : (Integer.compareUnsigned(reg.leer(rs), b) < 0 ? 1 : 0);
                    return String.format("%s = (%s < %d) = (%d < %d) = %d",
                            nom(rt), nom(rs), b, reg.leer(rs), b, r);
                }
                case Instruccion.OP_ANDI:
                    return String.format("%s = %s AND %s = %s AND %s = %s",
                            nom(rt), nom(rs), h(imm), h(reg.leer(rs)), h(imm), h(reg.leer(rs) & imm));
                case Instruccion.OP_ORI:
                    return String.format("%s = %s OR %s = %s OR %s = %s",
                            nom(rt), nom(rs), h(imm), h(reg.leer(rs)), h(imm), h(reg.leer(rs) | imm));
                case Instruccion.OP_XORI:
                    return String.format("%s = %s XOR %s = %s XOR %s = %s",
                            nom(rt), nom(rs), h(imm), h(reg.leer(rs)), h(imm), h(reg.leer(rs) ^ imm));
                case Instruccion.OP_LUI:
                    return String.format("%s = %s << 16 = %s", nom(rt), h(imm), h(imm << 16));
                case Instruccion.OP_LW: {
                    int dir = reg.leer(rs) + Instruccion.extensionSigno16(imm);
                    return String.format("%s = Mem[%s + %d] = Mem[%d] = %d",
                            nom(rt), nom(rs), Instruccion.extensionSigno16(imm),
                            dir, cpu.getMemoria().leer(dir));
                }
                case Instruccion.OP_SW: {
                    int dir = reg.leer(rs) + Instruccion.extensionSigno16(imm);
                    return String.format("Mem[%s + %d] = Mem[%d] = %s = %d",
                            nom(rs), Instruccion.extensionSigno16(imm),
                            dir, nom(rt), reg.leer(rt));
                }
                case Instruccion.OP_BEQ:
                    return rama(reg.leer(rs), reg.leer(rt), nom(rs), nom(rt), pc, imm, true);
                case Instruccion.OP_BNE:
                    return rama(reg.leer(rs), reg.leer(rt), nom(rs), nom(rt), pc, imm, false);
                case Instruccion.OP_J:
                    return String.format("PC = %d", Instruccion.direccionJ(instr));
                case Instruccion.OP_JAL:
                    return String.format("$ra = %d; PC = %d",
                            pc < 0 ? 0 : pc + 1, Instruccion.direccionJ(instr));
                default:
                    return "operacion desconocida (opcode 0x" + Integer.toHexString(opcode) + ")";
            }
        } catch (RuntimeException e) {
            return "no evaluable (" + e.getMessage() + ")";
        }
    }

    private static String operacionR(int instr, int pc, ProcesadorMIPS cpu, Registros reg) {
        if (instr == 0) {
            return "sin operacion";
        }
        int rs = Instruccion.rs(instr);
        int rt = Instruccion.rt(instr);
        int rd = Instruccion.rd(instr);
        int shamt = Instruccion.shamt(instr);
        int funct = Instruccion.funct(instr);
        int a = reg.leer(rs);
        int b = reg.leer(rt);
        switch (funct) {
            case Instruccion.FUNCT_ADD:
                return String.format("%s = %s + %s = %d + %d = %d",
                        nom(rd), nom(rs), nom(rt), a, b, (long) a + b);
            case Instruccion.FUNCT_ADDU:
                return String.format("%s = %s + %s = %d + %d = %d (sin signo)",
                        nom(rd), nom(rs), nom(rt), a, b, Integer.toUnsignedLong(a + b));
            case Instruccion.FUNCT_SUB:
                return String.format("%s = %s - %s = %d - %d = %d",
                        nom(rd), nom(rs), nom(rt), a, b, (long) a - b);
            case Instruccion.FUNCT_SUBU:
                return String.format("%s = %s - %s = %d - %d = %d (sin signo)",
                        nom(rd), nom(rs), nom(rt), a, b, Integer.toUnsignedLong(a - b));
            case Instruccion.FUNCT_AND:
                return String.format("%s = %s AND %s = %s AND %s = %s",
                        nom(rd), nom(rs), nom(rt), h(a), h(b), h(a & b));
            case Instruccion.FUNCT_OR:
                return String.format("%s = %s OR %s = %s OR %s = %s",
                        nom(rd), nom(rs), nom(rt), h(a), h(b), h(a | b));
            case Instruccion.FUNCT_XOR:
                return String.format("%s = %s XOR %s = %s XOR %s = %s",
                        nom(rd), nom(rs), nom(rt), h(a), h(b), h(a ^ b));
            case Instruccion.FUNCT_NOR:
                return String.format("%s = %s NOR %s = %s NOR %s = %s",
                        nom(rd), nom(rs), nom(rt), h(a), h(b), h(~(a | b)));
            case Instruccion.FUNCT_SLT:
                return String.format("%s = (%s < %s) = (%d < %d) = %d",
                        nom(rd), nom(rs), nom(rt), a, b, a < b ? 1 : 0);
            case Instruccion.FUNCT_SLTU:
                return String.format("%s = (%s < %s sin signo) = (%d < %d) = %d",
                        nom(rd), nom(rs), nom(rt),
                        Integer.toUnsignedLong(a), Integer.toUnsignedLong(b),
                        Integer.compareUnsigned(a, b) < 0 ? 1 : 0);
            case Instruccion.FUNCT_SLL:
                return String.format("%s = %s << %d = %d << %d = %d",
                        nom(rd), nom(rt), shamt, reg.leer(rt), shamt, reg.leer(rt) << (shamt & 0x1F));
            case Instruccion.FUNCT_SRL:
                return String.format("%s = %s >> %d = %d >> %d = %d",
                        nom(rd), nom(rt), shamt, reg.leer(rt), shamt, reg.leer(rt) >>> (shamt & 0x1F));
            case Instruccion.FUNCT_JR:
                return String.format("PC = %s = %d", nom(rs), a);
            case Instruccion.FUNCT_SYSCALL:
                return "syscall: " + servicio(reg.leer(2), reg.leer(4));
            case Instruccion.FUNCT_MFHI:
                return String.format("%s = HI = %d", nom(rd), (int) cpu.getHi());
            case Instruccion.FUNCT_MFLO:
                return String.format("%s = LO = %d", nom(rd), (int) cpu.getLo());
            case Instruccion.FUNCT_MULT: {
                long producto = (long) a * (long) b;
                return String.format("LO:HI = %s * %s = %d * %d -> LO = %d, HI = %d",
                        nom(rs), nom(rt), a, b, (int) producto, (int) (producto >>> 32));
            }
            case Instruccion.FUNCT_DIV:
                if (b == 0) {
                    return String.format("%s / %s = %d / 0 -> division por cero", nom(rs), nom(rt), a);
                }
                return String.format("LO:HI = %s / %s = %d / %d -> LO = %d, HI = %d",
                        nom(rs), nom(rt), a, b, a / b, a % b);
            default:
                return "operacion desconocida (funct 0x" + Integer.toHexString(funct) + ")";
        }
    }

    private static String rama(int a, int b, String na, String nb, int pc, int imm, boolean igual) {
        int desplazamiento = Instruccion.extensionSigno16(imm);
        boolean condicion = igual ? a == b : a != b;
        String comparacion = String.format("%s %s %s (%d %s %d)", na,
                igual ? "==" : "!=", nb, a, igual ? "==" : "!=", b);
        if (pc < 0) {
            return comparacion + (condicion ? " -> se salta" : " -> no se salta");
        }
        if (condicion) {
            return String.format("%s -> se salta, PC = %d", comparacion, pc + 1 + desplazamiento);
        }
        return String.format("%s -> no se salta, PC = %d", comparacion, pc + 1);
    }

    private static String servicio(int v0, int a0) {
        switch (v0) {
            case ProcesadorMIPS.SYSCALL_PRINT_INT:
                return "imprimir entero ($a0 = " + a0 + ")";
            case ProcesadorMIPS.SYSCALL_PRINT_STRING:
                return "imprimir cadena (direccion " + a0 + ")";
            case ProcesadorMIPS.SYSCALL_READ_INT:
                return "leer entero -> $v0";
            case ProcesadorMIPS.SYSCALL_PRINT_CHAR:
                return "imprimir caracter (" + (char) (a0 & 0xFF) + ")";
            case ProcesadorMIPS.SYSCALL_EXIT:
                return "terminar programa";
            default:
                return "servicio " + v0;
        }
    }

    private static String nom(int indice) {
        return Registros.nombre(indice);
    }

    private static String h(int valor) {
        return String.format("0x%08X", valor);
    }
}
