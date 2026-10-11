package ve.edu.unet;

import java_cup.runtime.*;
import java.io.Reader;

%%

%cup

%class Lexico

%line

%column

%{
	public Lexico(Reader r, SymbolFactory sf){
        this(r);
		this.sf=sf;
		lineanum=0;
		debug=true;
	}

	private SymbolFactory sf;
	private int lineanum;
	private boolean debug;

%}

%eofval{
	return sf.newSymbol("EOF", sym.EOF);
%eofval}

// Expresiones regulares

digito = [0-9]
numero = {digito}+
letra = [a-zA-Z]
identificador = {letra}({letra}|{digito})*
espacio = [ \t]+
nuevalinea = \n | \n\r | \r\n
cadena = \"[^\"]*\"

%%



	 /* Palabras Reservadas que pide el PDF */



"int" {
	if(debug) System.out.println("token INT");
	return sf.newSymbol("INT", sym.INT);
}

"bool" {
	if(debug) System.out.println("token BOOL");
	return sf.newSymbol("BOOL", sym.BOOL);
}

"string" {
	if(debug) System.out.println("token STRING");
	return sf.newSymbol("STRING", sym.STRING);
}

"true" {
	if(debug) System.out.println("token TRUE");
	return sf.newSymbol("TRUE", sym.TRUE);
}

"false" {
	if(debug) System.out.println("token FALSE");
	return sf.newSymbol("FALSE", sym.FALSE);
}

"function" {
	if(debug) System.out.println("token FUNCTION");
	return sf.newSymbol("FUNCTION", sym.FUNCTION);
}

"procedure" {
	if(debug) System.out.println("token PROCEDURE");
	return sf.newSymbol("PROCEDURE", sym.PROCEDURE);
}

"return" {
	if(debug) System.out.println("token RETURN");
	return sf.newSymbol("RETURN", sym.RETURN);
}



	 /* Palabras Reservadas */



"if"            {	if(debug) System.out.println("token IF");
			return sf.newSymbol("IF",sym.IF);
			}

"then"          { if(debug) System.out.println("token THEN");
			return sf.newSymbol("THEN",sym.THEN);
			}

"else"          {	if(debug) System.out.println("token ELSE");
			return sf.newSymbol("ELSE",sym.ELSE);
			}

"end"           {	if(debug) System.out.println("token END");
			return sf.newSymbol("END",sym.END);
			}

"repeat"        {	if(debug) System.out.println("token REPEAT");
			return sf.newSymbol("REPEAT",sym.REPEAT);
			}

"until"         {	if(debug) System.out.println("token UNTIL");
			return sf.newSymbol("UNTIL",sym.UNTIL);
			}

"read"          {	if(debug) System.out.println("token READ");
			return sf.newSymbol("READ",sym.READ);
			}

"write"         {	if(debug) System.out.println("token WRITE");
			return sf.newSymbol("WRITE",sym.WRITE);
			}

":="            {	if(debug) System.out.println("token ASSIGN");
			return sf.newSymbol("ASSIGN",sym.ASSIGN);
			}

"="             {	if(debug) System.out.println("token EQ");
			return sf.newSymbol("EQ",sym.EQ);
			}

"<"             {	if(debug) System.out.println("token LT");
			return sf.newSymbol("LT",sym.LT);
			}

"+"             {	if(debug) System.out.println("token PLUS");
			return sf.newSymbol("PLUS",sym.PLUS);
			}

"-"             {	if(debug) System.out.println("token MINUS");
			return sf.newSymbol("MINUS",sym.MINUS);
			}


				 /* Arreglos y Listas */


"["             {	if(debug) System.out.println("token LBRACKET");
			return sf.newSymbol("LBRACKET",sym.LBRACKET);
			}

"]"             {	if(debug) System.out.println("token RBRACKET");
			return sf.newSymbol("RBRACKET",sym.RBRACKET);
			}

","             {	if(debug) System.out.println("token COMMA");
			return sf.newSymbol("COMMA",sym.COMMA);
			}



				 /* Comparadores y logica */



">"             {	if(debug) System.out.println("token GT");
			return sf.newSymbol("GT",sym.GT);
			}

"<=" 			{	if(debug) System.out.println("token LE");
			return sf.newSymbol("LE",sym.LE);
			}

">="			{	if(debug) System.out.println("token GE");
			return sf.newSymbol("GE",sym.GE);
			}

"!="			{	if(debug) System.out.println("token NE");
			return sf.newSymbol("NE",sym.NE);
			}

"and"			{	if(debug) System.out.println("token AND");
			return sf.newSymbol("AND",sym.AND);
			}

"or"			{	if(debug) System.out.println("token OR");
			return sf.newSymbol("OR",sym.OR);
			}

"not"			{	if(debug) System.out.println("token NOT");
			return sf.newSymbol("NOT",sym.NOT);
			}



				 /* Operadores Aritmeticos */ 



"*"             {	if(debug) System.out.println("token TIMES");
			return sf.newSymbol("TIMES",sym.TIMES);
			}

"/"             {	if(debug) System.out.println("token OVER");
			return sf.newSymbol("OVER",sym.OVER);
			}

"("             {	if(debug) System.out.println("token LPAREN");
			return sf.newSymbol("LPAREN",sym.LPAREN);
			}

")"             {	if(debug) System.out.println("token RPAREN");
			return sf.newSymbol("RPAREN",sym.RPAREN);
			}

";"             {	if(debug) System.out.println("token SEMI");
			return sf.newSymbol("SEMI",sym.SEMI);
			}

{numero}        {	if(debug) System.out.println("token NUM");
			return sf.newSymbol("NUM",sym.NUM,new String(yytext()));
			}

{cadena}		{	if(debug) System.out.println("token STR");
			return sf.newSymbol("STR",sym.STR,new String(yytext()));
			}

{identificador}	{	if(debug) System.out.println("token ID");
				return sf.newSymbol("ID",sym.ID,new String(yytext()));
			}

{nuevalinea}       {lineanum++;}

{espacio}    { /* saltos espacios en blanco*/}

"{"[^}]+"}"  { /* salto comentarios */ if(debug) System.out.println("token COMENTARIO"); }

.               {System.err.println("Caracter Ilegal encontrado en analisis lexico: " + yytext() + "\n");}