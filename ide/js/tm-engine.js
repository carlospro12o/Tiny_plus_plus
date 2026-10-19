/* ═══════════════════════════════════════════════════════════════
   TM-ENGINE.JS — Motor de la Tiny Machine (LÓGICA INTOCABLE)
   ═══════════════════════════════════════════════════════════════
   
   ⚠️  ESTE ARCHIVO CONTIENE LA LÓGICA ORIGINAL DEL PROFESOR.
   ⚠️  NO MODIFICAR la lógica matemática ni el comportamiento.
   ⚠️  Solo se extrajo del HTML monolítico a su propio archivo.
   
   Contenido:
   - Constantes de la TM (tamaños de memoria, opcodes)
   - Estado de la máquina (registros, memorias, flags)
   - Parser de programas .tm
   - Función resetMachine()
   - Función stepTM() — ejecuta UN paso de la máquina
   ═══════════════════════════════════════════════════════════════ */
'use strict';

// ── Constantes ──────────────────────────────────────────────
// ISIZE = tamaño de memoria de instrucciones (1024 posiciones)
// DSIZE = tamaño de memoria de datos (1024 posiciones)
// NREGS = número de registros (8: r0-r6 + PC)
// PC = índice del registro Program Counter (siempre el 7)
const ISIZE = 1024, DSIZE = 1024, NREGS = 8, PC = 7;

// Tabla de opcodes de la Tiny Machine
// Los '????' son posiciones reservadas/no usadas en el ISA
const OP = ['HALT','IN','OUT','ADD','SUB','MUL','DIV','????',
            'LD','ST','????',
            'LDA','LDC','JLT','JLE','JGT','JGE','JEQ','JNE','????'];

// Códigos de resultado de stepTM()
// Cada paso devuelve uno de estos para saber qué pasó
const OK='OK', HALTED='Halted', IMEM='IMEM_ERR', DMEM='DMEM_ERR',
      ZDIV='ZERO_DIV', NEEDS_IN='NEEDS_IN', BP='Breakpoint', WP='Watchpoint';

// ── Estado de la Máquina ────────────────────────────────────
// Estas variables globales representan el estado completo de la TM.
// iMem = memoria de instrucciones, dMem = memoria de datos
let iMem = [], dMem = [], reg = [], stepCount = 0, lastResult = 'READY';
let ioLog = [], breakpoints = new Set();
let prevReg = [], prevDMem = [], lastWrite = -1;
let programLoaded = false, running = false, waitInput = false, pendingCb = null;
let dmemTab = 'low';
let tabCfg = { low: {start:0, count:64}, high: {start:960, count:64} };

// Watchpoint (punto de vigilancia en memoria de datos)
let wEn=false, wAddr=0, wValEn=false, wVal=0;

// ── opClass ─────────────────────────────────────────────────
// Clasifica una instrucción por su tipo de operandos:
// RR = Register-Register (HALT, IN, OUT, ADD, SUB, MUL, DIV)
// RM = Register-Memory (LD, ST)
// RA = Register-Address (LDA, LDC, saltos condicionales)
function cls(op) { return op<=6?'RR': op<=9?'RM':'RA'; }

// ── Format args ─────────────────────────────────────────────
// Formatea los argumentos de una instrucción para mostrar en la UI
function fmtArgs(inst) {
  if (cls(inst.iop)==='RR') return `${inst.a1},${inst.a2},${inst.a3}`;
  return `${inst.a1},${inst.a2}(${inst.a3})`;
}

// ── Parser ──────────────────────────────────────────────────
// Convierte el texto de un archivo .tm en instrucciones en iMem.
// Soporta tanto instrucciones con o sin asterisco en los comentarios.
function parseProgram(text) {
  iMem = Array.from({length:ISIZE}, (_,i) =>
    ({iop:0, a1:0, a2:0, a3:0, comment:'', loc:i, has:false}));

  const errors=[], lines=text.split('\n');
  let maxLoc=0;

  for (let ln=0; ln<lines.length; ln++) {
    let line = lines[ln].replace(/\r/g,'').trim();
    if (!line || line[0]==='*') continue;

    const ci = line.indexOf(':');
    if (ci<0) { errors.push(`L${ln+1}: missing colon`); continue; }

    const loc = parseInt(line.slice(0,ci).trim());
    if (isNaN(loc)||loc<0||loc>=ISIZE) { errors.push(`L${ln+1}: bad loc`); continue; }

    let rest = line.slice(ci+1).trim();

    // Opcode token
    const sep = rest.search(/[\s,]/);
    let opStr = (sep<0 ? rest : rest.slice(0,sep)).toUpperCase();
    let restArgs = sep<0 ? '' : rest.slice(sep).trim();

    // Match opcode (4-char prefix)
    let opNum = -1;
    for (let i=0; i<OP.length; i++) {
      if (OP[i]!=='????' && OP[i].startsWith(opStr.slice(0,4))) { opNum=i; break; }
    }
    if (opNum<0||OP[opNum]==='????') { errors.push(`L${ln+1}: unknown op "${opStr}"`); continue; }

    let a1=0, a2=0, a3=0, comment='';

    if (cls(opNum)==='RR') {
      const rrMatch = restArgs.match(/^(\d+)\s*,\s*(\d+)\s*,\s*(\d+)(.*)$/);
      if (rrMatch) {
        a1 = parseInt(rrMatch[1]);
        a2 = parseInt(rrMatch[2]);
        a3 = parseInt(rrMatch[3]);
        comment = rrMatch[4].replace(/^\s*\*?\s*/, '').trim();
      } else {
        const parts = restArgs.split(',');
        a1 = parseInt(parts[0])||0;
        a2 = parseInt(parts[1])||0;
        a3 = parseInt(parts[2])||0;
      }
    } else {
      // RM or RA: r, d(s)
      const rmMatch = restArgs.match(/^(\d+)\s*,\s*(-?\d+)(?:\s*\(\s*(\d+)\s*\))?(.*)$/);
      if (rmMatch) {
        a1 = parseInt(rmMatch[1]);
        a2 = parseInt(rmMatch[2]);
        a3 = rmMatch[3] !== undefined ? parseInt(rmMatch[3]) : 0;
        comment = rmMatch[4].replace(/^\s*\*?\s*/, '').trim();
      } else {
        const parts = restArgs.split(',');
        a1 = parseInt(parts[0])||0;
        const ds = (parts[1]||'')+(parts[2]||'');
        const m = ds.match(/^(-?\d+)\((\d+)\)/);
        if (m) { a2=parseInt(m[1]); a3=parseInt(m[2]); }
        else { a2=parseInt(parts[1])||0; a3=0; }
      }
    }

    iMem[loc] = {iop:opNum, a1, a2, a3, comment, loc, has:true};
    maxLoc=Math.max(maxLoc,loc);
  }
  return {errors,maxLoc};
}

