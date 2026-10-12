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

Clase: `src/almacenamiento/Registros.java`

### Contador de programa (PC)

- El PC de 32 bits contiene la direccion de la siguiente instruccion.
- Durante la busqueda de instrucciones el PC se incrementa para apuntar
  a la siguiente instruccion.
- Las ramas (beq, bne, j, jal, jr) escriben la direccion destino en el PC.

Clase: `src/almacenamiento/ContadorPrograma.java`

### Disco duro

Copia de la clase unidad de almacenamiento llamada DiscoDuro
(extends UnidadAlmacenamiento):

- ancho de palabra: 32 bits
- celdas: 100
- ademas guarda y carga su contenido en un archivo (guardar/cargar)

Clase: `src/almacenamiento/DiscoDuro.java`

### RAM

- ancho de palabra: 32 bits
- celdas: 100
- guarda los datos iniciales del programa

Clase: `src/almacenamiento/RAM.java`

### ROM

- ancho de palabra: 32 bits
- celdas: 100
- guarda las instrucciones
- se bloquea despues de cargar el programa

Clase: `src/almacenamiento/ROM.java`

### Mapa de direcciones

Las direcciones son por palabras de 32 bits:

| Rango | Unidad | Uso |
|-------|--------|-----|
| 0 a 99 | ROM | instrucciones |
| 100 a 199 | RAM | datos del programa |
| 200 a 299 | Disco duro | almacenamiento |

Clase: `src/mips/MemoriaSistema.java`

### Unidad de almacenamiento base

`UnidadAlmacenamiento(anchoPalabra, cantidadCeldas)` con leer(direccion),
escribir(direccion, valor), validacion de rango, limpiar() y volcar().

Clase: `src/almacenamiento/UnidadAlmacenamiento.java`

---

## ALU (Paso 2)

Operaciones de 32 bits (`src/alu/ALU.java`):

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
| NOP | sin operacion |

MIPS no tiene registros de flags: las comparaciones y ramas se hacen con
slt/slti/sltu seguido de beq o bne. En los programas se prefieren las
variantes sin signo (addu, subu, addiu) para que nunca haya excepcion por
desbordamiento.

---

## Ciclo de la CPU

1. FETCH: instr = ROM[PC]; PC = PC + 1
2. DECODE: separar opcode, rs, rt, rd, shamt, funct o inmediato
3. EXECUTE: ALU, memoria o rama
4. WRITEBACK: escribir el registro destino

Implementacion: `ProcesadorMIPS.ciclo()` en `src/mips/ProcesadorMIPS.java`.

## Instrucciones soportadas

R: add addu sub subu and or xor nor slt sltu sll srl mult div mflo mfhi
   jr syscall
I: addi addiu andi ori xori slti sltiu lui lw sw beq bne
J: j jal

El detalle de cada una esta en `docs/instrucciones.md`.

Los programas de `src/programas/` se arman con `mips.ProgramaBuilder`,
que emite estas instrucciones y resuelve las etiquetas de las ramas.

Syscall: 1 imprimir entero, 4 imprimir cadena, 11 imprimir caracter,
5 leer entero, 10 terminar.
