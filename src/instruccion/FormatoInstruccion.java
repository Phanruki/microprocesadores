package instruccion;

/**
 * Formatos de instruccion de MIPS.
 *
 * R: opcode, rs, rt, rd, shamt y funct (operaciones registro-registro).
 * I: opcode, rs, rt e inmediato de 16 bits (constantes, memoria, ramas).
 * J: opcode y direccion de salto de 26 bits.
 */
public enum FormatoInstruccion {
    R,
    I,
    J
}
