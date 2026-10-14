package ve.edu.unet;

import java.util.ArrayList;
import java.util.List;

public class RegistroSimbolo {
	private String identificador;
	private String tipo;           // "int", "bool", "string", "array", "function", "procedure"
	private String tipoBase;       // Para arreglos: tipo de los elementos ("int", "bool", "string")
	private int tamano;            // 1 para variables simples, N para arreglos
	private Integer tamanoArreglo; // Tamaño del arreglo si tipo == "array", o null
	private String tipoRetorno;    // Tipo retornado si es funcion, null si es procedimiento o variable
	private int NumLinea;
	private int DireccionMemoria;
	private List<RegistroSimbolo> parametros; // Lista de parametros formales si es funcion/procedimiento
	
	// Constructor original para compatibilidad
	public RegistroSimbolo(String identificador, int numLinea, int direccionMemoria) {
		this(identificador, numLinea, direccionMemoria, "int", 1);
	}

	// Constructor con tipo y tamano (usado por backend y TestBackend)
	public RegistroSimbolo(String identificador, int numLinea, int direccionMemoria, String tipo, int tamano) {
		super();
		this.identificador = identificador;
		this.NumLinea = numLinea;
		this.DireccionMemoria = direccionMemoria;
		this.tamano = (tamano > 0) ? tamano : 1;
		if (tamano > 1 || "array".equalsIgnoreCase(tipo)) {
			this.tipo = "array";
			this.tipoBase = (tipo != null && !tipo.equalsIgnoreCase("array")) ? tipo : "int";
			this.tamanoArreglo = tamano;
		} else {
			this.tipo = (tipo != null) ? tipo : "int";
			this.tipoBase = null;
			this.tamanoArreglo = null;
		}
		this.tipoRetorno = null;
		this.parametros = new ArrayList<RegistroSimbolo>();
	}

	// Constructor para variables simples (int x, bool flag, string s)
	public RegistroSimbolo(String identificador, String tipo, int numLinea, int direccionMemoria) {
		this(identificador, numLinea, direccionMemoria, tipo, 1);
	}

	// Constructor para arreglos (int A[10])
	public RegistroSimbolo(String identificador, String tipoBase, int tamanoArreglo, int numLinea, int direccionMemoria) {
		this(identificador, numLinea, direccionMemoria, tipoBase, tamanoArreglo);
		this.tipo = "array";
		this.tipoBase = tipoBase;
		this.tamanoArreglo = tamanoArreglo;
		this.tamano = tamanoArreglo;
	}

	// Constructor para funciones y procedimientos
	public RegistroSimbolo(String identificador, String tipoRetorno, List<RegistroSimbolo> parametros, int numLinea, int direccionMemoria) {
		this(identificador, numLinea, direccionMemoria);
		this.tipoRetorno = tipoRetorno;
		this.tipo = (tipoRetorno != null) ? "function" : "procedure";
		this.parametros = (parametros != null) ? parametros : new ArrayList<RegistroSimbolo>();
		this.tamano = 0;
		this.tamanoArreglo = null;
	}

	public String getIdentificador() {
		return identificador;
	}

	public void setIdentificador(String identificador) {
		this.identificador = identificador;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getTipoBase() {
		return tipoBase;
	}

	public void setTipoBase(String tipoBase) {
		this.tipoBase = tipoBase;
	}

	public int getTamano() {
		return (tamanoArreglo != null && tamanoArreglo > 0) ? tamanoArreglo : (tamano > 0 ? tamano : 1);
	}

	public void setTamano(int tamano) {
		this.tamano = tamano;
		if (tamano > 1) {
			this.tamanoArreglo = tamano;
			if (!"array".equals(this.tipo)) {
				this.tipoBase = this.tipo;
				this.tipo = "array";
			}
		}
	}

	public Integer getTamanoArreglo() {
		return (tamanoArreglo != null) ? tamanoArreglo : tamano;
	}

	public void setTamanoArreglo(Integer tamanoArreglo) {
		this.tamanoArreglo = tamanoArreglo;
		if (tamanoArreglo != null) {
			this.tamano = tamanoArreglo;
		}
	}

	public String getTipoRetorno() {
		return tipoRetorno;
	}

	public void setTipoRetorno(String tipoRetorno) {
		this.tipoRetorno = tipoRetorno;
	}

	public int getNumLinea() {
		return NumLinea;
	}

	public void setNumLinea(int numLinea) {
		NumLinea = numLinea;
	}

	public int getDireccionMemoria() {
		return DireccionMemoria;
	}

	public void setDireccionMemoria(int direccionMemoria) {
		DireccionMemoria = direccionMemoria;
	}

	public List<RegistroSimbolo> getParametros() {
		return parametros;
	}

	public void setParametros(List<RegistroSimbolo> parametros) {
		this.parametros = parametros;
	}

	public boolean esArreglo() {
		return "array".equals(tipo) || tamanoArreglo != null || tamano > 1;
	}

	public boolean esFuncion() {
		return "function".equals(tipo) || (tipoRetorno != null && !"void".equalsIgnoreCase(tipoRetorno));
	}

	public boolean esProcedimiento() {
		return "procedure".equals(tipo) || "void".equalsIgnoreCase(tipoRetorno);
	}

	public boolean esVariable() {
		return !esArreglo() && !esFuncion() && !esProcedimiento();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ID: ").append(identificador);
		if (esArreglo()) {
			sb.append(" | Tipo: ARREGLO de ").append(tipoBase != null ? tipoBase : "int").append("[").append(getTamano()).append("]");
			sb.append(" | DirMem: ").append(DireccionMemoria);
		} else if (esFuncion()) {
			sb.append(" | Tipo: FUNCION -> retorna ").append(tipoRetorno);
			sb.append(" | Params: ").append(formatearParams());
		} else if (esProcedimiento()) {
			sb.append(" | Tipo: PROCEDIMIENTO");
			sb.append(" | Params: ").append(formatearParams());
		} else {
			sb.append(" | Tipo: ").append(tipo != null ? tipo.toUpperCase() : "INT");
			sb.append(" | DirMem: ").append(DireccionMemoria);
		}
		sb.append(" | Linea: ").append(NumLinea);
		return sb.toString();
	}

	private String formatearParams() {
		if (parametros == null || parametros.isEmpty()) return "ninguno";
		StringBuilder sb = new StringBuilder("(");
		for (int i = 0; i < parametros.size(); i++) {
			RegistroSimbolo p = parametros.get(i);
			sb.append(p.getTipo()).append(" ").append(p.getIdentificador());
			if (i < parametros.size() - 1) sb.append(", ");
		}
		sb.append(")");
		return sb.toString();
	}
}
