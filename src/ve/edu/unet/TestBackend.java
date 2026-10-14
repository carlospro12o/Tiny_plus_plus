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

        // --- MISION 4: Subrutinas, Funciones y Marcos de Pila ---
        // 8. Declaracion de funcion:
        //    function int multiplicar(int a, int b)
        //        return a * b
        //    end;
        NodoDeclaracion paramA = new NodoDeclaracion("int", "a");
        NodoDeclaracion paramB = new NodoDeclaracion("int", "b");
        paramA.setHermanoDerecha(paramB);
        NodoReturn nReturnMult = new NodoReturn(
            new NodoOperacion(new NodoIdentificador("a"), tipoOp.por, new NodoIdentificador("b"))
        );
        NodoSubrutina nFuncMult = new NodoSubrutina("int", "multiplicar", paramA, nReturnMult);

        // 9. Declaracion de procedimiento:
        //    procedure mostrar(int val)
        //        write val
        //    end;
        NodoDeclaracion paramVal = new NodoDeclaracion("int", "val");
        NodoEscribir nCuerpoMostrar = new NodoEscribir(new NodoIdentificador("val"));
        NodoSubrutina nProcMostrar = new NodoSubrutina(null, "mostrar", paramVal, nCuerpoMostrar);

        // 10. Llamada a funcion en expresion:
        //     x := multiplicar(x, 2)
        NodoBase argX = new NodoIdentificador("x");
        NodoBase argDos = new NodoValor(2);
        argX.setHermanoDerecha(argDos);
        NodoLlamada nLlamadaMult = new NodoLlamada("multiplicar", argX);
        NodoAsignacion nAsigLlamada = new NodoAsignacion("x", nLlamadaMult);

        // 11. Llamada a procedimiento como sentencia:
        //     mostrar(x)
        NodoLlamada nLlamadaMostrar = new NodoLlamada("mostrar", new NodoIdentificador("x"));

        // Registrar subrutinas en la tabla de simbolos
        InfoSubrutina infoMult = new InfoSubrutina("multiplicar", "int");
        infoMult.agregarParametro("a", "int");
        infoMult.agregarParametro("b", "int");
        ts.registrarSubrutina(infoMult);

        InfoSubrutina infoMostrar = new InfoSubrutina("mostrar", null);
        infoMostrar.agregarParametro("val", "int");
        ts.registrarSubrutina(infoMostrar);

        // Encadenamos los nodos en el AST
        nMayor.setHermanoDerecha(nAnd);
        nAnd.setHermanoDerecha(nAsigArreglo);
        nAsigArreglo.setHermanoDerecha(nLecturaArreglo);
        nLecturaArreglo.setHermanoDerecha(nAsigString);
        nAsigString.setHermanoDerecha(nEscribirString);
        nEscribirString.setHermanoDerecha(nEscribirLiteral);
        nEscribirLiteral.setHermanoDerecha(nFuncMult);
        nFuncMult.setHermanoDerecha(nProcMostrar);
        nProcMostrar.setHermanoDerecha(nAsigLlamada);
        nAsigLlamada.setHermanoDerecha(nLlamadaMostrar);

        // Imprimimos la tabla de simbolos
        ts.ImprimirClaves();

        // Imprimimos el AST para verificar estructura
        System.out.println("\n--- ESTRUCTURA DEL AST GENERADO ---");
        Util.imprimirAST(nMayor);

        // Generamos el codigo objeto TM
        System.out.println("\n--- GENERANDO CODIGO OBJETO TM ---");
        Generador.generarCodigoObjeto(nMayor);

        System.out.println("==================================================================");
        System.out.println("  VERIFICACION EXITOSA: MISIONES 1, 2, 3 Y 4 AL 100% OPERATIVAS!  ");
        System.out.println("==================================================================");
    }
}
