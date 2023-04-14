package university.innopolis.javist.errors;

public class SemanticError extends Error {
    public SemanticError() {

    }
    public SemanticError(String errorMessage, int line, int column) {
        super(String.format("%s. Line: %d, Column: %d.",errorMessage, line, column));
    }
}
