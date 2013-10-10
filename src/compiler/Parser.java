/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import java.text.ParseException;

/**
 *
 * @author emanuellaude
 */
public class Parser {

    private Lexer lexer;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //String expr = "(abc+123+_12hkhl78)==(2*(2*(3!=5<=23+!(-12))))";
        String expr = " void arsch_losch(){;;} int  mainadsf ( int a , int  b , bool c )  {  int a,b; abc = (32+ -1)<=(-7) || +2== 2; while   ( true <=(12 %(a+b-c))*+a-b && asdf-y || +33 && -(a<b+1)%e ) { ; adsf = 12<=7 ; if(true) {} else {;} }  int a; bool ax; }   ";
        Parser parser = new Parser(expr);
        Node n = parser.parse();
        n.printDot("tree.dot");
    }

    public Parser(String input) {
        this.lexer = new Lexer(input);

    }

    private void finish() throws ParseException {
        lexer.matchToken("$");
        //TODO
    }

    public Node parse() {
        try {
            Node root = parseProgram();
            finish();
            return root;
        } catch (ParseException e) {
            System.out.println("Parse error: " + e.getMessage());
            System.out.println(lexer.getInput());
            for (int i = 0; i < e.getErrorOffset(); i++) {
                System.out.print(' ');
            }
            System.out.println('^');
            e.printStackTrace();
            return null;
        }
    }

    //program ::= function*
    private Node parseProgram() throws ParseException {
        Node program = new Node("EOF");
        while (lexer.getNextToken().getType() != Token.Type.EOF) {
            program.addChild(parseFunction());
        }
        return program;
    }

    //function ::= INT|BOOL|VOID ID(parameters) { functionDefinition }
    private Node parseFunction() throws ParseException {
        Token token = lexer.getNextToken();
        Node type;
        if (token.getType() == Token.Type.TYPE) {
            lexer.matchToken(token);
            type = new Node(token.getValue());
        } else {
            throw new ParseException("Unexpected token '" + token.getValue() + "'", lexer.getPosition());
        }

        token = lexer.getNextToken();
        if (token.getType() == Token.Type.IDENTIFIER) {
            lexer.matchToken(token);
            Node id = new Node(token.getValue());

            lexer.matchToken("(");

            Node parameters = parseParameters();

            lexer.matchToken(")");
            lexer.matchToken("{");

            Node definition = parseFunctionDefinition();

            lexer.matchToken("}");

            id.addChild(type);
            id.addChild(parameters);
            id.addChild(definition);
            return id;
        } else {
            throw new ParseException("Unexpected token '" + token.getValue() + "'", lexer.getPosition());
        }

    }

    //parameters ::= ((INT|BOOL) ID (, (INT|BOOL) ID)*)?
    private Node parseParameters() throws ParseException {
        Node parameters = new Node("(");

        if (!lexer.getNextToken().getValue().equals(")")) {
            Node type;
            Token token = lexer.getNextToken();
            if (token.getType() == Token.Type.TYPE) {
                lexer.matchToken(token);
                type = new Node(token.getValue());
            } else {
                throw new ParseException("Unexpected token '" + token.getValue() + "'", lexer.getPosition());
            }

            token = lexer.getNextToken();
            if (token.getType() != Token.Type.IDENTIFIER) {
                throw new ParseException("Unexpected token '" + token.getValue() + "'", lexer.getPosition());
            }
            lexer.matchToken(token);

            type.addChild(new Node(token.getValue()));
            parameters.addChild(type);

            while (lexer.getNextToken().getValue().equals(",")) {
                lexer.matchToken(",");

                token = lexer.getNextToken();

                if (token.getType() == Token.Type.TYPE) {
                    lexer.matchToken(token);
                    type = new Node(token.getValue());
                } else {
                    throw new ParseException("Unexpected token '" + token.getValue() + "'", lexer.getPosition());
                }

                token = lexer.getNextToken();
                if (token.getType() != Token.Type.IDENTIFIER) {
                    throw new ParseException("Unexpected token '" + token.getValue() + "'", lexer.getPosition());
                }
                lexer.matchToken(token);

                type.addChild(new Node(token.getValue()));
                parameters.addChild(type);
            }
        }
        return parameters;
    }

    // funtionDefinition ::= stmt*
    private Node parseFunctionDefinition() throws ParseException {
        Node definition = new Node("{");

        while (!lexer.getNextToken().getValue().equals("}")) {
            definition.addChild(parseStatement());
        }
        return definition;
    }

    /* statement ::= (INT|BOOL) ID (, ID)*; |
     *               WHILE (expression) statement |
     *               IF (expression) statement (ELSE statement)? |
     *               { statement* } |
     *               ;
     *               ID = expression;
     */
    private Node parseStatement() throws ParseException {
        Token token = lexer.getNextToken();

        switch (token.getType()) {
            case DELIMITER:
                switch (token.getValue()) {
                    case "{":
                        Node block = new Node("{");
                        lexer.matchToken("{");
                        while (!lexer.getNextToken().getValue().equals("}")) {
                            block.addChild(parseStatement());
                        }
                        lexer.matchToken("}");
                        return block;
                    case ";":
                        lexer.matchToken(";");
                        return new Node(";");
                }
            case IF:
                lexer.matchToken(token);
                lexer.matchToken("(");
                Node expression = parseExpression();
                lexer.matchToken(")");
                Node then = parseStatement();
                token = lexer.getNextToken();
                if (token.getType() == Token.Type.ELSE) {
                    lexer.matchToken(token);
                    return new Node("if", expression, then, parseStatement());
                }
                return new Node("if", expression, then);
            case WHILE:
                lexer.matchToken(token);
                lexer.matchToken("(");
                expression = parseExpression();
                lexer.matchToken(")");
                return new Node(token.getValue(), expression, parseStatement());
            case TYPE:
                lexer.matchToken(token);
                Node type = new Node(token.getValue());
                token = lexer.getNextToken();
                if (token.getType() != Token.Type.IDENTIFIER) {
                    throw new ParseException("Unexpected token '" + token.getValue() + "'", lexer.getPosition());
                }
                lexer.matchToken(token);

                type.addChild(new Node(token.getValue()));

                while (lexer.getNextToken().getValue().equals(",")) {
                    lexer.matchToken(",");
                    token = lexer.getNextToken();
                    if (token.getType() != Token.Type.IDENTIFIER) {
                        throw new ParseException("Unexpected token '" + token.getValue() + "'", lexer.getPosition());
                    }
                    lexer.matchToken(token);

                    type.addChild(new Node(token.getValue()));
                }
                lexer.matchToken(";");
                return type;
            case IDENTIFIER:
                lexer.matchToken(token);
                lexer.matchToken("=");
                expression = parseExpression();
                lexer.matchToken(";");
                return new Node("=", new Node(token.getValue()), expression);
            default:
                throw new ParseException("Unexpected token '" + token.getValue() + "'", lexer.getPosition());
        }
    }

    //expr ::= term ((+|-) expr)?
    private Node parseExpression() throws ParseException {
        Node term = parseTerm();
        
        Token token = lexer.getNextToken();

        if(token.getType() == Token.Type.PLUS_MINUS) {
            lexer.matchToken(token);
            return new Node(token.getValue(), term, parseExpression());
        }
        return term;
    }

    //term :: binaryExpression ((*|/|%) term)?
    private Node parseTerm() throws ParseException {
        Node binaryExpression = parseBinaryExpression();

        Token token = lexer.getNextToken();
        
        if(token.getType() == Token.Type.MUL_DIV_MOD) {
            lexer.matchToken(token);
            return new Node(token.getValue(), binaryExpression, parseTerm());
        }

        return binaryExpression;
    }

    //binaryExpression ::= binaryTerm (|| binaryExpression)?
    private Node parseBinaryExpression() throws ParseException {
        Node term = parseBinaryTerm();
        Token token = lexer.getNextToken();
        if (token.getType() == Token.Type.OR) {
            lexer.matchToken(token);
            return new Node(token.getValue(), term, parseBinaryExpression());
        }
        return term;
    }

    //binaryTerm ::= relation (&& binaryTerm)?
    private Node parseBinaryTerm() throws ParseException {
        Node relation = parseRelation();
        Token token = lexer.getNextToken();
        if (token.getType() == Token.Type.AND) {
            lexer.matchToken(token);
            return new Node(token.getValue(), relation, parseBinaryTerm());
        }

        return relation;
    }

    //relation ::= binaryFactor ((<|<=|>|>=|==|!=|) relation)?
    private Node parseRelation() throws ParseException {
        Node factor = parseBinaryFactor();

        Token token = lexer.getNextToken();
        
        if(token.getType() == Token.Type.RELATION) {
            lexer.matchToken(token);
            return new Node(token.getValue(), factor, parseRelation());
        }

        return factor;
    }

    //binaryFactor ::= (+|-|!)? ID|BOOL|INT|(expression)
    private Node parseBinaryFactor() throws ParseException {
        
        
        Token token = lexer.getNextToken();
        Node factor = null;
        if(token.getType() == Token.Type.PLUS_MINUS || token.getType() == Token.Type.NOT) {
            lexer.matchToken(token);
            factor = new Node(token.getValue());
            token = lexer.getNextToken();
        }
        
        
        switch(token.getType()) {
            case LITERAL:
                lexer.matchToken(token);
                Node literal = new Node(token.getValue());
                if(factor == null) {
                    return literal;
                }
                factor.addChild(literal);
                return factor;
                    
            case IDENTIFIER:
                lexer.matchToken(token);
                Node identifier = new Node(token.getValue());
                if(factor == null) {
                    return identifier;
                }
                factor.addChild(identifier);
                return factor;
                
            case DELIMITER:
                if(!token.getValue().equals("(")) {
                    throw new ParseException("Unexpected token '" + token.getValue() + "'", lexer.getPosition());
                }
                lexer.matchToken("(");
                Node expression = parseExpression();
                lexer.matchToken(")");
                if(factor == null) {
                    return expression;
                }
                factor.addChild(expression);
                return factor;
            default:
               throw new ParseException("Unexpected token '" + token.getValue() + "'", lexer.getPosition()); 
        }
    }
}
