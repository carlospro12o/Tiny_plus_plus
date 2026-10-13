package ve.edu.unet;

import ve.edu.unet.nodosAST.*;
import java.util.ArrayList;
import java.util.List;

public class ChequeadorSemantico {
    private TablaSimbolos tablaSimbolos;
    private List<String> errores;
    private String subrutinaActualRetorno; // null si estamos en el cuerpo principal, o tipo si es funcion, o "void" si es procedimiento
    private String subrutinaActualNombre;  // Nombre de la subrutina actual en verificacion

    public ChequeadorSemantico() {
        this.tablaSimbolos = new TablaSimbolos();
        this.errores = new ArrayList<String>();
        this.subrutinaActualRetorno = null;
        this.subrutinaActualNombre = null;
    }

    public ChequeadorSemantico(TablaSimbolos ts) {
        this.tablaSimbolos = ts;
        this.errores = new ArrayList<String>();
        this.subrutinaActualRetorno = null;
        this.subrutinaActualNombre = null;
    }

    public List<String> getErrores() {
        return errores;
    }

    public boolean tieneErrores() {
        return !errores.isEmpty();
    }

    public void agregarError(int linea, String mensaje) {
        int l = (linea > 0) ? linea : 1;
        errores.add("[Error Semantico] Linea " + l + ": " + mensaje);
    }

    public void imprimirErrores() {
        for (String err : errores) {
            System.err.println(err);
        }
    }

    // Metodo principal de analisis semantico
    public boolean analizar(NodoBase raiz) {
        errores.clear();

        // Paso 1: Pre-registrar todas las subrutinas globales para permitir llamadas mutuas
        preRegistrarSubrutinas(raiz);

        // Paso 2: Recorrido y chequeo semantico en orden secuencial
        verificarNodo(raiz);

        return !tieneErrores();
    }

    // Pre-registro de firmas de subrutinas globales
    private void preRegistrarSubrutinas(NodoBase nodo) {
        NodoBase actual = nodo;
        while (actual != null) {
            if (actual instanceof NodoSubrutina) {
                NodoSubrutina sub = (NodoSubrutina) actual;
                String nombre = sub.getIdentificador();

                // Verificar que no este redeclarada en el ambito global
                if (tablaSimbolos.getScopeGlobal().buscarLocal(nombre) != null) {
                    agregarError(sub.getLinea(), "La funcion o procedimiento '" + nombre + "' ya fue declarado previamente.");
                } else {
                    List<RegistroSimbolo> params = new ArrayList<RegistroSimbolo>();
                    NodoBase p = sub.getParametros();
                    int offset = 0;
                    while (p != null) {
                        if (p instanceof NodoDeclaracion) {
                            NodoDeclaracion dp = (NodoDeclaracion) p;
                            params.add(new RegistroSimbolo(dp.getIdentificador(), dp.getTipo(), dp.getLinea(), offset++));
                        }
                        p = p.getHermanoDerecha();
                    }
                    tablaSimbolos.getScopeGlobal().insertar(new RegistroSimbolo(nombre, sub.getTipoRetorno(), params, sub.getLinea(), 0));
                }
            }
            actual = actual.getHermanoDerecha();
        }
    }

    // Chequeo de sentencias y declaraciones
    private void verificarNodo(NodoBase nodo) {
        while (nodo != null) {
            if (nodo instanceof NodoDeclaracion) {
                verificarDeclaracion((NodoDeclaracion) nodo);
            } else if (nodo instanceof NodoSubrutina) {
                verificarSubrutina((NodoSubrutina) nodo);
            } else if (nodo instanceof NodoAsignacion) {
                verificarAsignacion((NodoAsignacion) nodo);
            } else if (nodo instanceof NodoLeer) {
                verificarLeer((NodoLeer) nodo);
            } else if (nodo instanceof NodoEscribir) {
                verificarEscribir((NodoEscribir) nodo);
            } else if (nodo instanceof NodoIf) {
                verificarIf((NodoIf) nodo);
            } else if (nodo instanceof NodoRepeat) {
                verificarRepeat((NodoRepeat) nodo);
            } else if (nodo instanceof NodoReturn) {
                verificarReturn((NodoReturn) nodo);
            } else if (nodo instanceof NodoLlamada) {
                verificarLlamadaSubrutina((NodoLlamada) nodo, false);
            }

            nodo = nodo.getHermanoDerecha();
        }
    }

