/**
 * ═══════════════════════════════════════════════════════════════
 * SERVER.JS — Servidor Local de Compilación para Tiny++ IDE
 * ═══════════════════════════════════════════════════════════════
 * 
 * Permite la compilación en segundo plano (Sprint 3: SCRUM-16).
 * Escucha en http://localhost:3000 y ofrece:
 * 1. Servidor web estático para la interfaz del IDE (HTML, CSS, JS).
 * 2. Endpoint REST POST /compile para invocar el compilador Java.
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

        // Guardar archivo temporal en la carpeta ejemplos/
        const tempFilePath = path.join(ROOT_DIR, 'ejemplos', `_ide_${safeName}`);
        fs.writeFileSync(tempFilePath, code, 'utf-8');

        // Comando para ejecutar el compilador Java sobre el archivo guardado
        const javaCmd = `java -cp "bin;src\\especificacion\\java-cup-11b-runtime.jar" ve.edu.unet.parser "ejemplos\\_ide_${safeName}"`;

        exec(javaCmd, { cwd: ROOT_DIR }, (error, stdout, stderr) => {
          const combinedLog = (stdout || '') + '\n' + (stderr || '');

          // Extraer bloque de código TM
          const startMarker = '------ CODIGO OBJETO DEL LENGUAJE TINY++ GENERADO PARA LA TM ------';
          const endMarker = '------ FIN DEL CODIGO OBJETO DEL LENGUAJE TINY++ GENERADO PARA LA TM ------';

          const startIdx = combinedLog.indexOf(startMarker);
          const endIdx = combinedLog.indexOf(endMarker);

          if (startIdx !== -1 && endIdx !== -1 && endIdx > startIdx) {
            const tmCode = combinedLog.slice(startIdx + startMarker.length, endIdx).trim();
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({
              success: true,
              tmCode: tmCode,
              log: 'Compilación y generación de código TM exitosa'
            }));
          } else {
            // Si no se encontró el bloque TM, probablemente hubo errores semánticos o sintácticos
            res.writeHead(200, { 'Content-Type': 'application/json' });
            res.end(JSON.stringify({
              success: false,
              errors: stderr || combinedLog || 'Error desconocido durante la compilación.',
              log: combinedLog
            }));
          }
        });
      } catch (err) {
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
  console.log(`=======================================================`);
});
