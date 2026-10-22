#!/usr/bin/env python3
"""
Genera el reporte final de la Práctica 2 como un ÚNICO archivo .docx
(docs/reporte_exposicion/reporte_final/reporte_final.docx).

- Portada y anteportada NO se generan: el equipo las agregará después en Word
  (las tienen en PDF de la plantilla UV).
- La sección "Uso de IA" queda con el índice de los 14 prompts y un espacio
  marcado donde el equipo inserta las capturas de los chats.
- Las imágenes de la sección de evidencia salen de las salidas reales
  (generarlas antes con generar_capturas.py).

Uso:
    python3 generar_capturas.py
    python3 generar_reporte_docx.py
"""

import os

from docx import Document
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.shared import Cm, Pt, RGBColor

AQUI = os.path.dirname(os.path.abspath(__file__))
IMAGENES = os.path.join(AQUI, "imagenes")
SALIDA = os.path.join(AQUI, "reporte_final.docx")

UV = RGBColor(0x1E, 0x59, 0x96)        # azul UV
GRIS = RGBColor(0x5A, 0x5A, 0x5A)
ANCHO_IMG = Cm(14.5)

# ---------------------------------------------------------------- contenido --

INTRODUCCION = [
    ("Los procesadores que dan vida a los dispositivos que usamos a diario se "
     "perciben como una caja negra: se sabe que ejecutan instrucciones, pero "
     "rara vez se tiene la oportunidad de construir uno para observar qué ocurre "
     "realmente dentro. En esta práctica desarrollamos, desde cero, un emulador "
     "del procesador MIPS en Java, con la intención de sustituir la memorización "
     "de conceptos por la experiencia de implementarlos."),
    ("¿Por qué construir un emulador de procesador desde cero? Un procesador "
     "comercial real no puede estudiarse pieza por pieza: contiene millones de "
     "transistores, su diseño es propiedad industrial y se construye en equipos "
     "grandes a lo largo de años. Diseñar el nuestro desde cero nos permitió tomar "
     "y ver cada una de las decisiones que en un chip comercial vienen ya tomadas: "
     "el ancho de palabra, el mapa de direcciones, el conjunto de instrucciones, "
     "el manejo de los desbordamientos y la forma en que se actualiza el contador "
     "de programa. Además, implementar el ciclo de instrucción (búsqueda, "
     "decodificación, ejecución, memoria y escritura) desmitifica el hardware: "
     "cada instrucción que escribe un programador en un lenguaje de alto nivel "
     "termina, al final, en operaciones de la ALU y en movimientos de datos entre "
     "registros y memoria. Y algo importante desde el punto de vista didáctico: "
     "en un procesador real un error cuesta silicio, mientras que en un emulador "
     "un error es una prueba fallida que se puede corregir, repetir y documentar."),
    ("¿Qué gana el alumno que sabe implementar un emulador de un procesador real? "
     "Quien ha construido un emulador deja de ver la arquitectura de computadoras "
     "como una asignatura de memorización y la entiende como un sistema que se "
     "puede razonar: sabe qué registros intervienen en una suma, qué significa que "
     "un dato desborde y por qué un bucle consume más ciclos que otro. En lo "
     "práctico, adquiere programación de bajo nivel (manejo de bits, registros, "
     "memoria y aritmética con y sin signo), capacidad de leer documentación "
     "técnica y traducirla a código, y disciplina de ingeniería de software: "
     "diseño modular, pruebas unitarias, pruebas de integración y pruebas de "
     "sistema. A esto se suma una ventaja profesional cada vez más valorada: la "
     "capacidad de usar la inteligencia artificial generativa con criterio, es "
     "decir, de pedirle apoyo a la herramienta y de comprobar después que lo que "
     "ésta devuelve es correcto."),
    ("¿Por qué un emulador para el procesador MIPS? Elegimos MIPS de entre las "
     "opciones disponibles (RISC-V, x86, M1 y AMD) por varias razones. Primero, es "
     "una arquitectura RISC con sólo tres formatos de instrucción fijos (R, I y "
     "J), de modo que la codificación de 32 bits es regular y predecible, lo que la "
     "hace factible de implementar en un curso sin sacrificar rigor. Segundo, es "
     "el procesador de referencia en la literatura de arquitectura de computadoras "
     "(por ejemplo, en los libros de Patterson y Hennessy), por lo que existe "
     "abundante material para resolver dudas. Tercero, su flujo de control es "
     "transparente: el PC se incrementa en una palabra por cada instrucción y las "
     "ramas se calculan con desplazamientos relativos, lo que permite observar con "
     "claridad cómo se modifica el flujo de un programa. Por último, sus servicios "
     "de sistema (syscall) permiten resolver la entrada y salida de datos sin "
     "necesidad de emular periféricos complejos."),
]

