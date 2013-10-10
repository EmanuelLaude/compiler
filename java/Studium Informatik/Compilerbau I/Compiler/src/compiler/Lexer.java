/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author emanuellaude
 */
public class Lexer {
    
    private int position = 0;
    private String input;
    
    private static final String LETTERS = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final Map<String, Token.Type> RESERVED_KEYWORDS = Collections.unmodifiableMap(new HashMap<String, Token.Type>() {
        {
            put("while", Token.Type.WHILE);
            put("if", Token.Type.IF);
            put("else", Token.Type.ELSE);
            put("int", Token.Type.TYPE);
            put("bool", Token.Type.TYPE);
            put("void", Token.Type.TYPE);
            put("true", Token.Type.LITERAL);
            put("false", Token.Type.LITERAL);
        }
    });

    public Lexer(String input) {
        this.input = input + Token.EOF;
        this.position = 0;
    }

    public String getInput() {
        return input;
    }

    public int getPosition() {
        return position;
    }
    
    private boolean isDigit(char lookahead) {
        for (int i = 0; i < DIGITS.length(); i++) {
            if (DIGITS.charAt(i) == lookahead) {
                return true;
            }
        }
        return false;
    }

    private boolean isLetter(char lookahead) {
        for (int i = 0; i < LETTERS.length(); i++) {
            if (LETTERS.charAt(i) == lookahead) {
                return true;
            }
        }
        return false;
    }

    private String getNumber() {
        int p = this.position;
        if (p < input.length()) {
            char y;
            String number = "";
            while (isDigit(y = input.charAt(p))) {
                number += y;
                p++;
                if (p >= input.length()) {
                    break;
                }
                y = input.charAt(p);
            }
            return number;

        } else {
            return "" + Token.EOF;
        }
    }

    private String getIdentifier() {
        int p = this.position;
        if (p < input.length()) {
            char y = input.charAt(p);
            String id = "";
            if (!isLetter(y)) {
                return id;
            }
            do {
                id += y;
                p++;
                if (p >= input.length()) {
                    break;
                }
                y = input.charAt(p);
            } while (isLetter(y) || isDigit(y));
            return id;
        } else {
            return "" + Token.EOF;
        }
    }
    
    private char peek(int n) {
        int p = position + n - 1;
        if (p < input.length()) {
            return input.charAt(p);
        }
        return Token.EOF;
    }
    
    public void matchToken(String value) throws ParseException {
        skipWhitespace();
        if (position + value.length() <= input.length()) {
            for (int i = 0; i < value.length(); i++) {
                char y = input.charAt(position);

                if (y == value.charAt(i)) {
                    position++;
                } else {
                    throw new ParseException("Unexpected character '" + y + "'.", position);
                }
            }
            
        } else {
            throw new ParseException("Unexpected end of file.", position + value.length());
        }
    }
    
    public void matchToken(Token token) throws ParseException {
        matchToken(token.getValue());
    }
    
    public Token getNextToken() throws ParseException {
        skipWhitespace();
        int p = this.position;
        if (p < input.length()) {
            if (isLetter(peek(1))) {
                String value = getIdentifier();
                Token.Type type = RESERVED_KEYWORDS.get(value);
                return new Token(type == null ? Token.Type.IDENTIFIER : type, value);
            } else if (isDigit(peek(1))) {
                return new Token(Token.Type.LITERAL, getNumber());
            }
            
            switch (peek(1)) {
                case '+':
                    return new Token(Token.Type.PLUS_MINUS, "+");
                case '-':
                    return new Token(Token.Type.PLUS_MINUS, "-");
                case '*':
                    return new Token(Token.Type.MUL_DIV_MOD, "*");
                case '/':
                    return new Token(Token.Type.MUL_DIV_MOD, "/");
                case '%':
                    return new Token(Token.Type.MUL_DIV_MOD, "%");
                case '=':
                    if (peek(2) == '=') {
                        return new Token(Token.Type.RELATION, "==");
                    } else {
                        return new Token(Token.Type.ASSIGNMENT, "=");
                    }
                case '!':
                    if (peek(2) == '=') {
                        return new Token(Token.Type.RELATION, "!=");
                    } else {
                        return new Token(Token.Type.NOT, "!");
                    }
                case '&':
                    if(peek(2) != '&')
                        throw new ParseException("Unexpected character '" + peek(2) + "'.", position+1);
                    return new Token(Token.Type.AND, "&&");
                case '|':
                    if(peek(2) != '|')
                        throw new ParseException("Unexpected character '" + peek(2) + "'.", position+1);
                    return new Token(Token.Type.OR, "||");
                case '<':
                    if (peek(2) == '=') {
                        return new Token(Token.Type.RELATION, "<=");
                    } else {
                        return new Token(Token.Type.RELATION, "<");
                    }

                case '>':
                    if (peek(2) == '=') {
                        return new Token(Token.Type.RELATION, ">=");
                    } else {
                        return new Token(Token.Type.RELATION, ">");
                    }
                case '(':
                    return new Token(Token.Type.DELIMITER, "(");
                case ')':
                    return new Token(Token.Type.DELIMITER, ")");
                case '{':
                    return new Token(Token.Type.DELIMITER, "{");
                case '}':
                    return new Token(Token.Type.DELIMITER, "}");
                case ';':
                    return new Token(Token.Type.DELIMITER, ";");
                case ',':
                    return new Token(Token.Type.DELIMITER, ",");
                case '$':
                    return new Token(Token.Type.EOF, "$");
                default:
                    throw new ParseException("Unexpected character '" + peek(1) + "'.", position);
                    
            }
        }
        return new Token(Token.Type.EOF, "$");
    }

    private void skipWhitespace() {
        if (position < input.length()) {
            for (;;) {
                char y = input.charAt(position);
                if (y != ' ' && y != '\t' && y != '\n') {
                    return;
                }
                position++;
            }
        }
    }
}
