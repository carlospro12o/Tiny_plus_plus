/* ═══════════════════════════════════════════════════════════════
   TM-UI.JS — Interfaz de Usuario y Event Bindings
   ═══════════════════════════════════════════════════════════════
   
   Este archivo conecta el MOTOR (tm-engine.js) con el HTML.
   Contiene:
   - Funciones de ejecución (doStep1, doStepN, doRun, etc.)
   - Manejo de input del usuario (IN)
   - Carga de programas
   - Renderizado de paneles (registros, memorias, IO log)
   - Event listeners (botones, teclado, archivos)
   
   IMPORTANTE: Este archivo depende de que tm-engine.js
   ya esté cargado (las variables globales como iMem, reg, etc.)
   ═══════════════════════════════════════════════════════════════ */
'use strict';

// ══════════════════════════════════════════════════════════════
// FUNCIONES DE EJECUCIÓN
// Estas funciones llaman a stepTM() del motor y actualizan la UI
// ══════════════════════════════════════════════════════════════

/** Ejecuta UN solo paso de la máquina */
function doStep1() {
  if (!programLoaded||waitInput) return;
  if (lastResult===HALTED) return;
  const r=stepTM();
  if (r===NEEDS_IN) { setupInputWait(()=>updateUI()); }
  else { lastResult=r; updateUI(); }
}

/** Ejecuta N pasos consecutivos (se detiene si hay IN, halt, etc.) */
function doStepN(n) {
  if (!programLoaded||waitInput) return;
  if (lastResult===HALTED) return;
  let r=OK;
  for (let i=0;i<n&&r===OK;i++){
    r=stepTM();
    if(r===NEEDS_IN){
      setupInputWait(()=>{ doStepN(n-i-1); });
      updateUI(); return;
    }
  }
  lastResult=r; updateUI();
}

/**
 * Ejecución continua (async chunked).
 * 
 * ¿Por qué "chunked"?
 * Si ejecutáramos 100,000 pasos de golpe, el navegador se
 * congelaría porque JavaScript es single-threaded.
 * En su lugar, ejecutamos 2000 pasos por frame y luego
 * cedemos el control al navegador con requestAnimationFrame.
 * Así la UI sigue respondiendo.
 */
function doRun() {
  if (!programLoaded||waitInput) return;
  if (lastResult===HALTED) return;
  running=true; updateRunBtns();

  function chunk() {
    if (!running) { updateUI(); return; }
    let r=OK;
    // Run up to 2000 steps per frame
    for (let i=0;i<2000&&r===OK;i++){
      r=stepTM();
      if (r===NEEDS_IN){
        setupInputWait(()=>{ running=true; updateRunBtns(); requestAnimationFrame(chunk); });
        lastResult=r; running=false; updateUI(); return;
      }
      if (r!==OK) break;
    }
    lastResult=r;
    if (r===OK) { requestAnimationFrame(chunk); }
    else { running=false; updateRunBtns(); updateUI(); }
  }
  requestAnimationFrame(chunk);
}

/** Detiene la ejecución continua */
function doStop() { running=false; updateRunBtns(); updateUI(); }

/** Reinicia la máquina al estado inicial (conserva el programa cargado) */
function doReset() {
  if (!programLoaded) return;
  running=false; waitInput=false; pendingCb=null;
  resetMachine();
  hideInput(); updateRunBtns(); updateUI();
}

// ══════════════════════════════════════════════════════════════
// MANEJO DE INPUT (instrucción IN)
// Cuando el programa ejecuta IN, necesita un valor del usuario.
// Mostramos un campo de texto y pausamos la ejecución.
// ══════════════════════════════════════════════════════════════

function setupInputWait(cb) {
  waitInput=true; pendingCb=cb;
  document.getElementById('in-row').style.display='flex';
  document.getElementById('in-val').focus();
}

function submitInput() {
  const raw=document.getElementById('in-val').value;
  const v=parseInt(raw);
  if (isNaN(v)) { document.getElementById('in-val').select(); return; }
  
  // Execute the IN manually: find the IN instruction at current PC
  const pc=reg[PC];
  const inst=iMem[pc];
  reg[inst.a1]=v;
  reg[PC]=pc+1;
  stepCount++;
  ioLog.push({t:'in', msg:`IN: ${v} → reg[${inst.a1}]`});
  
  // Check if new PC is a breakpoint
  let r=OK;
  if (breakpoints.has(reg[PC])) r=BP;
  
  hideInput();
  waitInput=false;
  if (r!==OK) { lastResult=r; const cb=pendingCb; pendingCb=null; updateUI(); return; }
  
  const cb=pendingCb; pendingCb=null;
  if (cb) cb();
}

function hideInput() {
  document.getElementById('in-row').style.display='none';
  document.getElementById('in-val').value='';
}

// ══════════════════════════════════════════════════════════════
// CARGA DE PROGRAMAS
// ══════════════════════════════════════════════════════════════

