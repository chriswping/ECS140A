/* *** This file is given as part of the programming assignment. *** */
// Author: Galen Tan
import java.util.*;


class Var {
    public String name;
    public int value, ref;
    public Var(String n1, int number, int n2){
        name = n1;
        value = number; // wasn't needed in the program as didn't need to save value
        ref = n2;
    }
    public void setname(String newString){
        name = newString;
    }
    public void setnumber(int newNumber){
        value = newNumber;
    }
    public void setref(int newref){
        ref = newref;
    }
}

public class Parser {
/*
    class Var {
        public String name;
        public int value;
    }
*/
    public boolean printing;
    public String ptvar;
    public int ptline;
    public int printed = 0;
    public Var temp;
    private int mlevel;
    ArrayList<ArrayList<Var>> list = new ArrayList<ArrayList<Var>>();
    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private int level = -1;
    private void scan() {
	tok = scanner.scan();
    }
    private void newlevel() {
        level++;
        mlevel = level;
        ArrayList<Var> lelist = new ArrayList<Var>(); // lelist = level list
        list.add(level,lelist);
    }

    private void exitlevel() {
        list.remove(level);
        level--;
        mlevel = level;
    }
    
    private void addvar() {
        Var temp1 = new Var(tok.string,0,level);
        list.get(level).add(temp1);
    }
    private boolean checkscope() {
        for(int i = 0; i < list.get(level).size(); i++) {
            if(list.get(level).get(i).name.equals(tok.string)) {
                ptline = i;
                return(true);
            }
        }
        return false;
    }

    private Scan scanner;
    Parser(Scan scanner) {
	this.scanner = scanner;
	scan();
	program();
	if( tok.kind != TK.EOF )
	    parse_error("junk after logical end of program");
    }

    private void program() {
        System.out.print("#include <stdio.h>\n" + "main(){\n");
	block();
        System.out.print("}");
    }

    private void block(){
        newlevel();
	declaration_list();
	statement_list();
        exitlevel();
    }

    private void declaration_list() {
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.
	while( is(TK.DECLARE) || is(TK.FOR)) {
	        declaration();
	}
    }

    private void declaration() {
	mustbe(TK.DECLARE);
        if(checkscope()){
            System.err.println("redeclaration of variable "+tok.string);
        }
        else {
            addvar();
            System.out.print("int ");
            System.out.print("x_" + tok.string + level + ";\n");
        }
	mustbe(TK.ID);
	while( is(TK.COMMA) ) {
	    scan();
            if(checkscope()){
                System.err.println("redeclaration of variable "+tok.string);
            }
            else {
                addvar();
                System.out.print("int ");
                System.out.print("x_" + tok.string + level + ";\n");
            }
	    mustbe(TK.ID);
	}
        if (is(TK.FOR)){
            ffor();
        }
    }
// starting

    private void ffor(){
        int first;
        int second;
        mustbe(TK.FOR);
        ptvar = tok.string;
        if (!checkscope()){
            System.err.println(tok.string + " is not declared");
            System.exit(1);
        }
        mustbe(TK.ID);
        if (!is(TK.NUM)){
            System.err.println("First number is not found");
            System.exit(1);
        }
        first = Integer.parseInt(tok.string);
        System.out.print("x_" + ptvar + level + " = " + tok.string + ";\n");
	System.out.print("for(x_" + ptvar + level  + "; x_" + ptvar + 
                         level+ " <= ");
        mustbe(TK.NUM);
        if (!is(TK.NUM)){
            System.err.println("Second number is not found");
            System.exit(1);
        }
        second = Integer.parseInt(tok.string);
        if (first > second){
            System.err.println("First number is greater than the Second");
            System.exit(1);
        }
        System.out.print(tok.string + "; x_" + ptvar + level + "++) {\n");
        mustbe(TK.NUM);
        while (first != second){
            first++;
            block();
        }
        System.out.print("}\n");
        mustbe(TK.ENDFOR);
    }
// for

    private void statement_list() {
        while(is(TK.TILDE) || is(TK.ID) || is(TK.PRINT) || is(TK.DO) || is(TK.IF)) { 
            statement();
        }
    } // statement_list

