package ejecucion;

import cpu.CPU;
import instruccion.Funct;
import instruccion.Instruccion;
import instruccion.Opcode;
import memoria.Registros;

/**
 * Ejecutor de instrucciones (etapa EX del ciclo).
 *
 * Aplica las senales de la UnidadControl sobre el camino de datos:
 * lee los registros, usa la ALU, resuelve ramas y saltos, actualiza
 * HI:LO y deja preparados los accesos a memoria y la escritura de
 * resultados para las etapas MEM y WB.
 */
public class EjecutorInstrucciones {

    /** Registro $ra (31): destino de la llamada jal. */
    public static final int REGISTRO_RA = 31;

    private final CPU cpu;

    public EjecutorInstrucciones(CPU cpu) {
        this.cpu = cpu;
    }

    /** Ejecuta la instruccion dada aplicando sus senales de control. */
    public void ejecutar(Instruccion instr, SenalesControl senales) {
        // Instrucciones que no usan el camino de datos de la ALU
        if (senales.esHalt) {
            cpu.detener();
            return;
        }
        if (senales.esSyscall) {
            cpu.syscall();
            return;
        }

        Registros registros = cpu.getRegistros();
        int a = registros.leer(instr.rs);
        int b;
        if (senales.usaInmediato) {
            b = senales.inmediatoSinSigno ? instr.inmediatoSinSigno : instr.inmediato;
        } else {
            b = registros.leer(instr.rt);
        }

        if (senales.actualizaHiLo) {
            if (instr.funct == Funct.DIV) {
                if (b == 0) {
                    throw new ArithmeticException("Division por cero");
                }
                cpu.fijarHiLo(a % b, a / b); // HI = resto, LO = cociente
            } else {
                long producto = (long) a * (long) b;
                cpu.fijarHiLo((int) (producto >>> 32), (int) producto);
            }
            return;
        }
        if (senales.transfiereHi) {
            cpu.programarEscritura(instr.rd, (int) cpu.getHi());
            return;
        }
        if (senales.transfiereLo) {
            cpu.programarEscritura(instr.rd, (int) cpu.getLo());
            return;
        }
        if (senales.esRama) {
            boolean condicion = instr.opcode == Opcode.BEQ ? a == b : a != b;
            if (condicion) {
                cpu.saltarA(cpu.getPc().get() + instr.inmediato);
            }
            return;
        }
        if (senales.esSalto) {
            if (instr.opcode == Opcode.JAL) {
                // $ra guarda la direccion de la siguiente instruccion
                cpu.programarEscritura(REGISTRO_RA, cpu.getPc().get());
            }
            cpu.saltarA(instr.direccion);
            return;
        }
        if (senales.esSaltoRegistro) {
            cpu.saltarA(registros.leer(instr.rs));
            return;
        }
        if (senales.leeMemoria || senales.escribeMemoria) {
            int direccion = a + instr.inmediato;
            if (senales.leeMemoria) {
                cpu.programarLecturaMemoria(instr.rt, direccion);
            } else {
                cpu.programarEscrituraMemoria(direccion, registros.leer(instr.rt));
            }
            return;
        }

        // Operacion aritmetica / logica en la ALU; el resultado se escribe en WB
        int resultado = cpu.getAlu().operar(senales.operacionAlu, a, b, instr.shamt);
        cpu.programarEscritura(registroDestino(instr), resultado);
    }

    /** Registro destino: rd en formato R, rt en formato I. */
    private int registroDestino(Instruccion instr) {
        return instr.opcode == Opcode.R ? instr.rd : instr.rt;
    }
}
