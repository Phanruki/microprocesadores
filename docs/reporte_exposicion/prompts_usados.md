# Prompts usados con IA (material para la sección "Uso de IA" del reporte)

> Fuente: los prompts redactados por el equipo durante la práctica. Este
> documento reemplaza al borrador anterior en `.docx` (que ya no se usa) y es
> el que hay que copiar/pegar en el reporte final, en la sección
> **"Uso de inteligencias artificiales para la generación de código"**.
> Las rutas y los números ya están ajustados al código actual del emulador.

La práctica pide declarar **plenamente** el uso de IA (rubro "Uso de IA":
*"el equipo reconoce y describe plenamente el uso de IA"*). Cada prompt lleva
quién lo formuló, su texto y qué produjo.

---

## Prompts del equipo

### Prompt 1 — Yadid (análisis del sistema de almacenamiento)

> «Analiza las siguientes clases de mi proyecto en Java: `UnidadAlmacenamiento`,
> `RAM`, `ROM`, `Registros` y contador de programa. `<CÓDIGO>` ¿Qué unidades
> funcionales faltan para emular un procesador completo y cómo las
> reorganizarías en paquetes sin romper el código existente?»

**Resultado**: arquitectura modular en 5 paquetes (`cpu`, `ejecucion`,
`memoria`, `alu`, `instruccion`), la clase `MemoriaSistema` con el mapa de
direcciones segmentado y el documento `docs/arquitectura.md`.

### Prompt 2 — José de Jesús (plan de pruebas unitarias)

> «Genera un plan de pruebas para el emulador del procesador MIPS. Considera la
> unidad de almacenamiento (rango de direcciones y limpieza), los registros
> (`$zero` de sólo lectura), la ALU (las operaciones y el desbordamiento), la
> decodificación de las instrucciones de formato R, I y J y el comportamiento
> del PC.»

**Resultado**: `docs/pruebas/plan_pruebas_unitarias.md` y
`src/pruebas/PruebasUnitarias.java`: **117 pruebas** automáticas, todas pasadas.

### Prompt 3 — José de Jesús (plan de pruebas de integración)

> «Genera un plan de pruebas de integración de código para el emulador de
> procesador MIPS. Verifica el ciclo completo (IF/ID/EX/MEM/WB), la conexión
> `MemoriaSistema` → `CPU` → `Registros` y RAM, el funcionamiento de las ramas
> (beq, bne, j, jr) y el uso de los syscalls.»

**Resultado**: `docs/pruebas/plan_pruebas_integracion.md` y
`src/pruebas/PruebasIntegracion.java`: **44 pruebas**, todas pasadas.

### Prompt 4 — José de Jesús (plan de pruebas de sistema)

> «Genera un plan de pruebas de sistema para el código para el emulador del
> procesador MIPS. Ejecuta los cinco programas de usuario (sumatoria, factorial,
> integral, promedio y perceptron), comprueba sus salidas exactas, que cada uno
> termine con `halt`/`syscall 10` y que el número de ciclos sea razonable.»

**Resultado**: `docs/pruebas/plan_pruebas_sistema.md` y
`src/pruebas/PruebasSistema.java`: **40 pruebas**, todas pasadas.

### Prompt 5 — Miguel (factorial; prompt literal que indica la práctica)

> «A continuación te paso una lista de instrucciones del procesador MIPS que
> puedo compilar en mi emulador del procesador MIPS. Genera un código en
> lenguaje ensamblador para el procesador MIPS que implemente el factorial de un
> número. Incluye la explicación de cada línea de código. Sólo utiliza las
> instrucciones que te muestro a continuación. `<add, addu, sub, subu, and, or,
> xor, nor, slt, sltu, sll, srl, mult, div, mflo, mfhi, jr, syscall, addi, addiu,
> andi, ori, xori, slti, sltiu, lui, lw, sw, beq, bne, j, jal>`»

**Resultado**: `src/programas/Factorial.java` (25 instrucciones) y su
explicación en `docs/programas/factorial_explicacion.md`; al ejecutarlo el
emulador muestra `Factorial de 7 = 5040` (63 ciclos).

### Prompt 6 — Yadid (sumatoria)

> «Genera un código en lenguaje ensamblador MIPS que realice la sumatoria de los
> números desde el 3 hasta el 10 usando un bucle con beq/bne y slt para el
> control. Sólo utiliza las instrucciones de mi emulador: addu, addiu, slt, beq,
> bne, j, lw, sw y syscall. Incluye la explicación de cada línea.»

**Resultado**: `src/programas/Sumatoria.java`; el emulador imprime
`Sumatoria de 3 a 10 = 52` (68 ciclos).

### Prompt 7 — Miguel (integral definida)

> «Genera código ensamblador MIPS que calcule la integral definida de
> f(x) = x² desde x = 1 hasta x = 5, aproximada con rectángulos de ancho 1
> (1² + 2² + 3² + 4²). Usa mult y mflo para elevar al cuadrado, slt/beq para el
> bucle y syscall para imprimir el resultado con un mensaje. Sólo instrucciones
> de mi emulador y explica cada línea.»

**Resultado**: `src/programas/Integral.java`; el emulador imprime
`Integral de x^2 dx desde x=1 hasta x=5 = 30` (56 ciclos).