DESARROLLO = [
    ("El emulador se implementó en Java con una arquitectura modular de cinco "
     "paquetes grandes, siguiendo el esqueleto del diagrama de clases de un "
     "compañero de equipo: cpu (la CPU con su ciclo de cinco etapas y el "
     "contador de programa), ejecucion (la unidad de control, sus señales y el "
     "ejecutor de instrucciones), memoria (las unidades de almacenamiento con "
     "ancho de palabra de 32 bits y 100 celdas cada una, más el banco de 32 "
     "registros), alu (la unidad aritmético-lógica) e instruccion (la "
     "codificación, decodificación y desensamblado de las instrucciones)."),
    ("La memoria está dividida en segmentos con dirección: la ROM guarda las "
     "instrucciones en las direcciones 0 a 99 y se bloquea una vez cargado el "
     "programa; la RAM guarda los datos del programa en las direcciones 100 a 199; "
     "y el DiscoDuro conserva información persistente en las direcciones 200 a "
     "299. El procesador cuenta con 32 registros de 32 bits, donde $zero siempre "
     "lee cero e ignora las escrituras, y con un contador de programa (PC) de 32 "
     "bits que contiene la dirección de la siguiente instrucción a ejecutar: "
     "durante la búsqueda de instrucciones el PC se incrementa para apuntar a la "
     "siguiente, y las ramas (beq, bne, j, jal, jr) escriben en él la dirección "
     "destino. Además, la CPU nunca muere: arranca en la dirección 0, el PC es "
     "circular dentro del mapa de memoria, la celda 0 contiene un NOP y existe un "
     "límite de seguridad de 100 000 ciclos; sólo se detiene con la instrucción "
     "halt (opcode 0x3F, una extensión propia del emulador) o con el servicio "
     "syscall 10."),
    ("El procesador seleccionado fue MIPS [x], frente a RISC-V, x86, M1 y AMD. La "
     "ALU procesa 32 bits y soporta trece operaciones: ADD, ADDU, SUB, SUBU, AND, "
     "OR, XOR, NOR, SLT, SLTU, SLL, SRL y LUI, además de NOP. ADD y SUB detectan "
     "desbordamiento con signo y detienen la emulación; ADDU y SUBU envuelven el "
     "resultado sin avisar. MIPS no tiene registros de banderas, por lo que las "
     "comparaciones se hacen con slt/slti/sltu seguidas de beq o bne."),
    ("Cada instrucción pasa por cinco etapas: IF (instrucción = ROM[PC]; PC = PC "
     "+ 1), ID (separar opcode, rs, rt, rd, shamt, funct o inmediato y generar "
     "las señales de control), EX (la ALU calcula), MEM (lw lee la memoria, sw "
     "la escribe) y WB (el resultado llega al registro destino). El conjunto "
     "soportado incluye las instrucciones de formato R (add, addu, sub, subu, "
     "and, or, xor, nor, slt, sltu, sll, srl, mult, div, mflo, mfhi, jr, syscall), "
     "de formato I (addi, addiu, andi, ori, xori, slti, sltiu, lui, lw, sw, beq, "
     "bne) y de formato J (j, jal), con cinco servicios de syscall: imprimir "
     "entero (1), imprimir cadena (4), leer entero (5), imprimir carácter (11) y "
     "terminar (10). Los programas de usuario se arman con ProgramaBuilder, que "
     "emite las instrucciones y resuelve las etiquetas de las ramas."),
]

