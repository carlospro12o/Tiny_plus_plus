/* ═══════════════════════════════════════════════════════════════
   EDITOR.JS — Controlador del Editor de Código y Consola Visual
   ═══════════════════════════════════════════════════════════════
   
   Módulos:
   1. Sincronización de Numeración de Líneas (Line Numbers Gutter).
   2. Manejo de Indentación y Atajos (Tab key, Ctrl+Enter / F5 para compilar).
   3. Carga y Guardado de Programas Tiny++ (.tny).
   4. Consola Visual Multicanal:
      - Consola General
      - Árbol AST (ve.edu.unet.nodosAST.*)
      - Tabla de Símbolos (ve.edu.unet.TablaSimbolos)
      - Problemas (Léxico, Sintáctico, Semántico)
      - Salida Código TM
   ═══════════════════════════════════════════════════════════════ */
'use strict';

// ── Estado del Editor ─────────────────────────────────────────
const Editor = {
  textarea: null,
  gutter: null,
  fileNameLabel: null,
  currentFileName: 'factorial_tiny_plus.tny',
  
  // Inicialización de componentes del editor
  init() {
    this.textarea = document.getElementById('code-editor');
    this.gutter = document.getElementById('editor-gutter');
    this.fileNameLabel = document.getElementById('editor-active-file');

    if (!this.textarea || !this.gutter) return;

    // Escuchar eventos de escritura y scroll
    this.textarea.addEventListener('input', () => this.updateLineNumbers());
    this.textarea.addEventListener('scroll', () => this.syncScroll());
    
    // Soporte para tecla Tab y atajos (Ctrl+Enter / F5)
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
      html += `<span class="line-num" data-line="${i}">${i}</span>`;
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
    } else if ((e.ctrlKey && e.key === 'Enter') || e.key === 'F5') {
      e.preventDefault();
      if (typeof TmIntegration !== 'undefined' && TmIntegration.handleCompileAndLoad) {
        TmIntegration.handleCompileAndLoad();
      }
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
  },

  // Salto visual a una línea específica (útil para errores)
  goToLine(lineNum) {
    if (!this.textarea) return;
    const lines = this.textarea.value.split('\n');
    const target = Math.max(1, Math.min(lineNum, lines.length));
    
    let charIndex = 0;
    for (let i = 0; i < target - 1; i++) {
      charIndex += lines[i].length + 1;
    }
    
    const lineLength = lines[target - 1] ? lines[target - 1].length : 0;
    this.textarea.focus();
    this.textarea.setSelectionRange(charIndex, charIndex + lineLength);

    const lineHeight = 20;
    this.textarea.scrollTop = Math.max(0, (target - 3) * lineHeight);
    this.syncScroll();
  }
};

