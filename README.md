# 🚀 Tiny++ Compiler (Tiny_plus_plus)

Compilador integral para el lenguaje de programación **Tiny++** desarrollado en **Java**, utilizando **JFlex** (Analizador Léxico) y **Java CUP** (Analizador Sintáctico), con generación de código objeto para la **Máquina Virtual TM (Tiny Machine)** y un **IDE Web con Depurador Visual interactivo**.

---

## 🌟 Características del Sistema

El compilador implementa todas las fases clásicas de la ingeniería de compiladores, estructurado en tres grandes componentes:

### 🔤 1. Frontend y Verificación Semántica
- **Análisis Léxico (JFlex):** Reconocimiento de tipos de datos primitivos (`int`, `bool`, `string`), palabras reservadas de control (`if`, `then`, `else`, `end`, `repeat`, `until`), subrutinas (`function`, `procedure`, `return`), operadores aritméticos, relacionales (`<`, `<=`, `>`, `>=`, `=`, `!=`), lógicos (`and`, `or`, `not`), literales numéricos, booleanos y cadenas de texto.
- **Análisis Sintáctico (Java CUP):** Gramática formal LALR(1) libre de conflictos con resolución estricta de precedencia y asociatividad de operadores. Construcción de un **Árbol de Sintaxis Abstracta (AST)** polimórfico compuesto por 18 tipos de nodos especializados.
- **Gestión de Ámbitos (Scopes):** Tabla de símbolos jerárquica basada en un árbol de entornos que permite el aislamiento de variables locales en subrutinas, resolución de identificadores globales y sombreado de variables (*shadowing*).
- **Chequeo Semántico Estricto:** Verificación estática con 14 reglas de validación en tiempo de compilación (declaración obligatoria antes del uso, equivalencia de tipos en asignaciones y operaciones, condiciones booleanas en estructuras de control, validación de índices de arreglos y correspondencia de firmas en llamadas y retornos). Bloquea la emisión de código objeto si se detecta cualquier inconsistencia.

### ⚙️ 2. Backend y Generación de Código TM
- **Operadores Relacionales y Booleanos:** Evaluación en cortocircuito y normalización de valores de verdad (1 para `true`, 0 para `false`) utilizando saltos condicionales de la TM (`JLT`, `JLE`, `JGT`, `JGE`, `JEQ`, `JNE`).
- **Arreglos Unidimensionales (Vectores):** Soporte completo para indexación y asignación con expresiones aritméticas complejas (`A[i * 2 + 1] := expr;`). Cálculo dinámico de direcciones efectivas en tiempo de ejecución utilizando el registro de soporte `R2` (`r2`).
- **Cadenas de Texto (`string`):** Asignación estática de literales en un *String Literal Pool* en la Memoria de Datos (`dMem`) con terminador nulo `\0`. Bucle de recorrido en memoria para emisión de caracteres hacia la terminal con instrucciones `OUT`.
- **Subrutinas y Marcos de Activación (Stack Frames):** Gestión completa de procedimientos y funciones con llamadas anidadas.
  - Registro de activación estándar: `0(MP)` dirección de retorno, `1(MP)` enlace dinámico (*Old MP*), `2..N(MP)` parámetros y desplazamientos negativos para temporales y locales.
  - Retorno estándar de resultados en el acumulador `AC` (`r0`), restauración de marco y salto a dirección de retorno.

### 🖥️ 3. IDE Web y Depurador Visual de la Tiny Machine
- **Editor de Código:** Interfaz moderna con numeración de líneas sincronizada, atajos de indentación y gestión de archivos fuente `.tny`.
- **Servidor de Compilación Local:** Servicio en Node.js (`ide/server.js`) con autodetección de ejecutables JDK y compilación en caliente con un clic (`POST /compile`).
- **Depurador Visual de la TM:** Motor simulador en JavaScript (`tm-engine.js`) que replica el ciclo de instrucción de la máquina:
  - Ejecución controlada paso a paso (`Step`), por bloques (`×N`), continua (`Run`), pausa y reinicio.
  - Visualización reactiva de registros del procesador (`PC`, `GP`, `MP`, `AC`, `AC1`, `R2`).
  - Explorador dinámico de Memoria de Instrucciones (`iMem`) y Memoria de Datos (`dMem`) con vistas Low/High.
  - Consola multicanal integrada con sincronización en tiempo real de entradas (`IN`) y salidas (`OUT`).

---

## 📁 Estructura del Proyecto

