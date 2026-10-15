# Tablero de Sprints - Módulo IDE y Simulador TM (Integrante 3)

## Sprint 1: Reestructuración y Modularización del Archivo Base (`tmsimv1.html`)
- [x] **Tarea 1.1:** Auditar el archivo monolítico `tmsimv1.html` suministrado por el profesor para identificar secciones clave (estilos CSS, lógica del simulador de la TM, estructura HTML y funciones de control).
- [x] **Tarea 1.2:** Modularizar y limpiar el código: separar los estilos en una hoja CSS externa (o estructurar bloques limpios) y organizar el HTML en paneles lógicos (Header, Editor, Consola, Registros y Memorias).
- [x] **Tarea 1.3:** Aplicar un diseño visual moderno, limpio y profesional (Dark Mode estilo VS Code) manteniendo intacta toda la lógica original de la Tiny Machine que está por debajo.

## Sprint 2: Editor de Código y Consola Visual
- [x] **Tarea 2.1:** Integrar el área de texto del editor de código Tiny++ en la nueva estructura visual modular (`ide/`).
- [x] **Tarea 2.2:** Añadir numeración de líneas sincronizada con scroll y soporte de indentación para Tiny++.
- [x] **Tarea 2.3:** Crear el panel inferior de consola visual con tabs (Consola, Salida, Problemas) y formato para mensajes y errores del compilador Java.

## Sprint 3: Puente de Integración (Compilador <-> IDE)
- [x] **Tarea 3.1:** Diseñar e incorporar el botón de "Compilar y Cargar" en el header del IDE.
- [x] **Tarea 3.2:** Configurar la ejecución del proceso en segundo plano que invoque el compilador de Java (`java -cp ...`).
- [x] **Tarea 3.3:** Implementar la lectura automática del archivo `.tm` generado para inyectarlo dinámicamente en la memoria del simulador web.

##  Sprint 4: Depurador Visual de la TM y Controles
- [ ] **Tarea 4.1:** Conectar los controles de ejecución (Play, Step, Pause, Reset) a la máquina virtual.
- [ ] **Tarea 4.2:** Diseñar paneles visuales en tiempo real para los registros de la TM (`PC`, `GP`, `MP`, `AC`).
- [ ] **Tarea 4.3:** Crear la visualización interactiva de la Memoria de Datos (`dMem`) y de Instrucciones (`iMem`) con resaltado de cambios por instrucción.
- [ ] **Tarea 4.4:** Implementar modales emergentes para las instrucciones `IN` y salida limpia para `OUT`.