# Comandos ejecutados y su resultado (tabla 1 de la evidencia)
TABLA_COMANDOS = [
    ("java -cp out ui.Emulador sumatoria", "Sumatoria de 3 a 10 = 52 (68 ciclos)"),
    ("java -cp out ui.Emulador factorial", "Factorial de 7 = 5040 (63 ciclos)"),
    ("java -cp out ui.Emulador integral", "Integral de x^2 dx desde x=1 hasta x=5 = 30 (56 ciclos)"),
    ("java -cp out ui.Emulador promedio", "Vector: 4 8 15 16 23 / Promedio = 13 (123 ciclos)"),
    ("java -cp out ui.Emulador perceptron", "z = 3  y = 1 (62 ciclos)"),
    ("java -cp out ui.Emulador factorial --traza --estado", "Traza por ciclo con el PC y el código en hexadecimal; volcado de registros y mapa de memoria"),
    ("java -cp out pruebas.PruebasUnitarias", "RESUMEN PRUEBAS UNITARIAS / Pasadas: 117  Fallidas: 0"),
    ("java -cp out pruebas.PruebasIntegracion", "RESUMEN PRUEBAS DE INTEGRACION / Pasadas: 44  Fallidas: 0"),
    ("java -cp out pruebas.PruebasSistema", "RESUMEN PRUEBAS DE SISTEMA / Pasadas: 40  Fallidas: 0"),
    ("java -cp out pruebas.EjecutorPruebas", "201 pruebas: resultado global, todas pasaron"),
]

# Traza real del factorial (PC, ensamblador y efecto); coincide con el binario impreso
TABLA_TRAZA = [
    ("lw $t0, 100($zero)   (PC = 0)", "$t0 = Mem[100] = 7"),
    ("addiu $v0, $zero, 4   (PC = 1)", "$v0 = 4 (servicio: imprimir cadena)"),
    ("addiu $a0, $zero, 101   (PC = 2)", "$a0 = 101 (dirección de la cadena \"Factorial de \")"),
    ("syscall   (PC = 3)", "Salida en pantalla: \"Factorial de \""),
    ("addu $a0, $t0, $zero   (PC = 4)", "$a0 = 7"),
    ("addiu $v0, $zero, 1   (PC = 5)", "$v0 = 1 (servicio: imprimir entero)"),
    ("syscall   (PC = 6)", "Salida en pantalla: \"7\""),
    ("addiu $v0, $zero, 4   (PC = 7)", "$v0 = 4 (servicio: imprimir cadena)"),
    ("addiu $a0, $zero, 115   (PC = 8)", "$a0 = 115 (dirección de la cadena \" = \")"),
    ("syscall   (PC = 9)", "Salida en pantalla: \" = \""),
    ("addiu $t1, $zero, 1   (PC = 10)", "$t1 = 1 (acumulador del factorial)"),
    ("addiu $t2, $zero, 1   (PC = 11)", "$t2 = 1 (contador del bucle)"),
    ("slt $t3, $t0, $t2   (PC = 12, 1.ª vez)", "$t3 = 0, porque 7 < 1 es falso"),
    ("bne $t3, $zero, salida   (PC = 13, 1.ª vez)", "No salta ($t3 = 0); PC = 14"),
    ("mult $t0, $t2   (PC = 14, 1.ª vez)", "LO:HI = 7 × 1 = 7"),
    ("mflo $t1   (PC = 15, 1.ª vez)", "$t1 = LO = 7"),
    ("addiu $t2, $t2, 1   (PC = 16, 1.ª vez)", "$t2 = 2"),
    ("j 12   (PC = 17)", "PC = 12 (regresa al inicio del bucle)"),
    ("Bucle PC 12 a 17 repetido hasta completar 7 multiplicaciones", "$t1 = 7 × 6 × 5 × 4 × 3 × 2 × 1 = 5040; $t2 = 8"),
    ("slt $t3, $t0, $t2   (PC = 12, 8.ª vez)", "$t3 = 1, porque 7 < 8 es verdadero"),
    ("bne $t3, $zero, salida   (PC = 13, 8.ª vez)", "Sí salta: PC = 18 (sale del bucle)"),
    ("addu $a0, $t1, $zero   (PC = 18)", "$a0 = 5040"),
    ("addiu $v0, $zero, 1   (PC = 19)", "$v0 = 1 (servicio: imprimir entero)"),
    ("syscall   (PC = 20)", "Salida en pantalla: \"5040\""),
    ("addiu $v0, $zero, 4   (PC = 21)", "$v0 = 4 (servicio: imprimir cadena)"),
    ("addiu $a0, $zero, 119   (PC = 22)", "$a0 = 119 (dirección del salto de línea)"),
    ("syscall   (PC = 23)", "Salida en pantalla: salto de línea"),
    ("addiu $v0, $zero, 10   (PC = 24)", "$v0 = 10 (servicio: terminar el programa)"),
    ("halt   (PC = 25)", "La CPU se detiene con HALT; 63 ciclos; salida final: \"Factorial de 7 = 5040\""),
]

