# Demo de ejecución: instrucciones, muestra real y cómo explicarla en pantalla

> Documento de trabajo para la **diapositiva 32 — "Demo en vivo (3 minutos)"**
> (`guion_exposicion.md`, min. 37:00 – 40:30). Está escrito para quien no ha
> corrido nunca el emulador: **paso a paso, con la salida real ya verificada**.
>
> Uso general del proyecto (estructura, opciones, programas): `README.md`.
> Preguntas del profesor con líneas de código: `preguntas_exposicion.md`.

---

## 0. Comprobación previa (hacerlo ANTES de la exposición)

Desde la **raíz del proyecto** (la carpeta que contiene `compilar.sh`):

```bash
java -version          # necesita JDK 8 o superior (aquí se probó con OpenJDK 25)
./compilar.sh          # debe decir: Compilacion OK -> out/
./probar.sh            # debe decir: Resultado global: todas las pruebas pasaron
```

Si esos tres funcionan, todo lo demás funciona. Guarda esta orden de "ensayo
general" para el día anterior:

```bash
./compilar.sh && ./probar.sh && ./ejecutar_programas.sh
```

---

## 1. Paso 1 — Compilar

```bash
./compilar.sh
```

Salida real:

```
Compilacion OK -> out/
```

**Qué decir**: "no hay Gradle ni Maven ni dependencias: es un `javac` sobre los
36 archivos `.java` de `src/`".

---

## 2. Paso 2 — Las 201 pruebas

```bash
./probar.sh
```

Salida final real (el archivo completo queda en `salidas/plan_pruebas_completo.txt`):

```
==================================================
 RESUMEN PRUEBAS UNITARIAS
 Pasadas: 117   Fallidas: 0
==================================================
...
==================================================
 RESUMEN PRUEBAS DE INTEGRACION
 Pasadas: 44   Fallidas: 0
==================================================
...
==================================================
 RESUMEN PRUEBAS DE SISTEMA
 Pasadas: 40   Fallidas: 0
==================================================

Resultado global: todas las pruebas pasaron
```

**Qué decir**: "tres niveles: unitarias (ALU, registros, decodificador),
integración (el ciclo completo) y sistema (los 5 programas con su salida
exacta). 117 + 44 + 40 = 201 pruebas, cero fallidas".

Si solo quieres enseñar el resumen sin scroll:

```bash
grep -E "Pasadas|Resultado global" salidas/plan_pruebas_completo.txt
```

---

## 3. Paso 3 — Los 5 programas de usuario

```bash
./ejecutar_programas.sh
```

Salida real (una por programa):

```
--- sumatoria ---
Sumatoria de 3 a 10 = 52
FIN DE EJECUCION - la CPU se detuvo con HALT/exit - ciclos: 68

--- factorial ---
Factorial de 7 = 5040
FIN DE EJECUCION - la CPU se detuvo con HALT/exit - ciclos: 63

--- integral ---
Integral de x^2 dx desde x=1 hasta x=5 = 30
FIN DE EJECUCION - la CPU se detuvo con HALT/exit - ciclos: 56

--- promedio ---
Vector: 4 8 15 16 23
Promedio = 13
FIN DE EJECUCION - la CPU se detuvo con HALT/exit - ciclos: 123

--- perceptron ---
z = 3  y = 1
FIN DE EJECUCION - la CPU se detuvo con HALT/exit - ciclos: 62

Capturas guardadas en salidas/
```

**Qué decir**: "cada programa además deja su captura completa (inicio y fin)
en `salidas/<programa>_captura.txt`; los ciclos son la medida objetiva de
cuánto trabajó el procesador".

---

## 4. Muestra de ejecución detallada: `--traza --estado`

Este es el comando que **muestra el interior del procesador**:

```bash
java -cp out ui.Emulador factorial --traza --estado
```

### 4.1 Salida real, explicada bloque por bloque

