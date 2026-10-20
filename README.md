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

- **Java JDK 8 o superior** (detectado automáticamente por los scripts y el servidor; compatible con OpenJDK, Oracle JDK o el JDK de NetBeans).
- **Node.js 16 o superior** (para el servidor local de compilación en caliente y puente de comunicación).
- **Navegador Web Moderno** (Google Chrome, Microsoft Edge, Brave o Mozilla Firefox).

---

## 🚀 Guía de Ejecución del IDE Web y Depurador Visual

El entorno de desarrollo integra un editor de código con numeración de líneas sincronizada, un servidor de compilación en segundo plano y el simulador interactivo de la **Tiny Machine**.

### 🌟 Método 1: Ejecución Automática con Servidor Local (Recomendado)

Este método inicia el servidor web local que conecta la interfaz gráfica con el compilador Java, permitiendo escribir código Tiny++ y compilarlo directamente en la máquina virtual con un solo clic.

1. **Iniciar el servidor:**
   - **Vía acceso directo (Doble clic):** Ejecuta el archivo `iniciar_servidor_ide.bat`.
   - **Vía consola de comandos:**
     ```cmd
     iniciar_servidor_ide.bat
     ```
     *O directamente mediante Node.js:*
     ```cmd
     node ide/server.js
     ```

2. **Acceso en el navegador:**
   - El script abrirá automáticamente tu navegador en la URL:
     ```text
     http://localhost:3000
     ```
   - En la barra superior verás el indicador de estado:
     `🟢 Servidor conectado`
     Esto confirma que el IDE está listo para compilar en caliente.

3. **Flujo de Trabajo dentro del IDE:**
   - **Escribir o Cargar Código:** Edita directamente en el panel del editor o abre cualquier programa de la carpeta `ejemplos/` (como `factorial_tiny_plus.tny` o `test_misiones_unificadas.tny`).
   - **Compilación en Caliente (1-Clic):** Haz clic en el botón **⚡ "Compilar y Cargar"**.
     - El servidor compila el código fuente en segundo plano con el compilador Java (`ve.edu.unet.parser`).
     - **En caso de errores:** La consola visual inferior mostrará los fallos léxicos, sintácticos o semánticos con indicación de línea exacta.
     - **En caso de éxito:** El código máquina `.tm` generado se inyectará automáticamente en la **Memoria de Instrucciones (`iMem`)**, restableciendo el contador de programa `PC` en 0.
   - **Depuración Visual Paso a Paso:**
     - **`Step`:** Ejecuta una sola instrucción en la Tiny Machine, animando en tiempo real los registros y memorias.
     - **`Step × N`:** Avanza un bloque de instrucciones consecutivas (por defecto 10, configurable en la casilla numérica).
     - **`Run`:** Ejecución continua fluida hasta encontrar un `HALT`, una solicitud de entrada (`read`) o un error en tiempo de ejecución.
     - **`Stop` / `Reset`:** Pausa la ejecución o reinicia el estado completo de la máquina.
     - **Explorador de Memoria:** Alterna entre la vista `Low (0..63)` para variables globales y cadenas, y la vista `High (960..1023)` para marcos de pila (stack frames) y variables locales.
     - **Consola de Entrada / Salida:** Al topar con una instrucción `read` (`IN`), la consola solicitará el ingreso del dato en el campo interactivo `IN ▸`.

---

### 🌐 Método 2: Modo Estático (Sin Servidor Node.js)

Si deseas utilizar únicamente el simulador y depurador visual de la Tiny Machine sin iniciar el servidor local:

1. Abre directamente el archivo `ide/index.html` con doble clic en tu navegador web.
2. El IDE funcionará de forma local e independiente:
   - Permite cargar programas `.tm` precompilados mediante el botón **"load .tm"**.
   - Permite pegar instrucciones directamente mediante el menú desplegable **"paste ▾"** -> **"load from text"**.
   - Si presionas **⚡ "Compilar y Cargar"** sin el servidor activo, se desplegará el panel de integración con instrucciones y comandos para compilar manualmente por terminal.

---

## 🛠️ Comandos por Consola (CLI)

Para ejecutar pruebas unitarias o compilar fuera del entorno visual:

### 1. Compilar y Ejecutar un Programa Tiny++ por Terminal
```cmd
java -cp "bin;src/especificacion/java-cup-11b-runtime.jar" ve.edu.unet.parser ejemplos/test_completo_tiny_plus.tny
```
*Muestra en consola el AST generado, la tabla de símbolos con ámbitos y el listado de instrucciones TM.*

### 2. Ejecutar Pruebas Automatizadas del Backend
Valida la generación de código para operadores relacionales/lógicos, arreglos, cadenas y subrutinas:
```cmd
java -cp "bin;src/especificacion/java-cup-11b-runtime.jar" ve.edu.unet.TestBackend
```

### 3. Recompilar Clases Java
```cmd
javac -encoding UTF-8 -cp ".;src;src/especificacion/java-cup-11b-runtime.jar" -d bin src/ve/edu/unet/*.java src/ve/edu/unet/nodosAST/*.java
```

### 4. Regenerar Analizadores con JFlex y CUP
```cmd
java -jar src/especificacion/jflex-full-1.8.2.jar -d src/ve/edu/unet src/especificacion/lexico.flex
java -jar src/especificacion/java-cup-11b.jar -destdir src/ve/edu/unet -parser parser -symbols sym src/especificacion/sintactico.cup
```

---

## 👨‍💻 Créditos

Proyecto desarrollado para la asignatura **Compiladores e Intérpretes** — **Universidad Nacional Experimental del Táchira (UNET)**.

- **Jesús Chacón** — *Módulo 1:* Frontend, Especificación Léxica, Gramática Sintáctica, Árbol de Ámbitos y Verificador Semántico Estricto.
- **Carlos Ocáriz** — *Módulo 2:* Backend, Generador de Código TM, Operadores Relacionales/Lógicos, Arreglos Unidimensionales, Cadenas y Marcos de Activación (Stack Frames).
- **Ana Paola Pineda** — *Módulos 3 y 4:* IDE Web, Servidor de Integración y Depurador Visual de la Tiny Machine.
