#  Directrices del Asistente para Tiny++ IDE (Integrante 3)

## Perfil y Estilo de Comunicación con el Usuario
- El usuario (Ana) está aprendiendo desarrollo e ingeniería. **Cada vez que modifiques, crees o refactorices código, debes explicar paso a paso y de forma didáctica** qué hace cada bloque, el porqué de la solución y cómo funciona, para que pueda dominarlo y defenderlo con seguridad en la universidad.

## Arquitectura y Buenas Prácticas (Principio DRY)
- **Reutilización de Código:** Está estrictamente prohibido duplicar código innecesariamente (por ejemplo, crear 50 botones a mano). Se deben usar componentes reutilizables, funciones genéricas o plantillas limpias tanto en el HTML/CSS como en el JavaScript.
- **Orden de Archivos:** El archivo HTML principal y sus recursos no deben estar regados en la raíz. Debemos estructurarlos de forma limpia (por ejemplo, creando una carpeta `ide/` o `public/` para ordenar la interfaz gráfica y los estilos).

## Lineamientos de Visuales y Diseño
- **Estilo Moderno (Dark Theme):** Inspirado en entornos profesionales como VS Code (queremos que los colores que predominen sean el rosa oscuro y el negro, `#1e1e1e`, `#252526`, acentos limpios).
- **Estructura por Paneles:** Header de control, Área de Editor, Panel de Consola inferior, Panel de Registros de la TM y Paneles de Memoria (`dMem` e `iMem`).
- **Respeto a la Lógica Base:** Toda la lógica matemática y de ejecución de la Tiny Machine original contenida en el simulador del profesor debe preservarse intacta por debajo.