```
================================================================
 INICIO DE EJECUCION - Emulador de procesador MIPS
 Programa: factorial
 Instrucciones cargadas: 25 | Datos cargados: 21 celdas
----------------------------------------------------------------
=== MAPA DE MEMORIA (palabras de 32 bits) ===
  texto      0 ..  99  (0x00000000 .. 0x00000063)  ROM
  datos    100 .. 199  (0x00000064 .. 0x000000C7)  RAM
  disco    200 .. 299  (0x000000C8 .. 0x0000012B)  DiscoDuro
ESTADO INICIAL:
=== REGISTROS (32 bits) ===
   0 $zero =           0  (0x00000000)
   ...
  31 $ra   =           0  (0x00000000)
PC = 0 (0x00000000)
```

| Bloque | Qué enseñar |
|--------|-------------|
| `Instrucciones cargadas: 25` | el programa ocupa 25 celdas de la ROM (0–24) |
| `Datos cargados: 21 celdas` | sus datos y cadenas ocupan 21 celdas de la RAM (100 en adelante) |
| `MAPA DE MEMORIA` | la división ROM / RAM / disco que vimos en la diapositiva 10 |
| `ESTADO INICIAL` | todo en 0: la CPU arranca limpia y el PC = 0 |

```
 SALIDA DEL PROGRAMA:
----------------------------------------------------------------
  [   0] PC=  0  0x8C080064
  [   1] PC=  1  0x24020004
  [   2] PC=  2  0x24040065
  [   3] PC=  3  0x0000000C
Factorial de   [   4] PC=  4  0x01002021
  [   5] PC=  5  0x24020001
  [   6] PC=  6  0x0000000C
7  [   7] PC=  7  0x24020004
  [   8] PC=  8  0x24040073
  [   9] PC=  9  0x0000000C
 =   [  10] PC= 10  0x24090001
  [  11] PC= 11  0x240A0001
  [  12] PC= 12  0x010A582A
  [  13] PC= 13  0x15600004
  [  14] PC= 14  0x012A0018
  [  15] PC= 15  0x00004812
  [  16] PC= 16  0x254A0001
  [  17] PC= 17  0x0800000C
  [  18] PC= 12  0x010A582A     <- volvió al inicio del bucle (PC 12)
  ...
  [  56] PC= 18  0x01202021
  [  57] PC= 19  0x24020001
  [  58] PC= 20  0x0000000C
5040  [  59] PC= 21  0x24020004
  [  60] PC= 22  0x24040077
  [ 61] PC= 23  0x0000000C
  [  62] PC= 24  0xFC000000      <- halt
----------------------------------------------------------------
FIN DE EJECUCION - la CPU se detuvo con HALT/exit - ciclos: 63
```

**Cómo leer una línea de la traza** (decirlo una vez y no repetirlo):

```
  [  12] PC= 12  0x010A582A
     ^      ^         ^
     |      |         instrucción en hexadecimal que se va a ejecutar
     |      dirección (PC) que apunta la CPU
     número de ciclo (va de 0 a 62 → 63 ciclos)
```

Puntos buenos para señalar con el mouse:

1. **`[3] PC=3 0x0000000C`** → `syscall` (funct `0x0C`): ahí el programa pinta
   texto en la consola.
2. **La salida del programa se escribe EN MEDIO de la traza**: `Factorial de`
   aparece pegado a la línea `[4]`, un `7` delante de `[7]` y un ` = ` delante
   de `[10]`. Es normal, no es un bug: los `syscall` de impresión se ejecutan
   en el ciclo anterior y la traza del siguiente se imprime después.
3. **`[17] PC=17 → [18] PC=12`** → el `j "bucle"` reescribió el PC: ahí se ve
   el salto hacia atrás sin ninguna magia.
4. **`5040 [59]`** → el `syscall` de impresión saca el resultado calculado en
   `$t1`.
5. **`[62] PC=24 0xFC000000`** → el `halt` (opcode `0x3F` en los 6 bits
   más significativos: `111111…`): la última instrucción.

**Si solo quieres la salida limpia** (sin ruido de la traza):

```bash
java -cp out ui.Emulador factorial --estado
```

**Si quieres guardar la evidencia**:

```bash
java -cp out ui.Emulador factorial --estado --captura salidas/mi_captura.txt
```

