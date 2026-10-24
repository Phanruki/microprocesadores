# Archivos de código para revisar en vivo

> Para la **etapa de preguntas (min. 45:00–55:00)** o para cuando el profesor
> dice *"abre el código y muéstrame..."*. Aquí está **dónde está cada cosa, con
> la línea exacta** (verificada) y qué decir al señalarla.
>
> Todas las rutas son **desde la raíz del proyecto**. Complementos:
> `preguntas_exposicion.md` (las preguntas y sus respuestas) y
> `demo_ejecucion.md` (la demo en pantalla).

---

## 0. Cómo abrir y no perderse (30 segundos)

```bash
code src/cpu/CPU.java            # VS Code
code -g src/cpu/CPU.java:119     # abrir directo en la línea 119
less -N src/cpu/CPU.java         # sin editor, con números de línea
vi +119 src/cpu/CPU.java         # o vi
```

Búsqueda de un método o palabra (esta es la que más vas a usar):

```bash
grep -n "etapaBusqueda" src/cpu/CPU.java         # ¿dónde está eso?
grep -rn "LIMITE_CICLOS" src/                    # en todo el código
grep -rn "ArithmeticException" src/              # ¿dónde detiene desbordamientos?
```

### Qué hay en cada paquete

```
src/
  cpu/          CPU (5 etapas) y PC              ← el cerebro
  ejecucion/    EjecutorInstrucciones, UnidadControl, SenalesControl  ← qué hacer
  alu/          ALU y enum OperacionALU          ← las operaciones
  memoria/      MemoriaSistema, ROM, RAM, DiscoDuro, Registros, UnidadAlmacenamiento
  instruccion/  Instruccion, Decodificador, Opcode, Funct, Desensamblador,
                Binario, ProgramaBuilder, ServicioSyscall, Syscall           ← la ISA
  programas/    Sumatoria, Factorial, Integral, Promedio, Perceptron, Impresion
  pruebas/      MiniTest, PruebasUnitarias, PruebasIntegracion, PruebasSistema
  ui/           Emulador (texto) y VentanaEmulador (ventana)
```

**Dónde NO buscar**: `out/` son los `.class` generados (no se abre), y
`src/ui/` solo contiene interfaz: la lógica nunca está ahí.

**Regla de oro**: el 90 % de lo que puede pedir está en estos 12 archivos:

| # | Archivo | Líneas | Para qué lo piden |
|---|---------|--------|-------------------|
| 1 | `src/cpu/CPU.java` | 383 | el ciclo de 5 etapas, el PC, la parada |
| 2 | `src/instruccion/Instruccion.java` | 96 | cómo es una instrucción de 32 bits |
| 3 | `src/instruccion/Decodificador.java` | 88 | cómo se extraen los campos del binario |
| 4 | `src/ejecucion/EjecutorInstrucciones.java` | 107 | dónde se ejecuta de verdad |
| 5 | `src/ejecucion/UnidadControl.java` | 115 | las señales de control |
| 6 | `src/alu/ALU.java` | 55 | operar y detectar desbordamiento |
| 7 | `src/memoria/Registros.java` | 64 | los 32 registros y `$zero` |
| 8 | `src/memoria/MemoriaSistema.java` | 91 | el mapa ROM/RAM/disco |
| 9 | `src/memoria/UnidadAlmacenamiento.java` | 138 | leer/escribir y validar direcciones |
| 10 | `src/cpu/PC.java` | 45 | el contador de programa |
| 11 | `src/ui/Emulador.java` | 205 | los flags y el bucle de la traza |
| 12 | `src/pruebas/PruebasIntegracion.java` | 326 | las pruebas que "ven" la CPU |

---

## 1. Fichas de los archivos principales

### 1.1 `src/cpu/CPU.java` (383 líneas) — el cerebro

**Qué es**: junta todo: memoria, registros, ALU, unidad de control y PC, y los
hace avanzar en 5 etapas.

