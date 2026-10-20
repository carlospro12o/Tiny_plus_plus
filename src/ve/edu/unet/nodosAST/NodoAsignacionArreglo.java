package ve.edu.unet.nodosAST;

public class NodoAsignacionArreglo extends NodoBase {
    private String identificador;
    private NodoBase indice;
    private NodoBase expresion;

    public NodoAsignacionArreglo(String identificador, NodoBase indice, NodoBase expresion) {
        super();
        this.identificador = identificador;
        this.indice = indice;
        this.expresion = expresion;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public NodoBase getIndice() {
        return indice;
    }

    public void setIndice(NodoBase indice) {
        this.indice = indice;
    }

    public NodoBase getExpresion() {
        return expresion;
    }

    public void setExpresion(NodoBase expresion) {
        this.expresion = expresion;
    }
}
