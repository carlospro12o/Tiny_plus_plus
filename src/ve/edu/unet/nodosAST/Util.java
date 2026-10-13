package ve.edu.unet.nodosAST;

public class Util {
	
	static int sangria = 0;
	
	// Imprimo en modo texto con sangrias el AST
	public static void imprimirAST(NodoBase raiz){
		  sangria+=2;
		  while (raiz != null) {
		    printSpaces();
		    if (raiz instanceof  NodoIf)
		    	System.out.println("If");
		    else if (raiz instanceof  NodoRepeat)
		    	System.out.println("Repeat");
		    
		    else if (raiz instanceof  NodoAsignacion) {
		    	NodoAsignacion asig = (NodoAsignacion) raiz;
		    	if (asig.esArreglo())
		    		System.out.println("Asignacion a arreglo: " + asig.getIdentificador());
		    	else
		    		System.out.println("Asignacion a: " + asig.getIdentificador());
		    }

		    else if (raiz instanceof NodoAsignacionArreglo)
		    	System.out.println("Asignacion a Arreglo: " + ((NodoAsignacionArreglo)raiz).getIdentificador() + "[...]");

		    else if (raiz instanceof  NodoLeer)  
		    	System.out.println("Lectura: "+((NodoLeer)raiz).getIdentificador());

		    else if (raiz instanceof  NodoEscribir)
		    	System.out.println("Escribir");
		    
		    else if (raiz instanceof NodoDeclaracion) {
		    	NodoDeclaracion dec = (NodoDeclaracion) raiz;
		    	if (dec.esArreglo())
		    		System.out.println("Declaracion Arreglo: " + dec.getTipo() + " " + dec.getIdentificador() + "[" + dec.getTamanoArreglo() + "]");
		    	else
		    		System.out.println("Declaracion: " + dec.getTipo() + " " + dec.getIdentificador());
		    }

		    else if (raiz instanceof NodoSubrutina) {
		    	NodoSubrutina sub = (NodoSubrutina) raiz;
		    	if (sub.esFuncion())
		    		System.out.println("Funcion: " + sub.getTipoRetorno() + " " + sub.getIdentificador());
		    	else
		    		System.out.println("Procedimiento: " + sub.getIdentificador());
		    }

		    else if (raiz instanceof NodoReturn)
		    	System.out.println("Return");

		    else if (raiz instanceof NodoLlamada)
		    	System.out.println("Llamada a: " + ((NodoLlamada)raiz).getIdentificador());

		    else if (raiz instanceof NodoAccesoArreglo)
		    	System.out.println("Acceso a Arreglo: " + ((NodoAccesoArreglo)raiz).getIdentificador() + "[...]");

		    else if (raiz instanceof NodoOperacion
		    		|| raiz instanceof NodoValor
		    		|| raiz instanceof NodoBooleano
		    		|| raiz instanceof NodoIdentificador)
		    	imprimirNodo(raiz);
		    else System.out.println("Tipo de nodo desconocido: " + raiz.getClass().getSimpleName());
		    
		    /* Hago el recorrido recursivo */
		    if (raiz instanceof  NodoIf){
		    	printSpaces();
		    	System.out.println("**Prueba IF**");
		    	imprimirAST(((NodoIf)raiz).getPrueba());
		    	printSpaces();
		    	System.out.println("**Then IF**");
		    	imprimirAST(((NodoIf)raiz).getParteThen());
		    	if(((NodoIf)raiz).getParteElse()!=null){
		    		printSpaces();
		    		System.out.println("**Else IF**");
		    		imprimirAST(((NodoIf)raiz).getParteElse());
		    	}
		    }
		    else if (raiz instanceof  NodoRepeat){
		    	printSpaces();
		    	System.out.println("**Cuerpo REPEAT**");
		    	imprimirAST(((NodoRepeat)raiz).getCuerpo());
		    	printSpaces();
		    	System.out.println("**Prueba REPEAT**");
		    	imprimirAST(((NodoRepeat)raiz).getPrueba());
		    }
		    else if (raiz instanceof  NodoAsignacion) {
		    	NodoAsignacion asig = (NodoAsignacion) raiz;
		    	if (asig.esArreglo()) {
		    		printSpaces();
		    		System.out.println("**Indice Arreglo**");
		    		imprimirAST(asig.getIndice());
		    	}
		    	printSpaces();
		    	System.out.println("**Valor Asignacion**");
		    	imprimirAST(asig.getExpresion());
		    }
		    else if (raiz instanceof NodoAsignacionArreglo){
		    	printSpaces();
		    	System.out.println("**Indice de Asignacion**");
		    	imprimirAST(((NodoAsignacionArreglo)raiz).getIndice());
		    	printSpaces();
		    	System.out.println("**Valor a Asignar**");
		    	imprimirAST(((NodoAsignacionArreglo)raiz).getExpresion());
		    }
		    else if (raiz instanceof NodoAccesoArreglo){
		    	printSpaces();
		    	System.out.println("**Indice de Acceso**");
		    	imprimirAST(((NodoAccesoArreglo)raiz).getIndice());
		    }
		    else if (raiz instanceof  NodoEscribir)
		    	imprimirAST(((NodoEscribir)raiz).getExpresion());
		    else if (raiz instanceof NodoIdentificador) {
		    	NodoIdentificador id = (NodoIdentificador) raiz;
		    	if (id.esArreglo()) {
		    		printSpaces();
		    		System.out.println("**Indice Arreglo**");
		    		imprimirAST(id.getIndice());
		    	}
		    }
		    else if (raiz instanceof NodoSubrutina) {
		    	NodoSubrutina sub = (NodoSubrutina) raiz;
		    	if (sub.getParametros() != null) {
		    		printSpaces();
		    		System.out.println("**Parametros**");
		    		imprimirAST(sub.getParametros());
		    	}
		    	if (sub.getCuerpo() != null) {
		    		printSpaces();
		    		System.out.println("**Cuerpo Subrutina**");
		    		imprimirAST(sub.getCuerpo());
		    	}
		    }
		    else if (raiz instanceof NodoReturn) {
		    	NodoReturn ret = (NodoReturn) raiz;
		    	if (ret.tieneExpresion()) {
		    		printSpaces();
		    		System.out.println("**Valor Return**");
		    		imprimirAST(ret.getExpresion());
		    	}
		    }
		    else if (raiz instanceof NodoLlamada) {
		    	NodoLlamada call = (NodoLlamada) raiz;
		    	if (call.getArgumentos() != null) {
		    		printSpaces();
		    		System.out.println("**Argumentos Llamada**");
		    		imprimirAST(call.getArgumentos());
		    	}
		    }
		    else if (raiz instanceof NodoOperacion){
		    	printSpaces();
		    	System.out.println("**Expr Izquierda Operacion**");
		    	imprimirAST(((NodoOperacion)raiz).getOpIzquierdo());
		    	if (((NodoOperacion)raiz).getOpDerecho() != null) {
		    		printSpaces();
		    		System.out.println("**Expr Derecha Operacion**");		    	
		    		imprimirAST(((NodoOperacion)raiz).getOpDerecho());
		    	}
		    }
		    raiz = raiz.getHermanoDerecha();
		  }
		  sangria-=2;
		}

/* Imprime espacios con sangria */
static void printSpaces()
{ int i;
  for (i=0;i<sangria;i++)
	  System.out.print(" ");
}

/* Imprime informacion de los nodos */
static void imprimirNodo( NodoBase raiz )
{
	if(	raiz instanceof NodoRepeat
		||	raiz instanceof NodoLeer
		||	raiz instanceof NodoEscribir  ){
		System.out.println("palabra reservada: "+ raiz.getClass().getName());
	}
	
	if(	raiz instanceof NodoAsignacion )
		System.out.println(":=");
	
	if(	raiz instanceof NodoOperacion ){
		tipoOp sel=((NodoOperacion) raiz).getOperacion();
		if(sel==tipoOp.menor) System.out.println("<"); 
		if(sel==tipoOp.igual) System.out.println("=");
		if(sel==tipoOp.mayor) System.out.println(">");
		if(sel==tipoOp.menor_igual || sel==tipoOp.menorIgual) System.out.println("<=");
		if(sel==tipoOp.mayor_igual || sel==tipoOp.mayorIgual) System.out.println(">=");
		if(sel==tipoOp.distinto || sel==tipoOp.diferente) System.out.println("!=");
		if(sel==tipoOp.and || sel==tipoOp.y) System.out.println("and");
		if(sel==tipoOp.or || sel==tipoOp.o) System.out.println("or");
		if(sel==tipoOp.not || sel==tipoOp.no) System.out.println("not");
		if(sel==tipoOp.mas) System.out.println("+");
		if(sel==tipoOp.menos) System.out.println("-");
		if(sel==tipoOp.por) System.out.println("*");
		if(sel==tipoOp.entre) System.out.println("/");
	}

	if(	raiz instanceof NodoValor ){
		NodoValor val = (NodoValor) raiz;
		if (val.esCadena())
			System.out.println("STR, val= \""+ val.getValorObjeto() + "\"");
		else if (val.esBooleano())
			System.out.println("BOOL, val= "+ val.getValorObjeto());
		else
			System.out.println("NUM, val= "+ val.getValor());
	}

	if(	raiz instanceof NodoBooleano ){
		System.out.println("BOOL, val= "+ ((NodoBooleano)raiz).getValor());
	}

	if(	raiz instanceof NodoIdentificador ){
		NodoIdentificador id = (NodoIdentificador) raiz;
		if (id.esArreglo())
			System.out.println("ID (arreglo), nombre= "+ id.getNombre());
		else
			System.out.println("ID, nombre= "+ id.getNombre());
	}

}

}