### 4.2 El bloque final con `--estado`

```
FIN DE EJECUCION - la CPU se detuvo con HALT/exit - ciclos: 63
ESTADO FINAL:
=== REGISTROS (32 bits) ===
   0 $zero =           0  (0x00000000)
   2 $v0   =           4  (0x00000004)   <- servicio 4: imprimir cadena
   4 $a0   =         119  (0x00000077)   <- $a0 = dirección de la última
                                             cadena (salto de línea)
   8 $t0   =           7  (0x00000007)   <- n = 7
   9 $t1   =        5040  (0x000013B0)   <- ¡5040 en hexadecimal!
  10 $t2   =           8  (0x00000008)   <- contador i
  11 $t3   =           1  (0x00000001)   <- comparación del bucle
   ...
  31 $ra   =           0  (0x00000000)
PC = 25 (0x00000019)
HI=0  LO=5040                            <- el resultado de mult/mflo
=== RAM | segmento=datos | direcciones=100..199 | ancho=32 bits | celdas=100 ===
  [  0] dir 100 (0x00000064) =           7  (0x00000007)   <- la n
  [  1] dir 101 (0x00000065) =          70  (0x00000046)   <- 'F'
  [  2] dir 102 (0x00000066) =          97  (0x00000061)   <- 'a'
  [  3] dir 103 (0x00000067) =          99  (0x00000063)   <- 'c'
  [  4] dir 104 (0x00000068) =         116  (0x00000074)   <- 't'
  ...
```

**Qué decir sobre esta pantalla** (3 ideas, nada más):

1. `$t1 = 5040` y `LO = 5040`: el resultado vive en un registro y en HI:LO.
2. `PC = 25`: el contador se quedó **justo después** del `halt` (que está en la
   dirección 24) y **dentro del mapa** (0–299): la CPU se detuvo, no murió.
3. En la RAM se ven `70, 97, 99, 116…`: son las letras de `"Factorial de "`
   guardadas como números (ASCII). **Los "punteros" de este emulador son esas
   direcciones: 100, 101, 102…** (esto responde de inmediato a la pregunta
   "¿cómo guarda las cosas en memoria?").

---

## 5. La ventana gráfica: qué hay en pantalla

```bash
./ventana.sh
```

(ó `java -cp out ui.VentanaEmulador`, ó `java -cp out ui.Emulador` sin argumentos)

Se abre la ventana **"Emulador MIPS - Practica 2"**, con tema oscuro y este
diseño:

```
┌───────────────────────────────────────────────────┬───────────────────┐
│ Instrucciones (Binario)                           │ [Ejecutar 1 ciclo]│
│ 00100000000010000000000000001010   # addi ...     │ [Ejecutar programa│
│ 00100000000010010000000000000011   # addi ...     │ [Cargar en el PC] │
│ (binario o 0x... ; comentarios con # o //)        │ [Cargar ejemplo]  │
│                                                   │ [Reiniciar CPU]   │
│                                                   │ [Limpiar historial│
├──────────────────────────────┬────────────────────┼───────────────────┤
│ Historial                    │ Registros          │                   │
│ Ciclo: 1                     │ $zero = 00000...0  │                   │
│ PC: 00000...0000             │ $t0   = 00000...1010                   │
│ Instruccion: addi $t0, ...   │ $t1   = 00000...0011                   │
│ Binario: 0010000000001000... │ ...               │                   │
│ Senales: ...                 │                   │                   │
│ Operacion: $t0 = $zero + 10 …│                   │                   │
│ PC Siguiente: 00000...0001   │                   │                   │
├──────────────────────────────┴────────────────────┴───────────────────┤
│ RAM                                │ Estado CPU                       │
│ RAM[0] (dir 100 / 0x00000064) = …  │ PC: 00000...0001                 │
│ RAM[1] (dir 101 / 0x00000065) = …  │ HI: 00000...0000                 │
│ ...                                │ LO: 00000...0000                 │
├────────────────────────────────────┴──────────────────────────────────┤
│ Listo - escriba instrucciones en binario (32 bits)…        Ciclos: 1  │
└───────────────────────────────────────────────────────────────────────┘
```