// ── Reset ───────────────────────────────────────────────────
// Reinicia toda la máquina a su estado inicial:
// - dMem[0] = DSIZE-1 (por convención del compilador Tiny)
// - Todos los registros en 0
function resetMachine() {
  dMem = new Array(DSIZE).fill(0); dMem[0]=DSIZE-1;
  reg  = new Array(NREGS).fill(0);
  stepCount=0; lastResult='READY'; ioLog=[]; lastWrite=-1;
  prevReg=[...reg]; prevDMem=[...dMem];
  running=false; waitInput=false; pendingCb=null;
}

// ── stepTM ──────────────────────────────────────────────────
// Ejecuta UNA instrucción de la Tiny Machine.
// Retorna un código de resultado (OK, HALTED, error, etc.)
function stepTM() {
  const pc=reg[PC];
  if (pc<0||pc>=ISIZE) return IMEM;

  reg[PC]=pc+1;
  const inst=iMem[pc];
  const c=cls(inst.iop);
  let r=inst.a1, s,t,m;

  if (c==='RR') { s=inst.a2; t=inst.a3; }
  else {
    s=inst.a3; m=inst.a2+reg[s];
    if (c==='RM'&&(m<0||m>=DSIZE)) {
      reg[PC]=pc;
      ioLog.push({t:'err', msg:`[Error TM] Acceso fuera de rango a memoria de datos: ${m}`});
      if (typeof IdeConsole !== 'undefined') {
        IdeConsole.error(`[Error de Ejecución TM] Acceso inválido a memoria de datos en dirección ${m} (instrucción ${pc}).`);
      }
      return DMEM;
    }
  }

  prevReg=[...reg];
  lastWrite=-1;

  switch(inst.iop){
    case 0:  // HALT
      reg[PC]=pc;
      ioLog.push({t:'halt',msg:`HALT at ${pc}`});
      if (typeof IdeConsole !== 'undefined') {
        IdeConsole.success(`[TM] Programa finalizado: HALT en instrucción ${pc}.`);
      }
      stepCount++; return HALTED;

    case 1:  // IN
      reg[PC]=pc;
      return NEEDS_IN;

    case 2:  // OUT
      const outVal = reg[r];
      let outStr = `OUT: ${outVal}`;
      if (outVal >= 32 && outVal <= 126) {
        outStr += ` ('${String.fromCharCode(outVal)}')`;
      }
      ioLog.push({t:'out', msg: outStr});
      if (typeof IdeConsole !== 'undefined') {
        IdeConsole.output(`[Salida TM] ${outVal}${outVal >= 32 && outVal <= 126 ? ` ('${String.fromCharCode(outVal)}')` : ''}`);
      }
      break;

    case 3:  reg[r]=reg[s]+reg[t]; break;
    case 4:  reg[r]=reg[s]-reg[t]; break;
    case 5:  reg[r]=reg[s]*reg[t]; break;
    case 6:
      if(reg[t]===0) {
        reg[PC]=pc;
        ioLog.push({t:'err', msg:`[Error TM] Division por cero en instruccion ${pc}`});
        if (typeof IdeConsole !== 'undefined') {
          IdeConsole.error(`[Error de Ejecución TM] Division por cero detectada en instruccion ${pc}.`);
        }
        return ZDIV;
      }
      reg[r]=Math.trunc(reg[s]/reg[t]); break;

    case 8:   reg[r]=dMem[m]; break; // LD
    case 9:   dMem[m]=reg[r]; lastWrite=m; break; // ST

    case 11:  reg[r]=m; break; // LDA
    case 12:  reg[r]=inst.a2; break; // LDC
    case 13:  if(reg[r]< 0) reg[PC]=m; break;
    case 14:  if(reg[r]<=0) reg[PC]=m; break;
    case 15:  if(reg[r]> 0) reg[PC]=m; break;
    case 16:  if(reg[r]>=0) reg[PC]=m; break;
    case 17:  if(reg[r]===0) reg[PC]=m; break;
    case 18:  if(reg[r]!==0) reg[PC]=m; break;
  }

  stepCount++;

  // Check data watchpoint
  if (wEn && lastWrite===wAddr) {
    if (!wValEn || dMem[wAddr]===wVal) return WP;
  }

  // Check next-PC breakpoint
  if (breakpoints.has(reg[PC])) return BP;

  return OK;
}
