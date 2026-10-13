package ve.edu.unet.nodosAST;

public class NodoDeclaracion extends NodoBase {
    private String tipo;
    private String identificador;
    private Integer tamanoArreglo; // null si es variable simple, o el tamao si es arreglo: int A[10]

    // Constructor para variable simple: int x;
    public NodoDeclaracion(String tipo, String identificador) {
        super();
        this.tipo = tipo;
        this.identificador = identificador;
        this.tamanoArreglo = null;
    }

    public NodoDeclaracion(String tipo, String identificador, int linea) {
        super(linea);
        this.tipo = tipo;
        this.identificador = identificador;
        this.tamanoArreglo = null;
    }

    // Constructor para arreglos: int A[10];
    public NodoDeclaracion(String tipo, String identificador, Integer tamanoArreglo) {
        super();
        this.tipo = tipo;
        this.identificador = identificador;
        this.tamanoArreglo = tamanoArreglo;
    }

    public NodoDeclaracion(String tipo, String identificador, Integer tamanoArreglo, int linea) {
        super(linea);
        this.tipo = tipo;
        this.identificador = identificador;
        this.tamanoArreglo = tamanoArreglo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public Integer getTamanoArreglo() {
        return tamanoArreglo;
    }

    public void setTamanoArreglo(Integer tamanoArreglo) {
        this.tamanoArreglo = tamanoArreglo;
    }

    public boolean esArreglo() {
        return this.tamanoArreglo != null;
    }
}