# (archivo, pie) de las capturas ya insertadas; se insertan en este orden
FIGURAS = [
    ("ev_sumatoria_inicio.png", "Sumatoria: estado inicial (todos los registros en 0 y el mapa de memoria segmentado)."),
    ("ev_sumatoria_fin.png", "Sumatoria: fin de la ejecución; resultado 52 en 68 ciclos."),
    ("ev_factorial_inicio.png", "Factorial: estado inicial de la emulación."),
    ("ev_factorial_fin.png", "Factorial: fin de la ejecución; $t1 = 5040 y LO = 5040 en 63 ciclos."),
    ("ev_integral_inicio.png", "Integral: estado inicial de la emulación."),
    ("ev_integral_fin.png", "Integral: fin de la ejecución; resultado 30 en 56 ciclos."),
    ("ev_promedio_inicio.png", "Promedio: estado inicial de la emulación."),
    ("ev_promedio_fin.png", "Promedio: fin de la ejecución; promedio 13 en 123 ciclos."),
    ("ev_perceptron_inicio.png", "Perceptrón: estado inicial de la emulación."),
    ("ev_perceptron_fin.png", "Perceptrón: fin de la ejecución; z = 3 y = 1 en 62 ciclos."),
    ("ev_traza_factorial.png", "Factorial con --traza: primeros ciclos con el PC y el código de la instrucción en hexadecimal."),
    ("ev_pruebas.png", "Ejecución de las 201 pruebas automáticas: 117 unitarias, 44 de integración y 40 de sistema, todas pasadas."),
]

# Índice de los prompts (el equipo pega aquí las capturas de los chats)
PROMPTS = [
    "Yadid — análisis de las clases del sistema de almacenamiento y reorganización en paquetes.",
    "José de Jesús — plan de pruebas unitarias del emulador MIPS.",
    "José de Jesús — plan de pruebas de integración de código del emulador MIPS.",
    "José de Jesús — plan de pruebas de sistema del emulador MIPS.",
    "Miguel — código de factorial en ensamblador MIPS (prompt literal de la práctica).",
    "Yadid — código de sumatoria (del 3 al 10) con bucle y control de flujo.",
    "Miguel — integral definida de x² de 1 a 5 con mult/mflo.",
    "Yadid — promedio del vector {4, 8, 15, 16, 23} almacenado en RAM.",
    "Miguel — perceptrón de 3 entradas con pesos y bias fijos.",
    "Yadid — clase ALU de 32 bits con detección de desbordamiento.",
    "José de Jesús — clase DiscoDuro copiada de la unidad de almacenamiento.",
    "Miguel — ventana gráfica Swing del emulador.",
    "José de Jesús — documentación del conjunto de instrucciones soportado.",
    "Equipo — reestructuración final en 5 paquetes, HALT y CPU que nunca muere.",
]

