/* ═══════════════════════════════════════════════════════════════
   EDITOR.JS — Controlador del Editor de Código y Consola Visual
   ═══════════════════════════════════════════════════════════════
   
   Módulos:
   1. Sincronización de Numeración de Líneas (Line Numbers Gutter).
   2. Manejo de Indentación y Atajos (Tab key).
   3. Carga y Guardado de Programas Tiny++ (.tny).
   4. Consola Visual Multicanal (Todo, Salida, Errores).
   ═══════════════════════════════════════════════════════════════ */
'use strict';

// ── Estado del Editor ─────────────────────────────────────────
const Editor = {
  textarea: null,
  gutter: null,
  fileNameLabel: null,
  currentFileName: 'programa.tny',
  
  // Inicialización de componentes del editor
  init() {
    this.textarea = document.getElementById('code-editor');
    this.gutter = document.getElementById('editor-gutter');
    this.fileNameLabel = document.getElementById('editor-active-file');

    if (!this.textarea || !this.gutter) return;

    // Escuchar eventos de escritura y scroll
    this.textarea.addEventListener('input', () => this.updateLineNumbers());
    this.textarea.addEventListener('scroll', () => this.syncScroll());
    
    // Soporte para tecla Tab (inserta 2 espacios en vez de perder foco)
    this.textarea.addEventListener('keydown', (e) => this.handleKeyDown(e));

    // Código de bienvenida por defecto (ejemplo factorial_tiny_plus.tny)
    const initialCode = 
`{ Programa de Ejemplo Tiny++: Factorial }
int x;
int fact;

read x; { Leer numero entero }
if 0 < x then { No calcular si x <= 0 }
  fact := 1;
  repeat
    fact := fact * x;
    x := x - 1
  until x = 0;
  write fact  { Imprime el factorial }
end`;

    this.textarea.value = initialCode;
    this.updateLineNumbers();
  },

  // Genera y actualiza los números de línea en el gutter
  updateLineNumbers() {
    const lines = this.textarea.value.split('\n');
    const lineCount = lines.length || 1;
    
    let html = '';
    for (let i = 1; i <= lineCount; i++) {
      html += `<span class="line-num">${i}</span>`;
    }
    this.gutter.innerHTML = html;
  },

  // Sincroniza el scroll vertical entre el textarea y el gutter
  syncScroll() {
    this.gutter.scrollTop = this.textarea.scrollTop;
  },

  // Soporte para Tab y atajos amigables dentro del editor
  handleKeyDown(e) {
    if (e.key === 'Tab') {
      e.preventDefault();
      const start = this.textarea.selectionStart;
      const end = this.textarea.selectionEnd;
      const val = this.textarea.value;

      // Insertar dos espacios
      this.textarea.value = val.substring(0, start) + '  ' + val.substring(end);
      this.textarea.selectionStart = this.textarea.selectionEnd = start + 2;
      this.updateLineNumbers();
    }
  },

  // Obtiene el código fuente actual
  getSource() {
    return this.textarea ? this.textarea.value : '';
  },

  // Asigna nuevo código fuente y refresca
  setSource(code, name = 'programa.tny') {
    if (this.textarea) {
      this.textarea.value = code;
      this.currentFileName = name;
      if (this.fileNameLabel) this.fileNameLabel.textContent = name;
      this.updateLineNumbers();
      this.textarea.scrollTop = 0;
      this.syncScroll();
    }
  }
};

// ── Estado de la Consola Visual ───────────────────────────────
const IdeConsole = {
  body: null,
  errorCountEl: null,
  activeTab: 'all', // 'all', 'output', 'errors'
  logs: [],

  init() {
    this.body = document.getElementById('console-body');
    this.errorCountEl = document.getElementById('console-error-count');
    
    // Log inicial de bienvenida
    this.info('Tiny++ IDE v1.0 listo. Editor y simulador integrados.');
  },

  // Agregar mensaje a la consola
  log(message, type = 'info') {
    const now = new Date();
    const timeStr = now.toTimeString().split(' ')[0]; // HH:MM:SS
    
    const entry = {
      id: Date.now() + Math.random(),
      type, // 'info', 'success', 'warning', 'error'
      message,
      time: timeStr
    };

    this.logs.push(entry);
    this.render();
    this.updateBadges();

    // Auto-scroll al fondo
    if (this.body) {
      this.body.scrollTop = this.body.scrollHeight;
    }
  },

  info(msg) { this.log(msg, 'info'); },
  success(msg) { this.log(msg, 'success'); },
  warning(msg) { this.log(msg, 'warning'); },
  error(msg) { 
    this.log(msg, 'error'); 
    // Si hay un error, activar o resaltar el tab de errores
  },

  // Limpiar mensajes
  clear() {
    this.logs = [];
    this.render();
    this.updateBadges();
  },

  // Cambiar pestaña activa de la consola ('all', 'output', 'errors')
  setTab(tab) {
    this.activeTab = tab;
    document.querySelectorAll('.console-tab').forEach(el => {
      el.classList.toggle('active', el.getAttribute('data-tab') === tab);
    });
    this.render();
  },

  // Renderizar entradas filtradas
  render() {
    if (!this.body) return;

    let filtered = this.logs;
    if (this.activeTab === 'output') {
      filtered = this.logs.filter(l => l.type === 'info' || l.type === 'success');
    } else if (this.activeTab === 'errors') {
      filtered = this.logs.filter(l => l.type === 'error' || l.type === 'warning');
    }

    if (filtered.length === 0) {
      this.body.innerHTML = '<div style="color:var(--text3);font-size:10px;padding:8px 0;">No hay mensajes que mostrar en este canal.</div>';
      return;
    }

    const icons = {
      info: 'ℹ',
      success: '✔',
      warning: '⚠',
      error: '✖'
    };

    this.body.innerHTML = filtered.map(item => `
      <div class="console-line c-${item.type}">
        <span class="c-time">[${item.time}]</span>
        <span class="c-icon">${icons[item.type] || '•'}</span>
        <span class="c-msg">${this.escapeHtml(item.message)}</span>
      </div>
    `).join('');
  },

  // Actualizar conteo de errores en badge
  updateBadges() {
    const errorCount = this.logs.filter(l => l.type === 'error').length;
    if (this.errorCountEl) {
      this.errorCountEl.textContent = `${errorCount} error${errorCount === 1 ? '' : 'es'}`;
      this.errorCountEl.style.display = errorCount > 0 ? 'inline-block' : 'none';
    }
  },

  escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }
};

// Inicializar cuando el DOM esté listo
window.addEventListener('DOMContentLoaded', () => {
  Editor.init();
  IdeConsole.init();
});
