package ve.edu.unet;

import ve.edu.unet.nodosAST.*;

import java.util.*;

public class TablaSimbolos {
	private HashMap<String, RegistroSimbolo> tabla;
	private int direccion;  // Contador de las localidades de memoria asignadas a la tabla
	
	public TablaSimbolos() {
		super();
		tabla = new HashMap<String, RegistroSimbolo>();
		direccion = 0;
	}

	public void cargarTabla(NodoBase raiz){
		while (raiz != null) {
			if (raiz instanceof NodoDeclaracion){
				NodoDeclaracion dec = (NodoDeclaracion) raiz;
				int tam = dec.esArreglo() ? dec.getTamanoArreglo() : 1;
				InsertarSimbolo(dec.getIdentificador(), -1, dec.getTipo(), tam);
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
			else if (raiz instanceof NodoSubrutina){
				cargarTabla(((NodoSubrutina)raiz).getParametros());
				cargarTabla(((NodoSubrutina)raiz).getCuerpo());
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
	
	public void ImprimirClaves(){
		System.out.println("*** Tabla de Simbolos ***");
		for(Iterator<String> it = tabla.keySet().iterator(); it.hasNext();) { 
			String s = it.next();
			RegistroSimbolo reg = BuscarSimbolo(s);
			System.out.println("Key: " + s + " | Tipo: " + reg.getTipo() + " | Tam: " + reg.getTamano() + " | Dir: " + reg.getDireccionMemoria());
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
