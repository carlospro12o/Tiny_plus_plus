package ve.edu.unet.nodosAST;

public class NodoLlamada extends NodoBase {
    private String identificador;
    private NodoBase argumentos; // expresiones pasadas como argumentos, enlazadas por getHermanoDerecha()

    public NodoLlamada(String identificador) {
        super();
        this.identificador = identificador;
        this.argumentos = null;
    }

    public NodoLlamada(String identificador, int linea) {
        super(linea);
        this.identificador = identificador;
        this.argumentos = null;
    }

    public NodoLlamada(String identificador, NodoBase argumentos) {
        super();
        this.identificador = identificador;
        this.argumentos = argumentos;
    }

    public NodoLlamada(String identificador, NodoBase argumentos, int linea) {
        super(linea);
        this.identificador = identificador;
        this.argumentos = argumentos;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public NodoBase getArgumentos() {
        return argumentos;
    }

    public void setArgumentos(NodoBase argumentos) {
        this.argumentos = argumentos;
    }
}
