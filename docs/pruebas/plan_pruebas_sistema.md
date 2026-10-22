# Plan de pruebas de sistema - Emulador MIPS

- Alcance: los 5 programas de usuario final ejecutados de punta a punta
- Codigo: `src/pruebas/PruebasSistema.java`
- Ejecucion: `java -cp out pruebas.PruebasSistema` o `./probar.sh`

## Programas bajo prueba

| # | Clase | Descripcion | Salida esperada |
|---|-------|-------------|-----------------|
| 1 | `Sumatoria` | Suma de 3 a 10 | `Sumatoria de 3 a 10 = 52` |
| 2 | `Factorial` | 7! | `Factorial de 7 = 5040` |
| 3 | `Integral` | Integral de x^2 de 1 a 5 | `Integral de x^2 dx desde x=1 hasta x=5 = 30` |
| 4 | `Promedio` | Promedio de {4,8,15,16,23} | `Vector: 4 8 15 16 23` y `Promedio = 13` |
| 5 | `Perceptron` | Perceptron con entradas fijas | `z = 3  y = 1` |

## Por cada programa

- **Construccion**: `clase.construir()` no lanza excepciones.
- **Capacidad**: el codigo generado tiene <= 100 instrucciones (cabe en ROM).
- **Salida exacta**: la salida capturada por syscall es igual a la esperada
  (cadena completa, incluyendo saltos de linea).

## Prueba global: todos los programas finalizan

Para cada uno de los 5 programas:

- Se construye sin error y cabe en la ROM.
- Se ejecuta hasta el final (`detenido == true`).
- Ciclos razonables (< 10000).
- Termina con syscall exit (PC dentro de ROM al detenerse).

## Capturas (Paso 4)

`./ejecutar_programas.sh` corre cada programa con `--estado` y
`--captura`, guardando en `salidas/` un archivo `*_captura.txt` por
programa con tres bloques:

1. ESTADO INICIAL (registros y PC)
2. SALIDA DEL PROGRAMA (syscalls)
3. ESTADO FINAL (registros, PC, HI/LO y RAM)

Las salidas esperadas son las mismas de la tabla anterior.
