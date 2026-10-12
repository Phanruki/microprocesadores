package mips;

/**
 * Instrucciones MIPS codificadas en 32 bits.
 * Proporciona codificacion y decodificacion de los tres formatos:
 * R, I y J.
 */
public final class Instruccion {

    private Instruccion() {
    }

    // Opcodes (formatos I y J)
    public static final int OP_R = 0x00;
    public static final int OP_J = 0x02;
    public static final int OP_JAL = 0x03;
    public static final int OP_BEQ = 0x04;
    public static final int OP_BNE = 0x05;
    public static final int OP_ADDI = 0x08;
    public static final int OP_ADDIU = 0x09;
    public static final int OP_SLTI = 0x0A;
    public static final int OP_SLTIU = 0x0B;
    public static final int OP_ANDI = 0x0C;
    public static final int OP_ORI = 0x0D;
    public static final int OP_XORI = 0x0E;
    public static final int OP_LUI = 0x0F;
    public static final int OP_LW = 0x23;
    public static final int OP_SW = 0x2B;

    // Funct (formato R)
    public static final int FUNCT_SLL = 0x00;
    public static final int FUNCT_SRL = 0x02;
    public static final int FUNCT_JR = 0x08;
    public static final int FUNCT_SYSCALL = 0x0C;
    public static final int FUNCT_MFHI = 0x10;
    public static final int FUNCT_MFLO = 0x12;
    public static final int FUNCT_MULT = 0x18;
    public static final int FUNCT_DIV = 0x1A;
    public static final int FUNCT_ADD = 0x20;
    public static final int FUNCT_ADDU = 0x21;
    public static final int FUNCT_SUB = 0x22;
    public static final int FUNCT_SUBU = 0x23;
    public static final int FUNCT_AND = 0x24;
    public static final int FUNCT_OR = 0x25;
    public static final int FUNCT_XOR = 0x26;
    public static final int FUNCT_NOR = 0x27;
    public static final int FUNCT_SLT = 0x2A;
    public static final int FUNCT_SLTU = 0x2B;

    public static int codificarR(int opcode, int rs, int rt, int rd, int shamt, int funct) {
        return ((opcode & 0x3F) << 26)
                | ((rs & 0x1F) << 21)
                | ((rt & 0x1F) << 16)
                | ((rd & 0x1F) << 11)
                | ((shamt & 0x1F) << 6)
                | (funct & 0x3F);
    }

    public static int codificarI(int opcode, int rs, int rt, int inmediato) {
        return ((opcode & 0x3F) << 26)
                | ((rs & 0x1F) << 21)
                | ((rt & 0x1F) << 16)
                | (inmediato & 0xFFFF);
    }

    public static int codificarJ(int opcode, int direccion) {
        return ((opcode & 0x3F) << 26) | (direccion & 0x03FFFFFF);
    }

    public static int opcode(int instr) {
        return (instr >>> 26) & 0x3F;
    }

    public static int rs(int instr) {
        return (instr >>> 21) & 0x1F;
    }

    public static int rt(int instr) {
        return (instr >>> 16) & 0x1F;
    }

    public static int rd(int instr) {
        return (instr >>> 11) & 0x1F;
    }

    public static int shamt(int instr) {
        return (instr >>> 6) & 0x1F;
    }

    public static int funct(int instr) {
        return instr & 0x3F;
    }

    public static int inmediatoSinSigno(int instr) {
        return instr & 0xFFFF;
    }

    public static int extensionSigno16(int inmediato) {
        return (short) inmediato;
    }

    public static int direccionJ(int instr) {
        return instr & 0x03FFFFFF;
    }

    public static boolean esTipoR(int instr) {
        return opcode(instr) == OP_R;
    }
}
