package ve.edu.unet.nodosAST;

public class NodoValor extends NodoBase {
	// puede guardar entero, booleano o cadena
	private Object valor;

	public NodoValor(Integer valor) {
		super();
		this.valor = valor;
	}

	public NodoValor(Integer valor, int linea) {
		super(linea);
		this.valor = valor;
	}

	public NodoValor(Boolean valor) {
		super();
		this.valor = valor;
	}

	public NodoValor(Boolean valor, int linea) {
		super(linea);
		this.valor = valor;
	}

	public NodoValor(String valor) {
		super();
		this.valor = valor;
	}

	public NodoValor(String valor, int linea) {
		super(linea);
		this.valor = valor;
	}

	public NodoValor() {
		super();
	}
	
	public Integer getValor() {
		if (valor instanceof Integer) {
			return (Integer) valor;
		}
		if (valor instanceof Boolean) {
			return ((Boolean) valor) ? 1 : 0;
		}
		return 0;
	}

	public Boolean getValorBooleano() {
		if (valor instanceof Boolean) {
			return (Boolean) valor;
		}
		return null;
	}

	public String getValorCadena() {
		if (valor instanceof String) {
			return (String) valor;
		}
		return null;
	}

	public Object getValorObjeto() {
		return valor;
	}

	public void setValor(Object valor) {
		this.valor = valor;
	}

	public boolean esEntero() {
		return valor instanceof Integer;
	}

	public boolean esBooleano() {
		return valor instanceof Boolean;
	}

	public boolean esCadena() {
		return valor instanceof String;
	}

}