| Línea | Qué hay | Frase para decir |
|-------|---------|------------------|
| `35` | `LIMITE_CICLOS_SEGURIDAD = 100000` | "red de seguridad por si un programa no termina" |
| `82` | `reiniciar()` | "limpia ROM, RAM, registros, PC, HI/LO: vuelta a cero" |
| `119` | `ciclo()` | "el corazón: IF → ID → EX → MEM → WB, y solo entonces `ciclos++`" |
| `120` | `if (detenido) return;` | "si está detenida, ni siquiera entra: 0 ciclos, como en la ventana" |
| `136` | `etapaBusqueda()` | "lee la palabra que apunta el PC y deja el PC en +1" |
| `138-142` | `if (!memoria.contiene(direccion)) → 0` | "el PC es circular: si se sale del mapa vuelve a 0. **Por eso la CPU nunca muere**" |
| `146` | `etapaDecodificacion()` | "Decodificador + UnidadControl: qué es y qué hay que hacer" |
| `159/164/175` | `etapaEjecucion/Memoria/Escritura` | "las otras tres etapas; la lógica está en `EjecutorInstrucciones`" |
| `186` | `programarEscritura()` | "la escritura en WB se **programa** aquí" |
| `218` | `syscall()` | "servicios del sistema: imprimir entero/cadena, leer, terminar" |
| `273` | `ejecutar()` | "bucle `while (!detenido && ciclos < 100000)`: esto es el modo texto" |
| `330` | `detener()` | "el único **método** que apaga la CPU y lo llama el `halt`; el `syscall 10` pone `detenido = true` directo en la **línea 254**" |

### 1.2 `src/instruccion/Instruccion.java` (96 líneas) — la instrucción

| Línea | Qué hay | Frase para decir |
|-------|---------|------------------|
| `14` | `PALABRA_NOP = 0` | "la palabra 0 es un `sll` a `$zero`: no hace nada" |
| `59` | `esNop()` | "decodifica y comprueba si es la palabra 0" |
| `64` | `esHalt()` | "opcode `0x3F`: no es un número mágico, está en el enum" |
| `72/81/88` | `codificarR/codificarI/codificarJ` | "aquí se **arma** la palabra con corrimientos y máscaras" |
| `93` | `codificarHalt()` | "la instrucción de parada, hecha con las mismas reglas que el resto" |

### 1.3 `src/instruccion/Decodificador.java` (88 líneas) — de la palabra a los campos

| Línea | Qué hay | Frase para decir |
|-------|---------|------------------|
| `15` | `decodificar(palabra)` | "primero saca el opcode de los 6 bits más significativos" |
| `45-73` | `opcode/rs/rt/rd/shamt/funct` | "cada campo es un desplazamiento y una máscara: `((p >> 21) & 0x1F)`" |
| `75` | `inmediatoSinSigno()` | "para `andi/ori/lui` no se extiende el signo" |
| `80` | `extensionSigno16()` | "para `addi/lw/sw/beq` sí: 16 bits → 32" |

### 1.4 `src/instruccion/Opcode.java` (54) y `Funct.java` (50) — la ISA

- `Opcode.java:13-28` → **16 entradas**: R, J, JAL, BEQ, BNE, ADDI, ADDIU,
  SLTI, SLTIU, ANDI, ORI, XORI, LUI, LW, SW, HALT (`0x3F`).
- `Funct.java:11-28` → **18 entradas** tipo R (SLL, SRL, JR, ADD, ADDU, SUB,
  SUBU, AND, OR, XOR, NOR, SLT, SLTU, MULT, DIV, MFLO, MFHI, SYSCALL).
- **18 R + 12 I + 2 J + halt = 33 instrucciones** (dato para la diapositiva 16).

### 1.5 `src/ejecucion/UnidadControl.java` (115) y `SenalesControl.java` (216)

