package instruccion;

/**
 * Campo funct de las instrucciones de formato R (opcode 0x00).
 *
 * En el formato R el opcode vale 0 y la operacion concreta se elige con
 * el campo funct (6 bits menos significativos).
 */
public enum Funct {

    SLL("sll", 0x00),
    SRL("srl", 0x02),
    JR("jr", 0x08),
    SYSCALL("syscall", 0x0C),
    MFHI("mfhi", 0x10),
    MFLO("mflo", 0x12),
    MULT("mult", 0x18),
    DIV("div", 0x1A),
    ADD("add", 0x20),
    ADDU("addu", 0x21),
    SUB("sub", 0x22),
    SUBU("subu", 0x23),
    AND("and", 0x24),
    OR("or", 0x25),
    XOR("xor", 0x26),
    NOR("nor", 0x27),
    SLT("slt", 0x2A),
    SLTU("sltu", 0x2B);

    /** Mnemonico de la instruccion en lenguaje ensamblador. */
    public final String mnemonico;

    /** Codigo de 6 bits del campo funct. */
    public final int codigo;

    private Funct(String mnemonico, int codigo) {
        this.mnemonico = mnemonico;
        this.codigo = codigo;
    }

    /** Funct correspondiente al codigo dado, o null si no esta soportado. */
    public static Funct porCodigo(int codigo) {
        for (Funct f : values()) {
            if (f.codigo == codigo) {
                return f;
            }
        }
        return null;
    }
}
