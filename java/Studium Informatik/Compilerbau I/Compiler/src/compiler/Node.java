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
public class Node {

    private Node parent = null;
    private List<Node> children;
    private String token;
    private static int UNIQUE_ID = 0;
    private int id;
    
    public Node(String token) {
        this.token = token;
        this.children = new ArrayList<>();
        this.parent = null;
        this.id = UNIQUE_ID;
        Node.UNIQUE_ID++;
    }

    public Node(String token, Node... children) {
        this(token);
        this.children = Arrays.asList(children);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getParent() {
        return parent;
    }

    public List<Node> getChildren() {
        return children;
    }

    public void addChild(Node child) {
        children.add(child);
    }

    public Node getChild(int i) {
        return children.get(i);
    }

    private void dotHelper(PrintStream out) throws IOException {
        out.println(id + " [label = \"" + token + "\"]");
        for (Node child : children) {
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
