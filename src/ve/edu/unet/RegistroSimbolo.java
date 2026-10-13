package ve.edu.unet;

public class RegistroSimbolo {
	private String identificador;
	private int NumLinea;
	private int DireccionMemoria;
	private String tipo; // "int", "bool", "string"
	private int tamano;  // 1 para variables simples, N para arreglos

	public RegistroSimbolo(String identificador, int numLinea, int direccionMemoria) {
		this(identificador, numLinea, direccionMemoria, "int", 1);
	}

	public RegistroSimbolo(String identificador, int numLinea, int direccionMemoria, String tipo, int tamano) {
		super();
		this.identificador = identificador;
		this.NumLinea = numLinea;
		this.DireccionMemoria = direccionMemoria;
		this.tipo = (tipo != null) ? tipo : "int";
		this.tamano = (tamano > 0) ? tamano : 1;
	}

	public String getIdentificador() {
		return identificador;
	}

	public int getNumLinea() {
		return NumLinea;
	}

	public int getDireccionMemoria() {
		return DireccionMemoria;
	}

	public void setDireccionMemoria(int direccionMemoria) {
		DireccionMemoria = direccionMemoria;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public int getTamano() {
		return tamano;
	}

	public void setTamano(int tamano) {
		this.tamano = tamano;
	}
}
