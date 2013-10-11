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
        String expr = "int main(int argc,int argv){bool b,c,d = a || !(c<=c) || -factorial(-12+3,false,a<=w&&c); b = true && false || true;}";
        Parser parser = new Parser(expr);
        Tree n = parser.parse();
        n.printDot("tree.dot");
    }

    public Parser(String input) {
        this.lexer = new Lexer(input);

    }

    private void finish() throws ParseException {
        lexer.matchToken("$");
        //TODO
    }

    public Tree parse() {
        try {
            Tree root = parseProgram();
            finish();
            return root;
        } catch (ParseException e) {
            System.out.println("Parse error: " + e.getMessage()+"\n\n");
            String[] lines = lexer.getInput().split("\n");
            int pos = 0;
            int i = 0;
            for (; i < lines.length; i++) {
                System.out.println((i+1)+": "+ lines[i]);
                if (e.getErrorOffset() <= pos + lines[i].length() && e.getErrorOffset() >= pos) {
                    for (int j = 0; j < e.getErrorOffset() - pos+3+(Math.floor(Math.log10(i+1))); j++) {
                        System.out.print('-');

                    }
                    System.out.println('^');
                }

                pos += (lines[i].length() + 1);
            }


            //e.printStackTrace();

        }
        return new Tree(new Token(Token.Type.EOF, "EOF"));
    }

    //program ::= function*
    private Tree parseProgram() throws ParseException {
        Tree program = new Tree(new Token(Token.Type.EOF, "EOF"));
        while (lexer.getNextToken().getType() != Token.Type.EOF) {
            program.addChild(parseFunction());
        }
        return program;
    }

    //function ::= INT|BOOL|VOID ID(parameters) { functionDefinition }
    private Tree parseFunction() throws ParseException {
        Token token = lexer.getNextToken();
        Tree type;
        if (token.getType() == Token.Type.TYPE) {
            lexer.matchToken(token);
            type = new Tree(token);
        } else {
            throw new ParseException("Unexpected token '" + token.getValue() + "' at line " + (lexer.getLine()+1)+". Expected '"+ Token.Type.TYPE +"'.", lexer.getPosition());
        }

        token = lexer.getNextToken();
        if (token.getType() == Token.Type.IDENTIFIER) {
            lexer.matchToken(token);
            Tree id = new Tree(token);

            lexer.matchToken("(");

            Tree parameters = parseParameters();

            lexer.matchToken(")");
            lexer.matchToken("{");

            Tree definition = parseFunctionDefinition();

            lexer.matchToken("}");

            id.addChild(type);
            id.addChild(parameters);
            id.addChild(definition);
            return id;
        } else {
            throw new ParseException("Unexpected token '" + token.getValue() + "' at line " + (lexer.getLine()+1)+". Expected '"+Token.Type.IDENTIFIER.name()+"'.", lexer.getPosition());
        }

    }

    //parameters ::= ((INT|BOOL) ID (, (INT|BOOL) ID)*)?
    private Tree parseParameters() throws ParseException {
        Tree parameters = new Tree(new Token(Token.Type.DELIMITER, "("));

        if (!lexer.getNextToken().getValue().equals(")")) {
            Tree type;
            Token token = lexer.getNextToken();
            if (token.getType() == Token.Type.TYPE) {
                lexer.matchToken(token);
                type = new Tree(token);
            } else {
                throw new ParseException("Unexpected token '" + token.getValue() + "' at line " + (lexer.getLine()+1), lexer.getPosition());
            }

            token = lexer.getNextToken();
            if (token.getType() != Token.Type.IDENTIFIER) {
                throw new ParseException("Unexpected token '" + token.getValue() + "' at line " + (lexer.getLine()+1), lexer.getPosition());
            }
            lexer.matchToken(token);

            type.addChild(new Tree(token));
            parameters.addChild(type);

            while (lexer.getNextToken().getValue().equals(",")) {
                lexer.matchToken(",");

                token = lexer.getNextToken();

                if (token.getType() == Token.Type.TYPE) {
                    lexer.matchToken(token);
                    type = new Tree(token);
                } else {
                    throw new ParseException("Unexpected token '" + token.getValue() + "' at line " + (lexer.getLine()+1), lexer.getPosition());
                }

                token = lexer.getNextToken();
                if (token.getType() != Token.Type.IDENTIFIER) {
                    throw new ParseException("Unexpected token '" + token.getValue() + "' at line " + (lexer.getLine()+1), lexer.getPosition());
                }
                lexer.matchToken(token);

                type.addChild(new Tree(token));
                parameters.addChild(type);
            }
        }
        return parameters;
    }

    // funtionDefinition ::= stmt*
    private Tree parseFunctionDefinition() throws ParseException {
        Tree definition = new Tree(new Token(Token.Type.DELIMITER, "{"));

        while (!lexer.getNextToken().getValue().equals("}")) {
            definition.addChild(parseStatement());
        }
        return definition;
    }

    /* statement ::= (INT|BOOL) ID (, ID)* (= expression)?; |
     *               WHILE (expression) statement |
     *               IF (expression) statement (ELSE statement)? |
     *               { statement* } |
     *               ;
     *               ID = expression;
     */
    private Tree parseStatement() throws ParseException {
        Token token = lexer.getNextToken();
      
        switch (token.getType()) {
            case DELIMITER:
                switch (token.getValue()) {
                    case "{":
                        Tree block = new Tree(new Token(Token.Type.DELIMITER, "{"));
                        lexer.matchToken("{");
                        while (!lexer.getNextToken().getValue().equals("}")) {
                            block.addChild(parseStatement());
                        }
                        lexer.matchToken("}");
                        return block;
                    case ";":
                        lexer.matchToken(";");
                        return new Tree(new Token(Token.Type.DELIMITER, ";"));
                }
            case IF:
                lexer.matchToken(token);
                lexer.matchToken("(");
                Tree expression = parseExpression();
                lexer.matchToken(")");
                Tree then = parseStatement();
                token = lexer.getNextToken();
                if (token.getType() == Token.Type.ELSE) {
                    lexer.matchToken(token);
                    return new Tree(new Token(Token.Type.IF, "if"), expression, then, parseStatement());
                }
                return new Tree(new Token(Token.Type.IF, "if"), expression, then);
            case WHILE:
                lexer.matchToken(token);
                lexer.matchToken("(");
                expression = parseExpression();
                lexer.matchToken(")");
                return new Tree(token, expression, parseStatement());
            case TYPE:
                lexer.matchToken(token);
                Tree type = new Tree(token);
                
                token = lexer.getNextToken();
                if (token.getType() != Token.Type.IDENTIFIER) {
                    throw new ParseException("Unexpected token '" + token.getValue() + "' at line " + (lexer.getLine()+1), lexer.getPosition());
                }
                lexer.matchToken(token);

                type.addChild(new Tree(token));

                while (lexer.getNextToken().getValue().equals(",")) {
                    lexer.matchToken(",");
                    token = lexer.getNextToken();
                    if (token.getType() != Token.Type.IDENTIFIER) {
                        throw new ParseException("Unexpected token '" + token.getValue() + "' at line " + (lexer.getLine()+1), lexer.getPosition());
                    }
                    lexer.matchToken(token);

                    type.addChild(new Tree(token));
                }
                
                token = lexer.getNextToken();
                if(token.getType() == Token.Type.ASSIGNMENT) {
                    lexer.matchToken(token);
                    expression = parseExpression();
                    type.addChild(new Tree(token, expression));
                }
                
                lexer.matchToken(";");
                return type;
            case IDENTIFIER:
                lexer.matchToken(token);
                lexer.matchToken("=");
                expression = parseExpression();
                lexer.matchToken(";");
                return new Tree(new Token(Token.Type.ASSIGNMENT, "="), new Tree(token), expression);
            default:
                throw new ParseException("Unexpected token '" + token.getValue() + "' at line " + (lexer.getLine()+1), lexer.getPosition());
        }
    }

    //expr ::= term ((+|-) expr)?
    private Tree parseExpression() throws ParseException {
        Tree term = parseTerm();

        Token token = lexer.getNextToken();

        if (token.getType() == Token.Type.PLUS_MINUS) {
            lexer.matchToken(token);
            return new Tree(token, term, parseExpression());
        }
        return term;
    }

    //term :: binaryExpression ((*|/|%) term)?
    private Tree parseTerm() throws ParseException {
        Tree binaryExpression = parseBinaryExpression();

        Token token = lexer.getNextToken();

        if (token.getType() == Token.Type.MUL_DIV_MOD) {
            lexer.matchToken(token);
            return new Tree(token, binaryExpression, parseTerm());
        }

        return binaryExpression;
    }

    //binaryExpression ::= binaryTerm (|| binaryExpression)?
    private Tree parseBinaryExpression() throws ParseException {
        Tree term = parseBinaryTerm();
        Token token = lexer.getNextToken();
        if (token.getType() == Token.Type.OR) {
            lexer.matchToken(token);
            return new Tree(token, term, parseBinaryExpression());
        }
        return term;
    }

    //binaryTerm ::= relation (&& binaryTerm)?
    private Tree parseBinaryTerm() throws ParseException {
        Tree relation = parseRelation();
        Token token = lexer.getNextToken();
        if (token.getType() == Token.Type.AND) {
            lexer.matchToken(token);
            return new Tree(token, relation, parseBinaryTerm());
        }

        return relation;
    }

    //relation ::= binaryFactor ((<|<=|>|>=|==|!=|) relation)?
    private Tree parseRelation() throws ParseException {
        Tree factor = parseBinaryFactor();

        Token token = lexer.getNextToken();

        if (token.getType() == Token.Type.RELATION) {
            lexer.matchToken(token);
            return new Tree(token, factor, parseRelation());
        }

        return factor;
    }

    //binaryFactor ::= (+|-|!)? ID|BOOL|INT|functionCall|(expression)
    private Tree parseBinaryFactor() throws ParseException {


        Token token = lexer.getNextToken();
        Tree factor = null;
        if (token.getType() == Token.Type.PLUS_MINUS || token.getType() == Token.Type.NOT) {
            lexer.matchToken(token);
            factor = new Tree(token);
            token = lexer.getNextToken();
        }


        switch (token.getType()) {
            case LITERAL:
                lexer.matchToken(token);
                Tree literal = new Tree(token);
                if (factor == null) {
                    return literal;
                }
                factor.addChild(literal);
                return factor;

            case IDENTIFIER:
                lexer.matchToken(token);
                Tree identifier = new Tree(token);
                token = lexer.getNextToken();
                if(token.getValue().equals("(")) {
                    lexer.matchToken(token);
                     
                    if(!lexer.getNextToken().getValue().equals(")")) {
                        Tree args = new Tree(token);
                        args.addChild(parseExpression());
                        while(lexer.getNextToken().getValue().equals(",")) {
                            lexer.matchToken(",");
                            args.addChild(parseExpression());
                        }
                        identifier.addChild(args);
                    }
                    lexer.matchToken(")");
                }
                
                if (factor == null) {
                    return identifier;
                }
                factor.addChild(identifier);
                return factor;

            case DELIMITER:
                if (!token.getValue().equals("(")) {
                    throw new ParseException("Unexpected token '" + token.getValue() + "' at line " + (lexer.getLine()+1), lexer.getPosition());
                }
                lexer.matchToken("(");
                Tree expression = parseExpression();
                lexer.matchToken(")");
                if (factor == null) {
                    return expression;
                }
                factor.addChild(expression);
                return factor;
            default:
                throw new ParseException("Unexpected token '" + token.getValue() + "' at line " + (lexer.getLine()+1), lexer.getPosition());
        }
    }
}
