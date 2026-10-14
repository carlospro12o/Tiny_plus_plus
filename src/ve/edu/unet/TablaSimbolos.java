package ve.edu.unet;

import ve.edu.unet.nodosAST.*;

import java.util.*;

public class TablaSimbolos {
	private HashMap<String, RegistroSimbolo> tabla;
	private HashMap<String, InfoSubrutina> subrutinas;
	private int direccion;  // Contador de las localidades de memoria asignadas a la tabla
	
	public TablaSimbolos() {
		super();
		tabla = new HashMap<String, RegistroSimbolo>();
		subrutinas = new LinkedHashMap<String, InfoSubrutina>();
		direccion = 0;
	}

	public void cargarTabla(NodoBase raiz){
		while (raiz != null) {
			if (raiz instanceof NodoDeclaracion){
				NodoDeclaracion dec = (NodoDeclaracion) raiz;
				int tam = dec.esArreglo() ? dec.getTamanoArreglo() : 1;
				InsertarSimbolo(dec.getIdentificador(), -1, dec.getTipo(), tam);
			}
			else if (raiz instanceof NodoSubrutina){
				NodoSubrutina sub = (NodoSubrutina) raiz;
				InfoSubrutina info = new InfoSubrutina(sub.getIdentificador(), sub.getTipoRetorno());
				
				// Registrar parametros de la subrutina
				NodoBase p = sub.getParametros();
				while (p != null) {
					if (p instanceof NodoDeclaracion) {
						NodoDeclaracion dec = (NodoDeclaracion) p;
						info.agregarParametro(dec.getIdentificador(), dec.getTipo());
					}
					p = p.getHermanoDerecha();
				}
				
				// Recolectar variables locales del cuerpo
				recolectarLocales(sub.getCuerpo(), info);
				
				registrarSubrutina(info);
				InsertarSimbolo(sub.getIdentificador(), -1, sub.esFuncion() ? sub.getTipoRetorno() : "void", 0);
				
				// Procesar cuerpo para recolectar referencias a variables globales o llamadas
				cargarTablaSubrutina(sub.getCuerpo(), info);
			}
			else if (raiz instanceof NodoIdentificador){
				NodoIdentificador id = (NodoIdentificador) raiz;
				if (!tabla.containsKey(id.getNombre())) {
					InsertarSimbolo(id.getNombre(), -1, "int", 1);
				}
			}
			else if (raiz instanceof NodoAsignacion){
				NodoAsignacion asig = (NodoAsignacion) raiz;
				if (!tabla.containsKey(asig.getIdentificador())) {
					InsertarSimbolo(asig.getIdentificador(), -1, "int", 1);
				}
			}

			/* Recorrido recursivo */
			if (raiz instanceof NodoIf){
				cargarTabla(((NodoIf)raiz).getPrueba());
				cargarTabla(((NodoIf)raiz).getParteThen());
				if(((NodoIf)raiz).getParteElse()!=null){
					cargarTabla(((NodoIf)raiz).getParteElse());
				}
			}
			else if (raiz instanceof NodoRepeat){
				cargarTabla(((NodoRepeat)raiz).getCuerpo());
				cargarTabla(((NodoRepeat)raiz).getPrueba());
			}
			else if (raiz instanceof NodoAsignacion){
				if (((NodoAsignacion)raiz).esArreglo())
					cargarTabla(((NodoAsignacion)raiz).getIndice());
				cargarTabla(((NodoAsignacion)raiz).getExpresion());
			}
			else if (raiz instanceof NodoEscribir){
				cargarTabla(((NodoEscribir)raiz).getExpresion());
			}
			else if (raiz instanceof NodoOperacion){
				cargarTabla(((NodoOperacion)raiz).getOpIzquierdo());
				if (((NodoOperacion)raiz).getOpDerecho() != null)
					cargarTabla(((NodoOperacion)raiz).getOpDerecho());
			}
			else if (raiz instanceof NodoLlamada){
				cargarTabla(((NodoLlamada)raiz).getArgumentos());
			}
			else if (raiz instanceof NodoReturn){
				if (((NodoReturn)raiz).tieneExpresion())
					cargarTabla(((NodoReturn)raiz).getExpresion());
			}
			raiz = raiz.getHermanoDerecha();
		}
	}

	private void recolectarLocales(NodoBase cuerpo, InfoSubrutina info) {
		while (cuerpo != null) {
			if (cuerpo instanceof NodoDeclaracion) {
				NodoDeclaracion dec = (NodoDeclaracion) cuerpo;
				info.agregarLocal(dec.getIdentificador(), dec.getTipo());
			} else if (cuerpo instanceof NodoIf) {
				recolectarLocales(((NodoIf) cuerpo).getParteThen(), info);
				if (((NodoIf) cuerpo).getParteElse() != null) recolectarLocales(((NodoIf) cuerpo).getParteElse(), info);
			} else if (cuerpo instanceof NodoRepeat) {
				recolectarLocales(((NodoRepeat) cuerpo).getCuerpo(), info);
			}
			cuerpo = cuerpo.getHermanoDerecha();
		}
	}

