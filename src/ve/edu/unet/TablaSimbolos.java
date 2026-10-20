package ve.edu.unet;

import ve.edu.unet.nodosAST.*;
import java.util.*;

public class TablaSimbolos {
	private Scope scopeGlobal;
	private Scope scopeActual;
	private Stack<Scope> pilaScopes;
	private List<Scope> todosLosScopes;

	// Registro de subrutinas con metadata para generacion de marcos de pila en la TM
	private Map<String, InfoSubrutina> subrutinas;

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
		this.subrutinas = new LinkedHashMap<String, InfoSubrutina>();
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

	// Inserta una funcion o procedimiento en el ambito actual y registra su InfoSubrutina
	public boolean insertarSubrutina(String identificador, String tipoRetorno, List<RegistroSimbolo> parametros, int numLinea) {
		RegistroSimbolo simbolo = new RegistroSimbolo(identificador, tipoRetorno, parametros, numLinea, 0);
		boolean insertado = scopeActual.insertar(simbolo);
		if (!subrutinas.containsKey(identificador)) {
			InfoSubrutina info = new InfoSubrutina(identificador, tipoRetorno);
			if (parametros != null) {
				for (RegistroSimbolo p : parametros) {
					info.agregarParametro(p.getIdentificador(), p.getTipo());
				}
			}
			subrutinas.put(identificador, info);
		}
		return insertado;
	}

	// Metodos de insercion con tipo y tamano (para backend y TestBackend)
	public boolean InsertarSimbolo(String identificador, int numLinea, String tipo, int tamano) {
		if (tamano > 1 || "array".equalsIgnoreCase(tipo)) {
			return insertarArreglo(identificador, tipo, tamano, numLinea);
		} else {
			return insertarVariable(identificador, tipo, numLinea);
		}
	}

	// Metodo original para compatibilidad
	public boolean InsertarSimbolo(String identificador, int numLinea) {
		return InsertarSimbolo(identificador, numLinea, "int", 1);
	}

	// Busca un simbolo desde el ambito actual subiendo por los padres
	public RegistroSimbolo buscar(String identificador) {
		return scopeActual.buscar(identificador);
	}

	// Metodo BuscarSimbolo compatible con Generador
	public RegistroSimbolo BuscarSimbolo(String identificador) {
		return buscar(identificador);
	}

	// Busca un simbolo unicamente en el ambito actual (util para detectar redeclaraciones)
	public RegistroSimbolo buscarEnScopeActual(String identificador) {
		return scopeActual.buscarLocal(identificador);
	}

	// Busca un simbolo en el ambito global
	public RegistroSimbolo buscarGlobal(String identificador) {
		return scopeGlobal.buscarLocal(identificador);
	}

	// Gestion de subrutinas (Mision 4 TM)
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

	public int getDireccion(String clave) {
		RegistroSimbolo s = buscar(clave);
		return (s != null) ? s.getDireccionMemoria() : 0;
	}

	public String getTipo(String clave) {
		RegistroSimbolo s = buscar(clave);
		if (s == null) return "int";
		if (s.esArreglo() && s.getTipoBase() != null) return s.getTipoBase();
		return (s.getTipo() != null) ? s.getTipo() : "int";
	}

	public int getTamano(String clave) {
		RegistroSimbolo s = buscar(clave);
		return (s != null) ? s.getTamano() : 1;
	}

	public int getSiguienteDireccionDisponible() {
		return direccionGlobal;
	}

	// Recolecta declaraciones locales de una subrutina
	public void recolectarLocales(NodoBase cuerpo, InfoSubrutina info) {
		while (cuerpo != null) {
			if (cuerpo instanceof NodoDeclaracion) {
				NodoDeclaracion dec = (NodoDeclaracion) cuerpo;
				info.agregarLocal(dec.getIdentificador(), dec.getTipo());
			} else if (cuerpo instanceof NodoIf) {
				recolectarLocales(((NodoIf) cuerpo).getParteThen(), info);
				if (((NodoIf) cuerpo).getParteElse() != null) {
					recolectarLocales(((NodoIf) cuerpo).getParteElse(), info);
				}
			} else if (cuerpo instanceof NodoRepeat) {
				recolectarLocales(((NodoRepeat) cuerpo).getCuerpo(), info);
			}
			cuerpo = cuerpo.getHermanoDerecha();
		}
	}

	// Recorre el AST registrando declaraciones y preparando subrutinas
	public void cargarTabla(NodoBase raiz) {
		while (raiz != null) {
			if (raiz instanceof NodoDeclaracion) {
				NodoDeclaracion dec = (NodoDeclaracion) raiz;
				if (dec.esArreglo()) {
					insertarArreglo(dec.getIdentificador(), dec.getTipo(), (dec.getTamanoArreglo() != null) ? dec.getTamanoArreglo() : 1, dec.getLinea());
				} else {
					insertarVariable(dec.getIdentificador(), dec.getTipo(), dec.getLinea());
				}
			} else if (raiz instanceof NodoSubrutina) {
				NodoSubrutina sub = (NodoSubrutina) raiz;
				
				InfoSubrutina info = new InfoSubrutina(sub.getIdentificador(), sub.getTipoRetorno());

				// Extraer lista de parametros formales
				List<RegistroSimbolo> listaParams = new ArrayList<RegistroSimbolo>();
				NodoBase p = sub.getParametros();
				int offsetParam = 0;
				while (p != null) {
					if (p instanceof NodoDeclaracion) {
						NodoDeclaracion dp = (NodoDeclaracion) p;
						listaParams.add(new RegistroSimbolo(dp.getIdentificador(), dp.getTipo(), dp.getLinea(), offsetParam++));
						info.agregarParametro(dp.getIdentificador(), dp.getTipo());
					}
					p = p.getHermanoDerecha();
				}

				// Recolectar variables locales del cuerpo
				recolectarLocales(sub.getCuerpo(), info);
				registrarSubrutina(info);

				// Registrar la subrutina en el ambito actual
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
			} else if (raiz instanceof NodoIdentificador) {
				NodoIdentificador id = (NodoIdentificador) raiz;
				if (buscar(id.getNombre()) == null) {
					InsertarSimbolo(id.getNombre(), -1, "int", 1);
				}
			} else if (raiz instanceof NodoAsignacion) {
				NodoAsignacion asig = (NodoAsignacion) raiz;
				if (buscar(asig.getIdentificador()) == null) {
					InsertarSimbolo(asig.getIdentificador(), -1, "int", 1);
				}
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

	public void ImprimirClaves() {
		System.out.println();
		System.out.println("*********************************************************");
		System.out.println("       TABLA DE SIMBOLOS CON AMBITOS (SCOPES)");
		System.out.println("*********************************************************");
		scopeGlobal.imprimir(0);
		if (!subrutinas.isEmpty()) {
			System.out.println();
			System.out.println("---------------------------------------------------------");
			System.out.println("             SUBRUTINAS REGISTRADAS (TM)");
			System.out.println("---------------------------------------------------------");
			for (InfoSubrutina s : subrutinas.values()) {
				System.out.println("Subrutina: " + s.getNombre() + " | Retorno: " + (s.esFuncion() ? s.getTipoRetorno() : "void") + " | Params: " + s.getCantidadParametros() + " | Locales: " + s.getCantidadLocales());
			}
		}
		System.out.println("*********************************************************");
		System.out.println();
	}
}
