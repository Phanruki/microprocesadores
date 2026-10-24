# Preparación de la exposición — explicación desde cero

> Este documento es para estudiar antes de exponer. Está escrito para quien
> "no sabe nada del tema": cada concepto se explica con palabras simples y
> con ejemplos del propio proyecto. Al final hay preguntas probables del
> profesor con sus respuestas.

---

## 0. La idea en 30 segundos (la "elevator pitch")

> "Emulamos un procesador MIPS en Java. El programa del usuario se convierte en
> instrucciones de 32 bits que se guardan en la memoria; la CPU, que nunca
> muere, arranca en la dirección 0 y va ejecutando un ciclo de 5 etapas
> (buscar, decodificar, ejecutar, memoria, escribir) hasta encontrar la
> instrucción `halt`, que le dice 'ya terminó el programa'. La memoria está
> dividida en segmentos con dirección: texto (ROM, 0..99), datos (RAM,
> 100..199) y disco (200..299). Todo validado con 201 pruebas automáticas."

---

## 1. ¿Qué es un procesador (sin jerga)?

Un procesador es una máquina que hace **muy pocas cosas, pero muy rápido**:

1. **Lee** una instrucción de la memoria.
2. **Entiende** qué pide (decodificar).
3. **La ejecuta** (sumar, comparar, leer/escribir memoria, saltar).
4. **Repite** desde el 1 con la siguiente instrucción.

Ese bucle se llama **ciclo de instrucción** (fetch–decode–execute). Todo lo
que hace tu computadora —desde abrir un navegador hasta renderizar este
documento— es la repetición miles de millones por segundo de ese ciclo.

Las "órdenes" que entiende el procesador no son `x = a + b` sino **instrucciones
en binario**, cada una de **32 bits** (una "palabra"). Un programa, para el
procesador, es simplemente una lista de palabras en memoria.

## 2. ¿Qué es MIPS y por qué lo elegimos?

**MIPS** es una arquitectura de procesador (un "diseño" con reglas definidas):
conjunto de instrucciones **RISC** (Reduced Instruction Set Computer): pocas
instrucciones, todas del mismo tamaño (32 bits) y con **3 formatos fijos**:

- **Formato R** (operaciones registro↔registro, p. ej. `add $t0, $t1, $t2`):
  opcode 0x00 + rs + rt + rd + shamt + funct.
- **Formato I** (operaciones con un número "en la propia instrucción" o
  memoria, p. ej. `addi`, `lw`, `sw`, `beq`): opcode + rs + rt + inmediato.
- **Formato J** (saltos largos: `j`, `jal`): opcode + dirección.

Elegimos MIPS (vs. RISC-V, x86, M1, AMD) porque sus formatos fijos hacen
fácil escribir el **decodificador**, y su documentación es abierta y clara.
Además, los conceptos son los mismos que en cualquier procesador real.

## 3. Las piezas del procesador que implementamos (los "5 paquetes grandes")

Organizamos el núcleo en 5 paquetes grandes, igual que el diagrama del
compañero (`docs/practica/DiagramaDiego.jpg`):

| Paquete | Qué es en la vida real | Clases |
|---|---|---|
| `cpu` | El "cerebro" que orquesta todo: el ciclo de 5 etapas | `CPU`, `PC` |
| `ejecucion` | La "unidad de control" y el que aplica las órdenes | `EjecutorInstrucciones`, `UnidadControl`, `SenalesControl` |
| `memoria` | Dónde viven las instrucciones y los datos | `MemoriaSistema`, `UnidadAlmacenamiento`, `ROM`, `RAM`, `DiscoDuro`, `Registros` |
| `alu` | La "calculadora": suma, resta, comparaciones | `ALU`, `OperacionALU` |
| `instruccion` | El "diccionario" de instrucciones: cómo se codifican y decodifican | `Instruccion`, `Decodificador`, `Opcode`, `Funct`, `ServicioSyscall`, `ProgramaBuilder`, ... |

Fuera del núcleo (pero usando lo): `ui` (la ventana), `programas` (los 5
programas de usuario) y `pruebas` (las 201 pruebas).

### 3.1 Registros (`memoria/Registros`)

Los **registros** son la memoria más rápida del procesador: 32 celdas de 32
bits dentro de la CPU ( `$zero`, `$v0`, `$a0`, `$t0`... `$ra` ). Las
instrucciones aritméticas solo operan con registros; para usar un número
"en la instrucción" o leer memoria hay que traerlo antes a un registro.

