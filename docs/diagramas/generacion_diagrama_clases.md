# Generación del diagrama de clases

Este documento explica cómo se generaron los diagramas de clases del proyecto,
qué herramientas hace falta y cómo regenerarlos o modificarlos.

## Diagramas generados

| Archivo | Contenido |
|---|---|
| `diagrama_clases_nucleo.puml` / `.png` | Núcleo del simulador: los **5 paquetes grandes** (`cpu`, `ejecucion`, `memoria`, `alu`, `instruccion`): `CPU` (ciclo de 5 etapas IF/ID/EX/MEM/WB, "nunca muere") y `PC`; `EjecutorInstrucciones`, `UnidadControl` y `SenalesControl`; `MemoriaSistema` con el mapa segmentado (ROM "texto"@0, RAM "datos"@100, DiscoDuro "disco"@200) y `Registros`; `ALU` + enum `OperacionALU`; `Instruccion`, `Decodificador`, `Opcode` (con HALT=0x3F), `Funct`, `FormatoInstruccion`, `ServicioSyscall`, `ProgramaBuilder` + `Programa`, `Desensamblador`, `Binario` |
| `diagrama_clases_exterior.puml` / `.png` | Cómo se usa el núcleo desde fuera: el núcleo aparece como clases simplificadas (`CPU`, `MemoriaSistema`, `Registros`, `RAM`, `SenalesControl`, `ProgramaBuilder`, `ServicioSyscall`); paquete `ui` (`Simulador`, `VentanaSimulador`); paquete `programas` (los 5 programas + `Impresion`); paquete `pruebas` (`MiniTest`, `EjecutorPruebas` y las 3 suites) |

Se separaron en dos diagramas porque una única imagen con todas las clases del
proyecto resultaba demasiado grande y difícil de leer. El primero concentra el
núcleo (el que pide el profesor: 5 paquetes); el segundo muestra las
relaciones con la interfaz, los programas y las pruebas sin repetir todos los
miembros del núcleo.

## Herramientas utilizadas

| Herramienta | Detalle | ¿Instalada en el sistema? |
|---|---|---|
| Java (JRE/JDK) | Usado para ejecutar PlantUML. El proyecto ya compilaba con `javac` | Ya estaba (`openjdk 25.0.4.1`, `/usr/bin/java`) |
| PlantUML | JAR oficial `plantuml-1.2026.5.jar` (~28 MB) descargado de Maven Central | No. Solo se descargó un archivo a `/tmp` |
| Smetana | Motor de layout (diseño de cajas y flechas) incluido dentro del propio JAR de PlantUML | No requiere nada: evita instalar Graphviz |
| Graphviz (`dot`) | Habitual en PlantUML para otros tipos de diagrama | **No hace falta** para diagramas de clases con Smetana |
| Mermaid | — | No se usa: el estilo visual no convenció y se descartó |

No se instaló ningún paquete del sistema en ningún momento. PlantUML se ejecuta
directamente con `java -jar`.

### Descarga de PlantUML

El JAR se obtuvo de Maven Central:

```bash
mkdir -p /tmp/opencode/plantuml
curl -fSL -o /tmp/opencode/plantuml/plantuml.jar \
  https://repo1.maven.org/maven2/net/sourceforge/plantuml/plantuml/1.2026.5/plantuml-1.2026.5.jar
```

> `/tmp` se limpia al reiniciar. Si se quiere conservar el JAR, copiarlo a un
> sitio permanente (por ejemplo `~/.local/share/plantuml/plantuml.jar`) y ajustar
> los comandos de abajo.

### Alternativa: instalar PlantUML de forma permanente

Si prefieres tenerlo instalado de verdad (requiere privilegios de administrador,
no se ejecutó en este entorno):

```bash
# Fedora / RHEL
sudo dnf install plantuml

# Ubuntu / Debian
sudo apt install plantuml
```

Con eso, basta con `plantuml docs/diagramas/diagrama_clases_nucleo.puml` en lugar de
`java -jar /tmp/opencode/plantuml/plantuml.jar ...`.

## Cómo regenerar los diagramas

Desde la raíz del proyecto:

```bash
JAR=/tmp/opencode/plantuml/plantuml.jar

# PNG (mapa de bits, tamaño real) — sin -o: la salida queda junto al .puml
java -jar "$JAR" -tpng \
  docs/diagramas/diagrama_clases_nucleo.puml \
  docs/diagramas/diagrama_clases_exterior.puml

# SVG (vectorial), si hace falta
java -jar "$JAR" -tsvg \
  docs/diagramas/diagrama_clases_nucleo.puml \
  docs/diagramas/diagrama_clases_exterior.puml
```

Notas:
- No indicar `-o docs/diagramas` cuando los `.puml` ya estan en
  `docs/diagramas/`: PlantUML interpreta `-o` como relativo al archivo de
  entrada y crearia `docs/diagramas/docs/diagramas/`. Sin `-o`, el PNG se
  escribe junto al `.puml`.
- No usar dos flags de formato en la misma llamada (por ejemplo
  `-tsvg -tpng`): solo aplica el último. Ejecutar un comando por formato.
- Evitar `-Sdpi=144` (u otros DPI altos) en el PNG: la imagen supera el límite
  de 4096 px de PlantUML y sale deformada. Con el PNG a escala 1:1 el resultado
  es correcto.
- PlantUML valida la sintaxis al ejecutar: si hay un error en el `.puml`,
  aparece un mensaje indicando la línea.
- Sin Graphviz instalado hace falta `!pragma layout smetana` en la primera
  linea de cada `.puml` (ya esta puesto en ambos).

### Verificación rápida

Tras regenerar, comprobar que los PNG tienen un tamaño razonable:

```bash
python3 - <<'EOF'
import struct
for f in ['docs/diagramas/diagrama_clases_nucleo.png', 'docs/diagramas/diagrama_clases_exterior.png']:
    with open(f, 'rb') as fh:
        d = fh.read(33)
    print(f, struct.unpack('>II', d[16:24]))
EOF
```

## Cómo modificar los diagramas

Los `.puml` son texto plano y están en este mismo directorio. Cualquier cambio
(métodos nuevos, clases nuevas, otras relaciones) se edita ahí y se vuelve a
generar con los comandos de arriba. Puntos clave de la fuente actual:

- `!pragma layout smetana` en la primera línea: obligatorio para que el
  diagrama se dibuje sin Graphviz instalado.
- El diseño (posición de cajas y flechas) lo calcula Smetana a partir del orden
  de declaración de las clases y de las relaciones: reordenando declaraciones o
  añadiendo enlaces ocultos (`.[hidden].`) se puede influir en la disposición.
- Estilo visual: bloques `skinparam` al principio (colores, tipografía,
  esquinas redondeadas). Los colores usan la paleta Nord (`#D8DEE9`,
  `#4C566A`, `#2E3440`).
- Notación UML de las flechas:
  - `--|>` herencia
  - `*--` composición
  - `o--` agregación
  - `..>` dependencia / uso

Cuando el diagrama cambie, actualizar también las tablas del inicio de este
documento si se añaden o eliminan clases.
