package ui;

import cpu.CPU;
import ejecucion.SenalesControl;
import instruccion.Binario;
import instruccion.Desensamblador;
import instruccion.Funct;
import instruccion.Instruccion;
import instruccion.Opcode;
import memoria.RAM;
import memoria.Registros;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ventana grafica del emulador del procesador MIPS (Practica 2).
 *
 * Campos:
 *   - Instrucciones (Binario): entrada de instrucciones en binario (32 bits)
 *   - Historial: ciclo, PC, instruccion, operacion, senales y PC siguiente
 *   - Registros: nombre = binario
 *   - RAM: nombre (direccion) = binario
 *   - Estado CPU: PC, HI y LO en binario
 */
public class VentanaEmulador extends JFrame {

    private static final int LIMITE_CICLOS = 10000;

    private final CPU cpu = new CPU();

    private final JTextArea entrada = new JTextArea(4, 46);
    private final JTextArea historial = new JTextArea();
    private final JTextArea registros = new JTextArea();
    private final JTextArea ram = new JTextArea();

    private final JLabel valorPc = new JLabel();
    private final JLabel valorHi = new JLabel();
    private final JLabel valorLo = new JLabel();
    private final JLabel barraEstado = new JLabel("Listo.");
    private final JLabel barraCiclos = new JLabel();

    private String ultimaCarga;

    public VentanaEmulador() {
        super("Emulador MIPS - Practica 2");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(TemaOscuro.BASE);
        setLayout(new BorderLayout(6, 6));
        add(construirEntrada(), BorderLayout.NORTH);
        add(construirPanelCentral(), BorderLayout.CENTER);
        add(construirBarraEstado(), BorderLayout.SOUTH);

        instalarAtajos();
        reiniciarCpu();
        estado("Listo - escriba instrucciones en binario (32 bits) y pulse "
                + "\"Ejecutar 1 ciclo\" (Ctrl+Intro). Boton \"Cargar ejemplo\" para probar.");
        setSize(1180, 760);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------
    // Construccion de la interfaz
    // ------------------------------------------------------------------

    private JPanel construirEntrada() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBackground(TemaOscuro.PANEL);
        panel.setBorder(TemaOscuro.panelBorde("Instrucciones (Binario)"));

        TemaOscuro.estiloArea(entrada, true);
        entrada.setLineWrap(true);
        entrada.setWrapStyleWord(false);
        panel.add(TemaOscuro.scroll(entrada), BorderLayout.CENTER);

        JPanel botones = new JPanel();
        botones.setOpaque(false);
        botones.setLayout(new BoxLayout(botones, BoxLayout.Y_AXIS));
        botones.add(boton("Ejecutar 1 ciclo", e -> ejecutarCiclos(1), TemaOscuro.VERDE));
        botones.add(Box.createVerticalStrut(4));
        botones.add(boton("Ejecutar programa", e -> ejecutarCiclos(LIMITE_CICLOS), TemaOscuro.VERDE_HOV));
        botones.add(Box.createVerticalStrut(4));
        botones.add(boton("Cargar en el PC", e -> cargarInstrucciones(), null));
        botones.add(Box.createVerticalStrut(4));
        botones.add(boton("Cargar ejemplo", e -> cargarEjemplo(), null));
        botones.add(Box.createVerticalStrut(4));
        botones.add(boton("Reiniciar CPU", e -> reiniciarCpu(), null));
        botones.add(Box.createVerticalStrut(4));
        botones.add(boton("Limpiar historial", e -> historial.setText(""), null));
        panel.add(botones, BorderLayout.EAST);
        return panel;
    }

    private JSplitPane construirPanelCentral() {
        JScrollPane scHistorial = conTitulo(historial, "Historial");
        JScrollPane scRegistros = conTitulo(registros, "Registros");
        JScrollPane scRam = conTitulo(ram, "RAM");

        JSplitPane superior = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scHistorial, scRegistros);
        superior.setResizeWeight(0.5);
        superior.setContinuousLayout(true);

