package university.innopolis.javist.symantic.symbol;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ClassSymbol extends Symbol {

    @Setter
    @Getter
    String parentClass;

    public ClassSymbol(String name, String parentClass) {
        super(name);
        this.parentClass = parentClass;
    }
}
