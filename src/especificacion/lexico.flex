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
		this.sf= (DefaultSymbolFactory) sf;
		debug=false;
	}

	private DefaultSymbolFactory sf;
	private boolean debug;

%}

%eofval{
	return sf.newSymbol("EOF", sym.EOF, yyline+1, yycolumn+1);
%eofval}

	/* Expresiones regulares */

digito = [0-9]
numero = {digito}+
letra = [a-zA-Z]
identificador = {letra}({letra}|{digito}|_)*
espacio = [ \t]+
nuevalinea = \r|\n|\r\n
cadena = \"[^\r\n\"\\]*\"

%%


	 /* Tipos de datos y literales */


"int"           { if(debug) System.out.println("token INT");
					return sf.newSymbol("INT", sym.INT, yyline + 1, yycolumn + 1); }

"bool"          { if(debug) System.out.println("token BOOL");
					return sf.newSymbol("BOOL", sym.BOOL, yyline + 1, yycolumn + 1); }

"string"        { if(debug) System.out.println("token STRING");   
					return sf.newSymbol("STRING", sym.STRING, yyline + 1, yycolumn + 1); }

"true"          { if(debug) System.out.println("token TRUE");      
					return sf.newSymbol("TRUE", sym.TRUE, yyline + 1, yycolumn + 1); }

"false"         { if(debug) System.out.println("token FALSE");     
					return sf.newSymbol("FALSE", sym.FALSE, yyline + 1, yycolumn + 1); }


	/* Subrutinas y funciones */


"function"      { if(debug) System.out.println("token FUNCTION"); 
					return sf.newSymbol("FUNCTION", sym.FUNCTION, yyline + 1, yycolumn + 1); }

"procedure"     { if(debug) System.out.println("token PROCEDURE"); 
					return sf.newSymbol("PROCEDURE", sym.PROCEDURE, yyline + 1, yycolumn + 1); }

"return"        { if(debug) System.out.println("token RETURN");    
					return sf.newSymbol("RETURN", sym.RETURN, yyline + 1, yycolumn + 1); }


	/* Palabras reservadas base */


"if"            { if(debug) System.out.println("token IF");
			        return sf.newSymbol("IF", sym.IF, yyline + 1, yycolumn + 1); }

"then"          { if(debug) System.out.println("token THEN");
					return sf.newSymbol("THEN", sym.THEN, yyline + 1, yycolumn + 1); }

"else"          { if(debug) System.out.println("token ELSE");
					return sf.newSymbol("ELSE", sym.ELSE, yyline + 1, yycolumn + 1); }

"end"           { if(debug) System.out.println("token END");       
					return sf.newSymbol("END", sym.END, yyline + 1, yycolumn + 1); }

"repeat"        { if(debug) System.out.println("token REPEAT");    
					return sf.newSymbol("REPEAT", sym.REPEAT, yyline + 1, yycolumn + 1); }

"until"         { if(debug) System.out.println("token UNTIL");     
					return sf.newSymbol("UNTIL", sym.UNTIL, yyline + 1, yycolumn + 1); }

"read"          { if(debug) System.out.println("token READ");      
					return sf.newSymbol("READ", sym.READ, yyline + 1, yycolumn + 1); }

"write"         { if(debug) System.out.println("token WRITE");     
					return sf.newSymbol("WRITE", sym.WRITE, yyline + 1, yycolumn + 1); }


	/* Operadores y asignacion */


":="            { if(debug) System.out.println("token ASSIGN");    
					return sf.newSymbol("ASSIGN", sym.ASSIGN, yyline + 1, yycolumn + 1); }

"="             { if(debug) System.out.println("token EQ");        
					return sf.newSymbol("EQ", sym.EQ, yyline + 1, yycolumn + 1); }

"<"             { if(debug) System.out.println("token LT");        
					return sf.newSymbol("LT", sym.LT, yyline + 1, yycolumn + 1); }

