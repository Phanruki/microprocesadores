package instruccion;

/**
 * Decodificador de instrucciones.
 *
 * Separa los campos de una palabra de 32 bits (etapa ID del ciclo) y
 * construye el objeto Instruccion correspondiente.
 */
public final class Decodificador {

    private Decodificador() {
    }

    /** Decodifica una palabra de 32 bits en su Instruccion. */
    public static Instruccion decodificar(int palabra) {
        int codigoOpcode = opcode(palabra);
        Opcode op = Opcode.porCodigo(codigoOpcode);
        FormatoInstruccion formato = op != null ? op.formato : FormatoInstruccion.I;
        Funct funct = (op == Opcode.R) ? Funct.porCodigo(funct(palabra)) : null;
        int inmediato = inmediatoSinSigno(palabra);
        return new Instruccion(
                palabra,
                formato,
                op,
                funct,
                rs(palabra),
                rt(palabra),
                rd(palabra),
                shamt(palabra),
                extensionSigno16(inmediato),
                inmediato,
                (op == Opcode.J || op == Opcode.JAL) ? direccionJ(palabra) : 0);
    }

    // ------------------------------------------------------------------
    // Extraccion de campos (bits de la palabra de 32 bits)
    //
    //  31..26   25..21   20..16   15..11   10..6    5..0
    //  opcode   rs       rt       rd       shamt    funct
    //  opcode   rs       rt       inmediato de 16 bits      (formato I)
    //  opcode   direccion de 26 bits                        (formato J)
    // ------------------------------------------------------------------

    /** Bits 31..26: codigo de operacion. */
    public static int opcode(int palabra) {
        return (palabra >>> 26) & 0x3F;
    }

    /** Bits 25..21: primer registro operando. */
    public static int rs(int palabra) {
        return (palabra >>> 21) & 0x1F;
    }

    /** Bits 20..16: segundo registro operando / registro destino (I). */
    public static int rt(int palabra) {
        return (palabra >>> 16) & 0x1F;
    }

    /** Bits 15..11: registro destino (R) / parte del inmediato (I). */
    public static int rd(int palabra) {
        return (palabra >>> 11) & 0x1F;
    }

    /** Bits 10..6: cantidad de desplazamiento (shamt). */
    public static int shamt(int palabra) {
        return (palabra >>> 6) & 0x1F;
    }

    /** Bits 5..0: campo funct (solo formato R). */
    public static int funct(int palabra) {
        return palabra & 0x3F;
    }

    /** Bits 15..0: inmediato sin extension de signo. */
    public static int inmediatoSinSigno(int palabra) {
        return palabra & 0xFFFF;
    }

    /** Extiende a 32 bits el signo de un inmediato de 16 bits. */
    public static int extensionSigno16(int inmediato) {
        return (short) inmediato;
    }

    /** Bits 25..0: direccion destino de un salto (formato J). */
    public static int direccionJ(int palabra) {
        return palabra & 0x03FFFFFF;
    }
}
