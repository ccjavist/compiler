package university.innopolis.javist.syntax;

import university.innopolis.javist.lexer.Token;

public class TokenLexemaPair implements NodeValue {
    private final Token token;

    private final String lexema;

    private final int line;

    private final int position;

    public TokenLexemaPair(Token token, String lexema, int line, int position) {
        this.token = token;
        this.lexema = lexema;
        this.line = line;
        this.position = position;
    }

    public Token getToken() {
        return token;
    }

    public String getLexema() {
        return lexema;
    }
}
