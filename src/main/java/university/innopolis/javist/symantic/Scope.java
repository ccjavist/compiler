package university.innopolis.javist.symantic;

import lombok.Getter;
import university.innopolis.javist.symantic.symbol.Symbol;
import university.innopolis.javist.symantic.symbol.VariableSymbol;

import java.util.HashMap;
import java.util.Map;

public class Scope {

    @Getter
    private final Integer level;

    @Getter
    private final Map<String, VariableSymbol> variables;

    private final Scope parentScope;

    public Scope(Scope scope) {
        this.level = scope.getLevel()+1;
        this.variables = new HashMap<>();
        this.parentScope = scope;
    }

    public Scope() {
        this.level = 0;
        this.variables = new HashMap<>();
        this.parentScope = null;
    }

    public boolean isExist(String name) {
        if (this.variables.containsKey(name)) {
            return true;
        }
        if (parentScope == null) {
            return false;
        }
        return parentScope.isExist(name);
    }

    public Symbol variableLookup(String name) {
        if (this.variables.containsKey(name)) {
            return this.variables.get(name);
        }
        if (parentScope == null) {
            return null;
        }
        return parentScope.variableLookup(name);
    }

    public void put(String name, VariableSymbol variable) {
        this.variables.put(name, variable);
    }
}
