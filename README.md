# Practica 2 - Simulador basico de un procesador (Java)

Simulador de un procesador MIPS construido con unidades funcionales:
almacenamiento, ALU y control de flujo. Los programas se construyen
directamente con instrucciones MIPS desde Java mediante `ProgramaBuilder`,
sin archivos de texto ensamblado.

## Requisitos

- Java (JDK 8 o superior)

## Estructura

```
src/
  almacenamiento/   UnidadAlmacenamiento, DiscoDuro, RAM, ROM, Registros, ContadorPrograma
  alu/              ALU y OperacionALU
  mips/             Instruccion, MemoriaSistema, ProcesadorMIPS, ProgramaBuilder,
                    Desensamblador, Binario, Simulador, VentanaSimulador
  programas/        Sumatoria, Factorial, Integral, Promedio, Perceptron, Impresion
  pruebas/          MiniTest, PruebasUnitarias, PruebasIntegracion, PruebasSistema, EjecutorPruebas
docs/               arquitectura, planes de pruebas, explicacion del factorial
salidas/            capturas de ejecucion (inicio y fin) de cada programa
```

## Compilar

```bash
./compilar.sh
```

## Ventana grafica

```bash
java -cp out mips.VentanaSimulador    # o simplemente: java -cp out mips.Simulador
./ventana.sh
```

Campos de la ventana:

| Campo | Contenido |
|-------|-----------|
| Instrucciones (Binario) | entrada de instrucciones en binario (32 bits por palabra, una por linea; tambien acepta `0x` hexadecimal y comentarios con `#`) |
| Historial | por cada ciclo: `Ciclo`, `PC` (binario), `Instruccion` (ensamblador + binario), `Operacion` y `PC Siguiente` (binario) |
| Registros | `nombre = binario` de los 32 registros |
| RAM | `RAM[i] (direccion) = binario` de las 100 celdas |
| Estado CPU | `PC`, `HI` y `LO` en binario |

Botones: `Ejecutar 1 ciclo`, `Ejecutar programa`, `Cargar en el PC`,
`Cargar ejemplo`, `Reiniciar CPU`, `Limpiar historial`.

Atajos de teclado: `Ctrl+Intro` ejecuta un ciclo y `Ctrl+Mayus+Intro`
ejecuta todo el programa.

## Ejecutar un programa (modo texto)

```bash
java -cp out mips.Simulador factorial
java -cp out mips.Simulador factorial --traza --estado
java -cp out mips.Simulador factorial --estado --captura salidas/factorial_captura.txt
```

Opciones:

- `--traza`: imprime cada ciclo (PC y codigo de la instruccion en hexadecimal)
- `--estado`: vuelca registros, PC, HI/LO y RAM al final
- `--captura <archivo>`: guarda la salida de la ejecucion en un archivo

Programas disponibles: `sumatoria`, `factorial`, `integral`, `promedio`,
`perceptron`.

## Pruebas (Paso 3)

```bash
java -cp out pruebas.PruebasUnitarias
java -cp out pruebas.PruebasIntegracion
java -cp out pruebas.PruebasSistema

java -cp out pruebas.EjecutorPruebas   # las tres suites
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

- `docs/arquitectura.md` - Paso 1 y 2
- `docs/plan_pruebas_unitarias.md`
- `docs/plan_pruebas_integracion.md`
- `docs/plan_pruebas_sistema.md`
- `docs/factorial_explicacion.md`
- `docs/instrucciones.md`
