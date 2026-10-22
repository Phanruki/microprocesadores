# Plan de pruebas unitarias - Simulador MIPS

- Alcance: clases unidad (`almacenamiento`, `alu`, `Instruccion`, `ProgramaBuilder`)
- Herramienta propia: `src/pruebas/MiniTest.java`
- Codigo: `src/pruebas/PruebasUnitarias.java`
- Ejecucion: `java -cp out pruebas.PruebasUnitarias` o `./probar.sh`

## 1. UnidadAlmacenamiento

| Prueba | Esperado |
|--------|----------|
| ancho de palabra | 32 bits |
| cantidad de celdas | 100 |
| lectura celda 0 | valor escrito |
| lectura celda 99 | valor escrito |
| escritura fuera de rango (100) | ArrayIndexOutOfBoundsException |
| lectura fuera de rango (-1) | ArrayIndexOutOfBoundsException |
| limpiar() | celda 0 = 0 |
| constructor con celdas <= 0 | IllegalArgumentException |

## 2. RAM / ROM

| Prueba | Esperado |
|--------|----------|
| RAM ancho 32 bits | 32 |
| RAM 100 celdas | 100 |
| RAM escritura/lectura | valor leido = escrito |
| ROM ancho 32 bits | 32 |
| ROM 100 celdas | 100 |
| ROM bloqueada rechaza escritura | IllegalStateException |
| ROM desbloqueada permite escritura | ok |
| cargarPrograma celda 0 | primer valor cargado |
| cargarPrograma celda 3 | cuarto valor cargado |
| ROM bloqueada tras cargarPrograma | escritura rechazada |

## 3. DiscoDuro

| Prueba | Esperado |
|--------|----------|
| ancho de palabra | 32 bits |
| celdas | 100 |
| guardar/cargar celda 0 | valor persistido en archivo |
| guardar/cargar celda 1 | valor persistido en archivo |
| importar desde RAM | contenido copiado |
| exportar a RAM | contenido copiado |

## 4. Registros

| Prueba | Esperado |
|--------|----------|
| cantidad de registros | 32 |
| escritura/lectura $t0 | valor leido = escrito |
| $zero siempre es 0 | escribir en 0 no cambia el valor |
| indice invalido | IllegalArgumentException |
| limpiar registros | todos en 0 |

## 5. Contador de programa

| Prueba | Esperado |
|--------|----------|
| PC inicia en 0 | 0 |
| PC incrementa en 1 | 1 (siguiente instruccion) |
| PC tras 3 incrementos | 3 |
| PC set manual | valor escrito |
| PC reiniciar | 0 |
| ancho del PC | 32 bits |

## 6. ALU

| Prueba | Esperado |
|--------|----------|
| ADD 2+3 | 5 |
| ADD negativos | -8 |
| ADD con desbordamiento | ArithmeticException |
| ADDU MAX+1 | envuelve a MIN (sin excepcion) |
| SUB 10-4 | 6 |
| SUB con desbordamiento | ArithmeticException |
| SUBU MIN-1 | envuelve a MAX |
| AND, OR, XOR, NOR | resultados bitwise |
| SLT con signo | 1 o 0 segun signo |
| SLTU sin signo | -1 > 1 sin signo (no usa flags) |
| SLL / SRL | desplazamientos |

MIPS no tiene flags: no hay pruebas de Z/N/C/V; las comparaciones se hacen
con SLT/SLTU.

## 7. Codificacion de instrucciones

| Prueba | Esperado |
|--------|----------|
| opcode de R-type | 0x00 |
| rs/rt/rd/funct de add | campos correctos |
| funct de addu / subu / sltu | 0x21 / 0x23 / 0x2B |
| opcode de addi / addiu | 0x08 / 0x09 |
| extension de signo de -3 | 0xFFFD |
| opcode de sltiu | 0x0B |
| opcode/direccion de j | 0x02 y direccion |
| funct de syscall | 0x0C |

## 8. ProgramaBuilder

| Prueba | Esperado |
|--------|----------|
| 3 instrucciones generadas | longitud = 3 |
| cargarConstante(42) | usa addiu con inmediato 42 |
| constante de 32 bits | ocupa lui + ori |
| datos en celda 100 | primera palabra en RAM[0] |
| cadena | ocupa 4 + nulo celdas |
| lwAbs con direccion absoluta | opcode lw, offset correcto |
| offset de bne hacia adelante | destino - (indice + 1) |
| direccion de j | absoluta en palabras |
| etiqueta sin definir | IllegalStateException al generar |
| mas de 100 instrucciones | IllegalStateException |
