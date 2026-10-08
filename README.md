# 🚀 Tiny++ Compiler (Tiny_plus_plus)

Compilador para el lenguaje de programación **Tiny++** desarrollado en Java utilizando **JFlex** (Analizador Léxico) y **Java CUP** (Analizador Sintáctico), con generación de código objeto para la Máquina Virtual TM (Tiny Machine).

---

## 📁 Estructura del Proyecto

```text
TinyPlus/
├── src/
│   ├── especificacion/
│   │   ├── lexico.flex          # Especificación del analizador léxico (JFlex)
│   │   ├── sintactico.cup       # Gramática del analizador sintáctico (Java CUP)
│   │   ├── java-cup-11b.jar     # Librería CUP Compiler
│   │   ├── java-cup-11b-runtime.jar # Librería CUP Runtime
│   │   └── jflex-full-1.8.2.jar # Librería JFlex
│   └── ve/edu/unet/
│       ├── Generador.java       # Generador de código máquina TM
│       ├── TablaSimbolos.java   # Gestión de la Tabla de Símbolos
│       └── nodosAST/            # Nodos del Árbol de Sintaxis Abstracta (AST)
├── ejemplos/                    # Archivos de prueba en código Tiny++ (.tny)
├── compilar_gramatica.bat       # Script para regenerar Léxico.java y parser.java
├── compilar_codigo.bat          # Script para compilar las clases Java
├── ejecutar.bat                 # Script para ejecutar el compilador
└── TM.C                         # Simulador C para la máquina TM
```

---

## ⚙️ Requisitos

- **Java JDK 8+** (configurado en las variables de entorno `PATH` y `JAVA_HOME`).
- **GCC / MinGW** (opcional, para compilar el simulador C `TM.C`).

---

## 🚀 Compilación y Ejecución

### 1. Regenerar Léxico y Sintáctico (JFlex + CUP)
```cmd
compilar_gramatica.bat
```

### 2. Compilar el Código Fuente Java
```cmd
compilar_codigo.bat
```

### 3. Ejecutar el Compilador
```cmd
ejecutar.bat
```

---

## 👨‍💻 Créditos
Desarrollado para la materia de **Compiladores** — UNET.
