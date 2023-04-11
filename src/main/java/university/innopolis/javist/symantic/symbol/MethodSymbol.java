package university.innopolis.javist.symantic.symbol;

import university.innopolis.javist.errors.SymbolNotFoundException;
import university.innopolis.javist.symantic.symbol.Symbol;

import java.util.*;


public class MethodSymbol extends Symbol {
    private String type;

    public MethodSymbol(String name, String type) {
        super(name);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
