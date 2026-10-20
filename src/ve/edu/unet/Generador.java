package ve.edu.unet;

import ve.edu.unet.nodosAST.*;
import java.util.*;

public class Generador {
	/* Ilustracion de la disposicion de la memoria en
	 * este ambiente de ejecucion para el lenguaje Tiny++
	 *
	 * |t1          |<- mp (Maxima posicion de memoria de la TM)
	 * |t1          |<- desplazamientoTmp (tope actual)
	 * |free        |
	 * |free        |
	 * |string pool |<- Literales de cadenas (reservados estaticamente)
	 * |V[0..N]     |<- Arreglos unidimensionales (bloques continuos)
	 * |x, y, flag  |<- Variables globales escalares
	 * |            |<- gp (0)
	 * */
	
	private static int desplazamientoTmp = 0;
	private static TablaSimbolos tablaSimbolos = null;
	private static InfoSubrutina subrutinaActual = null;
	
	// Pool de memoria para literales de cadena (Mision 3: Cadenas de Texto)
	private static Map<String, Integer> poolCadenas = new LinkedHashMap<>();
	private static int dirSiguienteString = 0;
	
	public static void setTablaSimbolos(TablaSimbolos tabla){
		tablaSimbolos = tabla;
	}
	
	public static void generarCodigoObjeto(NodoBase raiz){
		System.out.println();
		System.out.println();
		System.out.println("------ CODIGO OBJETO DEL LENGUAJE TINY++ GENERADO PARA LA TM ------");
		System.out.println();
		System.out.println();
		
		// Inicializar y recolectar cadenas en memoria
		poolCadenas.clear();
		subrutinaActual = null;
		desplazamientoTmp = 0;
		if (tablaSimbolos != null) {
			dirSiguienteString = tablaSimbolos.getSiguienteDireccionDisponible() + 1;
		} else {
			dirSiguienteString = 20;
		}
		recolectarCadenas(raiz);
		
		generarPreludioEstandar();
		generar(raiz);
		
		/* Codigo de finalizacion de ejecucion */   
		UtGen.emitirComentario("Fin de la ejecucion.");
		UtGen.emitirRO("HALT", 0, 0, 0, "");
		System.out.println();
		System.out.println();
		System.out.println("------ FIN DEL CODIGO OBJETO DEL LENGUAJE TINY++ GENERADO PARA LA TM ------");
	}
	
	// Recolector de literales de cadena para reservar memoria en el string pool
	private static void recolectarCadenas(NodoBase raiz) {
		while (raiz != null) {
			if (raiz instanceof NodoValor && ((NodoValor) raiz).esCadena()) {
				String cad = limpiarCadena(((NodoValor) raiz).getValorCadena());
				if (!poolCadenas.containsKey(cad)) {
					poolCadenas.put(cad, dirSiguienteString);
					dirSiguienteString += cad.length() + 1; // caracteres + '\0'
				}
			}
			
			if (raiz instanceof NodoIf) {
				recolectarCadenas(((NodoIf) raiz).getPrueba());
				recolectarCadenas(((NodoIf) raiz).getParteThen());
				if (((NodoIf) raiz).getParteElse() != null) recolectarCadenas(((NodoIf) raiz).getParteElse());
			} else if (raiz instanceof NodoRepeat) {
				recolectarCadenas(((NodoRepeat) raiz).getCuerpo());
				recolectarCadenas(((NodoRepeat) raiz).getPrueba());
			} else if (raiz instanceof NodoAsignacion) {
				NodoAsignacion asig = (NodoAsignacion) raiz;
				if (asig.esArreglo()) recolectarCadenas(asig.getIndice());
				recolectarCadenas(asig.getExpresion());
			} else if (raiz instanceof NodoAsignacionArreglo) {
				recolectarCadenas(((NodoAsignacionArreglo) raiz).getIndice());
				recolectarCadenas(((NodoAsignacionArreglo) raiz).getExpresion());
			} else if (raiz instanceof NodoEscribir) {
				recolectarCadenas(((NodoEscribir) raiz).getExpresion());
			} else if (raiz instanceof NodoOperacion) {
				recolectarCadenas(((NodoOperacion) raiz).getOpIzquierdo());
				if (((NodoOperacion) raiz).getOpDerecho() != null) recolectarCadenas(((NodoOperacion) raiz).getOpDerecho());
			} else if (raiz instanceof NodoSubrutina) {
				recolectarCadenas(((NodoSubrutina) raiz).getCuerpo());
			} else if (raiz instanceof NodoLlamada) {
				recolectarCadenas(((NodoLlamada) raiz).getArgumentos());
			} else if (raiz instanceof NodoReturn) {
				if (((NodoReturn) raiz).tieneExpresion()) recolectarCadenas(((NodoReturn) raiz).getExpresion());
			}
			raiz = raiz.getHermanoDerecha();
		}
	}

