# Arquitectura del sistema - Practica 2

## Procesador seleccionado (Paso 2)

- [ ] RISC-V
- [x] MIPS
- [ ] x86
- [ ] M1
- [ ] AMD

MIPS es RISC con formatos de instruccion fijos (R, I y J), lo que facilita
implementar el simulador por unidades funcionales.

---

## Estructura: 5 paquetes grandes

El nucleo del emulador esta organizado en 5 paquetes grandes, al estilo del
diagrama del compañero (`docs/DiagramaDiego.jpg`):

| Paquete | Contenido |
|---------|-----------|
| `cpu` | `CPU` (el ciclo de 5 etapas) y `PC` (contador de programa) |
| `ejecucion` | `EjecutorInstrucciones`, `UnidadControl` y `SenalesControl` |
| `memoria` | `MemoriaSistema`, `UnidadAlmacenamiento`, `ROM`, `RAM`, `DiscoDuro`, `Registros` |
| `alu` | `ALU` y el enum `OperacionALU` |
| `instruccion` | `Instruccion`, `Decodificador`, `Opcode`, `Funct`, `FormatoInstruccion`, `ServicioSyscall`, `ProgramaBuilder`, `Desensamblador`, `Binario` |

Fuera del nucleo (clientes que usan el nucleo): `ui` (interfaz),
`programas` (los 5 programas de usuario) y `pruebas` (el plan de pruebas).

---

## Paso 1. Arquitectura del almacenamiento de datos

### Ancho de palabra

Todas las unidades usan ancho de palabra de 32 bits.

### Registros del procesador

- 32 registros de 32 bits ($zero ... $ra)
- $zero siempre lee 0; las escrituras se ignoran
- $at reservado para el ensamblador
- $v0-$v1 valores de retorno / servicio syscall
- $a0-$a3 argumentos
- $t0-$t9 temporales
- $s0-$s7 salvados por el llamador
- $sp, $fp, $ra pila, marco y retorno

Clase: `src/memoria/Registros.java`

### Contador de programa (PC)

- El PC de 32 bits contiene la direccion de la siguiente instruccion.
- Durante la busqueda de instrucciones el PC se incrementa para apuntar
  a la siguiente instruccion.
- Las ramas (beq, bne, j, jal, jr) escriben la direccion destino en el PC.
- **El PC es circular**: si se sale del mapa de memoria vuelve a 0, porque
  la CPU nunca muere.

Clase: `src/cpu/PC.java`

### Segmentos y direcciones

La memoria esta dividida en **segmentos**; cada segmento es una
`UnidadAlmacenamiento` con **nombre** y **direccion base** dentro del mapa
total (palabras de 32 bits):

| Segmento | Clase | Nombre | Direcciones | Celdas | Uso |
|----------|-------|--------|-------------|--------|-----|
| Texto | `ROM` | "texto" | 0 a 99 | 100 | instrucciones (se bloquea tras cargar) |
| Datos | `RAM` | "datos" | 100 a 199 | 100 | datos del programa (inicia en 000) |
| Disco | `DiscoDuro` | "disco" | 200 a 299 | 100 | almacenamiento; guarda/carga en archivo |

`MemoriaSistema` enruta cada acceso al segmento que contiene la direccion
(`segmentoDe(direccion)`), y el volcado `mapa()` muestra todas las celdas con
su direccion en decimal y en hexadecimal (0x).

- `ROM.cargarPrograma(programa, 0)` carga el codigo en el segmento de texto y
  `bloquear()` impide escribirlo despues.
- `RAM` arranca en 000 (verificado por pruebas).
- `DiscoDuro` ademas guarda y carga su contenido en un archivo
  (`guardar(ruta)` / `cargar(ruta)`), y puede importar/exportar el contenido
  de otra unidad de almacenamiento.

Clases: `src/memoria/` (`UnidadAlmacenamiento`, `ROM`, `RAM`, `DiscoDuro`,
`MemoriaSistema`)

### Unidad de almacenamiento base

