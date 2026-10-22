package instruccion;

/**
 * Servicios ofrecidos por la instruccion syscall.
 * El servicio se elige con el registro $v0 y los argumentos van en $a0.
 */
public enum ServicioSyscall {

    IMPRIMIR_ENTERO(1, "imprimir entero ($a0)"),
    IMPRIMIR_CADENA(4, "imprimir cadena ($a0 = direccion terminada en 0)"),
    LEER_ENTERO(5, "leer entero -> $v0"),
    IMPRIMIR_CARACTER(11, "imprimir caracter ($a0)"),
    TERMINAR(10, "terminar programa (detiene la CPU)");

    /** Codigo de servicio (valor de $v0). */
    public final int codigo;

    /** Descripcion legible del servicio. */
    public final String descripcion;

    private ServicioSyscall(int codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    /** Servicio correspondiente al codigo dado, o null si no esta soportado. */
    public static ServicioSyscall porCodigo(int codigo) {
        for (ServicioSyscall s : values()) {
            if (s.codigo == codigo) {
                return s;
            }
        }
        return null;
    }
}
