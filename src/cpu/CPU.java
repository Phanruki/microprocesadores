package cpu;

import alu.ALU;
import ejecucion.EjecutorInstrucciones;
import ejecucion.SenalesControl;
import ejecucion.UnidadControl;
import instruccion.Decodificador;
import instruccion.Instruccion;
import instruccion.ServicioSyscall;
import memoria.DiscoDuro;
import memoria.MemoriaSistema;
import memoria.RAM;
import memoria.ROM;
import memoria.Registros;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * CPU del emulador MIPS: ciclo lineal de 5 etapas por instruccion
 * (busqueda IF, decodificacion ID, ejecucion EX, memoria MEM, escritura WB).
 *
 * La CPU nunca muere: siempre esta ejecutando algo. Arranca en la
 * direccion 0 y, si el programa no tiene instrucciones, sigue
 * ejecutando NOPs (las celdas de memoria estan en 0x00000000 y la
 * palabra 0 es un NOP). Solo se detiene con la instruccion HALT o con
 * el servicio syscall 10; ademas el PC es circular dentro del mapa de
 * memoria para que la ejecucion nunca se salga del sistema.
 */
public class CPU {

    /** Lmite de seguridad de ciclos de ejecucion() (por si falta un HALT). */
    public static final long LIMITE_CICLOS_SEGURIDAD = 100000;

    private final ROM rom;
    private final RAM ram;
    private final DiscoDuro disco;
    private final Registros registros;
    private final PC pc;
    private final ALU alu;
    private final MemoriaSistema memoria;
    private final UnidadControl unidadControl;
    private final EjecutorInstrucciones ejecutor;

    private long hi;
    private long lo;
    private boolean detenido;
    private long ciclos;
    private final List<String> consola = new ArrayList<>();
    private ByteArrayOutputStream bufferSalida;
    private PrintStream salidaOriginal;
    private int[] entradaEntera = new int[0];
    private int indiceEntrada;

    // Estado de la instruccion en curso (pasa por las 5 etapas)
    private int palabraActual;
    private Instruccion instrActual;
    private SenalesControl senalesActuales;
    private boolean pendienteEscritura;
    private int registroDestino;
    private int valorResultado;
    private boolean pendienteLecturaMemoria;
    private boolean pendienteEscrituraMemoria;
    private int direccionMemoria;
    private int valorAMemoria;

    public CPU() {
        this.rom = new ROM();
        this.ram = new RAM();
        this.disco = new DiscoDuro();
        this.registros = new Registros();
        this.pc = new PC();
        this.alu = new ALU();
        this.memoria = new MemoriaSistema(rom, ram, disco);
        this.unidadControl = new UnidadControl();
        this.ejecutor = new EjecutorInstrucciones(this);
        reiniciar();
    }

    public void reiniciar() {
        rom.limpiar();
        rom.desbloquear();
        ram.limpiar();
        registros.limpiar();
        pc.reiniciar();
        hi = 0;
        lo = 0;
        detenido = false;
        ciclos = 0;
        consola.clear();
        indiceEntrada = 0;
        palabraActual = 0;
        instrActual = null;
        senalesActuales = null;
        pendienteEscritura = false;
        pendienteLecturaMemoria = false;
        pendienteEscrituraMemoria = false;
    }

    /** Carga el programa en el segmento de texto (ROM) y fija el PC en 0. */
    public void cargarPrograma(int[] programa) {
        rom.cargarPrograma(programa, 0);
        rom.bloquear();
        pc.set(0);
    }

    public void fijarEntradas(int... valores) {
        this.entradaEntera = valores.clone();
        this.indiceEntrada = 0;
    }

    /**
     * Ejecuta un ciclo (una instruccion) recorriendo linealmente las
     * 5 etapas: IF (busqueda), ID (decodificacion), EX (ejecucion),
     * MEM (memoria) y WB (escritura de resultados).
     */
    public void ciclo() {
        if (detenido) {
            return;
        }
        etapaBusqueda();
        etapaDecodificacion();
        etapaEjecucion();
        etapaMemoria();
        etapaEscritura();
        ciclos++;
    }

    /**
     * IF: busca la instruccion en el segmento de texto y apunta el PC
     * a la siguiente. El PC es circular: si se sale del mapa de memoria
     * vuelve a 0, porque la CPU nunca muere.
     */
    private void etapaBusqueda() {
        int direccion = pc.get();
        if (!memoria.contiene(direccion)) {
            direccion = ROM.DIRECCION_BASE; // la CPU sigue viva: empieza de nuevo en 0
        }
        palabraActual = memoria.leer(direccion);
        pc.set(direccion + 1); // durante la busqueda el PC apunta a la siguiente instruccion
    }

    /** ID: decodifica los campos y pide las senales de control. */
    private void etapaDecodificacion() {
        instrActual = Decodificador.decodificar(palabraActual);
        senalesActuales = unidadControl.decodificar(instrActual);
        pendienteEscritura = false;
        registroDestino = -1;
        valorResultado = 0;
        pendienteLecturaMemoria = false;
        pendienteEscrituraMemoria = false;
        direccionMemoria = 0;
        valorAMemoria = 0;
    }

    /** EX: el ejecutor aplica las senales sobre el camino de datos. */
    private void etapaEjecucion() {
        ejecutor.ejecutar(instrActual, senalesActuales);
    }

    /** MEM: accede a la memoria para lw (lectura) y sw (escritura). */
    private void etapaMemoria() {
        if (pendienteLecturaMemoria) {
            valorResultado = memoria.leer(direccionMemoria);
            pendienteEscritura = true;
        }
        if (pendienteEscrituraMemoria) {
            memoria.escribir(direccionMemoria, valorAMemoria);
        }
    }

