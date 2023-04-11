package university.innopolis.javist.symantic.symbol;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> symbols;

    public SymbolTable() {
        symbols = new HashMap<>();
    }

    public Symbol get(String name) {
        return symbols.get(name);
    }

    public void put(String name, Symbol symbol) {
        symbols.put(name, symbol);
    }
}


