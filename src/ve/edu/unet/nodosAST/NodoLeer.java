package ve.edu.unet.nodosAST;

public class NodoLeer extends NodoBase {
	private String id;

	public NodoLeer(String identificador) {
		super();
		this.id = identificador;
	}

	public NodoLeer(String identificador, int linea) {
		super(linea);
		this.id = identificador;
	}

	public NodoLeer() {
		super();
		id="";
	}

	public String getIdentificador() {
		return id;
	}

	public void setIdentificador(String identificador) {
		this.id = identificador;
	}

	public void setExpresion(String identificador) {
		this.id = identificador;
	}

}