### Prompt 8 — Yadid (promedio de un vector)

> «Genera código ensamblador MIPS que calcule el promedio del vector fijo
> {4, 8, 15, 16, 23} almacenado en RAM: acumula los elementos en un bucle con lw
> y addu, después divide entre 5 con div y mflo, e imprime el vector y el
> promedio con los syscalls de cadena y entero. Sólo instrucciones soportadas
> por mi emulador.»

**Resultado**: `src/programas/Promedio.java`; el emulador imprime
`Vector: 4 8 15 16 23` y `Promedio = 13` (123 ciclos).

### Prompt 9 — Miguel (perceptrón)

> «Genera código ensamblador MIPS que evalúe un perceptrón de 3 entradas con
> datos fijos: entradas x = {1, 1, 0}, pesos w = {2, 2, -3} y bias -1. Acumula
> el producto punto con mult/mflo en un bucle, suma el bias, aplica el umbral
> con slt contra $zero y muestra el valor de z y la activación y. Sólo
> instrucciones de mi emulador, con la explicación de cada línea.»

**Resultado**: `src/programas/Perceptron.java`; el emulador imprime
`z = 3  y = 1` (62 ciclos; z = 2·1 + 2·1 + (−3)·0 − 1 = 3, y = 1 porque z ≥ 0).

### Prompt 10 — Yadid (clase ALU)

> «Implementa en Java la clase ALU de un procesador MIPS de 32 bits con las
> operaciones ADD, ADDU, SUB, SUBU, AND, OR, XOR, NOR, SLT, SLTU, SLL, SRL y
> LUI, más NOP. Detecta el desbordamiento con signo en ADD y SUB y detiene el
> programa en ese caso, mientras que ADDU y SUBU envuelven el resultado. Devuelve
> también el resultado de las comparaciones SLT y SLTU.»

**Resultado**: `src/alu/ALU.java` y `src/alu/OperacionALU.java` (13 operaciones
+ NOP), probadas con las pruebas unitarias de la ALU.

> **Nota:** el prompt original sobre `DiscoDuro` (copiar la unidad de
> almacenamiento y agregar persistencia en archivo) **no se incluye en el
> reporte**: el equipo decidió declarar sólo los prompts cuyas capturas se
> conservan y se entregan como evidencia.

### Prompt 11 — Miguel (ventana gráfica del emulador)

> «Genera una ventana en Java (Swing) para mi emulador del procesador MIPS con
> estos campos: instrucciones en binario (32 bits por línea, acepta hexadecimal
> y comentarios con #), historial con Ciclo, PC, Instrucción, Operación,
> señales de control y PC siguiente, los 32 registros, la memoria por segmentos
> y el estado CPU (PC, HI, LO); botones: Ejecutar 1 ciclo, Ejecutar programa,
> Cargar en el PC, Cargar ejemplo, Reiniciar CPU y Limpiar historial.»

**Resultado**: `src/ui/VentanaEmulador.java`, usada para observar el estado de
la CPU ciclo a ciclo.

### Prompt 12 — José de Jesús (documentación del conjunto de instrucciones)

> «Genera la documentación del conjunto de instrucciones del procesador MIPS
> que soporta mi emulador: formato R con su campo funct, formato I con su
> opcode, formato J, los efectos de cada instrucción sobre los registros en
> binario de 32 bits y la tabla de servicios de syscall con sus argumentos.»

**Resultado**: `docs/instrucciones.md`, referencia para escribir y revisar los
cinco programas de usuario.

### Prompt 13 — Equipo (reestructuración final a 5 paquetes)

> «Reorganiza el emulador en 5 paquetes grandes (cpu, ejecucion, memoria, alu,
> instruccion) al estilo del diagrama del compañero, con la memoria segmentada
> por dirección (ROM texto@0, RAM datos@100, disco@200), una CPU que nunca
> muere (arranca en 0, PC circular, memoria en 0 = NOP, límite de 100000 ciclos)
> y una instrucción HALT para que la CPU sepa que el programa terminó; añade
> pruebas y regenera los diagramas.»

**Resultado**: la estructura actual del proyecto, `docs/diagramas/` y las
**201 pruebas** (117 + 44 + 40) que pasan con `./probar.sh`.

---

## Reflexión sobre el uso de IA (para el reporte)

- La IA se usó como **copiloto**: análisis del diseño, planes de pruebas,
  programas de usuario, ventanas y explicaciones.
- Las **decisiones** las tomó el equipo: elegir MIPS, la segmentación de la
  memoria por dirección, el `HALT` propio (opcode 0x3F), la CPU que nunca muere
  y el límite de 100000 ciclos.
- La **validación** también: correr las 201 pruebas y las capturas de
  `salidas/`. Varios resultados iniciales de la IA no compilaban o no
  comportaban como se esperaba (p. ej. las señales de control, que se
  reconstruyeron con un `Builder` para evitar errores de argumentos
  posicionales) y se corrigieron a mano.
- Conclusión para el reporte: la IA ahorra horas, pero sólo es útil si se sabe
  qué pedir y se puede comprobar la respuesta con pruebas automatizadas.