```text
TinyPlus/
├── src/                             # Código fuente del compilador
│   ├── especificacion/
│   │   ├── lexico.flex              # Especificación léxica (JFlex)
│   │   ├── sintactico.cup           # Gramática sintáctica (Java CUP)
│   │   ├── java-cup-11b.jar         # Compilador Java CUP
│   │   ├── java-cup-11b-runtime.jar # Runtime de Java CUP
│   │   └── jflex-full-1.8.2.jar     # Generador léxico JFlex
│   └── ve/edu/unet/
│       ├── Lexico.java / parser.java / sym.java # Analizadores generados
│       ├── ChequeadorSemantico.java # Verificador semántico y de tipos
│       ├── Scope.java               # Árbol de ámbitos para variables
│       ├── TablaSimbolos.java       # Tabla de símbolos y memoria
│       ├── RegistroSimbolo.java     # Metadata de variables y subrutinas
│       ├── InfoSubrutina.java       # Información de marcos de pila
│       ├── Generador.java           # Generador de código máquina TM
│       ├── UtGen.java               # Utilidades de emisión de instrucciones
│       ├── TestBackend.java         # Suite de pruebas unitarias
│       └── nodosAST/                # Clases polimórficas del AST
├── ide/                             # Entorno de desarrollo web
│   ├── index.html                   # Interfaz visual del IDE y simulador
│   ├── server.js                    # Servidor local Node.js
│   ├── js/                          # editor.js, tm-engine.js, tm-ui.js
│   └── styles/                      # Hojas de estilo modulares
├── ejemplos/                        # Programas de prueba en Tiny++ (.tny)
│   ├── test_completo_tiny_plus.tny  # Prueba integral de características
│   ├── test_misiones_unificadas.tny # Prueba unificada de backend
│   ├── test_tiny_plus_full.tny      # Prueba completa de sintaxis
│   ├── test_scopes.tny              # Prueba de aislamiento de ámbitos
│   ├── test_errores_semanticos.tny  # Casos negativos de validación
│   ├── factorial_tiny_plus.tny      # Cálculo de factorial iterativo
│   └── ejemplo2_tiny_plus.tny       # Condicionales y lectura
├── compilar_codigo.bat              # Compila las clases Java en bin/
├── compilar_gramatica.bat           # Regenera el analizador con JFlex y CUP
├── ejecutar.bat                     # Compila y ejecuta un .tny por consola
├── probar_backend.bat               # Ejecuta las pruebas unitarias del backend
└── iniciar_servidor_ide.bat         # Inicia el IDE Web en el navegador
```

---

## ⚙️ Requisitos del Sistema

- **Java JDK 8 o superior** (detectado automáticamente por los scripts y el servidor).
- **Node.js 16 o superior** (para el entorno del IDE Web y servidor de compilación local).

---

## 🚀 Guía de Uso y Ejecución

### 1. Iniciar el IDE Web y Depurador Visual
Ejecute el script de inicio con un doble clic o desde la terminal:
```cmd
iniciar_servidor_ide.bat
```
El navegador se abrirá automáticamente en `http://localhost:3000`. Permite escribir o abrir código Tiny++, compilar con el botón **⚡ "Compilar y Cargar"** y depurar paso a paso en la Tiny Machine visualizando la memoria y los registros.

### 2. Ejecutar Pruebas Automatizadas del Backend
Valida la generación de código TM para operadores, arreglos, cadenas y subrutinas:
```cmd
probar_backend.bat
```

### 3. Compilar el Código Fuente Java
Recompila todas las clases Java del proyecto en la carpeta `bin/`:
```cmd
compilar_codigo.bat
```

### 4. Ejecutar el Compilador por Consola
Para compilar un programa específico y visualizar el AST, la tabla de símbolos y el listado TM:
```cmd
ejecutar.bat ejemplos\test_completo_tiny_plus.tny
```

### 5. Regenerar Analizadores Léxico y Sintáctico
Si se modifican las especificaciones `lexico.flex` o `sintactico.cup`:
```cmd
compilar_gramatica.bat
```

---

## 👨‍💻 Créditos

Proyecto desarrollado para la asignatura **Compiladores e Intérpretes** — **Universidad Nacional Experimental del Táchira (UNET)**.

- **Jesús Chacón** — *Módulo 1:* Frontend, Especificación Léxica, Gramática Sintáctica, Árbol de Ámbitos y Verificador Semántico Estricto.
- **Carlos Ocáriz** — *Módulo 2:* Backend, Generador de Código TM, Operadores Relacionales/Lógicos, Arreglos Unidimensionales, Cadenas y Marcos de Activación (Stack Frames).
- **Ana Paola Pineda** — *Módulos 3 y 4:* IDE Web, Servidor de Integración y Depurador Visual de la Tiny Machine.