	private static String limpiarCadena(String cad) {
		if (cad == null) return "";
		if (cad.startsWith("\"") && cad.endsWith("\"") && cad.length() >= 2) {
			return cad.substring(1, cad.length() - 1);
		}
		return cad;
	}

	// Funcion principal de generacion de codigo
	private static void generar(NodoBase nodo){
		if(tablaSimbolos != null){
			if (nodo instanceof NodoIf){
				generarIf(nodo);
			}else if (nodo instanceof NodoRepeat){
				generarRepeat(nodo);
			}else if (nodo instanceof NodoAsignacion){
				NodoAsignacion asig = (NodoAsignacion) nodo;
				if (asig.esArreglo())
					generarAsignacionArreglo(asig.getIdentificador(), asig.getIndice(), asig.getExpresion());
				else
					generarAsignacion(nodo);
			}else if (nodo instanceof NodoAsignacionArreglo){
				NodoAsignacionArreglo asig = (NodoAsignacionArreglo) nodo;
				generarAsignacionArreglo(asig.getIdentificador(), asig.getIndice(), asig.getExpresion());
			}else if (nodo instanceof NodoAccesoArreglo){
				NodoAccesoArreglo acc = (NodoAccesoArreglo) nodo;
				generarAccesoArreglo(acc.getIdentificador(), acc.getIndice());
			}else if (nodo instanceof NodoLeer){
				generarLeer(nodo);
			}else if (nodo instanceof NodoEscribir){
				generarEscribir(nodo);
			}else if (nodo instanceof NodoValor){
				NodoValor val = (NodoValor) nodo;
				if (val.esCadena())
					generarCadenaLiteral(limpiarCadena(val.getValorCadena()));
				else if (val.esBooleano())
					generarBooleanoDirecto((Boolean) val.getValorObjeto());
				else
					generarValor(nodo);
			}else if (nodo instanceof NodoBooleano){
				generarBooleanoDirecto(((NodoBooleano)nodo).getValor());
			}else if (nodo instanceof NodoIdentificador){
				NodoIdentificador id = (NodoIdentificador) nodo;
				if (id.esArreglo())
					generarAccesoArreglo(id.getNombre(), id.getIndice());
				else
					generarIdentificador(nodo);
			}else if (nodo instanceof NodoOperacion){
				generarOperacion(nodo);
			}else if (nodo instanceof NodoDeclaracion){
				if(UtGen.debug) UtGen.emitirComentario("declaracion: " + ((NodoDeclaracion)nodo).getIdentificador() + " (" + ((NodoDeclaracion)nodo).getTipo() + ")");
			}else if (nodo instanceof NodoSubrutina){
				generarSubrutina((NodoSubrutina)nodo);
			}else if (nodo instanceof NodoReturn){
				generarReturn((NodoReturn)nodo);
			}else if (nodo instanceof NodoLlamada){
				generarLlamada((NodoLlamada)nodo);
			}else{
				System.out.println("BUG: Tipo de nodo a generar desconocido: " + nodo.getClass().getSimpleName());
			}
			/* Si el hijo tiene hermano a la derecha lo genero tambien */
			if(nodo.TieneHermano())
				generar(nodo.getHermanoDerecha());
		}else{
			System.out.println("¡ERROR: por favor fije la tabla de simbolos a usar antes de generar codigo objeto!");
		}
	}

