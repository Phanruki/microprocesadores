package ejecucion;

import alu.OperacionALU;

/**
 * Senales de control que gobiernan el camino de datos de una instruccion.
 * Las genera la UnidadControl en la etapa ID y las aplica el
 * EjecutorInstrucciones en la etapa EX.
 *
 * Se construyen con el builder estatico:
 *   SenalesControl.alu(OperacionALU.ADD).inmediato().escribeRegistro().build()
 */
public final class SenalesControl {

    /** Operacion que debe realizar la ALU. */
    public final OperacionALU operacionAlu;

    /** true si la entrada B de la ALU es el inmediato (y no el registro rt). */
    public final boolean usaInmediato;

    /** true si el inmediato se usa sin extension de signo (andi, ori, xori, lui). */
    public final boolean inmediatoSinSigno;

    /** true si la etapa WB escribe el registro destino. */
    public final boolean escribeRegistro;

    /** true si la etapa MEM lee memoria (lw). */
    public final boolean leeMemoria;

    /** true si la etapa MEM escribe memoria (sw). */
    public final boolean escribeMemoria;

    /** true si la instruccion es una rama condicionada (beq / bne). */
    public final boolean esRama;

    /** true si la instruccion salta a una direccion del propio codigo (j / jal). */
    public final boolean esSalto;

    /** true si la instruccion salta a la direccion guardada en un registro (jr). */
    public final boolean esSaltoRegistro;

    /** true si la instruccion calcula HI:LO (mult / div). */
    public final boolean actualizaHiLo;

    /** true si la instruccion transfiere HI a un registro (mfhi). */
    public final boolean transfiereHi;

    /** true si la instruccion transfiere LO a un registro (mflo). */
    public final boolean transfiereLo;

    /** true si la instruccion pide un servicio del sistema (syscall). */
    public final boolean esSyscall;

    /** true si la instruccion detiene la CPU (halt). */
    public final boolean esHalt;

    private SenalesControl(Builder b) {
        this.operacionAlu = b.operacionAlu;
        this.usaInmediato = b.usaInmediato;
        this.inmediatoSinSigno = b.inmediatoSinSigno;
        this.escribeRegistro = b.escribeRegistro;
        this.leeMemoria = b.leeMemoria;
        this.escribeMemoria = b.escribeMemoria;
        this.esRama = b.esRama;
        this.esSalto = b.esSalto;
        this.esSaltoRegistro = b.esSaltoRegistro;
        this.actualizaHiLo = b.actualizaHiLo;
        this.transfiereHi = b.transfiereHi;
        this.transfiereLo = b.transfiereLo;
        this.esSyscall = b.esSyscall;
        this.esHalt = b.esHalt;
    }

    /** Crea el builder con la operacion de la ALU (NOP si no interviene). */
    public static Builder alu(OperacionALU operacion) {
        return new Builder(operacion);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("alu=").append(operacionAlu);
        if (usaInmediato) {
            sb.append(inmediatoSinSigno ? ", immSinSigno" : ", imm");
        }
        if (escribeRegistro) {
            sb.append(", escribeRegistro");
        }
        if (leeMemoria) {
            sb.append(", leeMemoria");
        }
        if (escribeMemoria) {
            sb.append(", escribeMemoria");
        }
        if (esRama) {
            sb.append(", rama");
        }
        if (esSalto) {
            sb.append(", salto");
        }
        if (esSaltoRegistro) {
            sb.append(", saltoRegistro");
        }
        if (actualizaHiLo) {
            sb.append(", hiLo");
        }
        if (transfiereHi) {
            sb.append(", mfhi");
        }
        if (transfiereLo) {
            sb.append(", mflo");
        }
        if (esSyscall) {
            sb.append(", syscall");
        }
        if (esHalt) {
            sb.append(", HALT");
        }
        return sb.toString();
    }

    /** Constructor imperativo de las senales (evita errores de posicion). */
    public static final class Builder {

        private final OperacionALU operacionAlu;
        private boolean usaInmediato;
        private boolean inmediatoSinSigno;
        private boolean escribeRegistro;
        private boolean leeMemoria;
        private boolean escribeMemoria;
        private boolean esRama;
        private boolean esSalto;
        private boolean esSaltoRegistro;
        private boolean actualizaHiLo;
        private boolean transfiereHi;
        private boolean transfiereLo;
        private boolean esSyscall;
        private boolean esHalt;

        private Builder(OperacionALU operacionAlu) {
            this.operacionAlu = operacionAlu;
        }

        /** La ALU recibe el inmediato con extension de signo. */
        public Builder inmediato() {
            this.usaInmediato = true;
            return this;
        }

        /** La ALU recibe el inmediato extendido con ceros. */
        public Builder inmediatoSinSigno() {
            this.usaInmediato = true;
            this.inmediatoSinSigno = true;
            return this;
        }

        public Builder escribeRegistro() {
            this.escribeRegistro = true;
            return this;
        }

        public Builder leeMemoria() {
            this.leeMemoria = true;
            return this;
        }

        public Builder escribeMemoria() {
            this.escribeMemoria = true;
            return this;
        }

        public Builder rama() {
            this.esRama = true;
            return this;
        }

        public Builder salto() {
            this.esSalto = true;
            return this;
        }

        public Builder saltoRegistro() {
            this.esSaltoRegistro = true;
            return this;
        }

        public Builder hiLo() {
            this.actualizaHiLo = true;
            return this;
        }

        public Builder mfhi() {
            this.transfiereHi = true;
            return this;
        }

        public Builder mflo() {
            this.transfiereLo = true;
            return this;
        }

        public Builder syscall() {
            this.esSyscall = true;
            return this;
        }

        public Builder halt() {
            this.esHalt = true;
            return this;
        }

        public SenalesControl build() {
            return new SenalesControl(this);
        }
    }
}
