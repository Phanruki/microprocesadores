package ejecucion;

import alu.OperacionALU;
import instruccion.Instruccion;
import instruccion.Opcode;

/**
 * Unidad de control del procesador.
 *
 * A partir de la instruccion decodificada (etapa ID) genera las senales
 * de control que indican que debe hacer el camino de datos en la etapa
 * EX: que operacion hace la ALU, si se escribe registro, si se accede
 * a memoria, si hay rama/salto, si se detiene la CPU, etc.
 */
public class UnidadControl {

    /**
     * Genera las senales de control de la instruccion dada.
     *
     * @throws UnsupportedOperationException si la instruccion no esta soportada
     */
    public SenalesControl decodificar(Instruccion instr) {
        if (instr.opcode == null) {
            throw new UnsupportedOperationException(
                    "Opcode no soportado: 0x" + Integer.toHexString(instr.palabra >>> 26));
        }
        if (instr.opcode == Opcode.R) {
            return senalesTipoR(instr);
        }

        switch (instr.opcode) {
            case ADDI:
                return SenalesControl.alu(OperacionALU.ADD).inmediato().escribeRegistro().build();
            case ADDIU:
                return SenalesControl.alu(OperacionALU.ADDU).inmediato().escribeRegistro().build();
            case SLTI:
                return SenalesControl.alu(OperacionALU.SLT).inmediato().escribeRegistro().build();
            case SLTIU:
                return SenalesControl.alu(OperacionALU.SLTU).inmediato().escribeRegistro().build();
            case ANDI:
                return SenalesControl.alu(OperacionALU.AND).inmediatoSinSigno().escribeRegistro().build();
            case ORI:
                return SenalesControl.alu(OperacionALU.OR).inmediatoSinSigno().escribeRegistro().build();
            case XORI:
                return SenalesControl.alu(OperacionALU.XOR).inmediatoSinSigno().escribeRegistro().build();
            case LUI:
                return SenalesControl.alu(OperacionALU.LUI).inmediatoSinSigno().escribeRegistro().build();
            case LW:
                return SenalesControl.alu(OperacionALU.ADD).inmediato()
                        .escribeRegistro().leeMemoria().build();
            case SW:
                return SenalesControl.alu(OperacionALU.ADD).inmediato()
                        .escribeMemoria().build();
            case BEQ:
            case BNE:
                return SenalesControl.alu(OperacionALU.NOP).rama().build();
            case J:
                return SenalesControl.alu(OperacionALU.NOP).salto().build();
            case JAL:
                return SenalesControl.alu(OperacionALU.NOP).escribeRegistro().salto().build();
            case HALT:
                return SenalesControl.alu(OperacionALU.NOP).halt().build();
            default:
                throw new UnsupportedOperationException(
                        "Opcode sin senales de control: " + instr.opcode.mnemonico);
        }
    }

    /** Senales de las instrucciones de formato R (segun el campo funct). */
    private SenalesControl senalesTipoR(Instruccion instr) {
        if (instr.funct == null) {
            throw new UnsupportedOperationException(
                    "Funct no soportado: 0x" + Integer.toHexString(instr.palabra & 0x3F));
        }
        switch (instr.funct) {
            case ADD:
                return SenalesControl.alu(OperacionALU.ADD).escribeRegistro().build();
            case ADDU:
                return SenalesControl.alu(OperacionALU.ADDU).escribeRegistro().build();
            case SUB:
                return SenalesControl.alu(OperacionALU.SUB).escribeRegistro().build();
            case SUBU:
                return SenalesControl.alu(OperacionALU.SUBU).escribeRegistro().build();
            case AND:
                return SenalesControl.alu(OperacionALU.AND).escribeRegistro().build();
            case OR:
                return SenalesControl.alu(OperacionALU.OR).escribeRegistro().build();
            case XOR:
                return SenalesControl.alu(OperacionALU.XOR).escribeRegistro().build();
            case NOR:
                return SenalesControl.alu(OperacionALU.NOR).escribeRegistro().build();
            case SLT:
                return SenalesControl.alu(OperacionALU.SLT).escribeRegistro().build();
            case SLTU:
                return SenalesControl.alu(OperacionALU.SLTU).escribeRegistro().build();
            case SLL:
                return SenalesControl.alu(OperacionALU.SLL).escribeRegistro().build();
            case SRL:
                return SenalesControl.alu(OperacionALU.SRL).escribeRegistro().build();
            case JR:
                return SenalesControl.alu(OperacionALU.NOP).saltoRegistro().build();
            case MULT:
            case DIV:
                return SenalesControl.alu(OperacionALU.NOP).hiLo().build();
            case MFHI:
                return SenalesControl.alu(OperacionALU.NOP).escribeRegistro().mfhi().build();
            case MFLO:
                return SenalesControl.alu(OperacionALU.NOP).escribeRegistro().mflo().build();
            case SYSCALL:
                return SenalesControl.alu(OperacionALU.NOP).syscall().build();
            default:
                throw new UnsupportedOperationException("Funct sin senales: " + instr.funct.mnemonico);
        }
    }
}
