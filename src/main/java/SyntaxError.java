public class SyntaxError extends Error {
    public SyntaxError() {
        super();
    }

    public SyntaxError(String unexpected) {
        super("Unexpected \"" + unexpected + "\"");
    }

    public SyntaxError(String expected, String got) {
        super("Expected " + expected + " but got " + got + ".");
    }
}
