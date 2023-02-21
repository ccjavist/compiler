import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Token {
    TK_CLASS ("class"),
    TK_EXTENDS ("extends"),
    TK_IS ("is"),
    TK_END ("end"),
    TK_VAR ("var"),
    TK_METHOD ("method"),
    TK_THIS ("this"),
    TK_WHILE ("while"),
    TK_LOOP ("loop"),
    TK_IF ("if"),
    TK_THEN ("then"),
    TK_ELSE ("else"),
    TK_RETURN ("return"),

    // Identifiers and literals
    TK_IDENTIFIER ("[a-zA-Z_][a-zA-Z0-9_]*"),
    TK_INTEGER_LITERAL ("\\d+"),
    TK_REAL_LITERAL ("\\d+\\.\\d+"),
    TK_BOOLEAN_LITERAL ("true|false"),

    // Punctuation
	TK_OPEN_BRACKET ("\\["),
	TK_CLOSE_BRACKET ("\\]"),
    TK_OPEN_BRACE ("\\{"),
    TK_CLOSE_BRACE ("\\}"),
    TK_OPEN_PAREN ("\\("),
    TK_CLOSE_PAREN ("\\)"),
    TK_COLON (":"),
    TK_COMMA (","),
    TK_DOT ("\\."),
    TK_ASSIGN ("="),

    // Operators
    TK_PLUS ("\\+"),
    TK_MINUS ("-"),
    TK_MUL ("\\*"),
    TK_DIV ("/"),
    TK_LT ("<"),
    TK_LTE ("<="),
    TK_GT (">"),
    TK_GTE (">="),
    TK_EQ ("=="),
    TK_NEQ ("<>"),

    // Keywords
    TK_TRUE ("true"),
    TK_FALSE ("false");
	
    private final Pattern pattern;

    Token(String regex) {
        pattern = Pattern.compile("^" + regex);
    }

    int endOfMatch(String s) {
        Matcher m = pattern.matcher(s);

        if (m.find()) {
            return m.end();
        }

        return -1;
    }
}
