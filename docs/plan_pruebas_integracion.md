# Plan de pruebas de integracion - Simulador MIPS

- Alcance: composicion de unidades y ciclo de la CPU completo
- Codigo: `src/pruebas/PruebasIntegracion.java`
- Ejecucion: `java -cp out pruebas.PruebasIntegracion` o `./probar.sh`

Flujo: `ProgramaBuilder -> ROM + RAM -> ProcesadorMIPS.ciclo() -> salida`.

## 1. ProgramaBuilder y ROM

- Construir un programa de 5 instrucciones.
- Verificar: 5 instrucciones en ROM, ROM bloqueada despues de cargar,
  PC inicia en 0, instruccion 0 == generada (bit a bit).

## 2. Aritmetica

Programa con addu, subu, andi, ori, xori y terminacion por syscall:

- `$t0 = 10`, `$t1 = 32`
- `$t2 = 10 + 32 = 42`
- `$t3 = 32 - 10 = 22`
- `andi` 0xFF & 0x0F = 0x0F
- `ori` resultado correcto
- `xor` 0x0F ^ 0xF0 = 0xFF
- CPU detenida por syscall exit

## 3. Desbordamiento

- `addi` con desbordamiento de signo -> ArithmeticException y CPU detenida
- `addiu` MAX+1 envuelve sin excepcion

## 4. Ramas condicionales

- `slt` 3 < 5 = 1
- `beq` no tomado: ejecucion continua, `$t3 = 99`
- `bne` tomado: salta a la etiqueta, `$t3 = 55`

## 5. Bucle

- Suma de 1 a 5 con `slt` + `bne` + `addu` + `j`
- Resultado: suma = 15, ciclos ejecutados > 10 (hay bucle real)

## 6. Memoria de datos

- `sw` escribe 123 y `lw` relee 123
- `RAM[var] = 123`
- Arreglo de 3 elementos: arr[0]=10, arr[1]=20, arr[2]=30
- Suma del arreglo = 60

## 7. Syscall

- Servicio 4 (cadena): salida exacta "Hola MIPS42"
- Servicio 10 (exit): CPU detenida = true

## 8. Subrutina (jal / jr)

- `jal doble` con argumento 10
- `$ra` guarda el retorno, `jr $ra` vuelve
- Resultado: doble(10) = 20, `$t9 = 20` despues del retorno

## 9. mult / div

- `mult` 7 * 6 = 42 (en LO)
- `div` 7 / 6 cociente = 1 (LO), resto = 1 (HI)
- `mflo` / `mfhi` copian los valores a registros
