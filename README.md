# 🚀 Tiny++ Compiler (Tiny_plus_plus)

Compilador para el lenguaje de programación **Tiny++** desarrollado en Java utilizando **JFlex** (Analizador Léxico) y **Java CUP** (Analizador Sintáctico), con generación de código objeto para la Máquina Virtual TM (Tiny Machine) e IDE con Depurador Visual.

---

## 📊 Estado Actual del Proyecto y Módulos

El proyecto se encuentra dividido en las siguientes áreas de responsabilidad:

### 👤 Módulo 1: Frontend y Verificador Semántico (Integrante 1)
- [x] Ampliación del analizador léxico (`lexico.flex`) con nuevos tokens: tipos (`int`, `bool`, `string`), subrutinas (`function`, `procedure`, `return`), delimitadores de arreglos (`[`, `]`, `,`), comparadores (`>`, `<=`, `>=`, `!=`), operadores booleanos (`and`, `or`, `not`) y literales de cadena.
- [ ] Ampliación de la gramática sintáctica (`sintactico.cup`).
- [ ] Tabla de símbolos con pila de ámbitos (Scopes locales y globales) y chequeo estricto de tipos.

### 👤 Módulo 2: Backend y Generación de Código TM (Integrante 2)
- [x] **Misión 1 - Operadores Relacionales, Lógicos y Booleanos:**
  - Nuevos operadores en `tipoOp.java`: `>`, `<=`, `>=`, `!=`, `and`, `or`, `not`.
  - Nodo AST `NodoBooleano.java` para literales `true` (1) y `false` (0).
  - Emisión de instrucciones TM con saltos condicionales (`JGT`, `JLE`, `JGE`, `JNE`, `JEQ`, `JNE`) y operaciones lógicas normalizadas en `Generador.java`.
- [x] **Misión 2 - Arreglos Unidimensionales (Vectores):**
  - Nuevos nodos AST: `NodoAccesoArreglo.java` (lectura `A[indice]`) y `NodoAsignacionArreglo.java` (escritura `A[indice] := exp`).
  - Asignación del registro temporal `R2` (`r2`) en `UtGen.java` para cálculo aritmético de direcciones en tiempo de ejecución.
  - Soporte de expresiones complejas en el índice (ej: `A[i * 2 + 1] := 10;` y `x := A[i * 2 + 1] + 5;`).
  - Manejo de desplazamientos dinámicos en la Memoria de Datos (`dMem`) mediante `LDA`, `ADD` e indexación indirecta con `LD`/`ST`.
- [ ] **Misión 3 - Cadenas de Texto (`string`):** Almacenamiento continuo en memoria y rutinas de E/S.
- [ ] **Misión 4 - Subrutinas, Funciones y Marcos de Pila (Stack Frames):** Gestión de llamadas con registros `MP`, `GP` y `PC`.

### 👤 Módulos 3 y 4: IDE y Depurador Visual (Integrante 3)
- [x] Prototipo del simulador visual de la Tiny Machine (`tmsimv1.html`) con visualización de `dMem`, `iMem` y registros (`PC`, `GP`, `MP`, `AC`, `AC1`).
- [ ] Integración del editor de código Tiny++, consola de compilación y enlace de depuración paso a paso.

---

## 📁 Estructura del Proyecto

```text
TinyPlus/
├── src/
│   ├── especificacion/
│   │   ├── lexico.flex              # Especificación del analizador léxico (JFlex)
│   │   ├── sintactico.cup           # Gramática del analizador sintáctico (Java CUP)
│   │   ├── java-cup-11b.jar         # Librería CUP Compiler
│   │   ├── java-cup-11b-runtime.jar # Librería CUP Runtime
│   │   └── jflex-full-1.8.2.jar     # Librería JFlex
│   └── ve/edu/unet/
│       ├── Generador.java           # Generador de código máquina TM
│       ├── UtGen.java               # Utilidades de emisión de instrucciones TM
│       ├── TablaSimbolos.java       # Gestión de la Tabla de Símbolos
│       ├── TestBackend.java         # Suite de pruebas unitarias del backend
│       └── nodosAST/                # Nodos del Árbol de Sintaxis Abstracta (AST)
│           ├── NodoAccesoArreglo.java     # Lectura de arreglos A[exp]
│           ├── NodoAsignacionArreglo.java # Escritura en arreglos A[exp] := exp
│           ├── NodoBooleano.java          # Literales true / false
│           ├── tipoOp.java                # Enumeración de operadores ampliados
│           └── ...                        # Demás nodos de expresiones y sentencias
├── ejemplos/                        # Archivos de prueba en código Tiny++ (.tny)
├── compilar_gramatica.bat           # Script para regenerar Léxico.java y parser.java
├── compilar_codigo.bat              # Script para compilar las clases Java
├── ejecutar.bat                     # Script para ejecutar el compilador sobre un archivo .tny
├── probar_backend.bat               # Script para validar la generación de código TM aislada
├── tmsimv1.html                     # Simulador gráfico web de la Tiny Machine
└── TM.C                             # Simulador en lenguaje C para la máquina TM
```

---

## ⚙️ Requisitos

- **Java JDK 8+** (configurado en `PATH` o detectado automáticamente por los scripts).
- **GCC / MinGW** (opcional, para compilar el simulador C `TM.C`).

---

## 🚀 Compilación y Ejecución

### 1. Probar la Generación de Código del Backend (Misiones 1 y 2)
Permite validar el funcionamiento de operadores relacionales, booleanos y arreglos unidimensionales en la TM de forma directa:
```cmd
probar_backend.bat
```

### 2. Regenerar Léxico y Sintáctico (JFlex + CUP)
```cmd
compilar_gramatica.bat
```

### 3. Compilar el Código Fuente Java
```cmd
compilar_codigo.bat
```

### 4. Ejecutar el Compilador sobre un archivo `.tny`
```cmd
ejecutar.bat ejemplos\factorial.tny
```

---

## 👨‍💻 Créditos
Desarrollado para la materia de **Compiladores e Intérpretes** — **Universidad Nacional Experimental del Táchira (UNET)**.