REFLEXIONES = [
    ("Nivel de programación que se requiere",
     "Implementar este tipo de emuladores no exige ser un programador experto, "
     "pero sí un nivel intermedio sólido y, sobre todo, orden. Se necesita dominar "
     "programación orientada a objetos en Java (clases, paquetes y "
     "encapsulamiento; incluso algo de herencia, como DiscoDuro que se deriva de "
     "UnidadAlmacenamiento), manejar arreglos y operaciones a nivel de bits, y "
     "estar cómodo con números binarios y hexadecimales. El verdadero reto no es "
     "escribir la primera versión del código, sino depurar: cuando la salida no "
     "coincide con la esperada hay que rastrear el estado del PC, los registros y "
     "la memoria ciclo por ciclo. También se requiere capacidad de leer una "
     "especificación técnica (el manual de MIPS) y traducirla a código, más el "
     "hábito de escribir pruebas antes de dar por bueno un módulo. En síntesis, el "
     "nivel de programación es el de un estudiante avanzado, pero lo que hace que "
     "el proyecto funcione es la constancia y la disciplina de la verificación."),
    ("El trabajo en equipo en el diseño e implementación de un procesador",
     "Un procesador, aun tratándose de un emulador básico, es una tarea demasiado "
     "grande para una sola persona, y esta práctica lo confirmó. Repartimos el "
     "trabajo por módulos: Yadid trabajó las unidades de almacenamiento y la ALU, "
     "además de los programas de sumatoria y promedio; José de Jesús armó el "
     "sistema de pruebas (unitarias, de integración y de sistema), la documentación "
     "técnica y el DiscoDuro; Miguel implementó los programas de factorial, "
     "integral y perceptrón y la ventana gráfica. Lo más importante no fue el "
     "reparto, sino la integración: acordamos desde el inicio convenciones comunes "
     "(nombres de paquetes, ancho de palabra, mapa de direcciones y firmas de las "
     "clases), nos revisamos el código mutuamente y corríamos las tres suites de "
     "pruebas antes de dar por concluida cualquier tarea. Comparar nuestro diagrama "
     "de clases con el de un compañero fue especialmente útil: nos obligó a "
     "explicitar las decisiones de diseño y a unificar el esqueleto de cinco "
     "paquetes antes de escribir el código."),
    ("Por qué les conviene a quienes cursarán esta experiencia el próximo año",
     "A los estudiantes que tomen este curso el próximo año les conviene este "
     "conocimiento por tres razones prácticas. La primera es que conecta todas las "
     "capas de la computación: quien entiende el ciclo de un procesador entiende "
     "qué hace realmente un compilador, por qué una instrucción cuesta lo que "
     "cuesta y cómo se comporta la memoria; deja de programar a ciegas. La segunda "
     "es que mejora como programador: depurar a nivel de registros enseña a "
     "razonar con estado y con tiempos, habilidad aplicable a sistemas embebidos, "
     "IoT, sistemas operativos y verificación de hardware, áreas con demanda "
     "laboral constante. La tercera es que aprenden a usar la inteligencia "
     "artificial con criterio: en esta práctica la IA nos ahorró muchas horas, pero "
     "sólo funcionó porque sabíamos qué pedir y podíamos comprobar la respuesta "
     "con pruebas; sin esos dos componentes, la herramienta devuelve código que "
     "parece correcto y no lo es."),
    ("Valores en juego",
     "La práctica puso en acción los valores de la universidad: libertad para elegir "
     "cómo estructurar cada módulo y qué decisiones de diseño tomar; responsabilidad "
     "por la parte que a cada quien le tocó y por entregar el trabajo en la fecha y "
     "por el medio establecidos; solidaridad al apoyarnos cuando un compañero se "
     "atascaba y al compartir lo que entendíamos; respeto por las ideas y el estilo "
     "de los demás, sin imponer la propia solución; honestidad al reportar "
     "exactamente lo que las pruebas arrojaron, incluidos los errores que "
     "encontramos, sin maquillar resultados; e integridad al cumplir con lo que nos "
     "comprometimos, presentando nuestro propio trabajo y utilizando la "
     "inteligencia artificial únicamente como herramienta de apoyo, dejando "
     "constancia de su uso en este reporte."),
]