    private void verificarDeclaracion(NodoDeclaracion dec) {
        String id = dec.getIdentificador();

        // Verificar que no se redeclare en el mismo ambito
        if (tablaSimbolos.buscarEnScopeActual(id) != null) {
            agregarError(dec.getLinea(), "La variable '" + id + "' ya ha sido declarada en este ambito.");
            return;
        }

        if (dec.esArreglo()) {
            if (dec.getTamanoArreglo() == null || dec.getTamanoArreglo() <= 0) {
                agregarError(dec.getLinea(), "El tamao del arreglo '" + id + "' debe ser un entero positivo.");
            }
            tablaSimbolos.insertarArreglo(id, dec.getTipo(), (dec.getTamanoArreglo() != null) ? dec.getTamanoArreglo() : 1, dec.getLinea());
        } else {
            tablaSimbolos.insertarVariable(id, dec.getTipo(), dec.getLinea());
        }
    }

    private void verificarSubrutina(NodoSubrutina sub) {
        String nombre = sub.getIdentificador();
        String retornoAnterior = this.subrutinaActualRetorno;
        String nombreAnterior = this.subrutinaActualNombre;

        this.subrutinaActualRetorno = (sub.getTipoRetorno() != null) ? sub.getTipoRetorno() : "void";
        this.subrutinaActualNombre = nombre;

        // Abrir ambito local de la subrutina
        tablaSimbolos.abrirScope(nombre);

        // Insertar parametros en el ambito local y verificar duplicados
        NodoBase p = sub.getParametros();
        while (p != null) {
            if (p instanceof NodoDeclaracion) {
                NodoDeclaracion dp = (NodoDeclaracion) p;
                if (tablaSimbolos.buscarEnScopeActual(dp.getIdentificador()) != null) {
                    agregarError(dp.getLinea(), "El parametro '" + dp.getIdentificador() + "' esta duplicado en la definicion de '" + nombre + "'.");
                } else {
                    tablaSimbolos.insertarVariable(dp.getIdentificador(), dp.getTipo(), dp.getLinea());
                }
            }
            p = p.getHermanoDerecha();
        }

        // Verificar el cuerpo de la subrutina
        if (sub.getCuerpo() != null) {
            verificarNodo(sub.getCuerpo());
        }

        // Cerrar el ambito local
        tablaSimbolos.cerrarScope();

        this.subrutinaActualRetorno = retornoAnterior;
        this.subrutinaActualNombre = nombreAnterior;
    }

    private void verificarAsignacion(NodoAsignacion asig) {
        String id = asig.getIdentificador();
        RegistroSimbolo simbolo = tablaSimbolos.buscar(id);

        if (simbolo == null) {
            agregarError(asig.getLinea(), "La variable '" + id + "' no ha sido declarada antes de su uso.");
            return;
        }

        if (simbolo.esFuncion() || simbolo.esProcedimiento()) {
            agregarError(asig.getLinea(), "'" + id + "' es una subrutina y no puede recibir una asignacion.");
            return;
        }

        String tipoExpresion = tipoDeExpresion(asig.getExpresion());

        if (asig.esArreglo()) {
            if (!simbolo.esArreglo()) {
                agregarError(asig.getLinea(), "'" + id + "' no es un arreglo y no puede usarse con corchetes [].");
                return;
            }

            // Verificar que el indice sea entero
            String tipoIndice = tipoDeExpresion(asig.getIndice());
            if (!"int".equals(tipoIndice) && !"error".equals(tipoIndice)) {
                agregarError(asig.getLinea(), "El indice del arreglo '" + id + "' debe ser de tipo 'int', pero se obtuvo '" + tipoIndice + "'.");
            }

            // Verificar compatibilidad de tipo con el tipo base del arreglo
            if (!"error".equals(tipoExpresion) && !simbolo.getTipoBase().equals(tipoExpresion)) {
                agregarError(asig.getLinea(), "Tipos incompatibles en asignacion a '" + id + "[]'. Se esperaba '" + simbolo.getTipoBase() + "' pero se obtuvo '" + tipoExpresion + "'.");
            }
        } else {
            if (simbolo.esArreglo()) {
                agregarError(asig.getLinea(), "'" + id + "' es un arreglo y requiere un indice de acceso para ser asignado.");
                return;
            }

            // Verificar compatibilidad de tipos
            if (!"error".equals(tipoExpresion) && !simbolo.getTipo().equals(tipoExpresion)) {
                agregarError(asig.getLinea(), "Tipos incompatibles en la asignacion a '" + id + "'. Se esperaba '" + simbolo.getTipo() + "' pero se obtuvo '" + tipoExpresion + "'.");
            }
        }
    }

    private void verificarLeer(NodoLeer leer) {
        String id = leer.getIdentificador();
        RegistroSimbolo simbolo = tablaSimbolos.buscar(id);

        if (simbolo == null) {
            agregarError(leer.getLinea(), "La variable '" + id + "' no ha sido declarada.");
            return;
        }

        if (simbolo.esArreglo()) {
            agregarError(leer.getLinea(), "No se puede leer directamente un arreglo completo '" + id + "'.");
        } else if (simbolo.esFuncion() || simbolo.esProcedimiento()) {
            agregarError(leer.getLinea(), "'" + id + "' es una subrutina y no puede usarse en una instruccion de lectura.");
        }
    }

