package ve.edu.unet.nodosAST;

public class NodoIdentificador extends NodoBase {
	private String nombre;
	private NodoBase indice; // null si es variable normal o expresion si es arreglo tipo A[i]

	public NodoIdentificador(String nombre) {
		super();
		this.nombre = nombre;
		this.indice = null;
	}

	public NodoIdentificador(String nombre, int linea) {
		super(linea);
		this.nombre = nombre;
		this.indice = null;
	}

	public NodoIdentificador(String nombre, NodoBase indice) {
		super();
		this.nombre = nombre;
		this.indice = indice;
	}

	public NodoIdentificador(String nombre, NodoBase indice, int linea) {
		super(linea);
		this.nombre = nombre;
		this.indice = indice;
	}

	public NodoIdentificador() {
		super();
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public NodoBase getIndice() {
		return indice;
	}

	public void setIndice(NodoBase indice) {
		this.indice = indice;
	}

	public boolean esArreglo() {
		return this.indice != null;
	}

}
