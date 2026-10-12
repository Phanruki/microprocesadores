# Factorial: explicacion linea a linea

Programa: `src/programas/Factorial.java`
Salida: `Factorial de 7 = 5040`
Captura: `salidas/factorial_captura.txt`

El programa no esta en un archivo de texto: se construye en Java con
`mips.ProgramaBuilder`, que emite una instruccion MIPS por cada llamada.
Cada instruccion ocupa una celda de la ROM (100 celdas maximo). Este
programa usa 26.

## 1. Datos

```java
int dirN     = p.palabra(7);              // una celda en RAM con el valor 7
int dirMsg1  = p.cadena("Factorial de "); // caracteres + terminador 0
int dirMsg2  = p.cadena(" = ");
int dirSalto = p.cadena("\n");
```

- `palabra` reserva una celda con un entero.
- `cadena` guarda un caracter por celda y termina en 0 (NULL).
- Las direcciones son absolutas: la RAM empieza en la celda 100.

## 2. Carga de n y mensaje inicial

```java
p.lwAbs(T0, dirN);            // $t0 = RAM[dirN] = 7
Impresion.cadena(p, dirMsg1); // imprime "Factorial de "
Impresion.entero(p, T0);      // imprime "7"
Impresion.cadena(p, dirMsg2); // imprime " = "
```

`Impresion` (clase auxiliar) arma la secuencia de syscalls: direccion de
la cadena en `$a0` con servicio 4, valor en `$a0` con servicio 1.

## 3. Inicializacion

```java
p.cargarConstante(T1, 1);   // $t1 = 1  (acumulado)
p.cargarConstante(T2, 1);   // $t2 = 1  (contador i)
```

## 4. Bucle (etiqueta `bucle`)

```java
p.etiqueta("bucle");
p.slt(T3, T0, T2);             // $t3 = 1 si $t0 < $t2, si no 0
p.bne(T3, ZERO, "fin_bucle");  // si n < i, salta al fin
p.mult(T1, T2);                // LO:HI = $t1 * $t2
p.mflo(T1);                    // $t1 = LO  (acumulado *= i)
p.addiu(T2, T2, 1);            // $t2 = $t2 + 1  (i++)
p.j("bucle");                  // vuelve al inicio
p.etiqueta("fin_bucle");
```

`slt rd, rs, rt` pone 1 en `rd` cuando `rs < rt` con signo. Como `$t0`
(n = 7) y `$t2` (i) son positivos, equivale a la comparacion `n < i`.
No hay `bgt` ni flags: la rama se hace con `slt` + `bne`.

| i antes | acumulado antes | operacion | acumulado despues | n < i? |
|---------|-----------------|-----------|-------------------|--------|
| 1 | 1 | 1 * 1 | 1 | no |
| 2 | 1 | 1 * 2 | 2 | no |
| 3 | 2 | 2 * 3 | 6 | no |
| 4 | 6 | 6 * 4 | 24 | no |
| 5 | 24 | 24 * 5 | 120 | no |
| 6 | 120 | 120 * 6 | 720 | no |
| 7 | 720 | 720 * 7 | 5040 | no |
| 8 | 5040 | — | — | si: sale |

## 5. Impresion del resultado y fin

```java
Impresion.entero(p, T1);   // imprime "5040"  ($a0 = $t1, servicio 1)
Impresion.cadena(p, dirSalto); // imprime "\n"
Impresion.terminar(p);     // servicio 10: detiene la CPU
```

## Registros finales (captura)

| Registro | Valor | Significado |
|----------|-------|-------------|
| $t0 | 7 | n |
| $t1 | 5040 | resultado (7!) |
| $t2 | 8 | contador i al salir del bucle |
| $t3 | 1 | n < i se cumplio |
| $v0 | 10 | servicio exit |
| LO | 5040 | ultima multiplicacion |

## Instrucciones usadas

| Instruccion | Significado |
|-------------|-------------|
| `lw rt, dir` | carga de memoria a registro (direccion absoluta) |
| `addiu rt, rs, imm` | suma inmediata sin signo |
| `slt rd, rs, rt` | 1 si rs < rt con signo |
| `bne rs, rt, label` | salta si rs != rt |
| `mult rs, rt` | LO:HI = rs * rt |
| `mflo rd` | rd = LO |
| `j label` | salto incondicional |
| `syscall` | servicio simulado ($v0 = 1, 4 o 10) |