function loadProgram(text, name) {
  const {errors}=parseProgram(text);
  if (errors.length>0) {
    document.getElementById('paste-err').textContent=errors.slice(0,3).join(' | ');
    alert('Parse errors:\n'+errors.join('\n')); return false;
  }
  document.getElementById('paste-err').textContent='';
  programLoaded=true;
  breakpoints.clear();
  resetMachine();
  document.getElementById('file-name').textContent=name||'pasted program';
  updateUI();
  return true;
}

// ══════════════════════════════════════════════════════════════
// ACTUALIZACIÓN DE UI
// ══════════════════════════════════════════════════════════════

/** Muestra/oculta botones Run y Stop según estado */
function updateRunBtns() {
  document.getElementById('btn-run').style.display =running?'none':'';
  document.getElementById('btn-stop').style.display=running?'':'none';
}

/** Actualiza TODOS los paneles de la interfaz */
function updateUI() {
  renderRegisters();
  renderDmem();
  renderImem();
  renderIOLog();

  document.getElementById('step-count').textContent=`Step: ${stepCount}`;

  const rEl=document.getElementById('step-result');
  rEl.textContent=lastResult;
  rEl.style.color=
    lastResult===HALTED?'var(--red)':
    lastResult===OK?'var(--green)':
    lastResult==='READY'?'var(--text2)':
    lastResult===BP||lastResult===WP?'var(--amber)':'var(--red)';

  document.getElementById('sb-status').textContent=lastResult;
  document.getElementById('sb-pc').textContent=`PC: ${programLoaded?reg[PC]:'—'}`;
  document.getElementById('sb-watch').textContent=
    wEn?`▲ watch addr:${wAddr}${wValEn?'='+wVal:''}` : '';
}

// ══════════════════════════════════════════════════════════════
// RENDERIZADO DE PANELES
// ══════════════════════════════════════════════════════════════

// ── Registros ──────────────────────────────────────────────
const REG_NAMES=['r0','r1','r2','r3','r4','r5','r6','pc'];

function renderRegisters() {
  const g=document.getElementById('reg-grid');
  g.innerHTML='';
  for(let i=0;i<NREGS;i++){
    const chg=reg[i]!==prevReg[i];
    const d=document.createElement('div');
    d.className='reg-row'+(chg?' chg':'');
    d.innerHTML=`<span class="reg-lbl${i===PC?' sp':''}">${REG_NAMES[i]}:</span>`
               +`<span class="reg-val">${reg[i]}</span>`;
    g.appendChild(d);
  }
}

// ── Data Memory ────────────────────────────────────────────
function renderDmem() {
  const start=parseInt(document.getElementById('rng-start').value)||0;
  const count=parseInt(document.getElementById('rng-count').value)||64;
  const fp=reg[6]; // reg[6] often used as FP in TINY
  const tb=document.getElementById('dmem-body');
  tb.innerHTML='';

  let addrs=[];
  if(dmemTab==='low'){
    for(let a=start;a<Math.min(start+count,DSIZE);a++) addrs.push(a);
  } else {
    const top=Math.min(start+count-1,DSIZE-1);
    for(let a=top;a>=start;a--) addrs.push(a);
  }

  document.getElementById('dmem-info').textContent=
    `[${addrs[addrs.length-1]}…${addrs[0]}]`;

  for(const a of addrs){
    const v=dMem[a], nz=v!==0, chg=a===lastWrite;
    const isWatch=wEn&&a===wAddr;
    const isFP=a===fp&&fp!==0;
    let note='';
    if(a===0) note='gp';
    if(isFP) note+=(note?'+':'')+'fp';

    const tr=document.createElement('tr');
    tr.className=isWatch?'d-watch':isFP?'d-fp':'';
    tr.innerHTML=`<td class="d-addr">${a}</td>`
                +`<td class="d-val${nz?' nz':''}${chg?' chg':''}">${v}</td>`
                +`<td class="d-note">${note}</td>`;
    tb.appendChild(tr);
  }
}

// ── Instruction Memory ─────────────────────────────────────
function renderImem() {
  const tb=document.getElementById('imem-body');
  const pc=reg[PC];
  tb.innerHTML='';
  let pcRow=null, anyInstr=false;

  for(let loc=0;loc<ISIZE;loc++){
    const inst=iMem[loc];
    if(!inst||!inst.has) continue;
    anyInstr=true;

    const isPC=loc===pc, isBP=breakpoints.has(loc);
    const tr=document.createElement('tr');
    tr.className='i-row'+(isPC?' pc':'')+(isBP?' bp':'');
    tr.onclick=()=>toggleBP(loc);

    const marker=isBP?'<span style="color:var(--red)">●</span>':
                  isPC?'<span style="color:var(--green)">▶</span>':'';

    tr.innerHTML=`<td class="i-marker">${marker}</td>`
                +`<td class="i-loc">${loc}</td>`
                +`<td class="i-op">${OP[inst.iop]||'?'}</td>`
                +`<td class="i-args">${fmtArgs(inst)}</td>`
                +`<td class="i-comment">${inst.comment}</td>`;
    tb.appendChild(tr);
    if(isPC) pcRow=tr;
  }

  if(!anyInstr){
    tb.innerHTML='<tr><td colspan="5" class="empty-msg">Load a .tm file or paste code above.<br>Click a row to toggle ● breakpoint.</td></tr>';
    return;
  }
  if(pcRow) setTimeout(()=>pcRow.scrollIntoView({block:'center',behavior:'smooth'}),30);
}