	private static void generarIf(NodoBase nodo){
		NodoIf n = (NodoIf)nodo;
		int localidadSaltoElse, localidadSaltoEnd, localidadActual;
		if(UtGen.debug) UtGen.emitirComentario("-> if");
		generar(n.getPrueba());
		localidadSaltoElse = UtGen.emitirSalto(1);
		UtGen.emitirComentario("If: el salto hacia el else debe estar aqui");
		generar(n.getParteThen());
		localidadSaltoEnd = UtGen.emitirSalto(1);
		UtGen.emitirComentario("If: el salto hacia el final debe estar aqui");
		localidadActual = UtGen.emitirSalto(0);
		UtGen.cargarRespaldo(localidadSaltoElse);
		UtGen.emitirRM_Abs("JEQ", UtGen.AC, localidadActual, "if: jmp hacia else");
		UtGen.restaurarRespaldo();
		if(n.getParteElse() != null){
			generar(n.getParteElse());
			localidadActual = UtGen.emitirSalto(0);
			UtGen.cargarRespaldo(localidadSaltoEnd);
			UtGen.emitirRM_Abs("LDA", UtGen.PC, localidadActual, "if: jmp hacia el final");
			UtGen.restaurarRespaldo();
		}
		if(UtGen.debug) UtGen.emitirComentario("<- if");
	}
	
	private static void generarRepeat(NodoBase nodo){
		NodoRepeat n = (NodoRepeat)nodo;
		int localidadSaltoInicio;
		if(UtGen.debug) UtGen.emitirComentario("-> repeat");
		localidadSaltoInicio = UtGen.emitirSalto(0);
		UtGen.emitirComentario("repeat: el salto hacia el final del repeat debe estar aqui");
		generar(n.getCuerpo());
		generar(n.getPrueba());
		UtGen.emitirRM_Abs("JEQ", UtGen.AC, localidadSaltoInicio, "repeat: jmp hacia el inicio del cuerpo");
		if(UtGen.debug) UtGen.emitirComentario("<- repeat");
	}		
	
	private static void generarAsignacion(NodoBase nodo){
		NodoAsignacion n = (NodoAsignacion)nodo;
		if(UtGen.debug) UtGen.emitirComentario("-> asignacion");		
		generar(n.getExpresion());
		if (subrutinaActual != null && subrutinaActual.esLocalOParametro(n.getIdentificador())) {
			int offset = subrutinaActual.getOffset(n.getIdentificador());
			UtGen.emitirRM("ST", UtGen.AC, offset, UtGen.MP, "asig: almacenar en param/local " + n.getIdentificador() + " en " + offset + "(MP)");
		} else {
			int direccion = tablaSimbolos.getDireccion(n.getIdentificador());
			UtGen.emitirRM("ST", UtGen.AC, direccion, UtGen.GP, "asignacion: almaceno el valor para el id "+n.getIdentificador());
		}
		if(UtGen.debug) UtGen.emitirComentario("<- asignacion");
	}

	private static void generarAsignacionArreglo(String identificador, NodoBase indice, NodoBase expresion){
		if(UtGen.debug) UtGen.emitirComentario("-> asignacion arreglo: " + identificador + "[...] :=");
		
		/* 1. Evaluar la expresion del indice -> AC (r0) */
		generar(indice);
		
		/* 2. Obtener direccion base del arreglo */
		int dirBase = tablaSimbolos.getDireccion(identificador);
		
		/* 3. Calcular direccion efectiva: R2 = dirBase + GP + indice */
		UtGen.emitirRM("LDA", UtGen.R2, dirBase, UtGen.GP, "asig arreglo: cargar direccion base de " + identificador + " en R2");
		UtGen.emitirRO("ADD", UtGen.R2, UtGen.R2, UtGen.AC, "asig arreglo: sumar indice para direccion efectiva");
		
		/* 4. Guardar direccion calculada en la pila temporal */
		UtGen.emitirRM("ST", UtGen.R2, desplazamientoTmp--, UtGen.MP, "asig arreglo: push direccion destino en pila temporal");
		
		/* 5. Evaluar expresion de la derecha -> AC (r0) */
		generar(expresion);
		
		/* 6. Recuperar direccion destino en AC1 (r1) */
		UtGen.emitirRM("LD", UtGen.AC1, ++desplazamientoTmp, UtGen.MP, "asig arreglo: pop direccion destino en AC1");
		
		/* 7. Almacenar valor de AC en casilla destino */
		UtGen.emitirRM("ST", UtGen.AC, 0, UtGen.AC1, "asig arreglo: almacenar valor en " + identificador + "[indice]");
		
		if(UtGen.debug) UtGen.emitirComentario("<- asignacion arreglo: " + identificador);
	}