- `$zero` siempre vale 0 (y si intentas escribirlo, se ignora).
- `$v0` guarda el servicio del `syscall`; `$a0` el argumento.

### 3.2 PC — Contador de programa (`cpu/PC`)

El PC ("Program Counter") guarda **la dirección de la siguiente instrucción**.
En cada ciclo, tras buscar la instrucción, el PC se incrementa para apuntar a
la siguiente. Los saltos (`j`, `beq`, `jr`) escriben una dirección distinta.

### 3.3 ALU (`alu/ALU`)

La ALU es la calculadora: suma (`add`/`addu`), resta (`sub`/`subu`), lógica
(`and`, `or`, `xor`, `nor`), comparaciones (`slt`, `sltu`) y desplazamientos
(`sll`, `srl`). En nuestro emulador también hace `lui` (carga en la palabra
alta). MIPS no tiene "banderas" (flags): para comparar se usa `slt` y luego
una rama `beq`/`bne`.

### 3.4 Memoria y segmentos con dirección (`memoria/`)

La memoria es un arreglo de **celdas de 32 bits**, y cada celda tiene una
**dirección**. La dividimos en **segmentos**; cada segmento es una unidad de
almacenamiento con **nombre** y **dirección base**:

| Segmento | Nombre | Direcciones | Qué guarda |
|---|---|---|---|
| ROM | "texto" | 0 .. 99 | las instrucciones (se bloquea tras cargar) |
| RAM | "datos" | 100 .. 199 | los datos del programa (arranca en 000) |
| Disco duro | "disco" | 200 .. 299 | almacenamiento; se guarda/carga en archivo |

`MemoriaSistema` recibe cualquier dirección y la enruta al segmento que la
contiene. El volcado muestra la dirección en **decimal y hexadecimal**
(p. ej. `dir 181 (0x000000B5) = 0`). En un procesador de verdad ocurre igual:
el programa vive en una zona (texto/código) y los datos en otra (pila, heap).

### 3.5 Señales de control (`ejecucion/SenalesControl`)

La **unidad de control** mira la instrucción decodificada y produce un
conjunto de **señales** (booleanas) que le dicen al camino de datos qué hacer
este ciclo: ¿escribe registro? ¿lee memoria? ¿usa el inmediato? ¿hay salto?
Ej.: para `lw`, las señales son "usa inmediato + lee memoria + escribe
registro". Las construimos con un **Builder** para no equivocarnos al pasar
argumentos.

## 4. El ciclo de 5 etapas (el corazón de la exposición)

`CPU.ciclo()` recorre, en orden, 5 etapas:

1. **IF (buscar)**: se lee la instrucción en la dirección del PC; el PC queda
   apuntando a la siguiente.
2. **ID (decodificar)**: se separan los campos (opcode, rs, rt, rd, shamt,
   funct o inmediato) y la unidad de control genera las señales.
3. **EX (ejecutar)**: la ALU calcula; el ejecutor aplica las señales.
4. **MEM (memoria)**: si es `lw`, se lee la memoria; si es `sw`, se escribe.
5. **WB (escribir)**: el resultado llega al registro destino.

Después se repite. **Todo** lo que hace un procesador —incluidos los bucles y
las condiciones— se reduce a repetir esas 5 etapas.

## 5. ¿Qué es `halt` y por qué la CPU "nunca muere"?

Aquí está la idea que pidió el profesor:

- **La CPU nunca muere**: arranca siempre en la dirección **0** y siempre tiene
  algo que ejecutar. La celda 0 de la memoria contiene `0x00000000`, que es un
  **NOP** (no hace nada), así que si no hay programa, la CPU sigue "viva"
  ejecutando NOPs.
- El **PC es circular**: si se sale del mapa de memoria, vuelve a 0 y sigue.
- Pero entonces, ¿cómo sabe la CPU que el programa acabó? Con una instrucción
  especial propia de nuestro emulador: **`halt`** (opcode `0x3F`, "alto"). Es
  como el botón de paro: le avisa a la CPU "el programa terminó, puedes
  detenerte".
- También se detiene con la **llamada al sistema 10** (`syscall` con `$v0=10`,
  "terminar"), que es la forma "de MIPS" de salir.
- Por seguridad, hay un límite de **100000 ciclos** (`LIMITE_CICLOS_SEGURIDAD`):
  si un programa no trae `halt`, la CPU no se queda trabada para siempre.

En resumen: la CPU es un trabajador que no se rinde; solo se va cuando le
dicen "alto" (`halt`) o "es todo por hoy" (`syscall 10`).

## 6. Syscall: cómo habla el programa al "mundo exterior"

