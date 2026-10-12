package mips;

/**
 * Memoria unificada del simulador: enruta cada direccion a la unidad
 * fisica correspondiente.
 *
 * Mapa de direcciones (por palabras de 32 bits):
 *   0   .. 99   -> ROM (texto / instrucciones)
 *   100 .. 199  -> RAM (datos)
 *   200 .. 299  -> Disco duro
 */
public class MemoriaSistema {

    public static final int BASE_ROM = 0;
    public static final int BASE_RAM = 100;
    public static final int BASE_DISCO = 200;

    private final almacenamiento.ROM rom;
    private final almacenamiento.RAM ram;
    private final almacenamiento.DiscoDuro disco;

    public MemoriaSistema(almacenamiento.ROM rom, almacenamiento.RAM ram, almacenamiento.DiscoDuro disco) {
        this.rom = rom;
        this.ram = ram;
        this.disco = disco;
    }

    public int leer(int direccion) {
        if (direccion >= BASE_DISCO) {
            return disco.leer(direccion - BASE_DISCO);
        }
        if (direccion >= BASE_RAM) {
            return ram.leer(direccion - BASE_RAM);
        }
        if (direccion >= BASE_ROM) {
            return rom.leer(direccion - BASE_ROM);
        }
        throw new IndexOutOfBoundsException("Direccion invalida: " + direccion);
    }

    public void escribir(int direccion, int valor) {
        if (direccion >= BASE_DISCO) {
            disco.escribir(direccion - BASE_DISCO, valor);
        } else if (direccion >= BASE_RAM) {
            ram.escribir(direccion - BASE_RAM, valor);
        } else if (direccion >= BASE_ROM) {
            rom.escribir(direccion - BASE_ROM, valor);
        } else {
            throw new IndexOutOfBoundsException("Direccion invalida: " + direccion);
        }
    }
}
