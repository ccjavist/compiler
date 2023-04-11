package university.innopolis.javist.symantic;

import lombok.Getter;
import university.innopolis.javist.symantic.symbol.Symbol;
import university.innopolis.javist.symantic.symbol.SymbolTable;

public class Scope {

    @Getter
    private final Integer level;

    @Getter
    private final SymbolTable symbolTable;

    private final Scope parentScope;

    public Scope(Scope scope) {
        this.level = scope.getLevel();
        this.symbolTable = new SymbolTable();
        this.parentScope = scope;
    }

    public Scope() {
        this.level = 0;
        this.symbolTable = new SymbolTable();
        this.parentScope = null;
    }

    public boolean isExist(String name){
        if(this.symbolTable.get(name) != null){
            return true;
        }
        if(parentScope == null){
            return false;
        }
        return parentScope.isExist(name);
    }

    public Symbol lookup(String name){
        if(this.symbolTable.get(name) != null){
            return this.symbolTable.get(name);
        }
        if(parentScope == null){
            return null;
        }
        return parentScope.lookup(name);
    }

    public void put(String name, Symbol symbol){
        this.symbolTable.put(name, symbol);
    }
}
