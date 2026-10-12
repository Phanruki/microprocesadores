# Práctica 2. Simulador básico de un procesador.

## Objetivo:

Diseñar, implementar y evaluar un simulador educativo de un procesador en lenguaje Java, que permita comprender los principios fundamentales de la arquitectura de microprocesadores, mediante la construcción modular de unidades funcionales (almacenamiento, ALU, control de flujo), la incorporación de instrucciones aritméticas, lógicas y de control, y la validación del sistema a través de pruebas técnicas, de integración y de usuario final.

## Metodología de trabajo

### Paso 1. Arquitectura del sistema de almacenamiento de datos.

- Ancho de palabra (ver arquitectura del procesador)
- Registros del procesador  (ver arquitectura del procesador)
- Contador de programa

  El PC de 32 bits contiene la dirección de la siguiente instrucción a ejecutar.

  - Durante la búsqueda de instrucciones, el PC se incrementa para apuntar a la siguiente instrucción.

- Disco duro

  - Crear una copia de la clase “unidad de almacenamiento” y llamarla “DiscoDuro”
  - Ajustar el ancho de palabra a 32 bits.
  - Ajustar la cantidad de celdas de almacenamiento a 100.

- RAM

  - Ajustar el ancho de palabra a 32 bits.
  - Ajustar la cantidad de celdas de almacenamiento a 100.

- ROM

  - Ajustar el ancho de palabra a 32 bits.
  - Ajustar la cantidad de celdas de almacenamiento a 100.

### Paso 2: ALU

Seleccionar el procesador a simular

```
[   ] RISC-V               [ x   ] MIPS                    [    ] x86            [    ] M1             [    ] AMD
```

### Paso 3. Pruebas técnicas al simulador del procesador MIPS

- PROMPT: Analiza las siguientes clases. <CÓDIGO>
- PROMPT: Genera un plan de pruebas para el simulador del procesador MIPS.
- PROMPT: Genera un plan de pruebas de integración de código para el simulador de procesador MIPS.
- PROMPT: Genera un plan de pruebas de sistema para el código para el simulador del procesador MIPS.

### Paso 4. Pruebas de usuario final.

- Genera un código en el lenguaje de programación para el procesador MIPS.
- Sumatoria desde un número inicial a uno final.
- Factorial de un número.
- Integral definida desde un punto inicial a uno final.
- Promedio de un vector fijo de elementos.
- Cálculo de un perceptrón con entradas fijas.
- Capturar pantallas al inicio y al final de cada ejecución del código.

PROMPT.

A continuación te paso una lista de instrucciones del procesador MIPS que puedo compilar en mi simulador del procesador MIPS. Genera un código en lenguaje ensamblador para el procesador MIPS que implemente el factorial de un número. Incluye la explicación de cada línea de código. Sólo utiliza las instrucciones que te muestro a continuación.

### Paso 5. Generar el reporte.

Generar un reporte con el siguiente contenido.

Descargar la plantilla UV [Descargar] para generar la práctica.

- Portada

  - Nombres, matrículas, título, fecha de entrega del documento

- Introducción.

  - Razones por las cuales desarrollar un procesador propio desde cero.
  - Que ganancia tiene un alumno cuando sabe implementar un simulador de un procesador real.
  - Explique la razón de implementar un simulador para el procesador MIPS.

- Evidencia de ejecución del código.

  - Generar una tabla con la siguiente forma:

    - Columnas: Línea de código a ejecutar, resultado del simulador.
    - Filas: Líneas de código y su resultado.

- Uso de inteligencias artificiales para la generación de código

  - Incluir los PROMPTs

- Reflexiones finales

  - Sobre el nivel de programación que se requiere para implementar este tipo de simuladores.
  - Sobre el trabajo en equipo en el diseño e implementación de un procesador.
  - Explique a los estudiantes que cursarán esta experiencia educativa el próximo año porque les conviene tener el conocimiento que se ve en este curso.

- El documento debe entregarse en formato PDF de manera exclusiva.
- El único medio para entregar el documento es Eminus. Se rechazará la entrega por otros medios.

## Rúbrica de evaluación.

| Rubro | No cumple | Cumple parcialmente | Cumple completamente |
| --- | --- | --- | --- |
| Formato | No aplica, No corresponde, No entregó, Sin la calidad requerida, No cumple con ningún requisito solicitado. | Cumple con algunos de la siguiente información:<br>Nombres, matrículas, título, fecha de entrega del documento.<br>Plantilla UV | Cumple con toda la información solicitada:<br>Nombres, matrículas, título, fecha de entrega del documento.<br>Plantilla UV |
| Introducción | No aplica, No corresponde, No entregó, Sin la calidad requerida | Cumple con algunos de la siguiente información:<br>Razones por las cuales desarrollar un procesador propio desde cero.<br>Que ganancia tiene un alumno cuando sabe implementar un simulador de un procesador real.<br>Explique la razón de implementar un simulador para el procesador MIPS. | Cumple con toda la información solicitada:<br>Razones por las cuales desarrollar un procesador propio desde cero.<br>Que ganancia tiene un alumno cuando sabe implementar un simulador de un procesador real.<br>Explique la razón de implementar un simulador para el procesador MIPS. |
| Evidencias de realización | No aplica, No corresponde, No entregó, Sin la calidad requerida | Cumple con algunos de la siguiente información:<br>Tabla: Línea de código a ejecutar, resultado del simulador. | Cumple con toda la información solicitada:<br>Tabla: Línea de código a ejecutar, resultado del simulador. |
| Reflexiones finales | No aplica, No corresponde, No entregó, Sin la calidad requerida | Cumple con algunos de la siguiente información:<br>Sobre el nivel de programación que se requiere para implementar este tipo de simuladores.<br>Sobre el trabajo en equipo en el diseño e implementación de un procesador.<br>Explique a los estudiantes que cursarán esta experiencia educativa el próximo año porque les conviene tener el conocimiento que se ve en este curso. | Cumple con toda la información solicitada:<br>Sobre el nivel de programación que se requiere para implementar este tipo de simuladores.<br>Sobre el trabajo en equipo en el diseño e implementación de un procesador.<br>Explique a los estudiantes que cursarán esta experiencia educativa el próximo año porque les conviene tener el conocimiento que se ve en este curso. |
| Axiológico | No aplica, no entregó, no tiene la calidad requerida | Cumple con la mayoría:<br>Libertad. Responsabilidad. Solidaridad. Respeto. Honestidad.<br>Integridad. | Cumple con la totalidad:<br>Libertad. Responsabilidad. Solidaridad. Respeto. Honestidad.<br>Integridad. |
| Heurístico | No aplica, no entregó, no tiene la calidad requerida | Cumple con la mayoría:<br>Instrucciones de esta práctica | Cumple con la totalidad:<br>Instrucciones de esta práctica |
| Uso de CACHÉton | No aplica, no entregó, no tiene la calidad requerida | Están registrados de 20 a 50 consultas en CACHÉton. | Están registradas 51 o más consultas en CACHÉton. |