| Línea | Qué hay | Frase para decir |
|-------|---------|------------------|
| `UnidadControl.java:22` | `decodificar(instr)` | "switch por opcode → paquete de señales" |
| `UnidadControl.java:61-62` | `case HALT` | "`alu(NOP).halt()`: ni siquiera usa la ALU" |
| `UnidadControl.java:70` | `senalesTipoR()` | "las tipo R se resuelven por `funct`" |
| `SenalesControl.java:16-55` | los 15 campos de la señal | "operación de la ALU, usaInmediato, leeMemoria, escribeMemoria, esRama, esSalto, esHalt..." |
| `SenalesControl.java:114-117` | `toString()` | "esto es lo que la ventana muestra en el campo `Senales:`" |

### 1.6 `src/ejecucion/EjecutorInstrucciones.java` (107) — dónde se ejecuta

| Línea | Qué hay | Frase para decir |
|-------|---------|------------------|
| `29` | `ejecutar(instr, senales)` | "recibe la instrucción **y** sus señales, y las aplica" |
| `31-34` | `if (senales.esHalt) cpu.detener()` | "el `halt` solo llama a `detener()`: sin magia" |
| `35-38` | `if (senales.esSyscall) cpu.syscall()` | "los servicios salen por aquí" |
| `49-60` | HI:LO y `division por cero` | "`mult/div` van a HI:LO; dividir entre 0 detiene con error" |
| `69-75` | `esRama` | "`beq/bne`: `a == b` o `a != b` y si se cumple, `saltarA(pc + inmediato)`" |
| `99` | `cpu.getAlu().operar(...)` | "**aquí** se calcula el resultado" |
| `104` | `registroDestino(instr)` | "tipo R escribe en `rd`; tipo I en `rt`" |

### 1.7 `src/alu/ALU.java` (55) — operar y desbordar

| Línea | Qué hay | Frase para decir |
|-------|---------|------------------|
| `11` | `operar(op, a, b, shamt)` | "un switch de 32 bits" |
| `13-19` | `ADD` con `throw new ArithmeticException` | "suma con signo: si no cabe, **detiene la ejecución**" |
| `22-28` | `SUB` con `throw` | "ídem la resta" |
| `20-21`, `29-30` | `ADDU`, `SUBU` | "sin signo: envuelven sin avisar, como el MIPS real" |
| `39-40` | `SLT` | "aquí no hay banderas: para comparar se usa `slt` + `beq/bne`" |

### 1.8 `src/memoria/Registros.java` (64) — los 32 registros

| Línea | Qué hay | Frase para decir |
|-------|---------|------------------|
| `9-10` | `CANTIDAD = 32`, `ANCHO = 32` | "32 registros de 32 bits" |
| `29-32` | `leer()` → `indice == 0 ? 0 : ...` | "**`$zero` siempre devuelve 0**" |
| `34-38` | `escribir()` → `if (indice != 0)` | "y **ignora la escritura en 0**: por eso un NOP nunca rompe nada" |
| `41` | `validar()` | "índice fuera de 0..31 → excepción clara" |
| `57` | `volcar()` | "es lo que imprime `--estado` y lo que la ventana pinta" |

### 1.9 `src/memoria/` — mapa y direcciones

| Línea | Qué hay | Frase para decir |
|-------|---------|------------------|
| `MemoriaSistema.java:35` | `segmentoDe(direccion)` | "0–99 ROM, 100–199 RAM, 200–299 disco: **una dirección, tres segmentos**" |
| `MemoriaSistema.java:62/67` | `leer/escribir` | "traduce dirección absoluta → local del segmento" |
| `MemoriaSistema.java:81` | `mapa()` | "el bloque `MAPA DE MEMORIA` de la terminal sale de aquí" |
| `UnidadAlmacenamiento.java:53` | `direccionAbsoluta()` | "la base del segmento + el índice local" |
| `UnidadAlmacenamiento.java:76-80` | `validarDireccion()` | "`Direccion fuera de rango: 100 (0..99)` — el error que verías si te pasas" |
| `UnidadAlmacenamiento.java:83/88` | `leer/escribir` | "guardar es `celdas[direccion] = valor`: un vector de enteros" |
| `ROM.java:17` | `DIRECCION_BASE = 0` | "la ROM empieza en 0" |
| `RAM.java:17` | `DIRECCION_BASE = 100` | "la RAM empieza en 100 (por eso `sw 100($zero)`)" |
| `DiscoDuro.java:23` | `DIRECCION_BASE = 200` | "el disco en 200" |