">"             { if(debug) System.out.println("token GT");        
					return sf.newSymbol("GT", sym.GT, yyline + 1, yycolumn + 1); }

"<="            { if(debug) System.out.println("token LE");        
					return sf.newSymbol("LE", sym.LE, yyline + 1, yycolumn + 1); }

">="            { if(debug) System.out.println("token GE");        
					return sf.newSymbol("GE", sym.GE, yyline + 1, yycolumn + 1); }

"!="            { if(debug) System.out.println("token NE");        
					return sf.newSymbol("NE", sym.NE, yyline + 1, yycolumn + 1); }


	/* Logica booleana */


"and"           { if(debug) System.out.println("token AND");       
					return sf.newSymbol("AND", sym.AND, yyline + 1, yycolumn + 1); }

"or"            { if(debug) System.out.println("token OR");        
					return sf.newSymbol("OR", sym.OR, yyline + 1, yycolumn + 1); }

"not"           { if(debug) System.out.println("token NOT");       
					return sf.newSymbol("NOT", sym.NOT, yyline + 1, yycolumn + 1); }


	/* Operadores aritmeticos */


"+"             { if(debug) System.out.println("token PLUS");      
					return sf.newSymbol("PLUS", sym.PLUS, yyline + 1, yycolumn + 1); }

"-"             { if(debug) System.out.println("token MINUS");     
					return sf.newSymbol("MINUS", sym.MINUS, yyline + 1, yycolumn + 1); }

"*"             { if(debug) System.out.println("token TIMES");     
					return sf.newSymbol("TIMES", sym.TIMES, yyline + 1, yycolumn + 1); }

"/"             { if(debug) System.out.println("token OVER");      
					return sf.newSymbol("OVER", sym.OVER, yyline + 1, yycolumn + 1); }


	/* Delimitadores y puntuacion */


"("             { if(debug) System.out.println("token LPAREN");    
					return sf.newSymbol("LPAREN", sym.LPAREN, yyline + 1, yycolumn + 1); }

")"             { if(debug) System.out.println("token RPAREN");    
					return sf.newSymbol("RPAREN", sym.RPAREN, yyline + 1, yycolumn + 1); }

"["             { if(debug) System.out.println("token LBRACKET");  
					return sf.newSymbol("LBRACKET", sym.LBRACKET, yyline + 1, yycolumn + 1); }

"]"             { if(debug) System.out.println("token RBRACKET");  
					return sf.newSymbol("RBRACKET", sym.RBRACKET, yyline + 1, yycolumn + 1); }

","             { if(debug) System.out.println("token COMMA");     
					return sf.newSymbol("COMMA", sym.COMMA, yyline + 1, yycolumn + 1); }

";"             { if(debug) System.out.println("token SEMI");      
					return sf.newSymbol("SEMI", sym.SEMI, yyline + 1, yycolumn + 1); }


	/* Tokens con valor semantico */


{numero}        { if(debug) System.out.println("token NUM");       
					return sf.newSymbol("NUM", sym.NUM, yyline + 1, yycolumn + 1, yytext()); }

{cadena}		{ if(debug) System.out.println("token STR");       
					return sf.newSymbol("STR", sym.STR, yyline + 1, yycolumn + 1, yytext().substring(1, yytext().length() - 1)); }


{identificador} { if(debug) System.out.println("token ID");        
					return sf.newSymbol("ID", sym.ID, yyline + 1, yycolumn + 1, yytext()); }


	/* Espacios, comentarios y lneas */


{nuevalinea}    { /* JFlex actualiza yyline automticamente */ }

{espacio}       { /* Ignorar espacios y tabulaciones */ }

"{"[^}]*"}"     { if(debug) System.out.println("token COMENTARIO"); /* Comentarios vacos o con texto */ }


	/* Errores lexico */


.               {
                  System.err.println("Error lexico en la linea " + (yyline + 1) + ", columna " + (yycolumn + 1) + ": Caracter ilegal '" + yytext() + "'");
                }