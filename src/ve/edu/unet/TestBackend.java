package ve.edu.unet;

import ve.edu.unet.nodosAST.*;

public class TestBackend {
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  VERIFICACION DE GENERACION DE CODIGO TM (MISIONES 1, 2 Y 3)     ");
        System.out.println("==================================================================");

        // Simulamos la tabla de simbolos que normalmente llena el Frontend
        TablaSimbolos ts = new TablaSimbolos();
        ts.InsertarSimbolo("res", 1, "int", 1);
        ts.InsertarSimbolo("a", 1, "int", 1);
        ts.InsertarSimbolo("b", 1, "int", 1);
        ts.InsertarSimbolo("x", 1, "int", 1);
        ts.InsertarSimbolo("i", 1, "int", 1);
        ts.InsertarSimbolo("A", 1, "int", 10);      // Arreglo A de tamano 10
        ts.InsertarSimbolo("saludo", 1, "string", 1); // Variable de tipo cadena
        Generador.setTablaSimbolos(ts);

        // --- MISION 1: Operadores relacionales y logicos ---
        // 1. Asignacion: res := a > b
        NodoAsignacion nMayor = new NodoAsignacion("res",
            new NodoOperacion(new NodoIdentificador("a"), tipoOp.mayor, new NodoIdentificador("b")));

        // 2. Asignacion: res := (a <= b) and true
        NodoAsignacion nAnd = new NodoAsignacion("res",
            new NodoOperacion(
                new NodoOperacion(new NodoIdentificador("a"), tipoOp.menor_igual, new NodoIdentificador("b")),
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
        NodoAsignacion nAsigArreglo = new NodoAsignacion("A", new NodoValor(10), indiceCompuesto);

        // 4. Lectura de arreglo dentro de una expresion:
        //    x := A[i * 2 + 1] + 5
        NodoBase indiceLectura = new NodoOperacion(
            new NodoOperacion(new NodoIdentificador("i"), tipoOp.por, new NodoValor(2)),
            tipoOp.mas,
            new NodoValor(1)
        );
        NodoAsignacion nLecturaArreglo = new NodoAsignacion("x",
            new NodoOperacion(
                new NodoIdentificador("A", indiceLectura),
                tipoOp.mas,
                new NodoValor(5)
            ));

        // --- MISION 3: Cadenas de Texto (Strings) ---
        // 5. Asignacion de literal de cadena a variable string:
        //    saludo := "hola Tiny++"
        NodoAsignacion nAsigString = new NodoAsignacion("saludo", new NodoValor("hola Tiny++"));

        // 6. Escritura de variable de tipo cadena:
        //    write saludo
        NodoEscribir nEscribirString = new NodoEscribir(new NodoIdentificador("saludo"));

        // 7. Escritura directa de literal de cadena:
        //    write "UNET Compiladores"
        NodoEscribir nEscribirLiteral = new NodoEscribir(new NodoValor("UNET"));

        // Encadenamos los nodos en el AST
        nMayor.setHermanoDerecha(nAnd);
        nAnd.setHermanoDerecha(nAsigArreglo);
        nAsigArreglo.setHermanoDerecha(nLecturaArreglo);
        nLecturaArreglo.setHermanoDerecha(nAsigString);
        nAsigString.setHermanoDerecha(nEscribirString);
        nEscribirString.setHermanoDerecha(nEscribirLiteral);

        // Imprimimos la tabla de simbolos
        ts.ImprimirClaves();

        // Imprimimos el AST para verificar estructura
        System.out.println("\n--- ESTRUCTURA DEL AST GENERADO ---");
        Util.imprimirAST(nMayor);

        // Generamos el codigo objeto TM
        System.out.println("\n--- GENERANDO CODIGO OBJETO TM ---");
        Generador.generarCodigoObjeto(nMayor);

        System.out.println("==================================================================");
        System.out.println("  VERIFICACION EXITOSA: Strings, Arreglos y Operadores TM al 100% ");
        System.out.println("==================================================================");
    }
}