`UnidadAlmacenamiento(nombre, direccionBase, anchoPalabra, cantidadCeldas)`
con `leer(direccion)`, `escribir(direccion, valor)`, validacion de rango,
`rellenar(valor)`, `limpiar()` y `volcar(titulo)`.

Clase: `src/memoria/UnidadAlmacenamiento.java`

---

## CPU que nunca muere

La CPU arranca siempre en la direccion 0 y **nunca muere sola**:

- La celda 0 del mapa contiene la palabra `0x00000000`, que es un NOP: si la
  CPU no encuentra programa sigue ejecutando NOPs, no se detiene.
- El PC es **circular**: si sale del mapa de memoria vuelve a 0 y sigue.
- La unica forma de parar la CPU es la instruccion **HALT** (opcode 0x3F,
  "alto") o la llamada al sistema 10 (terminar).
- Ademas existe un limite de seguridad `LIMITE_CICLOS_SEGURIDAD = 100000`
  ciclos por si un programa no trae HALT.

Clase: `src/cpu/CPU.java`

---

## ALU (Paso 2)

Operaciones de 32 bits (`src/alu/ALU.java`, enum `OperacionALU`):

| Op | Significado |
|----|-------------|
| ADD | suma con signo; si desborda detiene el programa |
| ADDU | suma sin signo; envuelve sin avisar |
| SUB | resta con signo; si desborda detiene el programa |
| SUBU | resta sin signo; envuelve sin avisar |
| AND | y logico |
| OR | o logico |
| XOR | o exclusivo |
| NOR | ni logico |
| SLT | 1 si rs < rt con signo |
| SLTU | 1 si rs < rt sin signo |
| SLL | desplazamiento logico a la izquierda |
| SRL | desplazamiento logico a la derecha |
| LUI | carga el inmediato en la palabra alta (imm << 16) |
| NOP | sin operacion |

MIPS no tiene registros de flags: las comparaciones y ramas se hacen con
slt/slti/sltu seguido de beq o bne. En los programas se prefieren las
variantes sin signo (addu, subu, addiu) para que nunca haya excepcion por
desbordamiento.

---

## Ciclo de la CPU (5 etapas)

La `CPU.ciclo()` recorre linealmente las 5 etapas del camino de datos:

1. **IF** (busqueda): `etapaBusqueda()` - instruccion = ROM[PC]; PC = PC + 1
   (con PC circular si la direccion no existe en el mapa).
2. **ID** (decodificacion): `etapaDecodificacion()` - `Decodificador` separa
   opcode, rs, rt, rd, shamt, funct o inmediato, y `UnidadControl` genera las
   `SenalesControl` del ciclo.
3. **EX** (ejecucion): `etapaEjecucion()` - `EjecutorInstrucciones` aplica las
   senales sobre el camino de datos y la `ALU` calcula.
4. **MEM** (memoria): `etapaMemoria()` - lw lee y sw escribe en
   `MemoriaSistema`.
5. **WB** (escritura): `etapaEscritura()` - escribe el registro destino
   pendiente en `Registros`.

`ejecutar()` repite el ciclo hasta HALT o syscall 10, o hasta el limite de
seguridad de 100000 ciclos.

Implementacion: `src/cpu/CPU.java` + `src/ejecucion/`.

## Instrucciones soportadas

R: add addu sub subu and or xor nor slt sltu sll srl mult div mflo mfhi
   jr syscall
I: addi addiu andi ori xori slti sltiu lui lw sw beq bne
J: j jal
Emulador: halt (opcode 0x3F, "alto": detiene la CPU)

El detalle de cada una esta en `docs/instrucciones.md`.

Los programas de `src/programas/` se arman con `instruccion.ProgramaBuilder`,
que emite estas instrucciones y resuelve las etiquetas de las ramas, y
terminan siempre con `halt()` para que la CPU sepa que acabo.

Syscall: 1 imprimir entero, 4 imprimir cadena, 11 imprimir caracter,
5 leer entero, 10 terminar.
