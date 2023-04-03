package university.innopolis.javist.symantic;

public class ConditionalSymbolTable {
    private boolean insideConditional;

    public ConditionalSymbolTable() {
        insideConditional = false;
    }

    public void enterConditional() {
        insideConditional = true;
    }

    public void exitConditional() {
        insideConditional = false;
    }

    public boolean isInsideConditional() {
        return insideConditional;
    }
}
