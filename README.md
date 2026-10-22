# Practica 2 - Simulador basico de un procesador (Java)

Simulador de un procesador MIPS construido como un nucleo de 5 paquetes
grandes (`cpu`, `ejecucion`, `memoria`, `alu`, `instruccion`) al estilo del
diagrama del compañero, con concepto de **segmento y direccion** en la memoria,
una **CPU que nunca muere** (arranca en 0, siempre ejecuta) y una instruccion
**HALT** ("alto") con la que el programa le avisa a la CPU que termino.

Los programas se construyen directamente con instrucciones MIPS desde Java
mediante `ProgramaBuilder`, sin archivos de texto ensamblado.

## Requisitos

- Java (JDK 8 o superior)

## Estructura

```
src/
  cpu/              CPU (ciclo de 5 etapas IF/ID/EX/MEM/WB) y PC
  ejecucion/         EjecutorInstrucciones, UnidadControl y SenalesControl
  memoria/           MemoriaSistema, UnidadAlmacenamiento, ROM ("texto"@0),
                     RAM ("datos"@100), DiscoDuro ("disco"@200) y Registros
  alu/               ALU y enum OperacionALU (incluye LUI)
  instruccion/       Instruccion, Decodificador, Opcode, Funct, FormatoInstruccion,
                     ServicioSyscall, ProgramaBuilder, Desensamblador, Binario
  ui/                Simulador (modo texto) y VentanaSimulador (ventana grafica)
  programas/         Sumatoria, Factorial, Integral, Promedio, Perceptron, Impresion
  pruebas/           MiniTest, PruebasUnitarias, PruebasIntegracion,
                     PruebasSistema, EjecutorPruebas
docs/                arquitectura, planes de pruebas, diagramas de clases
salidas/             capturas de ejecucion (inicio y fin) de cada programa
```

## Compilar

```bash
./compilar.sh
```

## Ventana grafica

```bash
java -cp out ui.VentanaSimulador    # o simplemente: java -cp out ui.Simulador
./ventana.sh
```

Campos de la ventana:

| Campo | Contenido |
|-------|-----------|
| Instrucciones (Binario) | entrada de instrucciones en binario (32 bits por palabra, una por linea; tambien acepta `0x` hexadecimal y comentarios con `#`) |
| Historial | por cada ciclo: `Ciclo`, `PC` (binario), `Instruccion` (ensamblador + binario), `Operacion`, las senales de control y `PC Siguiente` (binario) |
| Registros | `nombre = binario` de los 32 registros |
| Memoria | volcado por segmentos (texto, datos, disco) con direccion decimal y hexadecimal; la RAM inicia en `000` |
| Estado CPU | `PC`, `HI` y `LO` en binario |

Botones: `Ejecutar 1 ciclo`, `Ejecutar programa`, `Cargar en el PC`,
`Cargar ejemplo`, `Reiniciar CPU`, `Limpiar historial`.

Atajos de teclado: `Ctrl+Intro` ejecuta un ciclo y `Ctrl+Mayus+Intro`
ejecuta todo el programa.

## Ejecutar un programa (modo texto)

```bash
java -cp out ui.Simulador factorial
java -cp out ui.Simulador factorial --traza --estado
java -cp out ui.Simulador factorial --estado --captura salidas/factorial_captura.txt
```

Opciones:

- `--traza`: imprime cada ciclo (PC y codigo de la instruccion en hexadecimal)
- `--estado`: vuelca registros, PC, HI/LO y el mapa de memoria al final
- `--captura <archivo>`: guarda la salida de la ejecucion en un archivo

Programas disponibles: `sumatoria`, `factorial`, `integral`, `promedio`,
`perceptron`.

## Pruebas (Paso 3)

```bash
java -cp out pruebas.PruebasUnitarias
java -cp out pruebas.PruebasIntegracion
java -cp out pruebas.PruebasSistema

java -cp out pruebas.EjecutorPruebas   # las tres suites (201 pruebas)
```

o bien `./probar.sh`

## Programas de usuario final (Paso 4)

| Programa | Descripcion | Salida |
|----------|-------------|--------|
| `sumatoria` | Suma de 3 a 10 | Sumatoria de 3 a 10 = 52 |
| `factorial` | 7! | Factorial de 7 = 5040 |
| `integral` | Integral de x^2 de 1 a 5 | = 30 |
| `promedio` | Promedio de {4,8,15,16,23} | Promedio = 13 |
| `perceptron` | Perceptron con entradas fijas | z = 3  y = 1 |

Capturas inicio/fin: carpeta `salidas/`. Script: `./ejecutar_programas.sh`

## Documentacion

- `docs/arquitectura.md` - Paso 1 y 2 (5 paquetes, segmentos, CPU nunca muere, HALT)
- `docs/instrucciones.md` - conjunto de instrucciones (incluye halt)
- `docs/diagrama_clases_nucleo.png` / `.puml` - diagrama del nucleo (5 paquetes)
- `docs/diagrama_clases_exterior.png` / `.puml` - como se usa el nucleo (ui, programas, pruebas)
- `docs/generacion_diagrama_clases.md` - como regenerar los diagramas
- `docs/plan_pruebas_unitarias.md`
- `docs/plan_pruebas_integracion.md`
- `docs/plan_pruebas_sistema.md`
- `docs/factorial_explicacion.md`
