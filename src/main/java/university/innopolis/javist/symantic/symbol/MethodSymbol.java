package university.innopolis.javist.symantic.symbol;

import lombok.Getter;
import university.innopolis.javist.symantic.Scope;

import java.util.List;


public class MethodSymbol extends ConstructorSymbol {

    @Getter
    private final String returnType;


    @Getter
    private final Scope scope = new Scope();


    public MethodSymbol(String name, String returnType, List<ParameterSymbol> parameters) {
        super(name, parameters);
        this.returnType = returnType;
    }

    public boolean equals(String name, List<ParameterSymbol> parameters) {
        if (this.getName().equals(name) && this.parameters.size() == parameters.size()) {
            for (int i = 0; i < this.parameters.size(); i++) {
                if (!this.parameters.get(i).equals(parameters.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