(Los botones viven a la derecha del campo de instrucciones; los cuatro paneles
se reparten en dos filas: **Historial | Registros** arriba y **RAM | Estado
CPU** abajo; la barra inferior recoge el mensaje del último acto y el
contador de ciclos.)

### 5.1 Las 5 zonas

| Zona (título en pantalla) | Qué muestra | Dónde en el código |
|---|---|---|
| **Instrucciones (Binario)** (arriba) | el programa a ejecutar, en binario o hexadecimal, con comentarios `#` | `VentanaEmulador.construirEntrada()` |
| **Historial** (arriba-izquierda) | un bloque por ciclo: Ciclo, PC, instrucción (mnemónico), Binario, Señales de control, Operación y PC siguiente | `registrar()` en `src/ui/VentanaEmulador.java:336` |
| **Registros** (arriba-derecha) | los 32 registros en binario, `nombre = 32 bits` | `actualizar()` en `src/ui/VentanaEmulador.java:385` |
| **RAM** (abajo-izquierda) | `RAM[i] (dir 100 / 0x00000064) = binario` | idem |
| **Estado CPU** (abajo-derecha) | PC, HI y LO | `construirCpu()` en `src/ui/VentanaEmulador.java:161` |
| **Barra inferior** | mensaje del último acto + contador `Ciclos:` | `estado()` / `barraCiclos` |

### 5.2 Botones y atajos

| Botón | Atajo | Qué hace |
|---|---|---|
| **Ejecutar 1 ciclo** | `Ctrl+Intro` | ejecuta **una** instrucción y anota una entrada en Historial |
| **Ejecutar programa** | `Ctrl+Shift+Intro` | corre hasta el `halt` o hasta **10 000 ciclos** (el límite de la ventana) |
| **Cargar en el PC** | — | vuelve a escribir el texto en la ROM desde la dirección actual del PC |
| **Cargar ejemplo** | — | rellena el campo con un programa de 6 instrucciones ya comentado |
| **Reiniciar CPU** | — | PC = 0, ROM y RAM en 0, registros en 0, ciclos en 0 y borra el Historial (hay que volver a cargar el ejemplo) |
| **Limpiar historial** | — | borra solo el Historial |

### 5.3 Qué se puede escribir en "Instrucciones (Binario)"

- **binario** de hasta 32 caracteres: `00100000000010000000000000001010`
- **hexadecimal** con `0x`: `0x2008000A` (mucho más cómodo para la demo)
- **comentarios** con `#` o `//` hasta el final de la línea
- separadores: espacios, saltos de línea, comas, `;`, paréntesis y corchetes

Eso lo hace `src/instruccion/Binario.java:33` (`parsear`). Si pones algo
inválido, salta un cuadro de diálogo de error, el mensaje aparece en la barra
inferior (`ERROR: ...`) y **no se carga nada**.

---

## 6. Guion de la demo en pantalla (4 minutos)

> Señala con el mouse **siempre lo mismo**: primero la zona, después el valor.
> No leas los 32 bits en voz alta.

### min. 0:00 — Arrancar

1. Terminal, en la raíz: `./ventana.sh`.
2. **Qué decir**: "esta es la misma CPU que corre los 5 programas; la
   diferencia es que aquí avanzamos instrucción por instrucción".

### min. 0:30 — Cargar el ejemplo

3. Pulsa **Cargar ejemplo**. Aparecen 6 líneas en el campo superior (en binario,
   con el mnemónico como comentario):
   ```
   00100000000010000000000000001010   # addi $t0, $zero, 10
   00100000000010010000000000000011   # addi $t1, $zero, 3
   00000001000010010101000000100000   # add $t2, $t0, $t1
   10101100000010100000000001100100   # sw $t2, 100($zero)   # RAM dir 100
   10001100000010110000000001100100   # lw $t3, 100($zero)
   11111100000000000000000000000000   # halt                 # alto: la CPU se detiene
   ```
   (los mnemónicos son comentarios; el campo solo acepta binario o `0x`, y la
   barra inferior confirma *Ejemplo cargado en el campo de instrucciones*).
