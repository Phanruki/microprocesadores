package ui;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;

/**
 * Paleta y utilidades de estilo para el tema oscuro de la ventana del emulador
 * (estilo editor de codigo, base negra). No contiene logica de negocio: sólo
 * apariencia.
 */
final class TemaOscuro {

    // --- Paleta (inspirada en GitHub Dark / VS Code) ---
    static final Color BASE      = new Color(0x0D1117); // fondo general (casi negro)
    static final Color PANEL     = new Color(0x161B22); // paneles y contenedores
    static final Color AREA      = new Color(0x0B0F14); // fondos de texto (mas oscuros)
    static final Color BOTON     = new Color(0x21262D); // cara de botones
    static final Color BOTON_HOV = new Color(0x30363D); // hover neutro
    static final Color BORDE     = new Color(0x30363D); // bordes y divisores
    static final Color TEXTO     = new Color(0xC9D1D9); // texto principal
    static final Color TEXTO_DIM = new Color(0x8B949E); // texto secundario
    static final Color TITULO    = new Color(0x58A6FF); // titulos de panel (azul)
    static final Color VERDE     = new Color(0x238636); // acciones "Ejecutar"
    static final Color VERDE_HOV = new Color(0x2EA043);
    static final Color VERDE_REPOSO = new Color(0x1A2E22); // verde tenue en reposo
    static final Color VERDE_BORDE  = new Color(0x2EA043); // borde verde para acciones
    static final Color SELECCION = new Color(0x264F78); // seleccion de texto
    static final Color OK        = new Color(0x3FB950); // barra de estado

    private TemaOscuro() { }

    static Font fuente() {
        return new Font(Font.MONOSPACED, Font.PLAIN, 13);
    }

    static Font fuente(float tamano, int estilo) {
        return fuente().deriveFont(estilo, tamano);
    }

    /** Aplica el aspecto oscuro a un area de texto. */
    static void estiloArea(JTextArea area, boolean editable) {
        area.setEditable(editable);
        area.setFont(fuente());
        area.setBackground(AREA);
        area.setForeground(TEXTO);
        area.setCaretColor(TEXTO);
        area.setSelectionColor(SELECCION);
        area.setCaretPosition(0);
    }

    /** Envuelve un componente en un JScrollPane con scrollbars oscuros y planos. */
    static JScrollPane scroll(JComponent comp) {
        JScrollPane sp = new JScrollPane(comp);
        sp.setBackground(PANEL);
        sp.getViewport().setBackground(AREA);
        sp.setBorder(new LineBorder(BORDE));
        BarraPlana.install(sp.getVerticalScrollBar());
        BarraPlana.install(sp.getHorizontalScrollBar());
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        return sp;
    }

    /** Borde con titulo estilo editor: linea sutil + titulo en azul. */
    static TitledBorder titulo(String texto) {
        TitledBorder tb = javax.swing.BorderFactory.createTitledBorder(
                new LineBorder(BORDE), texto);
        tb.setTitleColor(TITULO);
        tb.setTitleFont(fuente(12f, Font.BOLD));
        tb.setTitleJustification(TitledBorder.LEFT);
        tb.setTitlePosition(TitledBorder.ABOVE_TOP);
        return tb;
    }

    /** Borde de panel: padding exterior + titulo. */
    static CompoundBorder panelBorde(String texto) {
        return new CompoundBorder(new EmptyBorder(6, 6, 6, 6), titulo(texto));
    }

    /** Boton plano oscuro; si se pasa un color de acento, es una accion "principal". */
    static JButton boton(String texto, java.awt.event.ActionListener accion, Color acento) {
        JButton b = new JButton(texto);
        boolean principal = (acento != null);
        b.setFont(fuente(12f, Font.PLAIN));
        b.setForeground(principal ? Color.WHITE : TEXTO);
        b.setBackground(principal ? VERDE_REPOSO : BOTON);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
                new LineBorder(principal ? VERDE_BORDE : BORDE),
                new EmptyBorder(7, 14, 7, 14)));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(JButton.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        b.addActionListener(accion);
        final Color hover = principal ? acento : BOTON_HOV;
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setBackground(hover);
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBackground(principal ? VERDE_REPOSO : BOTON);
            }
        });
        return b;
    }

    /** Barra de scroll plana, sin flechas, con "thumb" redondeado. */
    static final class BarraPlana extends BasicScrollBarUI {

        static void install(javax.swing.JScrollBar barra) {
            barra.setUI(new BarraPlana());
            barra.setPreferredSize(new Dimension(10, Integer.MAX_VALUE));
        }

        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(0x3D444D);
            thumbDarkShadowColor = thumbColor;
            thumbHighlightColor = thumbColor;
            thumbLightShadowColor = thumbColor;
            trackColor = PANEL;
            trackHighlightColor = PANEL;
        }

        @Override protected JButton createDecreaseButton(int orient) { return flechaNula(); }
        @Override protected JButton createIncreaseButton(int orient) { return flechaNula(); }

        private JButton flechaNula() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }

        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r == null || r.height <= 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            int x = r.x + 2, y = r.y + 2, w = r.width - 4, h = r.height - 4;
            g2.fillRoundRect(x, y, w, h, 8, 8);
            g2.dispose();
        }

        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(trackColor);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
    }
}
