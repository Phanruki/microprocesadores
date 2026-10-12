package almacenamiento;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Disco duro: copia de la unidad de almacenamiento con ancho de palabra de
 * 32 bits y 100 celdas. Ademas permite persistir su contenido en un archivo.
 */
public class DiscoDuro extends UnidadAlmacenamiento {

    public static final int ANCHO_PALABRA = 32;
    public static final int CELDAS = 100;

    public DiscoDuro() {
        super(ANCHO_PALABRA, CELDAS);
    }

    /** Guarda el contenido del disco en un archivo de texto (una celda por linea). */
    public void guardar(String rutaArchivo) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(rutaArchivo))) {
            for (int i = 0; i < cantidadCeldas; i++) {
                bw.write(Integer.toString(celdas[i]));
                bw.newLine();
            }
        }
    }

    /** Carga el contenido del disco desde un archivo de texto. */
    public void cargar(String rutaArchivo) throws IOException {
        limpiar();
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            int i = 0;
            while ((linea = br.readLine()) != null && i < cantidadCeldas) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    celdas[i] = Integer.parseInt(linea);
                    i++;
                }
            }
        }
    }

    /** Copia el contenido de otra unidad de almacenamiento al disco. */
    public void importar(UnidadAlmacenamiento origen) {
        limpiar();
        int n = Math.min(cantidadCeldas, origen.getCantidadCeldas());
        for (int i = 0; i < n; i++) {
            celdas[i] = origen.leer(i);
        }
    }

    /** Copia el contenido del disco a otra unidad de almacenamiento. */
    public void exportar(UnidadAlmacenamiento destino) {
        int n = Math.min(cantidadCeldas, destino.getCantidadCeldas());
        for (int i = 0; i < n; i++) {
            destino.escribir(i, celdas[i]);
        }
    }
}
