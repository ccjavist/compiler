import java.util.ArrayList;

public class ProgramTree {
    private final TokenLexemaPair pair;
    private final ArrayList<ProgramTree> children;
    private final SyntaxComponent component;

    public ProgramTree (TokenLexemaPair pair, SyntaxComponent component) {
        this.pair = pair;
        this.children = new ArrayList<>();
        this.component = component;
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

    public SyntaxComponent getComponent() {
        return component;
    }

    public TokenLexemaPair getPair() {
        return pair;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (pair != null) {
            result.append("{" + pair.getToken() + " : " + pair.getLexema() + "} ");
        } else if (component != null) {
            result.append(component + " ");
        } else
            result.append("INVALID_NODE ");

        if (!children.isEmpty()) {
            result.append(":: Children:\n[");

            for (ProgramTree child: children)
                result.append(child.toString());

            result.append("]");
        }

        return result.toString();
    }
}
