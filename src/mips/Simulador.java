package mips;

import programas.Factorial;
import programas.Integral;
import programas.Perceptron;
import programas.Promedio;
import programas.Sumatoria;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Simulador basico del procesador MIPS - Practica 2.
 *
 * Uso:
 *   java -cp out mips.Simulador <programa> [--traza] [--estado] [--captura archivo]
 */
public class Simulador {

    public static void main(String[] args) throws IOException {
        if (args.length == 0 || "--gui".equals(args[0])) {
            abrirVentana();
            return;
        }
        if ("--ayuda".equals(args[0]) || "--help".equals(args[0])) {
            imprimirUso();
            return;
        }

        String nombre = args[0];
        boolean traza = false;
        boolean estado = false;
        String rutaCaptura = null;

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--traza":
                    traza = true;
                    break;
                case "--estado":
                    estado = true;
                    break;
                case "--captura":
                    if (i + 1 >= args.length) {
                        System.err.println("Falta el nombre de archivo tras --captura");
                        System.exit(2);
                    }
                    rutaCaptura = args[++i];
                    break;
                default:
                    System.err.println("Argumento desconocido: " + args[i]);
                    System.exit(2);
            }
        }

        ProgramaBuilder.Programa prog;
        try {
            prog = construir(nombre);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(2);
            return;
        }

        final PrintStream stdoutOriginal = System.out;
        final StringBuilder captura = new StringBuilder();
        final boolean hayCaptura = rutaCaptura != null;

        if (hayCaptura) {
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    stdoutOriginal.write(b);
                    captura.append((char) b);
                }

                @Override
                public void write(byte[] b, int off, int len) {
                    stdoutOriginal.write(b, off, len);
                    captura.append(new String(b, off, len, StandardCharsets.UTF_8));
                }
            }, true, StandardCharsets.UTF_8.name()));
        }

        try {
            ProcesadorMIPS cpu = new ProcesadorMIPS();
            cpu.cargarPrograma(prog.codigo);
            for (int i = 0; i < prog.datos.length; i++) {
                cpu.getMemoria().escribir(MemoriaSistema.BASE_RAM + i, prog.datos[i]);
            }

            System.out.println("================================================================");
            System.out.println(" INICIO DE EJECUCION - Simulador de procesador MIPS");
            System.out.println(" Programa: " + nombre);
            System.out.println(" Instrucciones cargadas: " + prog.codigo.length
                    + " | Datos cargados: " + prog.datos.length + " celdas");
            System.out.println("----------------------------------------------------------------");
            System.out.println("ESTADO INICIAL:");
            System.out.print(cpu.getRegistros().volcar());
            System.out.println(cpu.getPc().toString());
            System.out.println("================================================================");
            System.out.println(" SALIDA DEL PROGRAMA:");
            System.out.println("----------------------------------------------------------------");

            int limite = cpu.longitudPrograma();
            while (!cpu.isDetenido()) {
                int pcActual = cpu.getPc().get();
                if (pcActual < 0 || pcActual >= cpu.getRom().getCantidadCeldas()) {
                    System.out.println("[fin: PC fuera de la ROM]");
                    break;
                }
                int instr = cpu.getRom().leer(pcActual);
                if (instr == 0 && pcActual >= limite) {
                    System.out.println("[fin: se alcanzo el final del programa]");
                    break;
                }
                if (traza) {
                    System.out.println(String.format("  [%4d] PC=%3d  0x%08X",
                            cpu.getCiclos(), pcActual, instr));
                }
                cpu.ciclo();
            }

            System.out.println("----------------------------------------------------------------");
            System.out.println("FIN DE EJECUCION - ciclos: " + cpu.getCiclos());
            if (estado) {
                System.out.println("ESTADO FINAL:");
                System.out.print(cpu.getRegistros().volcar());
                System.out.println(cpu.getPc().toString());
                System.out.println("HI=" + cpu.getHi() + "  LO=" + cpu.getLo());
                System.out.print(cpu.getRam().volcar("RAM"));
            }
            System.out.println("================================================================");

        } catch (RuntimeException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.setOut(stdoutOriginal);
            if (hayCaptura) {
                Files.write(Paths.get(rutaCaptura), captura.toString().getBytes(StandardCharsets.UTF_8));
            }
            System.exit(1);
        }

        System.setOut(stdoutOriginal);
        if (hayCaptura) {
            Files.write(Paths.get(rutaCaptura), captura.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /** Abre la ventana grafica del simulador (modo interactivo). */
    private static void abrirVentana() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            System.out.println("No hay pantalla disponible: se muestra el modo de texto.");
            imprimirUso();
            return;
        }
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // se conserva el look and feel por defecto
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            VentanaSimulador ventana = new VentanaSimulador();
            ventana.setVisible(true);
        });
    }

    private static void imprimirUso() {
        System.out.println("Uso: java -cp out mips.Simulador [programa] [--traza] [--estado] [--captura <archivo>]");
        System.out.println("  (sin argumentos o con --gui se abre la ventana grafica)");
        System.out.println("  programas : sumatoria factorial integral promedio perceptron");
        System.out.println("  --traza   : imprime cada ciclo (PC y codigo de la instruccion)");
        System.out.println("  --estado  : imprime registros y memorias al final");
        System.out.println("  --captura : guarda toda la salida en el archivo indicado");
    }

    private static ProgramaBuilder.Programa construir(String nombre) {
        switch (nombre) {
            case "sumatoria":
                return Sumatoria.construir().generar();
            case "factorial":
                return Factorial.construir().generar();
            case "integral":
                return Integral.construir().generar();
            case "promedio":
                return Promedio.construir().generar();
            case "perceptron":
                return Perceptron.construir().generar();
            default:
                throw new IllegalArgumentException(
                        "Programa desconocido: " + nombre
                        + " (use: sumatoria factorial integral promedio perceptron)");
        }
    }
}