    private void statement() {
        if(is(TK.TILDE) || is(TK.ID)) {
            assignment();
        }
	else if (is(TK.PRINT)) {
            print();
        }
        else if (is(TK.DO)) {
            fdo();
        }
        else if (is(TK.IF)) {
            fif();
        }
    } // statement
    private void print() {
        mustbe(TK.PRINT);
        System.out.print("printf(\"%d\\n\", ");
        expr();
        System.out.print(");\n");
    } // print
    private void assignment() {
        ref_id();
	if (is (TK.ASSIGN)) {
            mustbe(TK.ASSIGN);
            System.out.print(" = ");
        }
        else {
            System.err.println(ptvar + " is an undeclared variable on line " + ptline);
            System.exit(1);
        }
        expr();
        System.out.print(";\n");
    } // assignment
    private void ref_id() { // may have errors
        printed = 0;
        if( is(TK.TILDE)) {
            mustbe(TK.TILDE);
            if(is(TK.NUM)) {
                int hold = Integer.parseInt(tok.string);
                level = level - (Integer.parseInt(tok.string));
                if (level < 0){ // out of scope range
                    ptvar = tok.string;
                    mustbe(TK.NUM);
                    System.err.println("no such variable ~" + ptvar + tok.string 
                                       + " on line " + tok.lineNumber); 
                    System.exit(1);
                }
                else { // check the specific scope
                    mustbe(TK.NUM);
                    if (!checkscope()){
                          System.err.println("no such variable ~" + hold + tok.string 
                                             + " on line " + tok.lineNumber);
                        System.exit(1);
                    }
                    System.out.print("x_" + tok.string + level);
                    printed = 1;
                }
                level = mlevel;
            }
            else { // check the global scope
                level = 0;
                if(!checkscope()){
                    System.err.println("no such variable ~" + tok.string 
                                        + " on line " + tok.lineNumber); 
                    System.exit(1);
                }
                System.out.print("x_" + tok.string + level);
                printed = 1;
                level = mlevel;
            }
        }
        if (is(TK.ID)) {
            while(!checkscope()){ // checks if in scope
                level--;
                if (level < 0) { // not in any scope
                   System.err.println(tok.string + " is an undeclared variable on line " 
                                      + tok.lineNumber);
                   System.exit(1);
                   break;
                }
            } 
            
            if (printed == 0 && level > -1) {// if found out of scope
                Var temp2 = new Var(tok.string, list.get(level).get(ptline).value,
                                    list.get(level).get(ptline).ref);
                if (printed == 0) {
                  System.out.print("x_"+tok.string + list.get(level).get(ptline).ref);
                }
                level = mlevel;
                list.get(level).add(temp2);
            }
            level = mlevel;
            ptline = tok.lineNumber;
            ptvar = tok.string;
            mustbe(TK.ID);
        }
        else {
            System.exit(1); }

    } // ref_id()
    private void fdo() {
        mustbe(TK.DO);
        System.out.print("while(");
        guarded_command();
        System.out.print("}");
        if (is(TK.ENDDO))
            mustbe(TK.ENDDO);
        else
            System.exit(1);

    }
    private void fif() {
        mustbe(TK.IF);
        System.out.print("if ( ");
        guarded_command();
        System.out.print("}\n");
        while( is(TK.ELSEIF) ) { // else if
            scan();
            System.out.print("else if (");
            guarded_command();
            System.out.print("}\n");
        }
        if (is(TK.ELSE)) {
            scan();
            System.out.print("else {\n");
            block();
            System.out.print("}\n");
        }
        mustbe(TK.ENDIF);
    }
    private void guarded_command() {
        expr();
        if (is(TK.THEN)){  // need to adjust where <= 0 is placed
            System.out.print(" <= 0) {\n");
            mustbe(TK.THEN);
        }
        else
            System.exit(1);
        block();
    }
    private void expr() {
        term();
        while( is(TK.PLUS) || is(TK.MINUS)) {
            addop();
            term();
        }
//        System.out.print(" <= 0");
    }
    private void term() {
        factor();
        while( is(TK.TIMES) || is(TK.DIVIDE)) {
            multop();
            factor();
        }

    }
    private void factor() {
        if (is(TK.LPAREN)) {
            System.out.print("(");
            mustbe(TK.LPAREN);
            expr();
            if(is(TK.RPAREN)){
                System.out.print(")");
                mustbe(TK.RPAREN);
            }
            else
                System.exit(1);
        }
        else if (is(TK.TILDE) || is(TK.ID)) {
            ref_id();
        }
        else if (is(TK.NUM)) {
            System.out.print(tok.string);
            mustbe(TK.NUM);
        }
    }
    private void addop() {
        if (is(TK.PLUS)) {
            System.out.print("+");
            mustbe(TK.PLUS);
        }
        else if(is(TK.MINUS)) {
            System.out.print("-");
            mustbe(TK.MINUS);
        } 
    }
    private void multop() {
        if (is(TK.TIMES)) {
            System.out.print("*");
            mustbe(TK.TIMES);
        }
        else if(is(TK.DIVIDE)) {
            System.out.print("/");
            mustbe(TK.DIVIDE);
        }
    }


// end edited
    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
	if( tok.kind != tk ) {
	    System.err.println( "mustbe: want " + tk + ", got " +
				    tok);
	    parse_error( "missing token (mustbe)" );
	}
	scan();
    }

    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	System.exit(1);
    }
}
