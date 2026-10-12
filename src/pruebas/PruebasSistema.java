package pruebas;

import mips.MemoriaSistema;
import mips.ProcesadorMIPS;
import mips.ProgramaBuilder;
import mips.ProgramaBuilder.Programa;
import programas.Factorial;
import programas.Integral;
import programas.Perceptron;
import programas.Promedio;
import programas.Sumatoria;

/**
 * Pruebas de sistema: ejecutan los programas de usuario final completos
 * y comparan la salida observable (caja negra).
 * Ejecucion: java -cp out pruebas.PruebasSistema
 */
public class PruebasSistema {

    public static void main(String[] args) {
        System.out.println("=== PRUEBAS DE SISTEMA - Simulador MIPS ===");
        System.out.println();

        probarPrograma("sumatoria", Sumatoria.construir(), "Sumatoria de 3 a 10 = 52\n");
        probarPrograma("factorial", Factorial.construir(), "Factorial de 7 = 5040\n");
        probarPrograma("integral", Integral.construir(),
                "Integral de x^2 dx desde x=1 hasta x=5 = 30\n");
        probarPrograma("promedio", Promedio.construir(),
                "Vector: 4 8 15 16 23 \nPromedio = 13\n");
        probarPrograma("perceptron", Perceptron.construir(), "z = 3  y = 1\n");

        probarProgramasCompletosFinalizan();

        int fallidas = MiniTest.resumen("RESUMEN PRUEBAS DE SISTEMA");
        System.exit(fallidas == 0 ? 0 : 1);
    }

    private static ProcesadorMIPS construirYcargar(ProgramaBuilder pb, String nombre) {
        Programa prog = pb.generar();
        MiniTest.assertTrue(nombre + ": se construye sin error", prog.codigo.length > 0);
        MiniTest.assertTrue(nombre + ": cabe en la ROM (<=100 instrucciones)",
                prog.codigo.length <= 100);
        ProcesadorMIPS cpu = new ProcesadorMIPS();
        cpu.cargarPrograma(prog.codigo);
        for (int i = 0; i < prog.datos.length; i++) {
            cpu.getMemoria().escribir(MemoriaSistema.BASE_RAM + i, prog.datos[i]);
        }
        return cpu;
    }

    private static void probarPrograma(String nombre, ProgramaBuilder pb, String salidaEsperada) {
        System.out.println("-- Sistema: " + nombre + " --");
        try {
            ProcesadorMIPS cpu = construirYcargar(pb, nombre);
            cpu.capturarSalida();
            try {
                cpu.ejecutar();
            } finally {
                cpu.restaurarSalida();
            }
            MiniTest.assertEquals(nombre + ": salida exacta", salidaEsperada, cpu.obtenerConsola());
        } catch (RuntimeException e) {
            MiniTest.assertTrue(nombre + ": ejecucion sin excepcion (" + e.getMessage() + ")", false);
        }
    }

    private static void probarProgramasCompletosFinalizan() {
        System.out.println("-- Sistema: todos los programas finalizan --");
        String[] nombres = {"sumatoria", "factorial", "integral", "promedio", "perceptron"};
        ProgramaBuilder[] programas = {
            Sumatoria.construir(), Factorial.construir(), Integral.construir(),
            Promedio.construir(), Perceptron.construir()
        };
        for (int i = 0; i < programas.length; i++) {
            String n = nombres[i];
            try {
                ProcesadorMIPS cpu = construirYcargar(programas[i], n);
                cpu.ejecutar();
                MiniTest.assertTrue(n + ": termina (detenido=true)", cpu.isDetenido());
                MiniTest.assertTrue(n + ": ciclos razonables (<10000)",
                        cpu.getCiclos() > 0 && cpu.getCiclos() < 10000);
                MiniTest.assertTrue(n + ": termina con syscall exit (PC dentro de ROM)",
                        cpu.getPc().get() >= 0 && cpu.getPc().get() < 100);
            } catch (Exception e) {
                MiniTest.assertTrue(n + ": finaliza sin error (" + e + ")", false);
            }
        }
    }
}
