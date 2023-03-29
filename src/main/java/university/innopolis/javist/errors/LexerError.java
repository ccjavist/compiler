package university.innopolis.javist.errors;

public class LexerError extends Error {
    public LexerError () {

    }
    public LexerError(String errorMessage) {
        super(errorMessage);
    }
}