	private void cargarTablaSubrutina(NodoBase nodo, InfoSubrutina info) {
		while (nodo != null) {
			if (nodo instanceof NodoIdentificador) {
				NodoIdentificador id = (NodoIdentificador) nodo;
				// Si no es parametro ni local, es variable global
				if (!info.esLocalOParametro(id.getNombre()) && !tabla.containsKey(id.getNombre())) {
					InsertarSimbolo(id.getNombre(), -1, "int", 1);
				}
				if (id.esArreglo()) {
					cargarTablaSubrutina(id.getIndice(), info);
				}
			} else if (nodo instanceof NodoAsignacion) {
				NodoAsignacion asig = (NodoAsignacion) nodo;
				if (!info.esLocalOParametro(asig.getIdentificador()) && !tabla.containsKey(asig.getIdentificador())) {
					InsertarSimbolo(asig.getIdentificador(), -1, "int", 1);
				}
				if (asig.esArreglo()) {
					cargarTablaSubrutina(asig.getIndice(), info);
				}
				cargarTablaSubrutina(asig.getExpresion(), info);
			} else if (nodo instanceof NodoIf) {
				cargarTablaSubrutina(((NodoIf) nodo).getPrueba(), info);
				cargarTablaSubrutina(((NodoIf) nodo).getParteThen(), info);
				if (((NodoIf) nodo).getParteElse() != null) {
					cargarTablaSubrutina(((NodoIf) nodo).getParteElse(), info);
				}
			} else if (nodo instanceof NodoRepeat) {
				cargarTablaSubrutina(((NodoRepeat) nodo).getCuerpo(), info);
				cargarTablaSubrutina(((NodoRepeat) nodo).getPrueba(), info);
			} else if (nodo instanceof NodoEscribir) {
				cargarTablaSubrutina(((NodoEscribir) nodo).getExpresion(), info);
			} else if (nodo instanceof NodoOperacion) {
				cargarTablaSubrutina(((NodoOperacion) nodo).getOpIzquierdo(), info);
				if (((NodoOperacion) nodo).getOpDerecho() != null) {
					cargarTablaSubrutina(((NodoOperacion) nodo).getOpDerecho(), info);
				}
			} else if (nodo instanceof NodoLlamada) {
				cargarTablaSubrutina(((NodoLlamada) nodo).getArgumentos(), info);
			} else if (nodo instanceof NodoReturn) {
				if (((NodoReturn) nodo).tieneExpresion()) {
					cargarTablaSubrutina(((NodoReturn) nodo).getExpresion(), info);
				}
			}
			nodo = nodo.getHermanoDerecha();
		}
	}
	
	// Insercion con tipo y tamano
	public boolean InsertarSimbolo(String identificador, int numLinea, String tipo, int tamano){
		if(tabla.containsKey(identificador)){
			return false;
		}else{
			RegistroSimbolo simbolo = new RegistroSimbolo(identificador, numLinea, direccion, tipo, tamano);
			tabla.put(identificador, simbolo);
			direccion += tamano;
			return true;			
		}
	}

	// Metodo original para compatibilidad
	public boolean InsertarSimbolo(String identificador, int numLinea){
		return InsertarSimbolo(identificador, numLinea, "int", 1);
	}
	
	public RegistroSimbolo BuscarSimbolo(String identificador){
		return tabla.get(identificador);
	}
	
	public void registrarSubrutina(InfoSubrutina info) {
		subrutinas.put(info.getNombre(), info);
	}

	public InfoSubrutina getSubrutina(String nombre) {
		return subrutinas.get(nombre);
	}

	public boolean esSubrutina(String nombre) {
		return subrutinas.containsKey(nombre);
	}

	public Map<String, InfoSubrutina> getSubrutinas() {
		return subrutinas;
	}

	public void ImprimirClaves(){
		System.out.println("*** Tabla de Simbolos ***");
		for(Iterator<String> it = tabla.keySet().iterator(); it.hasNext();) { 
			String s = it.next();
			RegistroSimbolo reg = BuscarSimbolo(s);
			System.out.println("Key: " + s + " | Tipo: " + reg.getTipo() + " | Tam: " + reg.getTamano() + " | Dir: " + reg.getDireccionMemoria());
		}
		if (!subrutinas.isEmpty()) {
			System.out.println("*** Subrutinas Registradas ***");
			for (InfoSubrutina s : subrutinas.values()) {
				System.out.println("Subrutina: " + s.getNombre() + " | Tipo Retorno: " + (s.esFuncion() ? s.getTipoRetorno() : "void") + " | Cantidad Params: " + s.getCantidadParametros());
			}
		}
	}

	public int getDireccion(String clave){
		RegistroSimbolo s = BuscarSimbolo(clave);
		return (s != null) ? s.getDireccionMemoria() : 0;
	}

	public String getTipo(String clave){
		RegistroSimbolo s = BuscarSimbolo(clave);
		return (s != null) ? s.getTipo() : "int";
	}

	public int getTamano(String clave){
		RegistroSimbolo s = BuscarSimbolo(clave);
		return (s != null) ? s.getTamano() : 1;
	}

	public int getSiguienteDireccionDisponible(){
		return direccion;
	}
}