4. **Qué decir**: "6 instrucciones: dos cargas, una suma, una escritura en
   RAM, una lectura y el `halt`. El campo admite binario o `0x` hexadecimal;
   el mnemónico lo desensambla el programa en el Historial".

### min. 1:00 — Un ciclo a la vez

5. Pulsa **Ejecutar 1 ciclo** (`Ctrl+Intro`) **seis veces**, narrando:

| Ciclo | Señalar | Qué decir |
|---|---|---|
| 1 | Historial: `Instruccion: addi $t0, $zero, 10` y `PC Siguiente: …0001`; Registros: `$t0 = …1010` | "IF leyó la instrucción, ID la decodificó, la ALU sumó 10 y WB lo dejó en `$t0`. El PC ya apunta a la siguiente" |
| 2 | Registros: `$t1 = …0011` | "igual pero con 3" |
| 3 | Historial: `add $t2, $t0, $t1`; Registros: `$t2 = …1101` | "10 + 3 = 13 en binario. **Aquí no hay banderas**: la ALU solo devuelve el resultado" |
| 4 | RAM: `RAM[0] (dir 100 …) = …1101` | "`sw` escribió 13 en la dirección 100: dato en la RAM, **no** en un registro" |
| 5 | Historial: `lw $t3, 100($zero)`; Registros: `$t3 = …1101` | "ahora se lee de vuelta: base `$zero` + desplazamiento 100 = dirección 100" |
| 6 | Historial: `halt`; barra inferior | "el `halt` (opcode `0x3F`) manda `detenido = true` y el ciclo se corta; en el campo `Operacion:` lo pone con palabras: *alto: la CPU se detiene (el programa termino)*" |

6. Después del 6.º ciclo, la **barra inferior** dice:

   ```
   Ejecutado(s) 6 ciclo(s) - CPU detenida (instruccion HALT o syscall exit)
   ```

   y el **Estado CPU** muestra `PC = …0110` (6), `Ciclos: 6`.

7. Pulsa **otra vez** "Ejecutar 1 ciclo": la barra dirá
   `Ejecutado(s) 0 ciclo(s) - CPU detenida…`.
   **Qué decir**: "0 ciclos: la CPU está detenida, ya no trabaja. Esa es la
   diferencia entre *detenida* y *muerta*: si quito el `halt` sigue andando".

### min. 2:30 — El truco: "¿y si no hay halt?"

8. **Borra la última línea** (`halt`) del campo de instrucciones y pulsa
   **Ejecutar programa**. La barra termina en:

   ```
   Ejecutado(s) 10000 ciclo(s) - limite de 10000 ciclos alcanzado (el programa no tiene HALT)
   ```

9. **Qué decir**: "sin `halt` la CPU no se apaga: sigue ejecutando la
   palabra `0000…0000`, que es `sll` hacia `$zero` → un NOP. Por eso existe un
   límite de seguridad (10 000 aquí en la ventana, 100 000 en el modo texto).
   Fíjate en el Historial: los bloques NOP dicen `Operacion: sin operacion`
   y `PC Siguiente` avanza de 1 en 1".
10. Pulsa **Reiniciar CPU** para volver a 0 y **Cargar ejemplo** otra vez.

### min. 3:20 — Respaldo y cierre (elige UNO)

- **Opción A — desbordamiento**: borra todo y escribe
  ```
  0x3C088000        # lui  $t0, 0x8000   -> $t0 = 0x80000000 (el más negativo)
  0x01084020        # add  $t0, $t0, $t0 -> no cabe en 32 bits
  0xFC000000        # halt
  ```
  Pulsa **Ejecutar 1 ciclo** dos veces: la primera carga el programa en la ROM
  y ejecuta el `lui`; en la segunda, el `add` lanza la excepción. Entonces el
  Historial muestra `Operacion: ERROR: Desbordamiento en
  la suma de -2147483648 + -2147483648` y la barra
  `Ciclo 2 - ERROR: …`.
  **Qué decir**: "`add` y `sub` sí vigilan el desbordamiento y detienen la
  ejecución; `addu` y `subu` lo envuelven. Los 5 programas usan `addu`".