    /** WB: escribe en los registros el resultado dejado pendiente en EX/MEM. */
    private void etapaEscritura() {
        if (pendienteEscritura) {
            registros.escribir(registroDestino, valorResultado);
        }
    }

    // ------------------------------------------------------------------
    // Interfaz del camino de datos (usada por el EjecutorInstrucciones)
    // ------------------------------------------------------------------

    /** Marca un resultado de registro para escribirlo en la etapa WB. */
    public void programarEscritura(int registro, int valor) {
        registroDestino = registro;
        valorResultado = valor;
        pendienteEscritura = true;
    }

    /** Fija el registro destino y la direccion de la lectura lw (etapa MEM). */
    public void programarLecturaMemoria(int registro, int direccion) {
        registroDestino = registro;
        direccionMemoria = direccion;
        pendienteLecturaMemoria = true;
    }

    /** Fija la direccion y el valor que la etapa MEM escribira en memoria (sw). */
    public void programarEscrituraMemoria(int direccion, int valor) {
        direccionMemoria = direccion;
        valorAMemoria = valor;
        pendienteEscrituraMemoria = true;
    }

    /** Escribe una direccion destino en el PC (ramas y saltos). */
    public void saltarA(int direccion) {
        pc.set(direccion);
    }

    /** Actualiza los registros especiales HI y LO (mult/div). */
    public void fijarHiLo(long hi, long lo) {
        this.hi = hi;
        this.lo = lo;
    }

    /** Ejecuta el servicio syscall indicado por $v0 (lo llama el ejecutor). */
    public void syscall() {
        int servicio = registros.leer(2); // $v0
        ServicioSyscall s = ServicioSyscall.porCodigo(servicio);
        if (s == null) {
            throw new UnsupportedOperationException("Servicio syscall no soportado: " + servicio);
        }
        switch (s) {
            case IMPRIMIR_ENTERO:
                imprimir(Integer.toString(registros.leer(4))); // $a0
                break;
            case IMPRIMIR_CADENA: {
                int dir = registros.leer(4);
                StringBuilder sb = new StringBuilder();
                while (true) {
                    int c = memoria.leer(dir);
                    if (c == 0) {
                        break;
                    }
                    sb.append((char) c);
                    dir++;
                }
                imprimir(sb.toString());
                break;
            }
            case IMPRIMIR_CARACTER:
                imprimir(String.valueOf((char) (registros.leer(4) & 0xFF)));
                break;
            case LEER_ENTERO: {
                int valor = 0;
                if (indiceEntrada < entradaEntera.length) {
                    valor = entradaEntera[indiceEntrada++];
                }
                registros.escribir(2, valor);
                break;
            }
            case TERMINAR:
                detenido = true;
                break;
            default:
                throw new UnsupportedOperationException("Servicio syscall no soportado: " + servicio);
        }
    }

    // ------------------------------------------------------------------
    // Ejecucion completa
    // ------------------------------------------------------------------

    /**
     * Ejecuta hasta que la instruccion HALT (o syscall 10) detenga la
     * CPU. La CPU no muere sola: si el programa no tiene HALT, sigue
     * ejecutando NOPs hasta el limite de seguridad de ciclos.
     *
     * @return true si la CPU se detuvo por HALT/exit, false si se alcanzo
     *         el limite de seguridad
     */
    public boolean ejecutar() {
        while (!detenido && ciclos < LIMITE_CICLOS_SEGURIDAD) {
            ciclo();
        }
        return detenido;
    }

    /** Longitud util del programa en la ROM (ultima celda no nula + 1). */
    public int longitudPrograma() {
        int n = 0;
        for (int i = 0; i < rom.getCantidadCeldas(); i++) {
            if (rom.leer(i) != 0) {
                n = i + 1;
            }
        }
        return n;
    }

    private void imprimir(String texto) {
        consola.add(texto);
        System.out.print(texto);
    }

    /** Redirige la salida del sistema al buffer interno (para pruebas). */
    public void capturarSalida() {
        if (salidaOriginal == null) {
            salidaOriginal = System.out;
            bufferSalida = new ByteArrayOutputStream();
            System.setOut(new PrintStream(bufferSalida));
        }
    }

    public void restaurarSalida() {
        if (salidaOriginal != null) {
            System.setOut(salidaOriginal);
            salidaOriginal = null;
        }
    }

    public String obtenerSalidaCapturada() {
        return bufferSalida == null ? "" : bufferSalida.toString();
    }

    public String obtenerConsola() {
        StringBuilder sb = new StringBuilder();
        for (String s : consola) {
            sb.append(s);
        }
        return sb.toString();
    }

    /** true si la CPU esta detenida (HALT o syscall 10). */
    public boolean isDetenido() {
        return detenido;
    }

    /** Detiene la CPU (como si se hubiera ejecutado HALT). */
    public void detener() {
        detenido = true;
    }

    public long getCiclos() {
        return ciclos;
    }

    public ROM getRom() {
        return rom;
    }

    public RAM getRam() {
        return ram;
    }

    public DiscoDuro getDisco() {
        return disco;
    }

    public Registros getRegistros() {
        return registros;
    }

    public PC getPc() {
        return pc;
    }

    public ALU getAlu() {
        return alu;
    }

    public MemoriaSistema getMemoria() {
        return memoria;
    }

    /** Instruccion decodificada del ciclo en curso (para la interfaz). */
    public Instruccion getInstruccionActual() {
        return instrActual;
    }

    /** Senales de control del ciclo en curso (para la interfaz). */
    public SenalesControl getSenalesActuales() {
        return senalesActuales;
    }

    public long getHi() {
        return hi;
    }

    public long getLo() {
        return lo;
    }
}