REFERENCIAS = [
    "Patterson, D. A., & Hennessy, J. L. (2009). Computer organization and design: The hardware/software interface (4.ª ed.). Morgan Kaufmann.",
    "Harris, D. G., & Harris, S. L. (2012). Digital design and computer architecture: MIPS Edition. Morgan Kaufmann.",
    "Hamacher, V. C., Vranesic, Z. G., Zaky, S. A., & Manjikian, N. (2002). Computer organization (5.ª ed.). McGraw-Hill.",
    "MIPS Technologies. (s. f.). MIPS32 architecture for programmers, volume I: Introduction to the MIPS32 architecture. https://www.mips.com/",
    "Universidad Veracruzana. (2026). Práctica 2. Emulador básico de un procesador [Instrucciones de la asignatura]. Eminus.",
]

# --------------------------------------------------------------- construir --

def titulo(doc, texto, nivel=1):
    p = doc.add_heading(texto, level=nivel)
    for r in p.runs:
        r.font.color.rgb = UV
        r.font.name = "Helvetica"
    return p


def parrafo(doc, texto, cursiva=False):
    p = doc.add_paragraph()
    r = p.add_run(texto)
    r.italic = cursiva
    p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    p.paragraph_format.space_after = Pt(8)
    return p


def figura(doc, archivo, pie):
    ruta = os.path.join(IMAGENES, archivo)
    if not os.path.exists(ruta):
        p = doc.add_paragraph()
        p.add_run(f"[FALTA LA IMAGEN {archivo} — ejecuta generar_capturas.py]").italic = True
        return
    doc.add_picture(ruta, width=ANCHO_IMG)
    doc.paragraphs[-1].alignment = WD_ALIGN_PARAGRAPH.CENTER
    c = doc.add_paragraph()
    r = c.add_run(f"Figura. {pie}")
    r.font.size = Pt(9)
    r.font.color.rgb = GRIS
    c.alignment = WD_ALIGN_PARAGRAPH.CENTER
    c.paragraph_format.space_after = Pt(12)


def tabla(doc, encabezados, filas, anchos=None):
    t = doc.add_table(rows=1, cols=len(encabezados))
    t.style = "Table Grid"
    for i, e in enumerate(encabezados):
        celda = t.rows[0].cells[i]
        celda.text = ""
        r = celda.paragraphs[0].add_run(e)
        r.bold = True
        r.font.size = Pt(10)
    for f in filas:
        cells = t.add_row().cells
        for i, v in enumerate(f):
            cells[i].text = ""
            r = cells[i].paragraphs[0].add_run(v)
            r.font.size = Pt(9.5)
    if anchos:
        for fila in t.rows:
            for i, a in enumerate(anchos):
                fila.cells[i].width = a
    return t


