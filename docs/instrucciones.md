# Conjunto de instrucciones del simulador MIPS

Instrucciones que el procesador ejecuta. Cada una se codifica en 32 bits
con los tres formatos de MIPS (`src/mips/Instruccion.java`).

## Formato R (opcode 0x00)

| Instruccion | funct | Efecto |
|-------------|-------|--------|
| `add rd, rs, rt` | 0x20 | rd = rs + rt (detiene si desborda) |
| `addu rd, rs, rt` | 0x21 | rd = rs + rt (envuelve siempre) |
| `sub rd, rs, rt` | 0x22 | rd = rs - rt (detiene si desborda) |
| `subu rd, rs, rt` | 0x23 | rd = rs - rt (envuelve siempre) |
| `and rd, rs, rt` | 0x24 | rd = rs AND rt |
| `or rd, rs, rt` | 0x25 | rd = rs OR rt |
| `xor rd, rs, rt` | 0x26 | rd = rs XOR rt |
| `nor rd, rs, rt` | 0x27 | rd = NOR(rs, rt) |
| `slt rd, rs, rt` | 0x2A | rd = 1 si rs < rt con signo, si no 0 |
| `sltu rd, rs, rt` | 0x2B | rd = 1 si rs < rt sin signo, si no 0 |
| `sll rd, rt, shamt` | 0x00 | rd = rt << shamt (rs = 0) |
| `srl rd, rt, shamt` | 0x02 | rd = rt >> shamt, logico (rs = 0) |
| `mult rs, rt` | 0x18 | LO:HI = rs * rt |
| `div rs, rt` | 0x1A | LO = rs / rt; HI = rs % rt (rt = 0 lanza excepcion) |
| `mflo rd` | 0x12 | rd = LO |
| `mfhi rd` | 0x10 | rd = HI |
| `jr rs` | 0x08 | PC = rs |
| `syscall` | 0x0C | servicio indicado por $v0 |

En `sltu` el operando -1 se interpreta como 4294967295 (valor sin signo).

## Formato I

| Instruccion | Opcode | Efecto |
|-------------|--------|--------|
| `addi rt, rs, imm` | 0x08 | rt = rs + imm (extiende signo; detiene si desborda) |
| `addiu rt, rs, imm` | 0x09 | rt = rs + imm (extiende signo; nunca desborda) |
| `slti rt, rs, imm` | 0x0A | rt = 1 si rs < imm con signo |
| `sltiu rt, rs, imm` | 0x0B | rt = 1 si rs < imm sin signo |
| `andi rt, rs, imm` | 0x0C | rt = rs AND imm (extiende ceros) |
| `ori rt, rs, imm` | 0x0D | rt = rs OR imm (extiende ceros) |
| `xori rt, rs, imm` | 0x0E | rt = rs XOR imm (extiende ceros) |
| `lui rt, imm` | 0x0F | rt = imm << 16 |
| `lw rt, offset(rs)` | 0x23 | rt = Mem[rs + offset] |
| `sw rt, offset(rs)` | 0x2B | Mem[rs + offset] = rt |
| `beq rs, rt, label` | 0x04 | si rs == rt, PC = label |
| `bne rs, rt, label` | 0x05 | si rs != rt, PC = label |

Las direcciones son indices de celdas de 32 bits. El offset de beq/bne
es relativo a la siguiente instruccion.

## Formato J

| Instruccion | Opcode | Efecto |
|-------------|--------|--------|
| `j label` | 0x02 | PC = label |
| `jal label` | 0x03 | $ra = PC + 1; PC = label |

## Servicios syscall ($v0)

| $v0 | Servicio | Argumento |
|-----|----------|-----------|
| 1 | imprimir entero | $a0 = valor |
| 4 | imprimir cadena | $a0 = direccion (terminada en 0) |
| 5 | leer entero | resultado en $v0 |
| 11 | imprimir caracter | $a0 = codigo ASCII |
| 10 | terminar programa | - |

## Registros

`$zero $at $v0 $v1 $a0-$a3 $t0-$t7 $s0-$s7 $t8 $t9 $k0 $k1 $gp $sp $fp $ra`
(o `$0` a `$31`).

## Como se construyen los programas

`mips.ProgramaBuilder` emite estas instrucciones desde Java y resuelve las
etiquetas de ramas al generar el programa. Los programas de
`src/programas/` solo usan beq/bne con slt/slti/sltu para comparaciones, y
prefieren addu/subu/addiu para no provocar desbordamientos.
