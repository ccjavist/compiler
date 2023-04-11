package university.innopolis.javist.symantic.symbol;


public class ConstructorSymbol extends Symbol {
    private String type;

    public ConstructorSymbol(String name, String type) {
        super(name);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}