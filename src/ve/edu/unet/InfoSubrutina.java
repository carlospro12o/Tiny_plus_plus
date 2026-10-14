package ve.edu.unet;

import java.util.*;

/**
 * Representa la metadata y estructura de activacion de una subrutina
 * (funcion o procedimiento) en la Tiny Machine para el lenguaje Tiny++.
 * 
 * Disposicion del Marco de Pila (Stack Frame) respecto a MP:
 *   offset  0(MP): Direccion de retorno (instruccion a la que vuelve el llamador)
 *   offset  1(MP): Enlace dinamico (Old MP, marco previo del llamador)
 *   offset  2..(2 + N - 1)(MP): Parametros pasados en la llamada
 *   offset -1..-K(MP): Variables locales
 *   offset < -(K+1)(MP): Temporales de evaluacion de expresiones
 */
public class InfoSubrutina {
    private String nombre;
    private String tipoRetorno; // null si es procedimiento, o tipo ("int", "bool", "string") si es funcion
    
    private List<String> parametros = new ArrayList<>();
    private Map<String, String> tiposParametros = new HashMap<>();
    private Map<String, Integer> offsetsParametros = new HashMap<>();
    
    private List<String> locales = new ArrayList<>();
    private Map<String, String> tiposLocales = new HashMap<>();
    private Map<String, Integer> offsetsLocales = new HashMap<>();
    
    private int direccionEntrada = -1; // Direccion de la primera instruccion en iMem
    private List<Integer> llamadasPendientes = new ArrayList<>(); // Direcciones para backpatching de llamadas anticipadas

    public InfoSubrutina(String nombre, String tipoRetorno) {
        this.nombre = nombre;
        this.tipoRetorno = tipoRetorno;
    }

    public void agregarParametro(String id, String tipo) {
        if (!parametros.contains(id)) {
            parametros.add(id);
            tiposParametros.put(id, tipo != null ? tipo : "int");
        }
        recalcularOffsetsParametros();
    }

    public void agregarLocal(String id, String tipo) {
        if (!locales.contains(id) && !parametros.contains(id)) {
            locales.add(id);
            tiposLocales.put(id, tipo != null ? tipo : "int");
            // Variables locales en offsets negativos: -1, -2, ...
            offsetsLocales.put(id, -locales.size());
        }
    }

    private void recalcularOffsetsParametros() {
        // En el marco de pila:
        // 0(MP) = Direccion de retorno
        // 1(MP) = Antiguo MP
        // Parametros: 2 + (numParams - 1 - i)
        int numParams = parametros.size();
        for (int i = 0; i < numParams; i++) {
            String p = parametros.get(i);
            int offset = 2 + (numParams - 1 - i);
            offsetsParametros.put(p, offset);
        }
    }

    public boolean esParametro(String id) {
        return offsetsParametros.containsKey(id);
    }

    public boolean esLocal(String id) {
        return offsetsLocales.containsKey(id);
    }

    public boolean esLocalOParametro(String id) {
        return esParametro(id) || esLocal(id);
    }

    public int getOffset(String id) {
        if (esParametro(id)) return offsetsParametros.get(id);
        if (esLocal(id)) return offsetsLocales.get(id);
        return 0;
    }

    public String getTipo(String id) {
        if (esParametro(id)) return tiposParametros.get(id);
        if (esLocal(id)) return tiposLocales.get(id);
        return "int";
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipoRetorno() {
        return tipoRetorno;
    }

    public boolean esFuncion() {
        return tipoRetorno != null;
    }

    public boolean esProcedimiento() {
        return tipoRetorno == null;
    }

    public int getCantidadParametros() {
        return parametros.size();
    }

    public List<String> getParametros() {
        return parametros;
    }

    public int getCantidadLocales() {
        return locales.size();
    }

    public List<String> getLocales() {
        return locales;
    }

    public int getDireccionEntrada() {
        return direccionEntrada;
    }

    public void setDireccionEntrada(int direccionEntrada) {
        this.direccionEntrada = direccionEntrada;
    }

    public List<Integer> getLlamadasPendientes() {
        return llamadasPendientes;
    }

    public void agregarLlamadaPendiente(int instruccion) {
        llamadasPendientes.add(instruccion);
    }
}
