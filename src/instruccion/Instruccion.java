package instruccion;

/**
 * Instruccion MIPS: una palabra de 32 bits.
 *
 * Por un lado codifica las palabras binarias (codificarR/codificarI/
 * codificarJ) y por otro representa la instruccion ya decodificada con
 * sus campos separados. Las instrucciones se construyen con
 * Decodificador.decodificar(palabra).
 */
public final class Instruccion {

    /** Palabra 0x00000000: sll $0, $0, 0 = NOP (sin operacion). */
    public static final int PALABRA_NOP = 0;

    /** Palabra binaria completa de 32 bits. */
    public final int palabra;

    /** Formato de la instruccion (R, I o J). */
    public final FormatoInstruccion formato;

    /** Opcode de la instruccion (null si no esta soportado). */
    public final Opcode opcode;

    /** Campo funct (solo formato R; null en I y J). */
    public final Funct funct;

    public final int rs;
    public final int rt;
    public final int rd;
    public final int shamt;

    /** Inmediato de 16 bits con extension de signo a 32 bits. */
    public final int inmediato;

    /** Inmediato de 16 bits sin extension de signo. */
    public final int inmediatoSinSigno;

    /** Direccion destino (solo formato J). */
    public final int direccion;

    Instruccion(int palabra, FormatoInstruccion formato, Opcode opcode, Funct funct,
                int rs, int rt, int rd, int shamt,
                int inmediato, int inmediatoSinSigno, int direccion) {
        this.palabra = palabra;
        this.formato = formato;
        this.opcode = opcode;
        this.funct = funct;
        this.rs = rs;
        this.rt = rt;
        this.rd = rd;
        this.shamt = shamt;
        this.inmediato = inmediato;
        this.inmediatoSinSigno = inmediatoSinSigno;
        this.direccion = direccion;
    }

    /** true si la palabra es la NOP (todo cero). */
    public boolean esNop() {
        return palabra == PALABRA_NOP;
    }

    /** true si la instruccion detiene la CPU (HALT). */
    public boolean esHalt() {
        return opcode == Opcode.HALT;
    }

    // ------------------------------------------------------------------
    // Codificacion (creacion de la palabra binaria)
    // ------------------------------------------------------------------

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

    /** Palabra binaria de la instruccion HALT (fin del programa). */
    public static int codificarHalt() {
        return codificarI(Opcode.HALT.codigo, 0, 0, 0);
    }
}
