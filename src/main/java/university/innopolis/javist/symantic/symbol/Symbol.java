package university.innopolis.javist.symantic.symbol;

public abstract class Symbol {
    private String name;

    public Symbol(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
