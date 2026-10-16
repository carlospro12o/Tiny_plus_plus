/**
 * ═══════════════════════════════════════════════════════════════
 * SERVER.JS — Servidor Local de Compilación para Tiny++ IDE
 * ═══════════════════════════════════════════════════════════════
 * 
 * Permite la compilación en segundo plano (Sprint 3: SCRUM-16).
 * Escucha en http://localhost:3000 y ofrece:
 * 1. Servidor web estático para la interfaz del IDE (HTML, CSS, JS).
 * 2. Endpoint REST POST /compile para invocar el compilador Java ve.edu.unet.parser.
 * 3. Endpoint GET /status para chequear conectividad.
 * ═══════════════════════════════════════════════════════════════
 */
const http = require('http');
const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');

const PORT = process.env.PORT || 3000;
const ROOT_DIR = path.resolve(__dirname, '..');
const IDE_DIR = __dirname;

// Tipos MIME para servir archivos estáticos
const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.tny': 'text/plain; charset=utf-8',
  '.tm': 'text/plain; charset=utf-8',
  '.txt': 'text/plain; charset=utf-8',
  '.png': 'image/png',
  '.svg': 'image/svg+xml'
};

const server = http.createServer((req, res) => {
  // Habilitar CORS para permitir peticiones si el IDE se abre por file:// o localhost
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // ── Endpoint: GET /status ──────────────────────────────────
  if (req.method === 'GET' && req.url === '/status') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'ok', message: 'Tiny++ Bridge Server Online' }));
    return;
  }

  // ── Endpoint: POST /compile ────────────────────────────────
  if (req.method === 'POST' && req.url === '/compile') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const payload = JSON.parse(body || '{}');
        const code = payload.code || '';
        const requestedName = payload.fileName || 'temp_programa.tny';
        const safeName = path.basename(requestedName);

        if (!code.trim()) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: false, errors: 'Código fuente vacío' }));
          return;
        }

        console.log(`\n-------------------------------------------------------`);
        console.log(`[IDE Server] Recibida solicitud de compilación para "${safeName}"`);

        // Guardar archivo temporal en la carpeta ejemplos/
        const tempFilePath = path.join(ROOT_DIR, 'ejemplos', `_ide_${safeName}`);
        fs.writeFileSync(tempFilePath, code, 'utf-8');

        // Deteccion automatica robusta del ejecutable Java (PATH, JetBrains JBR, NetBeans, JAVA_HOME)
        let javaBin = 'java';
        const candidatePaths = [
          process.env.JAVA_HOME ? path.join(process.env.JAVA_HOME, 'bin', 'java.exe') : null,
          'C:\\Program Files\\JetBrains\\IntelliJ IDEA 2026.2.3\\jbr\\bin\\java.exe',
          'C:\\Program Files\\Apache NetBeans\\jdk\\bin\\java.exe'
        ];
        for (const p of candidatePaths) {
          if (p && fs.existsSync(p)) {
            javaBin = `"${p}"`;
            break;
          }
        }
        if (javaBin === 'java') {
          const jbBase = 'C:\\Program Files\\JetBrains';
          if (fs.existsSync(jbBase)) {
            const dirs = fs.readdirSync(jbBase);
            for (const d of dirs) {
              const testP = path.join(jbBase, d, 'jbr', 'bin', 'java.exe');
              if (fs.existsSync(testP)) {
                javaBin = `"${testP}"`;
                break;
              }
            }
          }
        }

        // Comando para ejecutar el compilador Java sobre el archivo guardado
        const javaCmd = `${javaBin} -cp "bin;src\\especificacion\\java-cup-11b-runtime.jar" ve.edu.unet.parser "ejemplos\\_ide_${safeName}"`;
        console.log(`[IDE Server] Ejecutando: ${javaCmd}`);

        exec(javaCmd, { cwd: ROOT_DIR }, (error, stdout, stderr) => {
          const combinedLog = (stdout || '') + '\n' + (stderr || '');

          // Extraer bloque de código TM
          const startMarker = '------ CODIGO OBJETO DEL LENGUAJE TINY++ GENERADO PARA LA TM ------';
          const endMarker = '------ FIN DEL CODIGO OBJETO DEL LENGUAJE TINY++ GENERADO PARA LA TM ------';

          const startIdx = combinedLog.indexOf(startMarker);
          const endIdx = combinedLog.indexOf(endMarker);
          const hasTmCode = (startIdx !== -1 && endIdx !== -1 && endIdx > startIdx);
          const tmCode = hasTmCode ? combinedLog.slice(startIdx + startMarker.length, endIdx).trim() : '';

          // Extraer AST generado por ve.edu.unet.nodosAST.*
          let astOutput = '';
          const astMarker = 'IMPRESION DEL AST GENERADO';
          const astStart = combinedLog.indexOf(astMarker);
          if (astStart !== -1) {
            const symStart = combinedLog.indexOf('TABLA DE SIMBOLOS', astStart);
            const errStart = combinedLog.indexOf('ERRORES SEMANTICOS', astStart);
            let astEnd = -1;
            if (symStart !== -1 && errStart !== -1) astEnd = Math.min(symStart, errStart);
            else if (symStart !== -1) astEnd = symStart;
            else if (errStart !== -1) astEnd = errStart;
            else if (hasTmCode) astEnd = startIdx;

            if (astEnd !== -1) {
              astOutput = combinedLog.slice(astStart + astMarker.length, astEnd).trim();
            } else {
              astOutput = combinedLog.slice(astStart + astMarker.length).trim();
            }
          }

          // Extraer Tabla de Símbolos generada por ve.edu.unet.TablaSimbolos
          let symbolsOutput = '';
          const symMarker = 'TABLA DE SIMBOLOS';
          const symStart = combinedLog.indexOf(symMarker);
          if (symStart !== -1) {
            const errStart = combinedLog.indexOf('ERRORES SEMANTICOS', symStart);
            let symEnd = -1;
            if (hasTmCode && errStart !== -1) symEnd = Math.min(startIdx, errStart);
            else if (hasTmCode) symEnd = startIdx;
            else if (errStart !== -1) symEnd = errStart;

            if (symEnd !== -1) {
              symbolsOutput = combinedLog.slice(symStart, symEnd).trim();
            } else {
              symbolsOutput = combinedLog.slice(symStart).trim();
            }
          }

          // Extraer Errores Semánticos y Sintácticos con detalle de líneas
          const semanticErrors = [];
          const syntaxErrors = [];
          const lines = combinedLog.split('\n');
          for (const line of lines) {
            const trimmed = line.trim();
            if (trimmed.startsWith('[Error Semantico]') || trimmed.includes('Error Semantico') || trimmed.includes('Error semantico')) {
              semanticErrors.push(trimmed);
            } else if (trimmed.includes('Error sintactico') || trimmed.includes('Syntax error') || (trimmed.startsWith('Error:') && !trimmed.includes('Error Semantico'))) {
              syntaxErrors.push(trimmed);
            }
          }

          const isSuccess = hasTmCode && semanticErrors.length === 0;

          if (isSuccess) {
            console.log(`[IDE Server] ✔ Compilación EXITOSA.`);
            console.log(`[IDE Server] - Nodos AST: ${astOutput ? 'Generados' : 'Vacío'}`);
            console.log(`[IDE Server] - Tabla Símbolos: ${symbolsOutput ? 'Generada' : 'Vacío'}`);
            console.log(`[IDE Server] - Código TM: ${tmCode.split('\n').length} líneas generadas`);
          } else {
            console.log(`[IDE Server] ✖ Compilación CON ERRORES:`);
            console.log(`[IDE Server] - Errores semánticos: ${semanticErrors.length}`);
            console.log(`[IDE Server] - Errores sintácticos: ${syntaxErrors.length}`);
            if (semanticErrors.length > 0) {
              semanticErrors.forEach(err => console.log(`   ${err}`));
            }
          }
          console.log(`-------------------------------------------------------\n`);

          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({
            success: isSuccess,
            ast: astOutput,
            symbols: symbolsOutput,
            semanticErrors: semanticErrors,
            syntaxErrors: syntaxErrors,
            tmCode: tmCode,
            rawLog: combinedLog,
            errors: (semanticErrors.length > 0 ? semanticErrors.join('\n') : (syntaxErrors.length > 0 ? syntaxErrors.join('\n') : (stderr || 'Error desconocido durante la compilación.'))),
            log: isSuccess 
              ? 'Compilación y generación de código TM completadas con éxito.' 
              : 'Compilación detenida debido a errores léxicos, sintácticos o semánticos.'
          }));
        });
      } catch (err) {
        console.error('[IDE Server] Error procesando petición:', err);
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: false, errors: 'Error interno del servidor: ' + err.message }));
      }
    });
    return;
  }

  // ── Servir archivos estáticos del IDE ───────────────────────
  let reqPath = req.url.split('?')[0];
  if (reqPath === '/' || reqPath === '') reqPath = '/index.html';

  let filePath = path.join(IDE_DIR, reqPath);

  // Si no está en ide/, buscar en ROOT_DIR
  if (!fs.existsSync(filePath)) {
    filePath = path.join(ROOT_DIR, reqPath);
  }

  if (fs.existsSync(filePath) && fs.statSync(filePath).isFile()) {
    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';
    res.writeHead(200, { 'Content-Type': contentType });
    fs.createReadStream(filePath).pipe(res);
  } else {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('404 Not Found: ' + reqPath);
  }
});

server.listen(PORT, () => {
  console.log(`=======================================================`);
  console.log(`  🚀 Servidor Tiny++ IDE iniciado con éxito`);
  console.log(`  🌐 Abre en tu navegador: http://localhost:${PORT}`);
  console.log(`  ⚡ Endpoint de compilación activo: POST /compile`);
  console.log(`  ☕ Backend Java conectado: ve.edu.unet.parser`);
  console.log(`=======================================================`);
});
