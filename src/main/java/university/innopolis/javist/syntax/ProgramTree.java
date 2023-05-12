package university.innopolis.javist.syntax;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class ProgramTree {
    private final NodeValue value;

    @Setter
    @Getter
    private int line;

    @Getter
    @Setter
    private int column;

    @Getter
    private final ArrayList<ProgramTree> children;

    public ProgramTree(NodeValue value, int line, int column) {
        this.children = new ArrayList<>();
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public ProgramTree getChild(int n) {
        return children.get(n);
    }

    public void addChild(ProgramTree child) {
        children.add(child);
    }

    public void addChild(int n, ProgramTree child) {
        children.add(n, child);
    }

    public int getChildrenCount() {
        return children.size();
    }

    public boolean hasChildren() {
        return children.size() != 0;
    }

    public NodeValue getValue() {
        return value;
    }

    /**
     * Makes a good string visualisation of the AST.
     * @return A string containing all the nodes of the tree.
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (value instanceof TokenLexemaPair pair) {
            result.append("{").append(pair.getToken()).append(" : ").append(pair.getLexema()).append("} ");
        } else if (value instanceof SyntaxComponent component) {
            result.append(component).append(" ");
        } else
            result.append("INVALID_NODE ");
        return result.toString();
    }

    public ProgramTree clone(){
        ProgramTree clone = new ProgramTree(value, line, column);

        for (int i = 0; i < children.size(); i++) {
            clone.children.add(children.get(i).clone());
        }

        return clone;
    }
}
