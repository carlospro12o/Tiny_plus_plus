package ve.edu.unet.nodosAST;

public class NodoSubrutina extends NodoBase {
    private String tipoRetorno; // null si es procedimiento, o tipo ("int", "bool", "string") si es funcion
    private String identificador;
    private NodoBase parametros; // lista de NodoDeclaracion encadenados por HermanoDerecha
    private NodoBase cuerpo;     // stmt_seq del cuerpo de la subrutina

    public NodoSubrutina(String tipoRetorno, String identificador, NodoBase parametros, NodoBase cuerpo) {
        super();
        this.tipoRetorno = tipoRetorno;
        this.identificador = identificador;
        this.parametros = parametros;
        this.cuerpo = cuerpo;
    }

    public NodoSubrutina(String tipoRetorno, String identificador, NodoBase parametros, NodoBase cuerpo, int linea) {
        super(linea);
        this.tipoRetorno = tipoRetorno;
        this.identificador = identificador;
        this.parametros = parametros;
        this.cuerpo = cuerpo;
    }

    public String getTipoRetorno() {
        return tipoRetorno;
    }

    public void setTipoRetorno(String tipoRetorno) {
        this.tipoRetorno = tipoRetorno;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public NodoBase getParametros() {
        return parametros;
    }

    public void setParametros(NodoBase parametros) {
        this.parametros = parametros;
    }

    public NodoBase getCuerpo() {
        return cuerpo;
    }

    public void setCuerpo(NodoBase cuerpo) {
        this.cuerpo = cuerpo;
    }

    public boolean esFuncion() {
        return this.tipoRetorno != null;
    }

    public boolean esProcedimiento() {
        return this.tipoRetorno == null;
    }
}
