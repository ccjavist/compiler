package university.innopolis.javist.syntax;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ProgramTree {
    private final NodeValue value;
    private final ArrayList<ProgramTree> children;

    public ProgramTree (NodeValue value) {
        this.children = new ArrayList<>();
        this.value = value;
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

//    public String toString(StringBuilder [] result) {
//
//    }

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
}