	private static void generarAccesoArreglo(String identificador, NodoBase indice){
		if(UtGen.debug) UtGen.emitirComentario("-> acceso arreglo: " + identificador + "[...]");
		
		/* 1. Evaluar expresion del indice -> AC */
		generar(indice);
		
		/* 2. Obtener direccion base */
		int dirBase = tablaSimbolos.getDireccion(identificador);
		
		/* 3. Calcular direccion efectiva: R2 = dirBase + GP + indice */
		UtGen.emitirRM("LDA", UtGen.R2, dirBase, UtGen.GP, "arreglo: cargar direccion base de " + identificador + " en R2");
		UtGen.emitirRO("ADD", UtGen.R2, UtGen.R2, UtGen.AC, "arreglo: sumar indice para direccion efectiva");
		
		/* 4. Cargar valor de la casilla apuntada por R2 en AC */
		UtGen.emitirRM("LD", UtGen.AC, 0, UtGen.R2, "arreglo: cargar en AC el valor de " + identificador + "[indice]");
		
		if(UtGen.debug) UtGen.emitirComentario("<- acceso arreglo: " + identificador);
	}
	
	private static void generarLeer(NodoBase nodo){
		NodoLeer n = (NodoLeer)nodo;
		if(UtGen.debug) UtGen.emitirComentario("-> leer");
		UtGen.emitirRO("IN", UtGen.AC, 0, 0, "leer: lee un valor entero ");
		if (subrutinaActual != null && subrutinaActual.esLocalOParametro(n.getIdentificador())) {
			int offset = subrutinaActual.getOffset(n.getIdentificador());
			UtGen.emitirRM("ST", UtGen.AC, offset, UtGen.MP, "leer: almaceno en param/local " + n.getIdentificador() + " en " + offset + "(MP)");
		} else {
			int direccion = tablaSimbolos.getDireccion(n.getIdentificador());
			UtGen.emitirRM("ST", UtGen.AC, direccion, UtGen.GP, "leer: almaceno el valor entero leido en el id "+n.getIdentificador());
		}
		if(UtGen.debug) UtGen.emitirComentario("<- leer");
	}
	
	private static void generarEscribir(NodoBase nodo){
		NodoEscribir n = (NodoEscribir)nodo;
		if(UtGen.debug) UtGen.emitirComentario("-> escribir");
		
		// Deteccion si es una expresion de tipo cadena
		boolean esCadena = false;
		if (n.getExpresion() instanceof NodoValor && ((NodoValor) n.getExpresion()).esCadena()) {
			esCadena = true;
		} else if (n.getExpresion() instanceof NodoIdentificador) {
			NodoIdentificador id = (NodoIdentificador) n.getExpresion();
			String tipo = (subrutinaActual != null && subrutinaActual.esLocalOParametro(id.getNombre()))
				? subrutinaActual.getTipo(id.getNombre())
				: tablaSimbolos.getTipo(id.getNombre());
			if ("string".equalsIgnoreCase(tipo)) {
				esCadena = true;
			}
		}
		
		if (esCadena) {
			// Evalua la expresion -> puntero base de la cadena en AC
			generar(n.getExpresion());
			// R2 = direccion absoluta en memoria (AC + GP)
			UtGen.emitirRO("ADD", UtGen.R2, UtGen.AC, UtGen.GP, "escribir string: R2 = dir base en dMem");
			int inicioLoop = UtGen.emitirSalto(0);
			// Cargar caracter: AC = dMem[0(R2)]
			UtGen.emitirRM("LD", UtGen.AC, 0, UtGen.R2, "escribir string: cargar caracter");
			int saltoFin = UtGen.emitirSalto(1);
			UtGen.emitirRO("OUT", UtGen.AC, 0, 0, "escribir string: imprimir caracter");
			UtGen.emitirRM("LDA", UtGen.R2, 1, UtGen.R2, "escribir string: R2++ (siguiente caracter)");
			UtGen.emitirRM_Abs("LDA", UtGen.PC, inicioLoop, "escribir string: loop");
			int finLoop = UtGen.emitirSalto(0);
			UtGen.cargarRespaldo(saltoFin);
			UtGen.emitirRM_Abs("JEQ", UtGen.AC, finLoop, "escribir string: salir si terminador \\0");
			UtGen.restaurarRespaldo();
		} else {
			/* Expresion numerica o booleana normal */
			generar(n.getExpresion());
			UtGen.emitirRO("OUT", UtGen.AC, 0, 0, "escribir: genero la salida de la expresion");
		}
		if(UtGen.debug) UtGen.emitirComentario("<- escribir");
	}
	