def construir():
    doc = Document()

    est = doc.styles["Normal"]
    est.font.name = "Times New Roman"
    est.font.size = Pt(12)
    est.paragraph_format.line_spacing = 1.5

    for seccion in doc.sections:
        seccion.top_margin = Cm(2.5)
        seccion.bottom_margin = Cm(2.5)
        seccion.left_margin = Cm(3)
        seccion.right_margin = Cm(2.5)

    # ---- NOTA interNA: portada y anteportada las agrega el equipo desde Word
    n = doc.add_paragraph()
    r = n.add_run("[NOTA PARA EL EQUIPO: la portada y la anteportada de la "
                  "plantilla UV se insertan aquí después, desde Word "
                  "(archivo → insertar → texto desde archivo), ya que las tienen "
                  "en PDF.]")
    r.italic = True
    r.font.size = Pt(10)
    r.font.color.rgb = GRIS

    titulo(doc, "Introducción")
    for p in INTRODUCCION:
        parrafo(doc, p)

    titulo(doc, "Desarrollo de la actividad")
    titulo(doc, "Descripción del emulador", nivel=2)
    for p in DESARROLLO:
        parrafo(doc, p)

    # ---------------- Evidencia ----------------
    titulo(doc, "Evidencia de ejecución del código", nivel=2)
    parrafo(doc, "Las pruebas técnicas se ejecutaron con la orden "
                 "java -cp out pruebas.EjecutorPruebas y arrojaron 117 pruebas "
                 "unitarias, 44 pruebas de integración y 40 pruebas de sistema, "
                 "todas pasadas y ninguna fallida. La primera tabla reúne la "
                 "ejecución de los cinco programas de usuario y de las suites de "
                 "pruebas; la segunda muestra la traza de la ejecución del "
                 "programa factorial línea por línea, con el resultado que arrojó "
                 "el emulador. De cada ejecución se guardó una captura al inicio "
                 "y al final, que se muestran en las figuras siguientes.")

    doc.add_paragraph()
    tabla(doc, ("Línea de código a ejecutar", "Resultado del emulador"),
          TABLA_COMANDOS, (Cm(7.5), Cm(8)))
    c = doc.add_paragraph()
    r = c.add_run("Tabla 1. Ejecución de los cinco programas de usuario (Paso 4) "
                  "y de las suites de pruebas.")
    r.font.size = Pt(9)
    r.font.color.rgb = GRIS
    c.paragraph_format.space_after = Pt(12)

    doc.add_paragraph()
    tabla(doc, ("Línea de código a ejecutar", "Resultado del emulador"),
          TABLA_TRAZA, (Cm(7.5), Cm(8)))
    c = doc.add_paragraph()
    r = c.add_run("Tabla 2. Traza de la ejecución del programa factorial, línea "
                  "por línea. El binario real de cada instrucción se muestra en la "
                  "figura de la traza (--traza).")
    r.font.size = Pt(9)
    r.font.color.rgb = GRIS
    c.paragraph_format.space_after = Pt(12)

    for archivo, pie in FIGURAS:
        figura(doc, archivo, pie)

    # ---------------- Uso de IA ----------------
    doc.add_page_break()
    titulo(doc, "Uso de inteligencias artificiales para la generación de código", nivel=2)
    parrafo(doc, "La inteligencia artificial generativa se utilizó como "
                 "herramienta de apoyo en tres frentes: el análisis del diseño, la "
                 "generación de los planes de prueba y la redacción de los "
                 "programas en ensamblador MIPS. Los prompts fueron escritos por el "
                 "equipo y ajustados de forma colaborativa; la respuesta de la "
                 "herramienta nunca se usó tal cual: se ejecutó contra las suites "
                 "de pruebas y se corrigió hasta que el comportamiento fue el "
                 "esperado. A continuación se listan, en orden, los prompts "
                 "utilizados, quién los formuló y qué parte del proyecto produjo "
                 "cada uno.")
    for i, p in enumerate(PROMPTS, 1):
        par = doc.add_paragraph(style="List Number")
        par.add_run(p).font.size = Pt(10.5)

    doc.add_paragraph()
    n = doc.add_paragraph()
    r = n.add_run("[Pendiente del equipo: aquí se insertan, en orden, las "
                  "capturas de pantalla de cada prompt y su respuesta "
                  "(una figura por cada uno de los 14 prompts listados "
                  "anteriormente).]")
    r.italic = True
    r.font.size = Pt(10)
    r.font.color.rgb = GRIS

    # ---------------- Reflexiones ----------------
    doc.add_page_break()
    titulo(doc, "Reflexiones finales", nivel=2)
    for subt, texto in REFLEXIONES:
        titulo(doc, subt, nivel=3)
        parrafo(doc, texto)

    # ---------------- Referencias ----------------
    titulo(doc, "Referencias", nivel=2)
    for ref in REFERENCIAS:
        p = doc.add_paragraph()
        r = p.add_run(ref)
        r.font.size = Pt(10.5)
        p.paragraph_format.left_indent = Cm(1)
        p.paragraph_format.first_line_indent = Cm(-1)

    doc.save(SALIDA)
    print("Generado:", os.path.relpath(SALIDA, os.path.join(AQUI, "..", "..", "..")))


if __name__ == "__main__":
    construir()
