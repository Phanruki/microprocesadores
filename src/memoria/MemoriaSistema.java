package memoria;

import java.util.List;

/**
 * Mapa de memoria segmentado del simulador.
 *
 * Cada direccion global pertenece a un segmento; la unidad correspondiente
 * se elige segun la direccion base de cada segmento.
 *
 * Mapa de direcciones (palabras de 32 bits):
 *   0   .. 99   -> ROM   (segmento de texto: instrucciones)
 *   100 .. 199  -> RAM   (segmento de datos)
 *   200 .. 299  -> Disco (segmento de almacenamiento)
 *
 * Todas las celdas arrancan en 0x00000000: la palabra 0 se interpreta
 * como NOP, de modo que la CPU puede seguir ejecutando indefinidamente
 * (nunca muere) hasta encontrar la instruccion HALT.
 */
public class MemoriaSistema {

    private final ROM rom;
    private final RAM ram;
    private final DiscoDuro disco;
    private final List<UnidadAlmacenamiento> segmentos;

    public MemoriaSistema(ROM rom, RAM ram, DiscoDuro disco) {
        this.rom = rom;
        this.ram = ram;
        this.disco = disco;
        this.segmentos = List.of(rom, ram, disco);
    }

    /** Segmento al que pertenece la direccion dada, o null si no existe. */
    public UnidadAlmacenamiento segmentoDe(int direccion) {
        for (UnidadAlmacenamiento s : segmentos) {
            if (s.contieneAbsoluta(direccion)) {
                return s;
            }
        }
        return null;
    }

    /** true si la direccion pertenece a algun segmento del mapa. */
    public boolean contiene(int direccion) {
        return segmentoDe(direccion) != null;
    }

    /** Total de celdas del mapa (0 .. total-1). */
    public int totalCeldas() {
        int total = 0;
        for (UnidadAlmacenamiento s : segmentos) {
            total += s.getCantidadCeldas();
        }
        return total;
    }

    public List<UnidadAlmacenamiento> getSegmentos() {
        return segmentos;
    }

    public int leer(int direccion) {
        UnidadAlmacenamiento s = buscarSegmento(direccion);
        return s.leer(s.aLocal(direccion));
    }

    public void escribir(int direccion, int valor) {
        UnidadAlmacenamiento s = buscarSegmento(direccion);
        s.escribir(s.aLocal(direccion), valor);
    }

    private UnidadAlmacenamiento buscarSegmento(int direccion) {
        UnidadAlmacenamiento s = segmentoDe(direccion);
        if (s == null) {
            throw new IndexOutOfBoundsException("Direccion fuera del mapa de memoria: " + direccion);
        }
        return s;
    }

    /** Descripcion legible del mapa de segmentos y sus direcciones. */
    public String mapa() {
        StringBuilder sb = new StringBuilder("=== MAPA DE MEMORIA (palabras de 32 bits) ===\n");
        for (UnidadAlmacenamiento s : segmentos) {
            sb.append(String.format("  %-8s %3d .. %3d  (0x%08X .. 0x%08X)  %s%n",
                    s.getNombre(), s.getDireccionBase(), s.getDireccionFinal(),
                    s.getDireccionBase(), s.getDireccionFinal(),
                    s.getClass().getSimpleName()));
        }
        return sb.toString();
    }
}