	private static void generarValor(NodoBase nodo){
		NodoValor n = (NodoValor)nodo;
		if(UtGen.debug) UtGen.emitirComentario("-> constante");
		UtGen.emitirRM("LDC", UtGen.AC, n.getValor(), 0, "cargar constante: "+n.getValor());
		if(UtGen.debug) UtGen.emitirComentario("<- constante");
	}

	private static void generarBooleanoDirecto(boolean valor){
		if(UtGen.debug) UtGen.emitirComentario("-> booleano");
		int val = valor ? 1 : 0;
		UtGen.emitirRM("LDC", UtGen.AC, val, 0, "cargar booleano: " + (valor ? "true (1)" : "false (0)"));
		if(UtGen.debug) UtGen.emitirComentario("<- booleano");
	}

	private static void generarCadenaLiteral(String cad){
		if(UtGen.debug) UtGen.emitirComentario("-> literal string: \"" + cad + "\"");
		int dir = poolCadenas.getOrDefault(cad, dirSiguienteString);
		UtGen.emitirRM("LDC", UtGen.AC, dir, 0, "cargar puntero base a string \"" + cad + "\"");
		if(UtGen.debug) UtGen.emitirComentario("<- literal string");
	}
	
	private static void generarIdentificador(NodoBase nodo){
		NodoIdentificador n = (NodoIdentificador)nodo;
		if(UtGen.debug) UtGen.emitirComentario("-> identificador");
		if (subrutinaActual != null && subrutinaActual.esLocalOParametro(n.getNombre())) {
			int offset = subrutinaActual.getOffset(n.getNombre());
			UtGen.emitirRM("LD", UtGen.AC, offset, UtGen.MP, "cargar param/local: " + n.getNombre() + " desde " + offset + "(MP)");
		} else {
			int direccion = tablaSimbolos.getDireccion(n.getNombre());
			UtGen.emitirRM("LD", UtGen.AC, direccion, UtGen.GP, "cargar valor de identificador: "+n.getNombre());
		}
		if(UtGen.debug) UtGen.emitirComentario("<- identificador");
	}

