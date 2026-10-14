package ve.edu.unet.nodosAST;

public class NodoAccesoArreglo extends NodoBase {
    private String identificador;
    private NodoBase indice;

    public NodoAccesoArreglo(String identificador, NodoBase indice) {
        super();
        this.identificador = identificador;
        this.indice = indice;
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
}
