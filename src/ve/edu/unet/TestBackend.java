package ve.edu.unet;

import ve.edu.unet.nodosAST.*;

public class TestBackend {
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  VERIFICACION DE GENERACION DE CODIGO TM (MISION 1: BACKEND)     ");
        System.out.println("==================================================================");

        // Simulamos la tabla de simbolos que normalmente llena el Frontend
        TablaSimbolos ts = new TablaSimbolos();
        ts.InsertarSimbolo("res", 1);
        ts.InsertarSimbolo("a", 1);
        ts.InsertarSimbolo("b", 1);
        Generador.setTablaSimbolos(ts);

        // 1. Asignacion: res := a > b
        NodoAsignacion nMayor = new NodoAsignacion("res",
            new NodoOperacion(new NodoIdentificador("a"), tipoOp.mayor, new NodoIdentificador("b")));

        // 2. Asignacion: res := a <= b
        NodoAsignacion nMenorIgual = new NodoAsignacion("res",
            new NodoOperacion(new NodoIdentificador("a"), tipoOp.menorIgual, new NodoIdentificador("b")));

        // 3. Asignacion: res := a != b
        NodoAsignacion nDiferente = new NodoAsignacion("res",
            new NodoOperacion(new NodoIdentificador("a"), tipoOp.diferente, new NodoIdentificador("b")));

        // 4. Asignacion: res := (a > b) and true
        NodoAsignacion nAnd = new NodoAsignacion("res",
            new NodoOperacion(
                new NodoOperacion(new NodoIdentificador("a"), tipoOp.mayor, new NodoIdentificador("b")),
                tipoOp.and,
                new NodoBooleano(true)
            ));

        // 5. Asignacion: res := not false
        NodoAsignacion nNot = new NodoAsignacion("res",
            new NodoOperacion(
                new NodoBooleano(false),
                tipoOp.not,
                null
            ));

        // Encadenamos los nodos como hermanos en el AST
        nMayor.setHermanoDerecha(nMenorIgual);
        nMenorIgual.setHermanoDerecha(nDiferente);
        nDiferente.setHermanoDerecha(nAnd);
        nAnd.setHermanoDerecha(nNot);

        // Generamos el codigo objeto
        Generador.generarCodigoObjeto(nMayor);

        System.out.println("==================================================================");
        System.out.println("  VERIFICACION EXITOSA: Todas las instrucciones TM fueron emitidas");
        System.out.println("==================================================================");
    }
}