### 1.10 `src/cpu/PC.java` (45) — el contador

`23` `get()`, `27` `set()`, `32` `incrementar()`, `37` `reiniciar()`.
Frase: *"es un entero de 32 bits con nombre; las 5 etapas lo leen y lo escriben"*
(mira también `CPU.java:136-143`, que es quien lo avanza).

### 1.11 `src/instruccion/` — lo que se ve en pantalla

| Línea | Qué hay |
|-------|---------|
| `Desensamblador.java:30` | `mnemonico(palabra, pc)` → `addi $t0, $zero, 10` (campo `Instruccion:`) |
| `Desensamblador.java:138` | `operacion(...)` → `$t0 = $zero + 10 = 0 + 10 = 10` (campo `Operacion:`) |
| `Binario.java:16` | `a32(valor)` → los 32 bits que ves en la ventana |
| `Binario.java:33` | `parsear(texto)` → acepta binario, `0x`, comentarios `#` y `//` |
| `ServicioSyscall.java:9-13` | los 5 servicios: imprimir entero(1), cadena(4), leer(5), carácter(11), terminar(10) |

### 1.12 `src/instruccion/ProgramaBuilder.java` (361) — cómo se escriben los programas

| Línea | Qué hay | Frase para decir |
|-------|---------|------------------|
| `21` | `ROM_MAX = 100` | "el programa cabe en la ROM (100 palabras)" |
| `131` | `etiqueta(String)` | "aquí viven los bucles: `bucle:`, `fin:`" |
| `138-314` | un método por instrucción | "no hay ensamblador de texto: se arma desde Java" |
| `217` | `halt()` | "la instrucción de parada, igual que las demás" |
| `318` | `generar()` | "resuelve etiquetas y devuelve `{codigo, datos}`" |
| `334-343` | conversión final | "cada instrucción se codifica con `Instruccion.codificar*`" |

### 1.13 `src/ui/Emulador.java` (205) — modo texto

| Línea | Qué hay |
|-------|---------|
| `27-31` | sin argumentos (o `--gui`) → abre la ventana |
| `38-55` | los flags `--traza`, `--estado`, `--captura` |
| `114-124` | **el bucle con la traza**: `while (!cpu.isDetenido() && ...)` y la línea `[n] PC= x 0x...` |
| `136-141` | bloque `ESTADO FINAL` (`--estado`) |
| `178-185` | `imprimirUso()` (lo que dice `--ayuda`) |
| `187-204` | `construir(nombre)` → **aquí se registra cada programa** (`case "factorial": ...`) |

### 1.14 `src/ui/VentanaEmulador.java` (507) — la ventana

| Línea | Qué hay |
|-------|---------|
| `85` | mensaje inicial de la barra de estado |
| `239` | `ejecutarCiclos(max)` → la carga, el bucle y los mensajes de la barra |
| `275-281` | captura de excepciones → `ERROR: ...` en el Historial |
| `326` | `cargarEnRom()` → `Cargadas N instruccion(es)...` |
| `336` | `registrar()` → un bloque del Historial (Ciclo/PC/Instruccion/Binario/Senales/Operacion/PC Siguiente) |
| `359` | `cargarEjemplo()` → el programa de 6 instrucciones |
| `385` | `actualizar()` → paneles Registros, RAM, PC/HI/LO y `Ciclos:` |

### 1.15 `src/pruebas/` — dónde está cada prueba

