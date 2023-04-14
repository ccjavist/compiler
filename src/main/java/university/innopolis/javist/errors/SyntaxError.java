package university.innopolis.javist.errors;

public class SyntaxError extends Error {
    public SyntaxError() {
        super();
    }

    public SyntaxError(String unexpected, int line, int column) {
        super(String.format("Unexpected \"%s\". Line: %d, Column: %d", unexpected, line, column));
    }

    public SyntaxError(String expected, String got, int line, int column) {
        super(String.format("Expected %s but got %s. Line: %d, Column: %d", expected, got, line, column));
    }
}
