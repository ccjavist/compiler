package university.innopolis.javist.symantic.symbol;


import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class ConstructorSymbol extends Symbol {

    private final List<ParameterSymbol> parameters;

    public ConstructorSymbol(List<ParameterSymbol> parameters) {
        super(null);
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstructorSymbol that = (ConstructorSymbol) o;
        return parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters);
    }
}