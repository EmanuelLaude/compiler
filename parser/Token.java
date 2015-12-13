/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package parser;

/**
 *
 * @author emanuellaude
 */
public class Token {
    public enum Type {
        EOF, WHILE, IF, ELSE, TYPE, IDENTIFIER, 
        ASSIGNMENT, RELATION, AND, OR, NOT, PLUS_MINUS, MUL_DIV_MOD,
        DELIMITER, LITERAL
    }

    public static final char EOF = '$';
    
    private Type type;
    private String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Token() {
    }
    
    
    
    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
    
}
