package university.innopolis.javist.symantic.symbol;

import java.util.Objects;

public class ParameterSymbol extends Symbol {
    private String type;

    public ParameterSymbol(String type) {
        super(null);
        this.type = type;
    }

    public ParameterSymbol(String name, String type) {
        super(name);
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterSymbol that = (ParameterSymbol) o;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }
}