	private static void generarOperacion(NodoBase nodo){
		NodoOperacion n = (NodoOperacion) nodo;
		if(UtGen.debug) UtGen.emitirComentario("-> Operacion: " + n.getOperacion());

		// Operacion unaria NOT
		if (n.getOperacion() == tipoOp.no || n.getOperacion() == tipoOp.not) {
			NodoBase op = (n.getOpIzquierdo() != null) ? n.getOpIzquierdo() : n.getOpDerecho();
			generar(op);
			UtGen.emitirRM("JEQ", UtGen.AC, 2, UtGen.PC, "not: si AC==0 voy a verdadero");
			UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "not: caso de falso (AC=0)");
			UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "not: salto incondicional evitando verdadero");
			UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "not: caso de verdadero (AC=1)");
			if(UtGen.debug) UtGen.emitirComentario("<- Operacion: " + n.getOperacion());
			return;
		}

		/* Operaciones binarias */
		generar(n.getOpIzquierdo());
		UtGen.emitirRM("ST", UtGen.AC, desplazamientoTmp--, UtGen.MP, "op: push en la pila tmp el resultado expresion izquierda");
		generar(n.getOpDerecho());
		UtGen.emitirRM("LD", UtGen.AC1, ++desplazamientoTmp, UtGen.MP, "op: pop o cargo de la pila el valor izquierdo en AC1");
		
		switch(n.getOperacion()){
			case mas:
				UtGen.emitirRO("ADD", UtGen.AC, UtGen.AC1, UtGen.AC, "op: +");		
				break;
			case menos:
				UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: -");
				break;
			case por:
				UtGen.emitirRO("MUL", UtGen.AC, UtGen.AC1, UtGen.AC, "op: *");
				break;
			case entre:
				UtGen.emitirRO("DIV", UtGen.AC, UtGen.AC1, UtGen.AC, "op: /");
				break;		
				
			case menor:
				UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: < (AC1 - AC)");
				UtGen.emitirRM("JLT", UtGen.AC, 2, UtGen.PC, "voy dos instrucciones mas alla if verdadero (AC<0)");
				UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
				UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incondicional a direccion: PC+1 (es falso evito colocarlo verdadero)");
				UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
				break;

			case mayor:
				UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: > (AC1 - AC)");
				UtGen.emitirRM("JGT", UtGen.AC, 2, UtGen.PC, "voy dos instrucciones mas alla if verdadero (AC>0)");
				UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
				UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incondicional evitando verdadero");
				UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
				break;

			case menor_igual:
			case menorIgual:
				UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: <= (AC1 - AC)");
				UtGen.emitirRM("JLE", UtGen.AC, 2, UtGen.PC, "voy dos instrucciones mas alla if verdadero (AC<=0)");
				UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
				UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incondicional evitando verdadero");
				UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
				break;

			case mayor_igual:
			case mayorIgual:
				UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: >= (AC1 - AC)");
				UtGen.emitirRM("JGE", UtGen.AC, 2, UtGen.PC, "voy dos instrucciones mas alla if verdadero (AC>=0)");
				UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
				UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incondicional evitando verdadero");
				UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
				break;
				
			case igual:
				UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: == (AC1 - AC)");
				UtGen.emitirRM("JEQ", UtGen.AC, 2, UtGen.PC, "voy dos instrucciones mas alla if verdadero (AC==0)");
				UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
				UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incondicional a direccion: PC+1 (es falso evito colocarlo verdadero)");
				UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
				break;

			case distinto:
			case diferente:
				UtGen.emitirRO("SUB", UtGen.AC, UtGen.AC1, UtGen.AC, "op: != (AC1 - AC)");
				UtGen.emitirRM("JNE", UtGen.AC, 2, UtGen.PC, "voy dos instrucciones mas alla if verdadero (AC!=0)");
				UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
				UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incondicional evitando verdadero");
				UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
				break;

			case y:
			case and:
				UtGen.emitirRO("MUL", UtGen.AC, UtGen.AC1, UtGen.AC, "op: and");
				UtGen.emitirRM("JNE", UtGen.AC, 2, UtGen.PC, "and: si resultado!=0 es verdadero");
				UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
				UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incondicional evitando verdadero");
				UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
				break;

			case o:
			case or:
				UtGen.emitirRO("ADD", UtGen.AC, UtGen.AC1, UtGen.AC, "op: or");
				UtGen.emitirRM("JNE", UtGen.AC, 2, UtGen.PC, "or: si AC!=0 (al menos uno true) es verdadero");
				UtGen.emitirRM("LDC", UtGen.AC, 0, UtGen.AC, "caso de falso (AC=0)");
				UtGen.emitirRM("LDA", UtGen.PC, 1, UtGen.PC, "Salto incondicional evitando verdadero");
				UtGen.emitirRM("LDC", UtGen.AC, 1, UtGen.AC, "caso de verdadero (AC=1)");
				break;

			default:
				UtGen.emitirComentario("BUG: tipo de operacion desconocida: " + n.getOperacion());
		}
		if(UtGen.debug) UtGen.emitirComentario("<- Operacion: " + n.getOperacion());
	}
	
	// Preludio estandar: Inicializacion de memoria de ejecucion y carga de literales de cadena
	private static void generarPreludioEstandar(){
		UtGen.emitirComentario("Compilacion TINY++ para el codigo objeto TM");
		UtGen.emitirComentario("Archivo generado");
		UtGen.emitirComentario("Preludio estandar:");
		UtGen.emitirRM("LD", UtGen.MP, 0, UtGen.AC, "cargar la maxima direccion desde la localidad 0");
		UtGen.emitirRM("ST", UtGen.AC, 0, UtGen.AC, "limpio el registro de la localidad 0");
		
		// Carga inicial de literales de cadena en dMem (Mision 3: Cadenas de Texto)
		if (!poolCadenas.isEmpty()) {
			UtGen.emitirComentario("--- CARGA INICIAL DE LITERALES DE CADENA EN dMem ---");
			for (Map.Entry<String, Integer> entry : poolCadenas.entrySet()) {
				String cad = entry.getKey();
				int dir = entry.getValue();
				UtGen.emitirComentario("Cadena: \"" + cad + "\" en dMem[" + dir + ".." + (dir + cad.length()) + "]");
				for (int i = 0; i < cad.length(); i++) {
					char c = cad.charAt(i);
					UtGen.emitirRM("LDC", UtGen.AC, (int) c, 0, "caracter '" + c + "' (ASCII " + (int) c + ")");
					UtGen.emitirRM("ST", UtGen.AC, dir + i, UtGen.GP, "almacenar en dMem[" + (dir + i) + "]");
				}
				// Terminador nulo 0
				UtGen.emitirRM("LDC", UtGen.AC, 0, 0, "terminador nulo \\0 (0)");
				UtGen.emitirRM("ST", UtGen.AC, dir + cad.length(), UtGen.GP, "almacenar fin de cadena");
			}
			UtGen.emitirComentario("--- FIN CARGA DE LITERALES DE CADENA ---");
		}
	}

	/* =========================================================================
	 * MISION 4: SUBRUTINAS, FUNCIONES, LLAMADAS Y MARCOS DE PILA (STACK FRAMES)
	 * ========================================================================= */

	private static void generarSubrutina(NodoSubrutina n){
		String nombre = n.getIdentificador();
		if(UtGen.debug) UtGen.emitirComentario("-> definicion de subrutina: " + nombre);

		InfoSubrutina info = tablaSimbolos != null ? tablaSimbolos.getSubrutina(nombre) : null;
		if (info == null) {
			info = new InfoSubrutina(nombre, n.getTipoRetorno());
			NodoBase p = n.getParametros();
			while (p != null) {
				if (p instanceof NodoDeclaracion) {
					NodoDeclaracion dec = (NodoDeclaracion) p;
					info.agregarParametro(dec.getIdentificador(), dec.getTipo());
				}
				p = p.getHermanoDerecha();
			}
			if (tablaSimbolos != null) tablaSimbolos.registrarSubrutina(info);
		}

		// 1. Salto incondicional sobre el cuerpo de la subrutina para evitar ejecucion secuencial
		int saltoSobre = UtGen.emitirSalto(1);

		// 2. Punto de entrada de la subrutina en iMem
		int dirEntrada = UtGen.emitirSalto(0);
		info.setDireccionEntrada(dirEntrada);
		UtGen.emitirComentario("=== ENTRADA SUBRUTINA: " + nombre + " en iMem[" + dirEntrada + "] ===");

		// 3. Resolver llamadas previas que estaban pendientes (forward calls)
		for (int dirSaltoPendiente : info.getLlamadasPendientes()) {
			UtGen.cargarRespaldo(dirSaltoPendiente);
			UtGen.emitirRM_Abs("LDA", UtGen.PC, dirEntrada, "call (backpatch): saltar a subrutina " + nombre);
			UtGen.restaurarRespaldo();
		}
		info.getLlamadasPendientes().clear();

		// 4. Cambiar contexto de subrutina y preparar pila
		InfoSubrutina anterior = subrutinaActual;
		subrutinaActual = info;
		int tmpAnterior = desplazamientoTmp;
		desplazamientoTmp = -(info.getCantidadLocales() + 1);

		// 5. Generar cuerpo de la subrutina
		generar(n.getCuerpo());

		// 6. Epilogo por defecto al final del cuerpo
		UtGen.emitirComentario("Epilogo por defecto de subrutina: " + nombre);
		UtGen.emitirRM("LD", UtGen.AC1, 0, UtGen.MP, "epilogo: cargar direccion de retorno desde 0(MP)");
		UtGen.emitirRM("LD", UtGen.MP, 1, UtGen.MP, "epilogo: restaurar MP del llamador desde 1(MP)");
		UtGen.emitirRM("LDA", UtGen.PC, 0, UtGen.AC1, "epilogo: saltar a la direccion de retorno");

		// 7. Restaurar contexto
		subrutinaActual = anterior;
		desplazamientoTmp = tmpAnterior;

		// 8. Reajustar el salto que salta sobre el cuerpo de la subrutina
		int dirDespues = UtGen.emitirSalto(0);
		UtGen.cargarRespaldo(saltoSobre);
		UtGen.emitirRM_Abs("LDA", UtGen.PC, dirDespues, "salto sobre subrutina: " + nombre);
		UtGen.restaurarRespaldo();

		if(UtGen.debug) UtGen.emitirComentario("<- definicion de subrutina: " + nombre);
	}

	private static void generarReturn(NodoReturn n){
		if(UtGen.debug) UtGen.emitirComentario("-> return");
		if (n.tieneExpresion()) {
			generar(n.getExpresion()); // El resultado queda en AC (r0)
		}
		// Emitir epilogo inmediato de retorno
		UtGen.emitirRM("LD", UtGen.AC1, 0, UtGen.MP, "return: cargar direccion de retorno desde 0(MP)");
		UtGen.emitirRM("LD", UtGen.MP, 1, UtGen.MP, "return: restaurar MP del llamador desde 1(MP)");
		UtGen.emitirRM("LDA", UtGen.PC, 0, UtGen.AC1, "return: saltar a direccion de retorno");
		if(UtGen.debug) UtGen.emitirComentario("<- return");
	}

	private static void generarLlamada(NodoLlamada n){
		String nombre = n.getIdentificador();
		if(UtGen.debug) UtGen.emitirComentario("-> llamada a subrutina: " + nombre);

		InfoSubrutina info = tablaSimbolos != null ? tablaSimbolos.getSubrutina(nombre) : null;
		int tmpAnterior = desplazamientoTmp;

		// 1. Evaluar y apilar argumentos secuencialmente
		NodoBase arg = n.getArgumentos();
		int argCount = 0;
		while (arg != null) {
			NodoBase siguiente = arg.getHermanoDerecha();
			arg.setHermanoDerecha(null); // Desconectar temporalmente para aislar la expresion de este argumento
			generar(arg); // Resultado en AC
			arg.setHermanoDerecha(siguiente); // Restaurar
			UtGen.emitirRM("ST", UtGen.AC, desplazamientoTmp--, UtGen.MP, "call: push argumento " + argCount + " para " + nombre);
			argCount++;
			arg = siguiente;
		}

		// 2. Apilar antiguo MP (Dynamic Link)
		UtGen.emitirRM("ST", UtGen.MP, desplazamientoTmp--, UtGen.MP, "call: push antiguo MP (enlace dinamico)");

		// 3. Reservar slot y apilar direccion de retorno
		int instCargarRet = UtGen.emitirSalto(1); // placeholder para LDA AC, retAddr(PC)
		UtGen.emitirRM("ST", UtGen.AC, desplazamientoTmp--, UtGen.MP, "call: push direccion de retorno en el nuevo marco");

		// 4. Actualizar MP para que apunte al slot de direccion de retorno del nuevo marco
		UtGen.emitirRM("LDA", UtGen.MP, desplazamientoTmp + 1, UtGen.MP, "call: actualizar MP al nuevo marco de activacion");

		// 5. Emitir salto al inicio de la subrutina
		int instSaltoSub = UtGen.emitirSalto(1);

		// 6. Localidad de retorno (donde continua el llamador)
		int dirRetorno = UtGen.emitirSalto(0);

		// 7. Parchear la carga de la direccion de retorno
		UtGen.cargarRespaldo(instCargarRet);
		UtGen.emitirRM_Abs("LDA", UtGen.AC, dirRetorno, "call: cargar direccion de retorno en AC");
		UtGen.restaurarRespaldo();

		// 8. Parchear el salto a la subrutina
		if (info != null && info.getDireccionEntrada() != -1) {
			UtGen.cargarRespaldo(instSaltoSub);
			UtGen.emitirRM_Abs("LDA", UtGen.PC, info.getDireccionEntrada(), "call: saltar a subrutina " + nombre);
			UtGen.restaurarRespaldo();
		} else {
			// Subrutina aun no emitida (llamada hacia adelante)
			if (info == null) {
				info = new InfoSubrutina(nombre, null);
				if (tablaSimbolos != null) tablaSimbolos.registrarSubrutina(info);
			}
			info.agregarLlamadaPendiente(instSaltoSub);
		}

		// 9. Restaurar desplazamiento temporal del llamador
		desplazamientoTmp = tmpAnterior;

		if(UtGen.debug) UtGen.emitirComentario("<- llamada a subrutina: " + nombre);
	}

}