| Archivo | Líneas | Qué contiene |
|---------|--------|--------------|
| `MiniTest.java` | 120 | `assertEquals`, `assertTrue`, `assertThrows` (el "junit" casero) |
| `PruebasUnitarias.java` | 370 | `50` memoria, `74` segmentos, `102` memorias, `131` disco, `161` registros, `175` PC, `191` ALU, `227` codificación, `277` halt, `295` ProgramaBuilder |
| `PruebasIntegracion.java` | 326 | `97` aritmética, **`124` desbordamiento detiene la ejecución**, `142` ramas, `171` bucle y acumulador, `190` memoria de datos, `222` syscall, `240` subrutina `jal/jr`, `257` mult/div, **`281` "la CPU nunca muere"**, **`313` "halt detiene la CPU"** |
| `PruebasSistema.java` | 90 | los 5 programas con su salida exacta |
| `EjecutorPruebas.java` | 50 | `main` (línea 9) que lanza las tres suites: `16`, `20`, `24` |

---

## 2. Mapa: "si pregunta por X → abre Y"

| La pregunta | Ábrese | Dónde mirar |
|---|---|---|
| ¿Dónde está el ciclo del procesador? | `src/cpu/CPU.java` | `119-131` (5 etapas) |
| ¿Qué hace cada etapa? | `src/cpu/CPU.java` | `136` IF, `146` ID, `159` EX, `164` MEM, `175` WB |
| ¿Dónde está el PC y cómo avanza? | `src/cpu/CPU.java` + `src/cpu/PC.java` | `CPU:136-143`, `PC:23/27/32` |
| ¿Cómo guarda las cosas en memoria? | `src/memoria/UnidadAlmacenamiento.java` | `83/88` (`celdas[direccion] = valor`) |
| ¿Qué es un "puntero"? | `src/memoria/MemoriaSistema.java` | `35` (dirección → segmento) |
| ¿Cómo distingue ROM de RAM? | `MemoriaSistema.java:35`, `ROM/RAM/DiscoDuro` | bases `0 / 100 / 200` |
| ¿Qué pasa si la dirección no existe? | `UnidadAlmacenamiento.java:76-80` | `throw` con mensaje claro |
| ¿Tiene operación de parada? | `src/instruccion/Instruccion.java` | `93` `codificarHalt()` |
| ¿Cómo sabe la CPU que paró? | `EjecutorInstrucciones.java:31` → `CPU.java:330/254` | `detenido = true` y `ciclo()` sale en la línea 120 |
| ¿Y si no le mando halt? | `CPU.java:138-142` + `CPU.java:35` | PC circular + límite de 100 000 ciclos |
| ¿Qué es la "operación 0"? | `Instruccion.java:14` + `Registros.java:34` | palabra 0 = NOP y `$rd=0` se ignora |
| ¿Dónde se decodifica? | `Decodificador.java:15` | opcode = `palabra >>> 26` |
| ¿Dónde están los códigos de operación? | `Opcode.java:13-28`, `Funct.java:11-28` | 16 + 18 entradas |
| ¿Dónde se generan las señales? | `UnidadControl.java:22` | switch por opcode |
| ¿Qué significa cada señal? | `SenalesControl.java:16-55` | los 15 campos |
| ¿Dónde se ejecuta la instrucción? | `EjecutorInstrucciones.java:29` | el `ejecutar()` completo |
| ¿Dónde está la ALU? | `src/alu/ALU.java` | `11-54` |
| ¿Cómo detecta el desbordamiento? | `ALU.java:13-19` | compara `long` con `(int) r` |
| ¿Por qué no tiene banderas? | `ALU.java:39` + `EjecutorInstrucciones.java:69` | `slt` + `beq/bne` |
| ¿Qué pasa con `mult`/`div`? | `EjecutorInstrucciones.java:49-60` | HI:LO y división por cero |
| ¿Cómo funciona `$zero`? | `Registros.java:29/34` | lectura fija 0, escritura ignorada |
| ¿Dónde se imprimen los datos? | `CPU.java:218-259` | `syscall()` con `$v0` y `$a0` |
| ¿Cómo se escribe un programa? | `ProgramaBuilder.java:138-314` | un método por instrucción |
| ¿Dónde está el factorial? | `src/programas/Factorial.java` (51 líneas) | `construir()` |
| ¿Cómo se desensambla? | `Desensamblador.java:30/138` | lo que ves en la ventana |
| ¿Qué hace la ventana? | `VentanaEmulador.java:239/336/385` | ejecutar / historial / paneles |
| ¿Cómo se lanzan las pruebas? | `pruebas/EjecutorPruebas.java:9` | tres suites |
| ¿Dónde se comprueba el halt? | `PruebasIntegracion.java:313` | `ciclos = 2 (addiu + halt)` |
| ¿Dónde se comprueba que nunca muere? | `PruebasIntegracion.java:281` | programa sin halt → límite |
| ¿Dónde está el bucle de la traza? | `Emulador.java:114-124` | `while (!detenido)` |

