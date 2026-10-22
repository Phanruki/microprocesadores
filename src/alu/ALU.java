package alu;

/**
 * Unidad logico aritmetica de 32 bits.
 * Las variantes con signo (ADD, SUB) detienen la ejecucion si el
 * resultado no cabe en 32 bits; las sin signo (ADDU, SUBU) envuelven
 * sin avisar, igual que en un MIPS real.
 */
public class ALU {

    public int operar(OperacionALU op, int a, int b, int shamt) {
        switch (op) {
            case ADD: {
                long r = (long) a + (long) b;
                if (r != (int) r) {
                    throw new ArithmeticException("Desbordamiento en la suma de " + a + " + " + b);
                }
                return (int) r;
            }
            case ADDU:
                return a + b;
            case SUB: {
                long r = (long) a - (long) b;
                if (r != (int) r) {
                    throw new ArithmeticException("Desbordamiento en la resta de " + a + " - " + b);
                }
                return (int) r;
            }
            case SUBU:
                return a - b;
            case AND:
                return a & b;
            case OR:
                return a | b;
            case XOR:
                return a ^ b;
            case NOR:
                return ~(a | b);
            case SLT:
                return (a < b) ? 1 : 0;
            case SLTU:
                return (Integer.compareUnsigned(a, b) < 0) ? 1 : 0;
            case SLL:
                return a << (shamt & 0x1F);
            case SRL:
                return a >>> (shamt & 0x1F);
            case LUI:
                return b << 16;
            case NOP:
                return 0;
            default:
                throw new UnsupportedOperationException("Operacion no soportada: " + op);
        }
    }
}
