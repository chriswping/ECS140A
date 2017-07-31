/* *** This file is given as part of the programming assignment. *** */
import java.util.*;


class Var {
    public String name;
    public int value;
    public Var(String n1, int number){
        name = n1;
        value = number;
    }
    public void setname(String newString){
        name = newString;
    }
    public void setnumber(int newNumber){
        value = newNumber;
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
        Var temp1 = new Var(tok.string,0);
//        System.err.println("adding variable");
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
	block();
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
	while( is(TK.DECLARE) ) {
	    declaration();
	}
    }

    private void declaration() {
	mustbe(TK.DECLARE);
        if(checkscope()){
//            System.err.println("redeclaration of variable "+tok.string);
        }
        else {
            addvar();
        }
	mustbe(TK.ID);
	while( is(TK.COMMA) ) {
	    scan();
            if(checkscope()){
//                System.err.println("redeclaration of variable "+tok.string);
            }
            else {
                addvar();
            }
	    mustbe(TK.ID);
	}
    }
// starting
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
        expr();
    } // print
    private void assignment() {
        ref_id();
//	if (is (TK.ASSIGN)) {
            mustbe(TK.ASSIGN);
//        }
/*
        else {
            System.err.println(ptvar + " is an undeclared variable on line " + ptline);
            System.exit(1);
        }
*/
        expr();
    } // assignment
    private void ref_id() { // may have errors
        if( is(TK.TILDE)) {
            mustbe(TK.TILDE);
            if(is(TK.NUM)) {
                int hold = Integer.parseInt(tok.string);
                level = level - (Integer.parseInt(tok.string));
                if (level < 0){ // out of scope range
                    ptvar = tok.string;
                    mustbe(TK.NUM);
/*
                    System.err.println("no such variable ~" + ptvar + tok.string 
                                       + " on line " + tok.lineNumber); 
                    System.exit(1);
*/
                }
                else { // check the specific scope
                    mustbe(TK.NUM);
/*
                    if (!checkscope()){
                          System.err.println("no such variable ~" + hold + tok.string 
                                             + " on line " + tok.lineNumber);
                        System.exit(1);
                    }
*/
                }
                level = mlevel;
            }
            else { // check the global scope
                level = 0;
/*
                if(!checkscope()){
                    System.err.println("no such variable ~" + tok.string 
                                       + " on line " + tok.lineNumber); 
                }
*/
                level = mlevel;
            }
        }
     
        if (is(TK.ID)) {
/*
            while(!checkscope()){ // checks if in scope
                level--;
/*  
                if (level < 0) {
                   System.err.println(tok.string + " is an undeclared variable on line " 
                                      + tok.lineNumber);
                   System.exit(1);
                   break;
                }
*/
/*
            } 
            if (level > -1) {// if found
                Var temp2 = new Var(tok.string, list.get(level).get(ptline).value);
                level = mlevel;
                list.get(level).add(temp2);
            }
            level = mlevel;
            ptline = tok.lineNumber;
            ptvar = tok.string;
*/
            mustbe(TK.ID);
        }
        else
            System.exit(1);

    } // ref_id()
    private void fdo() {
        mustbe(TK.DO);
        guarded_command();

        if (is(TK.ENDDO))
            mustbe(TK.ENDDO);
        else
            System.exit(1);

    }
    private void fif() {
        mustbe(TK.IF);
        guarded_command();
        while( is(TK.ELSEIF) ) { // may have errors here
            scan();
            guarded_command();
        }
        if (is(TK.ELSE)) {
            scan();
            block();
        }
        mustbe(TK.ENDIF);
    }
    private void guarded_command() {
        expr();
        if (is(TK.THEN))
            mustbe(TK.THEN);
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
            mustbe(TK.LPAREN);
            expr();
            if(is(TK.RPAREN))
                mustbe(TK.RPAREN);
            else
                System.exit(1);
        }
        else if (is(TK.TILDE) || is(TK.ID)) {
            ref_id();
        }
        else if (is(TK.NUM)) {
            mustbe(TK.NUM);
        }
    }
    private void addop() {
        if (is(TK.PLUS)) {
            mustbe(TK.PLUS);
        }
        else if(is(TK.MINUS)) {
            mustbe(TK.MINUS);
        } 
    }
    private void multop() {
        if (is(TK.TIMES)) {
            mustbe(TK.TIMES);
        }
        else if(is(TK.DIVIDE)) {
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