`syscall` es una instrucción que le pide un servicio al emulador. El servicio
se elige con `$v0`:

| $v0 | Servicio |
|---|---|
| 1 | imprimir entero (de `$a0`) |
| 4 | imprimir cadena (dirección en `$a0`) |
| 5 | leer entero (resultado en `$v0`) |
| 11 | imprimir carácter |
| 10 | terminar programa (detiene la CPU) |

En un procesador real, `syscall` es la puerta al sistema operativo (entrar y
salir del modo usuario). En el emulador, imprimir por consola.

## 7. Los 5 programas de usuario (lo que exponemos como evidencia)

Se construyen con `ProgramaBuilder` (no escribiendo binario a mano) y terminan
siempre con `halt()`:

| Programa | Qué hace | Salida real | Ciclos |
|---|---|---|---|
| `sumatoria` | suma de 3 a 10 | `Sumatoria de 3 a 10 = 52` | 68 |
| `factorial` | 7! | `Factorial de 7 = 5040` | 63 |
| `integral` | ∫x² dx de 1 a 5 | `Integral de x^2 dx desde x=1 hasta x=5 = 30` | 56 |
| `promedio` | promedio de {4,8,15,16,23} | `Promedio = 13` | 123 |
| `perceptron` | perceptrón con entradas fijas | `z = 3  y = 1` | 62 |

## 8. Pruebas: cómo sabemos que funciona

- **Unitarias** (117): una unidad por vez (ALU, registros, decodificador,
  segmentos de memoria, RAM en 000, HALT...).
- **Integración** (44): varias unidades juntas (p. ej. ciclo completo, ramas,
  saltos, syscall).
- **Sistema** (40): los 5 programas completos con su salida esperada; incluyen
  "la CPU nunca muere" (PC circular, límite de seguridad).
- **Total: 201 pruebas, todas pasan** (`./probar.sh`).

## 9. Demo en vivo sugerida (3 minutos)

1. `./compilar.sh` → "Compilacion OK".
2. `java -cp out ui.Emulador factorial --traza --estado` → señalizar el traza:
   PC, instrucción en binario y ensamblador, y el resultado `5040`.
3. Mostrar el mapa de memoria (segmentos y direcciones hexadecimales).
4. Abrir `./ventana.sh`, cargar el ejemplo, pulsar "Ejecutar 1 ciclo" varias
   veces mostrando las 5 etapas y las señales, y luego "Ejecutar programa" hasta
   el `halt`.

---

# Preguntas probables del profesor (y cómo responderlas)

**P1: ¿Por qué un emulador y no un procesador real?**
R: Porque se puede construir, observar y modificar entero en software: se ven
los registros, la memoria y el ciclo instrucción a instrucción, algo imposible
de ver en silicio. Es la misma idea que un emulador de vuelo: se aprende el
sistema arriesgando cero.

**P2: ¿Por qué MIPS y no x86 o RISC-V?**
R: MIPS es RISC con 3 formatos fijos y conjunto pequeño: el decodificador es
simple y la práctica se centra en la arquitectura, no en la complejidad del
formato. x86 tiene instrucciones de longitud variable y microcódigo, fuera del
alcance. RISC-V también habría servido; MIPS tiene documentación muy didáctica.

**P3: ¿Qué significa que la "CPU nunca muere"?**
R: Que no existe un estado donde la CPU se apague sola. Arranca en 0, el PC es
circular (si sale del mapa vuelve a 0) y la celda 0 es un NOP, así que siempre
hay algo que ejecutar. Solo se detiene con `halt` o `syscall 10` (más un límite
de seguridad de 100000 ciclos).

**P4: ¿Por qué inventaron una instrucción `halt` si MIPS no la tiene?**
R: Porque en el MIPS real "terminar" es una llamada al sistema (o se llega al
final de un segmento protegido); en un emulador educativo queremos una orden
explícita y visible, el "botón de alto", que además permite mostrar el estado
final de la CPU y la memoria. Es una extensión propia, documentada como tal
(opcode 0x3F).

**P5: ¿Qué es un segmento y qué tiene que ver la dirección?**
R: Un segmento es una zona de la memoria con un nombre y una dirección base:
texto (ROM) en 0, datos (RAM) en 100, disco en 200; cada uno de 100 celdas de
32 bits. `MemoriaSistema` enruta cada acceso según la dirección. Es lo que
hacen los sistemas reales: separar código de datos (protege el código de
modificaciones accidentales, como nuestra ROM "bloqueada").