    private void verificarEscribir(NodoEscribir escribir) {
        String tipoExp = tipoDeExpresion(escribir.getExpresion());
        if ("void".equals(tipoExp)) {
            agregarError(escribir.getLinea(), "No se puede escribir el resultado de un procedimiento sin valor de retorno.");
        }
    }

    private void verificarIf(NodoIf nif) {
        String tipoCond = tipoDeExpresion(nif.getPrueba());
        if (!"bool".equals(tipoCond) && !"error".equals(tipoCond)) {
            agregarError(nif.getLinea(), "La condicion de la sentencia IF debe ser de tipo 'bool', pero se obtuvo '" + tipoCond + "'.");
        }

        if (nif.getParteThen() != null) {
            verificarNodo(nif.getParteThen());
        }
        if (nif.getParteElse() != null) {
            verificarNodo(nif.getParteElse());
        }
    }

    private void verificarRepeat(NodoRepeat nrep) {
        if (nrep.getCuerpo() != null) {
            verificarNodo(nrep.getCuerpo());
        }

        String tipoPrueba = tipoDeExpresion(nrep.getPrueba());
        if (!"bool".equals(tipoPrueba) && !"error".equals(tipoPrueba)) {
            agregarError(nrep.getLinea(), "La condicion de la sentencia REPEAT-UNTIL debe ser de tipo 'bool', pero se obtuvo '" + tipoPrueba + "'.");
        }
    }

    private void verificarReturn(NodoReturn ret) {
        if (subrutinaActualRetorno == null) {
            agregarError(ret.getLinea(), "La sentencia 'return' no puede estar fuera de una funcion o procedimiento.");
            return;
        }

        if ("void".equals(subrutinaActualRetorno)) {
            if (ret.tieneExpresion()) {
                agregarError(ret.getLinea(), "El procedimiento '" + subrutinaActualNombre + "' no debe retornar ningun valor.");
            }
        } else {
            if (!ret.tieneExpresion()) {
                agregarError(ret.getLinea(), "La funcion '" + subrutinaActualNombre + "' debe retornar una expresion de tipo '" + subrutinaActualRetorno + "'.");
            } else {
                String tipoRetornado = tipoDeExpresion(ret.getExpresion());
                if (!"error".equals(tipoRetornado) && !subrutinaActualRetorno.equals(tipoRetornado)) {
                    agregarError(ret.getLinea(), "Tipo de retorno incompatible en la funcion '" + subrutinaActualNombre + "'. Se esperaba '" + subrutinaActualRetorno + "' pero se obtuvo '" + tipoRetornado + "'.");
                }
            }
        }
    }

    private String verificarLlamadaSubrutina(NodoLlamada llamada, boolean enExpresion) {
        String nombre = llamada.getIdentificador();
        RegistroSimbolo simbolo = tablaSimbolos.buscar(nombre);

        if (simbolo == null) {
            agregarError(llamada.getLinea(), "La subrutina '" + nombre + "' no ha sido declarada.");
            return "error";
        }

        if (!simbolo.esFuncion() && !simbolo.esProcedimiento()) {
            agregarError(llamada.getLinea(), "'" + nombre + "' es una variable y no puede ser invocada como funcion.");
            return "error";
        }

        if (enExpresion && simbolo.esProcedimiento()) {
            agregarError(llamada.getLinea(), "El procedimiento '" + nombre + "' no retorna ningun valor y no puede ser usado dentro de una expresion.");
            return "error";
        }

        // Contar y verificar tipos de argumentos
        List<RegistroSimbolo> params = simbolo.getParametros();
        List<String> tiposArgs = new ArrayList<String>();
        NodoBase arg = llamada.getArgumentos();
        while (arg != null) {
            tiposArgs.add(tipoDeExpresion(arg));
            arg = arg.getHermanoDerecha();
        }

        if (params.size() != tiposArgs.size()) {
            agregarError(llamada.getLinea(), "Numero incorrecto de argumentos en la llamada a '" + nombre + "'. Se esperaban " + params.size() + " pero se recibieron " + tiposArgs.size() + ".");
            return simbolo.esFuncion() ? simbolo.getTipoRetorno() : "void";
        }

        for (int i = 0; i < params.size(); i++) {
            String esperado = params.get(i).getTipo();
            String recibido = tiposArgs.get(i);
            if (!"error".equals(recibido) && !esperado.equals(recibido)) {
                agregarError(llamada.getLinea(), "Tipo de argumento incompatible en parametro '" + params.get(i).getIdentificador() + "' de '" + nombre + "'. Se esperaba '" + esperado + "' pero se recibio '" + recibido + "'.");
            }
        }

        return simbolo.esFuncion() ? simbolo.getTipoRetorno() : "void";
    }

