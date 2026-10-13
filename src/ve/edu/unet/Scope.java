package ve.edu.unet;

import java.util.*;

public class Scope {
    private String nombre;
    private Scope padre;
    private List<Scope> hijos;
    private Map<String, RegistroSimbolo> tabla;
    private int nivel;

    public Scope(String nombre, Scope padre) {
        this.nombre = nombre;
        this.padre = padre;
        this.hijos = new ArrayList<Scope>();
        this.tabla = new LinkedHashMap<String, RegistroSimbolo>();
        this.nivel = (padre == null) ? 0 : padre.getNivel() + 1;
    }

    public String getNombre() {
        return nombre;
    }

    public Scope getPadre() {
        return padre;
    }

    public List<Scope> getHijos() {
        return hijos;
    }

    public void agregarHijo(Scope hijo) {
        this.hijos.add(hijo);
    }

    public int getNivel() {
        return nivel;
    }

    public boolean esGlobal() {
        return padre == null;
    }

    public Map<String, RegistroSimbolo> getTabla() {
        return tabla;
    }

    // Inserta un simbolo solo en este ambito
    // Retorna true si se inserto exitosamente, false si ya existia en este ambito
    public boolean insertar(RegistroSimbolo simbolo) {
        if (tabla.containsKey(simbolo.getIdentificador())) {
            return false;
        }
        tabla.put(simbolo.getIdentificador(), simbolo);
        return true;
    }

    // Busca un simbolo solo en el ambito local actual
    public RegistroSimbolo buscarLocal(String nombre) {
        return tabla.get(nombre);
    }

    // Busca un simbolo en el ambito actual, y si no esta, asciende por la cadena de padres
    public RegistroSimbolo buscar(String nombre) {
        RegistroSimbolo simbolo = tabla.get(nombre);
        if (simbolo != null) {
            return simbolo;
        }
        if (padre != null) {
            return padre.buscar(nombre);
        }
        return null;
    }

    public void imprimir(int sangria) {
        String indent = "";
        for (int i = 0; i < sangria; i++) {
            indent += "  ";
        }
        System.out.println(indent + "=== Ambito: " + nombre + " (Nivel " + nivel + ") ===");
        if (tabla.isEmpty()) {
            System.out.println(indent + "  (sin simbolos)");
        } else {
            for (RegistroSimbolo sim : tabla.values()) {
                System.out.println(indent + "  * " + sim.toString());
            }
        }
        for (Scope hijo : hijos) {
            hijo.imprimir(sangria + 1);
        }
    }
}
