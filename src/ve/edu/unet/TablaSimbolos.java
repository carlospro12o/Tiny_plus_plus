package ve.edu.unet;

import ve.edu.unet.nodosAST.*;
import java.util.*;

public class TablaSimbolos {
	private Scope scopeGlobal;
	private Scope scopeActual;
	private Stack<Scope> pilaScopes;
	private List<Scope> todosLosScopes;

	private int direccionGlobal; // Desplazamiento para memoria de datos global (apuntada por GP)
	private int direccionLocal;  // Desplazamiento para marcos de activacion (apuntados por MP)

	public TablaSimbolos() {
		super();
		this.scopeGlobal = new Scope("global", null);
		this.scopeActual = this.scopeGlobal;
		this.pilaScopes = new Stack<Scope>();
		this.pilaScopes.push(this.scopeGlobal);
		this.todosLosScopes = new ArrayList<Scope>();
		this.todosLosScopes.add(this.scopeGlobal);
		this.direccionGlobal = 0;
		this.direccionLocal = 0;
	}

	public Scope getScopeGlobal() {
		return scopeGlobal;
	}

	public Scope getScopeActual() {
		return scopeActual;
	}

	public List<Scope> getTodosLosScopes() {
		return todosLosScopes;
	}

	// Abre un nuevo ambito y lo coloca en el tope de la pila
	public Scope abrirScope(String nombre) {
		Scope nuevo = new Scope(nombre, scopeActual);
		scopeActual.agregarHijo(nuevo);
		pilaScopes.push(nuevo);
		scopeActual = nuevo;
		todosLosScopes.add(nuevo);
		direccionLocal = 0; // Reiniciar desplazamiento para variables locales del nuevo marco
		return nuevo;
	}

	// Cierra el ambito actual y regresa al ambito padre
	public Scope cerrarScope() {
		if (pilaScopes.size() > 1) {
			Scope cerrado = pilaScopes.pop();
			scopeActual = pilaScopes.peek();
			return cerrado;
		}
		return scopeGlobal;
	}

	// Inserta un simbolo en el ambito actual
	public boolean insertar(RegistroSimbolo simbolo) {
		return scopeActual.insertar(simbolo);
	}

	// Inserta una variable simple en el ambito actual
	public boolean insertarVariable(String identificador, String tipo, int numLinea) {
		int dir = scopeActual.esGlobal() ? direccionGlobal++ : direccionLocal++;
		RegistroSimbolo simbolo = new RegistroSimbolo(identificador, tipo, numLinea, dir);
		return scopeActual.insertar(simbolo);
	}

	// Inserta un arreglo en el ambito actual
	public boolean insertarArreglo(String identificador, String tipoBase, int tamano, int numLinea) {
		int dir;
		if (scopeActual.esGlobal()) {
			dir = direccionGlobal;
			direccionGlobal += tamano;
		} else {
			dir = direccionLocal;
			direccionLocal += tamano;
		}
		RegistroSimbolo simbolo = new RegistroSimbolo(identificador, tipoBase, tamano, numLinea, dir);
		return scopeActual.insertar(simbolo);
	}

	// Inserta una funcion o procedimiento en el ambito actual
	public boolean insertarSubrutina(String identificador, String tipoRetorno, List<RegistroSimbolo> parametros, int numLinea) {
		RegistroSimbolo simbolo = new RegistroSimbolo(identificador, tipoRetorno, parametros, numLinea, 0);
		return scopeActual.insertar(simbolo);
	}

	// Busca un simbolo desde el ambito actual subiendo por los padres
	public RegistroSimbolo buscar(String identificador) {
		return scopeActual.buscar(identificador);
	}

	// Busca un simbolo unicamente en el ambito actual (util para detectar redeclaraciones)
	public RegistroSimbolo buscarEnScopeActual(String identificador) {
		return scopeActual.buscarLocal(identificador);
	}

	// Busca un simbolo en el ambito global
	public RegistroSimbolo buscarGlobal(String identificador) {
		return scopeGlobal.buscarLocal(identificador);
	}

	// Recorre el AST registrando las declaraciones en sus ambitos correspondientes
	public void cargarTabla(NodoBase raiz) {
		while (raiz != null) {
			if (raiz instanceof NodoDeclaracion) {
				NodoDeclaracion dec = (NodoDeclaracion) raiz;
				if (dec.esArreglo()) {
					insertarArreglo(dec.getIdentificador(), dec.getTipo(), dec.getTamanoArreglo(), dec.getLinea());
				} else {
					insertarVariable(dec.getIdentificador(), dec.getTipo(), dec.getLinea());
				}
			} else if (raiz instanceof NodoSubrutina) {
				NodoSubrutina sub = (NodoSubrutina) raiz;
				
				// Extraer lista de parametros formales
				List<RegistroSimbolo> listaParams = new ArrayList<RegistroSimbolo>();
				NodoBase p = sub.getParametros();
				int offsetParam = 0;
				while (p != null) {
					if (p instanceof NodoDeclaracion) {
						NodoDeclaracion dp = (NodoDeclaracion) p;
						listaParams.add(new RegistroSimbolo(dp.getIdentificador(), dp.getTipo(), dp.getLinea(), offsetParam++));
					}
					p = p.getHermanoDerecha();
				}

				// Registrar la subrutina en el ambito exterior
				insertarSubrutina(sub.getIdentificador(), sub.getTipoRetorno(), listaParams, sub.getLinea());

				// Abrir el ambito local de la subrutina
				abrirScope(sub.getIdentificador());

				// Registrar cada parametro formal en el nuevo ambito local
				for (RegistroSimbolo param : listaParams) {
					insertar(param);
				}

				// Recorrer el cuerpo de la subrutina
				if (sub.getCuerpo() != null) {
					cargarTabla(sub.getCuerpo());
				}

				// Cerrar el ambito local y volver al anterior
				cerrarScope();
			}

			// Recorrer hijos para bloques de sentencias anidados
			if (raiz instanceof NodoIf) {
				cargarTabla(((NodoIf) raiz).getParteThen());
				if (((NodoIf) raiz).getParteElse() != null) {
					cargarTabla(((NodoIf) raiz).getParteElse());
				}
			} else if (raiz instanceof NodoRepeat) {
				cargarTabla(((NodoRepeat) raiz).getCuerpo());
			}

			raiz = raiz.getHermanoDerecha();
		}
	}

	// Metodos de compatibilidad con Generador.java y codigo original
	public boolean InsertarSimbolo(String identificador, int numLinea) {
		return insertarVariable(identificador, "int", numLinea);
	}

	public RegistroSimbolo BuscarSimbolo(String identificador) {
		return buscar(identificador);
	}

	public int getDireccion(String clave) {
		RegistroSimbolo s = buscar(clave);
		return (s != null) ? s.getDireccionMemoria() : 0;
	}

	public void ImprimirClaves() {
		System.out.println();
		System.out.println("*********************************************************");
		System.out.println("       TABLA DE SIMBOLOS CON AMBITOS (SCOPES)");
		System.out.println("*********************************************************");
		scopeGlobal.imprimir(0);
		System.out.println("*********************************************************");
		System.out.println();
	}
}
