public class TokenLexemaPair implements NodeValue {
    private Token token;
    private String lexema;

    public TokenLexemaPair(Token token, String lexema) {
        this.token = token;
        this.lexema = lexema;
    }

    public Token getToken() {
        return token;
    }

    public String getLexema() {
        return lexema;
    }
}
