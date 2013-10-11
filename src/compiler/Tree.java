/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package compiler;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author emanuellaude
 */
public class Tree {

    private Tree parent = null;
    private List<Tree> children;
    private Token token;
    private static int UNIQUE_ID = 0;
    private int id;
    
    public Tree(Token token) {
        this.token = token;
        this.children = new ArrayList<>();
        this.parent = null;
        this.id = UNIQUE_ID;
        Tree.UNIQUE_ID++;
    }

    public Tree(Token token, Tree... children) {
        this(token);
        this.children = Arrays.asList(children);
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public void setParent(Tree parent) {
        this.parent = parent;
    }

    public Tree getParent() {
        return parent;
    }

    public List<Tree> getChildren() {
        return children;
    }

    public void addChild(Tree child) {
        children.add(child);
    }

    public Tree getChild(int i) {
        return children.get(i);
    }

    private void dotHelper(PrintStream out) throws IOException {
        out.println(id + " [label = \"" + token.getValue() + "\"]");
        for (Tree child : children) {
            out.println(id + " -> " + child.id);
            child.dotHelper(out);
        }
    }

    public void printDot(String filename) {
        try {
            PrintStream out = new PrintStream(filename);
            out.println("digraph AST {");
            dotHelper(out);
            out.println("}");
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