    // Evaluacion del tipo de una expresion
    public String tipoDeExpresion(NodoBase nodo) {
        if (nodo == null) return "error";

        if (nodo instanceof NodoValor) {
            NodoValor val = (NodoValor) nodo;
            if (val.esCadena()) return "string";
            if (val.esBooleano()) return "bool";
            return "int";
        }

        if (nodo instanceof NodoIdentificador) {
            NodoIdentificador id = (NodoIdentificador) nodo;
            String nombre = id.getNombre();
            RegistroSimbolo simbolo = tablaSimbolos.buscar(nombre);

            if (simbolo == null) {
                agregarError(id.getLinea(), "La variable '" + nombre + "' no ha sido declarada.");
                return "error";
            }

            if (id.esArreglo()) {
                if (!simbolo.esArreglo()) {
                    agregarError(id.getLinea(), "'" + nombre + "' no es un arreglo.");
                    return "error";
                }
                String tipoIdx = tipoDeExpresion(id.getIndice());
                if (!"int".equals(tipoIdx) && !"error".equals(tipoIdx)) {
                    agregarError(id.getLinea(), "El indice del arreglo '" + nombre + "' debe ser de tipo 'int', pero se obtuvo '" + tipoIdx + "'.");
                }
                return simbolo.getTipoBase();
            } else {
                if (simbolo.esArreglo()) {
                    agregarError(id.getLinea(), "'" + nombre + "' es un arreglo y debe accederse con un indice entre corchetes [].");
                    return "error";
                }
                if (simbolo.esFuncion() || simbolo.esProcedimiento()) {
                    agregarError(id.getLinea(), "'" + nombre + "' es una subrutina y no puede usarse como variable.");
                    return "error";
                }
                return simbolo.getTipo();
            }
        }

        if (nodo instanceof NodoLlamada) {
            return verificarLlamadaSubrutina((NodoLlamada) nodo, true);
        }

        if (nodo instanceof NodoOperacion) {
            NodoOperacion op = (NodoOperacion) nodo;
            tipoOp operador = op.getOperacion();

            // Operador unario: NOT
            if (operador == tipoOp.not) {
                String tipoOpUnario = tipoDeExpresion(op.getOpIzquierdo());
                if (!"bool".equals(tipoOpUnario) && !"error".equals(tipoOpUnario)) {
                    agregarError(op.getLinea(), "El operador 'not' requiere un operando de tipo 'bool', pero se obtuvo '" + tipoOpUnario + "'.");
                }
                return "bool";
            }

            String tipoIzq = tipoDeExpresion(op.getOpIzquierdo());
            String tipoDer = tipoDeExpresion(op.getOpDerecho());

            if ("error".equals(tipoIzq) || "error".equals(tipoDer)) {
                return "error";
            }

            // Operadores aritmeticos: +, -, *, /
            if (operador == tipoOp.mas || operador == tipoOp.menos || operador == tipoOp.por || operador == tipoOp.entre) {
                if (!"int".equals(tipoIzq) || !"int".equals(tipoDer)) {
                    agregarError(op.getLinea(), "Operacion aritmetica requiere operandos de tipo 'int', pero se encontro '" + tipoIzq + "' y '" + tipoDer + "'.");
                    return "error";
                }
                return "int";
            }

            // Operadores relacionales de orden: <, >, <=, >=
            if (operador == tipoOp.menor || operador == tipoOp.mayor || operador == tipoOp.menor_igual || operador == tipoOp.mayor_igual) {
                if (!"int".equals(tipoIzq) || !"int".equals(tipoDer)) {
                    agregarError(op.getLinea(), "Operador relacional de orden requiere operandos de tipo 'int', pero se encontro '" + tipoIzq + "' y '" + tipoDer + "'.");
                    return "error";
                }
                return "bool";
            }

            // Operadores de igualdad: =, !=
            if (operador == tipoOp.igual || operador == tipoOp.distinto) {
                if (!tipoIzq.equals(tipoDer)) {
                    agregarError(op.getLinea(), "Comparacion de igualdad requiere operandos del mismo tipo, pero se encontro '" + tipoIzq + "' y '" + tipoDer + "'.");
                    return "error";
                }
                return "bool";
            }

            // Operadores logicos binarios: and, or
            if (operador == tipoOp.and || operador == tipoOp.or) {
                if (!"bool".equals(tipoIzq) || !"bool".equals(tipoDer)) {
                    agregarError(op.getLinea(), "Operador logico requiere operandos de tipo 'bool', pero se encontro '" + tipoIzq + "' y '" + tipoDer + "'.");
                    return "error";
                }
                return "bool";
            }
        }

        return "error";
    }
}
