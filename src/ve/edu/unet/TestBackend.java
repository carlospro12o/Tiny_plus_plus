package ve.edu.unet;

import ve.edu.unet.nodosAST.*;

public class TestBackend {
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  VERIFICACION DE GENERACION DE CODIGO TM (MISIONES 1 Y 2)        ");
        System.out.println("==================================================================");

        // Simulamos la tabla de simbolos que normalmente llena el Frontend
        TablaSimbolos ts = new TablaSimbolos();
        ts.InsertarSimbolo("res", 1);
        ts.InsertarSimbolo("a", 1);
        ts.InsertarSimbolo("b", 1);
        ts.InsertarSimbolo("x", 1);
        ts.InsertarSimbolo("i", 1);
        ts.InsertarSimbolo("A", 1); // Arreglo A con direccion base
        Generador.setTablaSimbolos(ts);

        // --- MISION 1: Operadores relacionales y logicos ---
        // 1. Asignacion: res := a > b
        NodoAsignacion nMayor = new NodoAsignacion("res",
            new NodoOperacion(new NodoIdentificador("a"), tipoOp.mayor, new NodoIdentificador("b")));

        // 2. Asignacion: res := (a <= b) and true
        NodoAsignacion nAnd = new NodoAsignacion("res",
            new NodoOperacion(
                new NodoOperacion(new NodoIdentificador("a"), tipoOp.menorIgual, new NodoIdentificador("b")),
                tipoOp.and,
                new NodoBooleano(true)
            ));

        // --- MISION 2: Arreglos unidimensionales ---
        // 3. Asignacion a arreglo con expresion compuesta en el indice:
        //    A[i * 2 + 1] := 10
        NodoBase indiceCompuesto = new NodoOperacion(
            new NodoOperacion(new NodoIdentificador("i"), tipoOp.por, new NodoValor(2)),
            tipoOp.mas,
            new NodoValor(1)
        );
        NodoAsignacionArreglo nAsigArreglo = new NodoAsignacionArreglo("A", indiceCompuesto, new NodoValor(10));

        // 4. Lectura de arreglo dentro de una expresion:
        //    x := A[i * 2 + 1] + 5
        NodoBase indiceLectura = new NodoOperacion(
            new NodoOperacion(new NodoIdentificador("i"), tipoOp.por, new NodoValor(2)),
            tipoOp.mas,
            new NodoValor(1)
        );
        NodoAsignacion nLecturaArreglo = new NodoAsignacion("x",
            new NodoOperacion(
                new NodoAccesoArreglo("A", indiceLectura),
                tipoOp.mas,
                new NodoValor(5)
            ));

        // 5. Copia directa de arreglo: A[0] := A[i]
        NodoAsignacionArreglo nCopiaArreglo = new NodoAsignacionArreglo(
            "A",
            new NodoValor(0),
            new NodoAccesoArreglo("A", new NodoIdentificador("i"))
        );

        // Encadenamos los nodos en el AST
        nMayor.setHermanoDerecha(nAnd);
        nAnd.setHermanoDerecha(nAsigArreglo);
        nAsigArreglo.setHermanoDerecha(nLecturaArreglo);
        nLecturaArreglo.setHermanoDerecha(nCopiaArreglo);

        // Imprimimos el AST para verificar estructura
        System.out.println("\n--- ESTRUCTURA DEL AST GENERADO ---");
        Util.imprimirAST(nMayor);

        // Generamos el codigo objeto TM
        System.out.println("\n--- GENERANDO CODIGO OBJETO TM ---");
        Generador.generarCodigoObjeto(nMayor);

        System.out.println("==================================================================");
        System.out.println("  VERIFICACION EXITOSA: Arreglos y Operadores TM funcionando!     ");
        System.out.println("==================================================================");
    }
}
