package instruccion;

/**
 * Codigos de operacion (opcode) de las instrucciones soportadas.
 *
 * El opcode ocupa los 6 bits mas significativos de la palabra de 32 bits.
 * HALT (0x3F) es una instruccion propia de este emulador: le indica a la
 * CPU que el programa termino (la CPU no muere sola: solo se detiene con
 * HALT o con syscall 10).
 */
public enum Opcode {

    R("tipo R", 0x00, FormatoInstruccion.R),
    J("j", 0x02, FormatoInstruccion.J),
    JAL("jal", 0x03, FormatoInstruccion.J),
    BEQ("beq", 0x04, FormatoInstruccion.I),
    BNE("bne", 0x05, FormatoInstruccion.I),
    ADDI("addi", 0x08, FormatoInstruccion.I),
    ADDIU("addiu", 0x09, FormatoInstruccion.I),
    SLTI("slti", 0x0A, FormatoInstruccion.I),
    SLTIU("sltiu", 0x0B, FormatoInstruccion.I),
    ANDI("andi", 0x0C, FormatoInstruccion.I),
    ORI("ori", 0x0D, FormatoInstruccion.I),
    XORI("xori", 0x0E, FormatoInstruccion.I),
    LUI("lui", 0x0F, FormatoInstruccion.I),
    LW("lw", 0x23, FormatoInstruccion.I),
    SW("sw", 0x2B, FormatoInstruccion.I),
    HALT("halt", 0x3F, FormatoInstruccion.I);

    /** Mnemonico de la instruccion en lenguaje ensamblador. */
    public final String mnemonico;

    /** Codigo de 6 bits del opcode. */
    public final int codigo;

    /** Formato en el que se codifica la instruccion. */
    public final FormatoInstruccion formato;

    private Opcode(String mnemonico, int codigo, FormatoInstruccion formato) {
        this.mnemonico = mnemonico;
        this.codigo = codigo;
        this.formato = formato;
    }

    /** Opcode correspondiente al codigo dado, o null si no esta soportado. */
    public static Opcode porCodigo(int codigo) {
        for (Opcode op : values()) {
            if (op.codigo == codigo) {
                return op;
            }
        }
        return null;
    }
}
