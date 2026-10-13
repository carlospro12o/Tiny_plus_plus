package ve.edu.unet.nodosAST;

public class NodoReturn extends NodoBase {
    private NodoBase expresion; // null si es return vacio, no-null si retorna un valor/expresion

    public NodoReturn() {
        super();
        this.expresion = null;
    }

    public NodoReturn(NodoBase expresion) {
        super();
        this.expresion = expresion;
    }

    public NodoReturn(NodoBase expresion, int linea) {
        super(linea);
        this.expresion = expresion;
    }

    public NodoReturn(int linea) {
        super(linea);
        this.expresion = null;
    }

    public NodoBase getExpresion() {
        return expresion;
    }

    public void setExpresion(NodoBase expresion) {
        this.expresion = expresion;
    }

    public boolean tieneExpresion() {
        return this.expresion != null;
    }
}