// ── IO Log ─────────────────────────────────────────────────
function renderIOLog() {
  const el=document.getElementById('io-log');
  el.innerHTML=ioLog.map(e=>{
    const c=e.t==='in'?'io-in':e.t==='out'?'io-out':e.t==='halt'?'io-halt':'io-err';
    return `<div class="io-e ${c}">${e.msg}</div>`;
  }).join('');
  el.scrollTop=el.scrollHeight;
}

// ── Breakpoints ────────────────────────────────────────────
function toggleBP(loc) {
  if(breakpoints.has(loc)) breakpoints.delete(loc);
  else breakpoints.add(loc);
  renderImem();
}

// ── Tab switch (Low / High memory view) ────────────────────
function switchTab(tab) {
  tabCfg[dmemTab].start=parseInt(document.getElementById('rng-start').value)||0;
  tabCfg[dmemTab].count=parseInt(document.getElementById('rng-count').value)||64;
  dmemTab=tab;
  document.getElementById('rng-start').value=tabCfg[tab].start;
  document.getElementById('rng-count').value=tabCfg[tab].count;
  document.querySelectorAll('.tab').forEach(t=>
    t.classList.toggle('active', t.dataset.tab===tab));
  document.getElementById('cfg-lbl').textContent=
    tab==='low'?'Start addr (↑):':'Start addr (↓):';
  renderDmem();
}

// ══════════════════════════════════════════════════════════════
// EVENT BINDINGS (Conexión de eventos del DOM)
//
// ¿Por qué addEventListener y no onclick="..."?
// → Es más limpio: separa HTML de JavaScript.
// → Permite agregar múltiples listeners al mismo elemento.
// → Es el estándar moderno recomendado.
// ══════════════════════════════════════════════════════════════

// ── Carga de archivo .tm ───────────────────────────────────
document.getElementById('file-in').addEventListener('change',e=>{
  const f=e.target.files[0]; if(!f) return;
  const r=new FileReader();
  r.onload=ev=>loadProgram(ev.target.result, f.name);
  r.readAsText(f);
  e.target.value='';
});

// ── Toggle del panel de paste ──────────────────────────────
document.getElementById('btn-paste-toggle').addEventListener('click',()=>{
  document.getElementById('paste-wrap').classList.toggle('visible');
});

// ── Cargar desde paste ─────────────────────────────────────
document.getElementById('btn-load-paste').addEventListener('click',()=>{
  const t=document.getElementById('paste-ta').value;
  if(!t.trim()) return;
  if(loadProgram(t,'pasted program'))
    document.getElementById('paste-wrap').classList.remove('visible');
});

// ── Botones de control ─────────────────────────────────────
document.getElementById('btn-step1').addEventListener('click', doStep1);
document.getElementById('btn-stepn').addEventListener('click',()=>
  doStepN(Math.max(1,parseInt(document.getElementById('n-val').value)||1)));
document.getElementById('btn-run').addEventListener('click', doRun);
document.getElementById('btn-stop').addEventListener('click', doStop);
document.getElementById('btn-reset').addEventListener('click', doReset);

// ── Input submit (cuando el programa pide IN) ──────────────
document.getElementById('btn-in-ok').addEventListener('click', submitInput);
document.getElementById('in-val').addEventListener('keydown',e=>{
  if(e.key==='Enter') submitInput();
});

// ── Atajos de teclado ──────────────────────────────────────
// S = Step, R = Run, Escape = Stop
// Solo funcionan si NO estás escribiendo en un input/textarea
document.addEventListener('keydown',e=>{
  if(e.target.tagName==='INPUT'||e.target.tagName==='TEXTAREA') return;
  if(e.key==='s'||e.key==='S') doStep1();
  if(e.key==='r'||e.key==='R') doRun();
  if(e.key==='Escape') doStop();
});

// ── Controles de Watchpoint ────────────────────────────────
document.getElementById('w-en').addEventListener('change',e=>{
  wEn=e.target.checked;
  wAddr=parseInt(document.getElementById('w-addr').value)||0;
  renderDmem(); updateUI();
});
document.getElementById('w-addr').addEventListener('input',e=>{
  wAddr=parseInt(e.target.value)||0;
  if(wEn) renderDmem();
});
document.getElementById('w-val-en').addEventListener('change',e=>{ wValEn=e.target.checked; });
document.getElementById('w-val').addEventListener('input',e=>{ wVal=parseInt(e.target.value)||0; });

// ── Range apply on Enter ───────────────────────────────────
['rng-start','rng-count'].forEach(id=>{
  document.getElementById(id).addEventListener('keydown',e=>{
    if(e.key==='Enter') renderDmem();
  });
});

// ══════════════════════════════════════════════════════════════
// INICIALIZACIÓN
// Al cargar la página, reseteamos y renderizamos el estado vacío
// ══════════════════════════════════════════════════════════════
resetMachine();
renderRegisters();
renderDmem();
