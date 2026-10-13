package ve.edu.unet.nodosAST;

public class NodoBase {
	/*Esta clase fue creada para tener un origen comun de todas las clases...
	 * 
	 * ESTE MODELO DE AST PUEDE SER MEJORADO DE MUCHAS FORMAS, POR SIMPLIFICACION Y
	 * DIDACTICA EN CLASE, SE LLEVARA A CABO DE FORMA MUY SIMPLE Y POCO FLEXIBLE
	 * */
	
	private NodoBase HermanoDerecha;
	private int linea;

	public NodoBase(NodoBase hermanoDerecha) {
		super();
		HermanoDerecha = hermanoDerecha;
	}

	public NodoBase() {
		super();
		HermanoDerecha = null;
	}

	public NodoBase(int linea) {
		this();
		this.linea = linea;
	}

	public NodoBase getHermanoDerecha() {
		return HermanoDerecha;
	}

	public void setHermanoDerecha(NodoBase hermanoDerecha) {
		HermanoDerecha = hermanoDerecha;
	}
	
	public boolean TieneHermano() {
		return (HermanoDerecha != null);
	}

	public int getLinea() {
		return linea;
	}

	public void setLinea(int linea) {
		this.linea = linea;
	}
	
}
