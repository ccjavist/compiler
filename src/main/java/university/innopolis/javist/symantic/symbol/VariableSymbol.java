package university.innopolis.javist.symantic.symbol;

public class VariableSymbol extends Symbol {
    private String type;

    public VariableSymbol(String name, String type) {
        super(name);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}