**P6: Explícame el ciclo de 5 etapas con un ejemplo.**
R: Con `addi $t0, $zero, 5`: IF busca la instrucción en ROM[PC] y suma 1 al PC;
ID separa opcode (0x08), rs (0) y rt (t0), y la unidad de control emite "usa
inmediato, ALU = ADD, escribe registro"; EX hace la ALU(0 + 5) = 5; MEM no hace
nada (no es memoria); WB escribe 5 en `$t0`.

**P7: ¿Qué pasa si un programa no tiene `halt`?**
R: La CPU sigue ejecutando: entra en el NOP de la celda 0 y da vueltas (PC
circular) hasta el límite de 100000 ciclos, donde el emulador la detiene por
seguridad y lo reporta ("el programa no tiene HALT"). La CPU nunca muere, pero
el emulador tiene un timbre de seguridad.

**P8: ¿Cómo manejan el desbordamiento (overflow)?**
R: `add`/`sub`/`addi` detienen la CPU si desbordan (como una excepción); las
variantes sin signo `addu`/`subu`/`addiu` envuelven sin avisar. Los programas
usan las variantes sin signo para no detenerse jamás por desborde.

**P9: ¿Qué es `syscall` y por qué `$v0`?**
R: Es la instrucción de "llamada al sistema": pone el número de servicio en
`$v0` (1 imprimir entero, 4 cadena, 5 leer, 10 terminar) y los argumentos en
`$a0`. En un procesador real es la frontera entre el programa y el sistema
operativo; aquí, la puerta a imprimir/leer/terminar.

**P10: ¿Cómo saben que su emulador funciona?**
R: Con 201 pruebas automáticas en 3 niveles (117 unitarias, 44 de integración,
40 de sistema) que se ejecutan con `./probar.sh`, más capturas de las 5
ejecuciones de usuario final en `salidas/`. Las de sistema verifican también
que la CPU nunca muere (PC circular, límite de ciclos) y que la RAM arranca en
000.

**P11: ¿Por qué 32 registros y no 8?**
R: Es el diseño real de MIPS (y de muchas arquitecturas RISC): muchos
registros para que el compilador pueda dejar variables "vivas" en registros y
no vaya a memoria a cada paso. Además cada instrucción solo necesita 5 bits por
campo de registro (2⁵ = 32), lo que cabe en los 32 bits.

**P12: ¿Qué hizo la IA y qué hicieron ustedes?**
R: La IA ayudó a generar código, planes de pruebas y explicaciones; el equipo
tomó las decisiones (MIPS, HALT propio, mapa segmentado, límite de ciclos),
ejecutó las pruebas y corrigió lo que falló. Los prompts están declarados en el
reporte, como pide la práctica.

**P13: ¿En qué se diferencia su emulador del del compañero?**
R: Mismo esqueleto de 5 unidades (cpu, ejecución, memoria, ALU, instrucción);
las diferencias que aportamos: segmentos con dirección base visible en el
volcado (decimal + hex), CPU que nunca muere (PC circular + NOP en 0 +
límite), instrucción `halt` explícita y las señales de control visibles en la
ventana.

**P14: ¿Qué es el ensamblador y cómo se relaciona con todo esto?**
R: El ensamblador traduce mnemónicos (`addi $t0, $zero, 5`) a la palabra
binaria de 32 bits que la CPU entiende. Aquí ese trabajo lo hace
`ProgramaBuilder` + `Decodificador` (en sentido inverso, `Desensamblador`
muestra el mnemónico de una palabra).

**P15: Si les piden agregar una instrucción nueva, ¿qué tocarían?**
R: 1) agregar el opcode/funct en `Opcode`/`Funct`; 2) decodificarla en
`Decodificador` (si necesita campos nuevos); 3) darle señales en
`UnidadControl`; 4) ejecutarla en `EjecutorInstrucciones` (y si es cálculo,
la operación en `ALU`/`OperacionALU`); 5) agregar un método de conveniencia en
`ProgramaBuilder`; 6) pruebas unitarias + de integración. El diseño modular
hace que el cambio esté confinado a esos archivos.

---

## Frases para no trabarse

- "En una frase: la CPU busca, decodifica, ejecuta, accede a memoria y escribe
  el resultado; y repite hasta que le dicen `halt`."
- "El PC es el puntero de '¿por dónde vamos?'; los saltos cambian el rumbo."
- "La memoria es un cuadro de celdas con dirección; nosotros la separamos en
  tres segmentos: texto, datos y disco."
- "La unidad de control es el semáforo: sus señales le dicen a cada pieza qué
  hacer en este ciclo."