---

## 3. Comandos de terminal para enseñar ahora (sin abrir editor)

```bash
# el ciclo completo (13 líneas, se lee en 20 segundos)
sed -n '119,131p' src/cpu/CPU.java

# el PC circular (la respuesta a "la CPU nunca muere")
sed -n '136,143p' src/cpu/CPU.java

# el desbordamiento: 6 líneas
sed -n '13,21p' src/alu/ALU.java

# $zero
sed -n '29,38p' src/memoria/Registros.java

# el mapa de memoria
grep -n "DIRECCION_BASE" src/memoria/*.java

# las dos formas de apagar la CPU (¿solo hay dos?)
grep -rn "detenido = true" src/     # CPU:331 (halt) y CPU:254 (syscall 10)

# cuántas instrucciones tiene la ISA (16 opcodes y 18 functs -> 33)
grep -c '("' src/instruccion/Opcode.java src/instruccion/Funct.java

# la lista de pruebas con sus nombres
grep -n "private static void probar" src/pruebas/*.java | wc -l

# quién tocó qué y cuándo (si pregunta por el proceso)
git log --oneline | head -15
git log --stat --oneline | head -30
```

---

## 4. Los 5 programas y dónde vive cada cosa

| Programa | Archivo | Líneas | Qué hace |
|---|---|---|---|
| sumatoria | `src/programas/Sumatoria.java` | 56 | suma 3..10 → 52 (68 ciclos) |
| factorial | `src/programas/Factorial.java` | 51 | 7! = 5040 con bucle `bucle:` (63 ciclos) |
| integral | `src/programas/Integral.java` | 58 | Σx² de 1 a 5 = 30 (56 ciclos) |
| promedio | `src/programas/Promedio.java` | 79 | {4,8,15,16,23} → 13 (123 ciclos) |
| perceptron | `src/programas/Perceptron.java` | 80 | z=3, y=1 (62 ciclos) |
| (auxiliar) | `src/programas/Impresion.java` | 32 | cadenas e impresión vía `syscall` |

Se registran en `src/ui/Emulador.java:187-204` (el `switch` de nombres).

---

## 5. Si te pierdes dentro de un archivo

1. **Vuelve a la raíz**: `grep -n "palabra clave" src/**/*.java` antes que
   leer de arriba abajo.
2. **Busca el método, no la clase**: casi todo lo que piden es un método de
   10 líneas; las clases son envoltorios con getters.
3. **Si no lo encuentras, dice verdad el tabulador**: ábrelo con `less -N` y
   salta con `/nombreDelMetodo` y `n`.
4. **Frases de seguridad**:
   - *"Esa lógica no está en la UI: te la muestro en `CPU.java`"*.
   - *"La respuesta exacta con línea está en `preguntas_exposicion.md`"*.
   - *"Lo ves mejor en ejecución: lo lanzo con `--traza`"* (→ `demo_ejecucion.md`).

---

**Verificado**: todas las líneas de este documento se comprobaron contra los
archivos el 24/10/2026 (última revisión tras mover el material a
`docs/reporte_exposicion/exposicion/`).