- **Opción B — salir limpio**: `Ctrl+Intro` para cerrar en el `halt`, señalar
  `Ciclos: 6` y decir: "seis instrucciones, seis ciclos; con los programas
  reales el mismo contador da 63 en factorial y 123 en el promedio".

- **Opción C — el estado completo**: `Reiniciar CPU`, **Cargar ejemplo**,
  **Ejecutar programa**, y enseñar Registros + RAM + Estado CPU de un vistazo:
  "mismo resultado, solo que sin esperar a que yo pulse".

### min. 4:00 — Cerrar

11. Deja la ventana abierta por si hay preguntas, y vuelve a la presentación
    (diapositiva 33 — Conclusiones).

---

## 7. Respuestas rápidas para lo que puedan preguntar DURANTE la demo

| Pregunta en vivo | Qué hacer/señalar |
|---|---|
| "¿Qué binario es esa instrucción?" | la columna `Binario:` del Historial; y en el campo puedes escribir `0x2008000A` a mano y cargarlo |
| "¿Dónde se ven las señales de control?" | campo `Senales:` de cada bloque del Historial |
| "¿Y el ensamblador?" | campo `Instruccion:` (lo escribe `src/instruccion/Desensamblador.java`) |
| "¿Cómo cambia el PC?" | comparar `PC:` con `PC Siguiente:` en dos bloques seguidos; en el `halt`, el PC queda en 6 |
| "¿Y si escribo una dirección que no existe?" | en modo texto: `IndexOutOfBoundsException: Direccion fuera del mapa de memoria: 300`; la ventana lo muestra en la barra como `Ciclo n - ERROR: ...` y ese ciclo queda en el Historial con `Senales: -` |
| "¿Se puede cargar un programa real?" | sí, pegando su binario/hex en el campo; los 5 programas se generan con `ProgramaBuilder` |
| "¿Por qué todo es 32 bits?" | porque la palabra y los registros miden 32: `Decodificador`, `Registros`, `ALU` |

---

## 8. Plan B: si algo falla delante del profesor

1. **No abre la ventana** (sin pantalla / `DISPLAY`): usa el modo texto, que
   muestra exactamente lo mismo en otra forma:
   `java -cp out ui.Emulador factorial --traza --estado`.
2. **No compila**: borra `out/` y vuelve a `./compilar.sh`. Si `javac` no
   existe, falta el JDK (no solo el JRE).
3. **Sin tiempo**: ya hay evidencia generada antes de la exposición:
   - `salidas/plan_pruebas_completo.txt` → las 201 pruebas
   - `salidas/<programa>_captura.txt` → inicio y fin de cada programa
   - `docs/reporte_exposicion/reporte_final/imagenes/*.png` → capturas de
     pantalla de la práctica (`ev_pruebas.png`, `ev_traza_factorial.png`,
     `ev_factorial_fin.png`)
4. **Se colgó un programa**: la ventana tiene **Reiniciar CPU** (limpia ROM,
   RAM, registros, ciclos e Historial; después vuelve a pulsar **Cargar
   ejemplo**); en texto, `Ctrl+C`. Nadie ha visto un `Ctrl+C` malo en una demo.

---

## 9. Chuleta de comandos

```bash
./compilar.sh                                  # compilar (siempre primero)
./probar.sh                                    # 201 pruebas, 0 fallidas
./ejecutar_programas.sh                        # los 5 programas + capturas
./ventana.sh                                   # ventana gráfica (demo)

java -cp out ui.Emulador factorial             # salida limpia
java -cp out ui.Emulador factorial --traza     # un ciclo por línea
java -cp out ui.Emulador factorial --estado    # registros, PC, HI/LO, RAM
java -cp out ui.Emulador factorial --estado --captura salidas/x.txt

grep -E "Pasadas|Resultado global" salidas/plan_pruebas_completo.txt
```

Programas disponibles: `sumatoria`, `factorial`, `integral`, `promedio`,
`perceptron`.