        JSplitPane inferior = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scRam, construirCpu());
        inferior.setResizeWeight(0.55);
        inferior.setContinuousLayout(true);

        JSplitPane raiz = new JSplitPane(JSplitPane.VERTICAL_SPLIT, superior, inferior);
        raiz.setResizeWeight(0.62);
        raiz.setContinuousLayout(true);
        raiz.setBorder(null);
        oscurecerDivisor(superior);
        oscurecerDivisor(inferior);
        oscurecerDivisor(raiz);
        return raiz;
    }

    /** Deja un divisor de JSplitPane con el mismo tono oscuro que el tema. */
    private void oscurecerDivisor(JSplitPane split) {
        split.setBackground(TemaOscuro.BASE);
        split.setDividerSize(6);
        SwingUtilities.invokeLater(() -> {
            for (java.awt.Component comp : split.getComponents()) {
                if (comp instanceof javax.swing.JSplitPane) {
                    continue;
                }
                comp.setBackground(TemaOscuro.BORDE);
            }
        });
    }

    private JPanel construirCpu() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(TemaOscuro.PANEL);
        panel.setBorder(TemaOscuro.panelBorde("Estado CPU"));

        JPanel rejilla = new JPanel(new GridBagLayout());
        rejilla.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 10, 4, 10);

        String[] etiquetas = {"PC:", "HI:", "LO:"};
        JLabel[] valores = {valorPc, valorHi, valorLo};
        for (int i = 0; i < etiquetas.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            JLabel etiqueta = new JLabel(etiquetas[i]);
            etiqueta.setFont(TemaOscuro.fuente(13f, Font.BOLD));
            etiqueta.setForeground(TemaOscuro.TEXTO_DIM);
            rejilla.add(etiqueta, c);

            c.gridx = 1;
            valores[i].setFont(TemaOscuro.fuente());
            valores[i].setForeground(TemaOscuro.TEXTO);
            rejilla.add(valores[i], c);
        }
        panel.add(rejilla, BorderLayout.NORTH);
        return panel;
    }

    private JPanel construirBarraEstado() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(TemaOscuro.PANEL);
        panel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, TemaOscuro.BORDE),
                new EmptyBorder(5, 10, 5, 10)));
        barraEstado.setFont(TemaOscuro.fuente());
        barraEstado.setForeground(TemaOscuro.OK);
        barraCiclos.setFont(TemaOscuro.fuente());
        barraCiclos.setForeground(TemaOscuro.TEXTO_DIM);
        panel.add(barraEstado, BorderLayout.CENTER);
        panel.add(barraCiclos, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane conTitulo(JTextArea area, String titulo) {
        TemaOscuro.estiloArea(area, false);
        JScrollPane scroll = TemaOscuro.scroll(area);
        scroll.setBorder(TemaOscuro.titulo(titulo));
        return scroll;
    }

    private JButton boton(String texto, java.awt.event.ActionListener accion, Color acento) {
        return TemaOscuro.boton(texto, accion, acento);
    }

    private void instalarAtajos() {
        atajo(entrada, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK),
                "unCiclo", e -> ejecutarCiclos(1));
        atajo(entrada, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK),
                "todoCiclos", e -> ejecutarCiclos(LIMITE_CICLOS));
    }

    private void atajo(JTextArea area, KeyStroke tecla, String nombre, java.awt.event.ActionListener accion) {
        area.getInputMap(JTextArea.WHEN_FOCUSED).put(tecla, nombre);
        area.getActionMap().put(nombre, new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                accion.actionPerformed(e);
            }
        });
    }

    // ------------------------------------------------------------------
    // Ejecucion
    // ------------------------------------------------------------------

    private void ejecutarCiclos(int maxCiclos) {
        int[] palabras;
        try {
            palabras = Binario.parsear(entrada.getText());
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
            return;
        }

        // Solo se vuelve a escribir en la ROM si el usuario cambio el texto;
        // asi, pulsar varias veces "Ejecutar 1 ciclo" avanza de verdad.
        if (palabras.length > 0 && !entrada.getText().equals(ultimaCarga)) {
            if (!cargarEnRom(palabras)) {
                return;
            }
            ultimaCarga = entrada.getText();
        }

        int ejecutados = 0;
        String motivo = null;

        // La CPU nunca muere: si no hay HALT sigue ejecutando NOPs (0000...).
        while (!cpu.isDetenido() && ejecutados < maxCiclos) {
            int pcActual = cpu.getPc().get();
            if (pcActual < 0 || pcActual >= cpu.getMemoria().totalCeldas()) {
                pcActual = 0; // el PC es circular
            }
            int instruccion = cpu.getMemoria().leer(pcActual);

            long ciclo = cpu.getCiclos() + 1;
            String ensamblador = Desensamblador.mnemonico(instruccion, pcActual);
            String operacion = Desensamblador.operacion(instruccion, pcActual, cpu);
            String binario = Binario.a32(instruccion);

            try {
                cpu.ciclo();
            } catch (RuntimeException ex) {
                registrar(ciclo, pcActual, binario, ensamblador,
                        "ERROR: " + ex.getMessage(), null, cpu.getPc().get());
                estado("Ciclo " + ciclo + " - ERROR: " + ex.getMessage());
                actualizar();
                return;
            }
            SenalesControl senales = cpu.getSenalesActuales();
            registrar(ciclo, pcActual, binario, ensamblador, operacion, senales, cpu.getPc().get());
            ejecutados++;
        }

        if (cpu.isDetenido()) {
            motivo = "CPU detenida (instruccion HALT o syscall exit)";
        } else if (ejecutados >= maxCiclos && maxCiclos == LIMITE_CICLOS) {
            motivo = "limite de " + LIMITE_CICLOS + " ciclos alcanzado (el programa no tiene HALT)";
        }

        actualizar();
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Ejecutado(s) ").append(ejecutados).append(" ciclo(s)");
        if (motivo != null) {
            mensaje.append(" - ").append(motivo);
        }
        String salida = cpu.obtenerConsola();
        if (!salida.isEmpty()) {
            mensaje.append(" | Salida: ").append(salida.replace('\n', ' '));
        }
        estado(mensaje.toString());
    }

    private void cargarInstrucciones() {
        int[] palabras;
        try {
            palabras = Binario.parsear(entrada.getText());
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
            return;
        }
        if (palabras.length == 0) {
            error("No hay instrucciones que cargar");
            return;
        }
        if (cargarEnRom(palabras)) {
            ultimaCarga = entrada.getText();
            estado("Cargadas " + palabras.length + " instruccion(es) en la ROM desde la direccion "
                    + (cpu.getPc().get()));
            actualizar();
        }
    }

    private boolean cargarEnRom(int[] palabras) {
        int direccion = cpu.getPc().get();
        if (direccion + palabras.length > cpu.getRom().getCantidadCeldas()) {
            error("Las instrucciones no caben en la ROM desde la direccion " + direccion);
            return false;
        }
        cpu.getRom().cargarPrograma(palabras, direccion);
        return true;
    }

    private void registrar(long ciclo, int pc, String binario, String ensamblador,
                           String operacion, SenalesControl senales, int pcSiguiente) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ciclo: ").append(ciclo).append('\n');
        sb.append("PC: ").append(Binario.a32(pc)).append('\n');
        sb.append("Instruccion: ").append(ensamblador).append('\n');
        sb.append("Binario: ").append(binario).append('\n');
        sb.append("Senales: ").append(senales == null ? "-" : senales.toString()).append('\n');
        sb.append("Operacion: ").append(operacion).append('\n');
        sb.append("PC Siguiente: ").append(Binario.a32(pcSiguiente)).append('\n');
        sb.append("--------------------------------------------------\n");
        historial.append(sb.toString());
        historial.setCaretPosition(historial.getDocument().getLength());
    }

    private void reiniciarCpu() {
        cpu.reiniciar();
        ultimaCarga = null;
        historial.setText("");
        actualizar();
        estado("CPU reiniciada: PC = 0 y segmentos de memoria en 0x00000000");
    }

    private void cargarEjemplo() {
        int[] ejemplo = {
            Instruccion.codificarI(Opcode.ADDI.codigo, 0, 8, 10),
            Instruccion.codificarI(Opcode.ADDI.codigo, 0, 9, 3),
            Instruccion.codificarR(Opcode.R.codigo, 8, 9, 10, 0, Funct.ADD.codigo),
            Instruccion.codificarI(Opcode.SW.codigo, 0, 10, RAM.DIRECCION_BASE),
            Instruccion.codificarI(Opcode.LW.codigo, 0, 11, RAM.DIRECCION_BASE),
            Instruccion.codificarHalt()
        };
        String[] comentarios = {
            "addi $t0, $zero, 10",
            "addi $t1, $zero, 3",
            "add $t2, $t0, $t1",
            "sw $t2, 100($zero)   # RAM dir 100",
            "lw $t3, 100($zero)",
            "halt                 # alto: la CPU se detiene"
        };
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ejemplo.length; i++) {
            sb.append(Binario.a32(ejemplo[i])).append("   # ").append(comentarios[i]).append('\n');
        }
        entrada.setText(sb.toString());
        entrada.setCaretPosition(0);
        estado("Ejemplo cargado en el campo de instrucciones (termina con HALT)");
    }

    private void actualizar() {
        StringBuilder sb = new StringBuilder(Registros.CANTIDAD * 46);
        for (int i = 0; i < Registros.CANTIDAD; i++) {
            sb.append(String.format("%-5s = %s%n", Registros.nombre(i), Binario.a32(cpu.getRegistros().leer(i))));
        }
        registros.setText(sb.toString());
        registros.setCaretPosition(0);

        int[] celdas = cpu.getRam().obtenerCeldas();
        StringBuilder rb = new StringBuilder(celdas.length * 62);
        for (int i = 0; i < celdas.length; i++) {
            rb.append(String.format("RAM[%d] (dir %d / 0x%08X) = %s%n",
                    i, RAM.DIRECCION_BASE + i, RAM.DIRECCION_BASE + i, Binario.a32(celdas[i])));
        }
        ram.setText(rb.toString());
        ram.setCaretPosition(0);

        valorPc.setText(Binario.a32(cpu.getPc().get()));
        valorHi.setText(Binario.a32((int) cpu.getHi()));
        valorLo.setText(Binario.a32((int) cpu.getLo()));
        barraCiclos.setText("Ciclos: " + cpu.getCiclos());
    }

    private void estado(String mensaje) {
        barraEstado.setText(mensaje);
        barraEstado.setToolTipText(mensaje);
    }

    private void error(String mensaje) {
        estado("ERROR: " + mensaje);
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ------------------------------------------------------------------
    // Arranque
    // ------------------------------------------------------------------

    public static void main(String[] args) {
        aplicarEscalaHiDPI(); // ANTES de cualquier uso de AWT
        try {
            // Look and feel cruzado (Metal): respeta los colores oscuros que
            // aplicamos a mano y se ve igual en todos los sistemas.
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // se conserva el look and feel por defecto
        }
        SwingUtilities.invokeLater(() -> {
            VentanaEmulador ventana = new VentanaEmulador();
            ventana.ajustarTamanoAPantalla();
            ventana.setVisible(true);
        });
    }

    /**
     * En pantallas HiDPI (p. ej. 3072x1728 con escala 2x en GNOME/Wayland) Swing
     * dibuja a tamano fisico y la ventana sale diminuta. Consultamos la escala
     * real al compositor (sin inicializar AWT) y fijamos sun.java2d.uiScale para
     * que Java renderice a tamano logico. Si algo falla, no se toca nada.
     */
    private static void aplicarEscalaHiDPI() {
        if (System.getProperty("sun.java2d.uiScale") != null) {
            return; // el usuario ya forzo una escala explicita
        }
        double escala = detectarEscalaCompositor();
        if (escala > 1.0) {
            String valor = (escala == Math.rint(escala))
                    ? String.valueOf((int) escala)
                    : String.valueOf(escala);
            System.setProperty("sun.java2d.uiScale", valor);
        }
    }

    /**
     * Detecta la escala HiDPI del compositor sin tocar AWT. Usa el DisplayConfig
     * de Mutter (GNOME): de cada modo ACTUAL (marcado "true") compara la
     * resolucion fisica contra su tamano logico. Devuelve 1.0 si no hay datos.
     */
    private static double detectarEscalaCompositor() {
        String cmd = "gdbus call --session --dest org.gnome.Mutter.DisplayConfig "
                + "--object-path /org/gnome/Mutter/DisplayConfig "
                + "--method org.gnome.Mutter.DisplayConfig.GetCurrentState";
        String salida;
        try {
            Process p = new ProcessBuilder("bash", "-lc", cmd)
                    .redirectErrorStream(true).start();
            BufferedReader r = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String linea;
            while ((linea = r.readLine()) != null) {
                sb.append(linea);
            }
            p.waitFor();
            salida = sb.toString();
        } catch (Exception ignored) {
            return 1.0; // sin gdbus (u otro DE): comportamiento por defecto
        }

        // ('3072x1728@60.000', '3072x1728', '16:9', true, 1, [1536.0, 864.0, ...])
        Matcher m = Pattern.compile(
                "'(\\d+)x(\\d+)@[^']*',\\s*'\\d+x\\d+',\\s*'[^']*',\\s*true,"
                        + "\\s*\\d+,\\s*\\[(\\d+\\.?\\d*),\\s*\\d+\\.?\\d*")
                .matcher(salida);
        double escala = 1.0;
        while (m.find()) {
            double fisica = Double.parseDouble(m.group(1));
            double logica = Double.parseDouble(m.group(3));
            if (logica > 0) {
                escala = Math.max(escala, fisica / logica);
            }
        }
        return escala;
    }

    /** Ajusta la ventana para que ocupe buena parte de la pantalla visible. */
    private void ajustarTamanoAPantalla() {
        Dimension pantalla = getToolkit().getScreenSize();
        int ancho = (int) Math.max(1000, Math.min(pantalla.width * 0.90, pantalla.width));
        int alto = (int) Math.max(640, Math.min(pantalla.height * 0.88, pantalla.height));
        setSize(ancho, alto);
        setLocationRelativeTo(null);
    }
}