// ── Estado de la Consola Visual ───────────────────────────────
const IdeConsole = {
  body: null,
  errorCountEl: null,
  activeTab: 'all', // 'all', 'ast', 'symbols', 'errors', 'output'
  logs: [],
  astData: '',
  symbolsData: '',
  semanticErrors: [],
  syntaxErrors: [],
  tmCode: '',

  init() {
    this.body = document.getElementById('console-body');
    this.errorCountEl = document.getElementById('console-error-count');
    
    // Log inicial de bienvenida
    this.info('Tiny++ IDE v1.0 listo. Backend Java ve.edu.unet conectado.');
    this.info('Usa "⚡ Compilar" o pulsa Ctrl+Enter para invocar el pipeline de compilación.');
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
    if (this.activeTab === 'all') {
      this.render();
    }
    this.updateBadges();

    // Auto-scroll al fondo
    if (this.body && this.activeTab === 'all') {
      this.body.scrollTop = this.body.scrollHeight;
    }
  },

  info(msg) { this.log(msg, 'info'); },
  success(msg) { this.log(msg, 'success'); },
  warning(msg) { this.log(msg, 'warning'); },
  error(msg) { this.log(msg, 'error'); },

  // Recibe la respuesta estructurada desde server.js
  setCompilationResult(data) {
    this.astData = data.ast || '';
    this.symbolsData = data.symbols || '';
    this.semanticErrors = data.semanticErrors || [];
    this.syntaxErrors = data.syntaxErrors || [];
    this.tmCode = data.tmCode || '';

    const totalErrors = this.semanticErrors.length + this.syntaxErrors.length;

    if (!data.success) {
      if (totalErrors > 0) {
        this.error(`[Compilador] Compilación abortada por ${totalErrors} error(es) detectados.`);
        this.setTab('errors');
      } else {
        this.error(`[Compilador] Error en el proceso de compilación.`);
        this.setTab('all');
      }
    } else {
      this.success(`[Compilador] Compilación exitosa. Código TM generado.`);
      // Si la pestaña actual es errors o ninguna, sugerir ver el AST
      if (this.activeTab === 'errors') {
        this.setTab('ast');
      } else {
        this.render();
      }
    }
    this.updateBadges();
  },

  // Limpiar mensajes y datos
  clear() {
    this.logs = [];
    this.semanticErrors = [];
    this.syntaxErrors = [];
    this.astData = '';
    this.symbolsData = '';
    this.tmCode = '';
    this.render();
    this.updateBadges();
  },

  // Cambiar pestaña activa de la consola ('all', 'ast', 'symbols', 'errors', 'output')
  setTab(tab) {
    this.activeTab = tab;
    document.querySelectorAll('.console-tab').forEach(el => {
      el.classList.toggle('active', el.getAttribute('data-tab') === tab);
    });
    this.render();
  },

  // Renderizar la vista correspondiente a la pestaña activa
  render() {
    if (!this.body) return;

    if (this.activeTab === 'all') {
      this.renderConsoleLogs();
    } else if (this.activeTab === 'ast') {
      this.renderAstView();
    } else if (this.activeTab === 'symbols') {
      this.renderSymbolsView();
    } else if (this.activeTab === 'errors') {
      this.renderErrorsView();
    } else if (this.activeTab === 'output') {
      this.renderOutputView();
    }
  },

  // Renderizar consola general
  renderConsoleLogs() {
    if (this.logs.length === 0) {
      this.body.innerHTML = '<div style="color:var(--text3);font-size:10px;padding:8px 0;">No hay mensajes en la consola.</div>';
      return;
    }

    const icons = {
      info: 'ℹ',
      success: '✔',
      warning: '⚠',
      error: '✖'
    };

    this.body.innerHTML = this.logs.map(item => `
      <div class="console-line c-${item.type}">
        <span class="c-time">[${item.time}]</span>
        <span class="c-icon">${icons[item.type] || '•'}</span>
        <span class="c-msg">${this.escapeHtml(item.message)}</span>
      </div>
    `).join('');
  },

  // Renderizar Árbol Sintáctico (AST) generado por ve.edu.unet.nodosAST
  renderAstView() {
    if (!this.astData) {
      this.body.innerHTML = `
        <div style="color:var(--text3);font-size:11px;padding:12px;line-height:20px;">
          🌿 <strong>Árbol Sintáctico (AST)</strong><br>
          Presiona <code>⚡ Compilar</code> para generar el árbol utilizando las clases del paquete <code>ve.edu.unet.nodosAST.*</code>.
        </div>`;
      return;
    }

    this.body.innerHTML = `
      <div class="ast-title-bar">
        <span>🌿 Árbol Sintáctico generado por <strong>ve.edu.unet.nodosAST</strong></span>
        <span>${this.astData.split('\n').length} nodos renderizados</span>
      </div>
      <div class="ast-container">${this.escapeHtml(this.astData)}</div>
    `;
  },

  // Renderizar Tabla de Símbolos y Ámbitos
  renderSymbolsView() {
    if (!this.symbolsData) {
      this.body.innerHTML = `
        <div style="color:var(--text3);font-size:11px;padding:12px;line-height:20px;">
          📋 <strong>Tabla de Símbolos con Ámbitos (Scopes)</strong><br>
          Presiona <code>⚡ Compilar</code> para inspeccionar las variables, tipos y direcciones de memoria generadas por <code>ve.edu.unet.TablaSimbolos</code>.
        </div>`;
      return;
    }

    // Extraer líneas y construir visualización estructurada
    const lines = this.symbolsData.split('\n');
    let html = `
      <div class="ast-title-bar">
        <span>📋 Tabla de Símbolos generada por <strong>ve.edu.unet.TablaSimbolos</strong></span>
      </div>
      <div class="symbols-container">
        <table class="symbols-table">
          <thead>
            <tr>
              <th>Ámbito / Scope</th>
              <th>Identificador</th>
              <th>Tipo Dato</th>
              <th>Dirección Memoria</th>
              <th>Línea Declaración</th>
            </tr>
          </thead>
          <tbody>
    `;

    let currentScope = 'global (Nivel 0)';
    let rowCount = 0;

    for (const rawLine of lines) {
      const line = rawLine.trim();
      if (!line || line.startsWith('***') || line.startsWith('---') || line.includes('TABLA DE SIMBOLOS')) continue;

      if (line.startsWith('=== Ambito:')) {
        currentScope = line.replace(/===/g, '').trim();
      } else if (line.startsWith('* ID:')) {
        // Formato: * ID: x | Tipo: INT | DirMem: 0 | Linea: 2
        rowCount++;
        const parts = line.split('|').map(p => p.trim());
        const id = (parts[0] || '').replace('* ID:', '').trim();
        const tipo = (parts[1] || '').replace('Tipo:', '').trim();
        const dir = (parts[2] || '').replace('DirMem:', '').trim();
        const lin = (parts[3] || '').replace('Linea:', '').trim();

        html += `
          <tr>
            <td><span class="badge-scope">${this.escapeHtml(currentScope)}</span></td>
            <td><strong>${this.escapeHtml(id)}</strong></td>
            <td><span class="badge-type">${this.escapeHtml(tipo)}</span></td>
            <td><span class="badge-offset">${this.escapeHtml(dir)}</span></td>
            <td>${this.escapeHtml(lin)}</td>
          </tr>
        `;
      } else if (line.includes('(sin simbolos)')) {
        html += `
          <tr>
            <td><span class="badge-scope">${this.escapeHtml(currentScope)}</span></td>
            <td colspan="4" style="color:var(--text3);font-style:italic;">(Sin símbolos declarados en este ámbito)</td>
          </tr>
        `;
      }
    }

    if (rowCount === 0) {
      // Fallback a texto limpio si el formato no coincide exactamente
      html = `
        <div class="ast-title-bar">
          <span>📋 Tabla de Símbolos generada por <strong>ve.edu.unet.TablaSimbolos</strong></span>
        </div>
        <div class="ast-container">${this.escapeHtml(this.symbolsData)}</div>
      `;
    } else {
      html += `</tbody></table></div>`;
    }

    this.body.innerHTML = html;
  },

  // Renderizar Problemas (Errores Semánticos y Sintácticos)
  renderErrorsView() {
    const allErrors = [];

    // Errores semánticos de ChequeadorSemantico
    for (const err of this.semanticErrors) {
      const match = err.match(/Linea\s+(\d+):\s*(.*)/i);
      const line = match ? parseInt(match[1], 10) : 1;
      const desc = match ? match[2] : err;
      allErrors.push({
        type: 'semantico',
        category: 'Error Semántico (ChequeadorSemantico)',
        line: line,
        message: desc
      });
    }

    // Errores sintácticos de Java CUP / JFlex
    for (const err of this.syntaxErrors) {
      const match = err.match(/linea\s+(\d+)/i) || err.match(/line\s+(\d+)/i);
      const line = match ? parseInt(match[1], 10) : 1;
      allErrors.push({
        type: 'sintactico',
        category: 'Error Sintáctico / Léxico (CUP/JFlex)',
        line: line,
        message: err
      });
    }

    if (allErrors.length === 0) {
      this.body.innerHTML = `
        <div style="color:var(--green2);font-size:11px;padding:12px;">
          ✔ <strong>No se detectaron problemas en el código fuente.</strong><br>
          <span style="color:var(--text3);font-size:10px;">Todas las variables han sido declaradas y los tipos son correctos.</span>
        </div>`;
      return;
    }

    let html = `
      <div class="problems-list">
        <div style="font-size:10px;color:var(--text2);margin-bottom:4px;">
          Haz click en un error para ir directamente a la línea afectada:
        </div>
    `;

    for (const item of allErrors) {
      html += `
        <div class="problem-card" onclick="Editor.goToLine(${item.line})" title="Ir a la línea ${item.line}">
          <span class="problem-badge-line">Línea ${item.line}</span>
          <div class="problem-details">
            <div class="problem-category">${this.escapeHtml(item.category)}</div>
            <div class="problem-message">${this.escapeHtml(item.message)}</div>
          </div>
        </div>
      `;
    }

    html += `</div>`;
    this.body.innerHTML = html;
  },

  // Renderizar salida del código objeto generado para TM
  renderOutputView() {
    if (!this.tmCode) {
      this.body.innerHTML = `
        <div style="color:var(--text3);font-size:11px;padding:12px;">
          ⚙ <strong>Código Objeto Tiny Machine (.tm)</strong><br>
          Presiona <code>⚡ Compilar</code> para generar las instrucciones de máquina con <code>ve.edu.unet.Generador</code>.
        </div>`;
      return;
    }

    this.body.innerHTML = `
      <div class="ast-title-bar">
        <span>⚙ Código Objeto TM generado por <strong>ve.edu.unet.Generador</strong></span>
        <span>${this.tmCode.split('\n').length} instrucciones</span>
      </div>
      <div class="ast-container">${this.escapeHtml(this.tmCode)}</div>
    `;
  },

  // Actualizar conteo de errores en badge
  updateBadges() {
    const errorCount = this.semanticErrors.length + this.syntaxErrors.length;
    if (this.errorCountEl) {
      this.errorCountEl.textContent = `${errorCount} problema${errorCount === 1 ? '' : 's'}`;
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
