package university.innopolis.javist.lexer;

import lombok.Getter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains all types of tokens that can be met in the O-language.
 * Used for simple checking and parsing the string data into tokens.
 */
public enum Token {
    TK_CLASS ("class"),
    TK_EXTENDS ("extends"),
	TK_INTEGER ("Integer"),
    TK_REAL ("Real"),
	TK_ARRAY ("Array"),
    TK_MOD ("mod"),
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
    TK_BOOLEAN_LITERAL ("true|false"),
    TK_IDENTIFIER ("[a-zA-Z_][a-zA-Z0-9_]*"),
    TK_REAL_LITERAL ("\\d+\\.\\d+"),
    TK_INTEGER_LITERAL ("\\d+"),

    // Punctuation
	TK_OPEN_BRACKET ("\\["),
	TK_CLOSE_BRACKET ("\\]"),
    TK_OPEN_BRACE ("\\{"),
    TK_CLOSE_BRACE ("\\}"),
    TK_OPEN_PAREN ("\\("),
    TK_CLOSE_PAREN ("\\)"),
    TK_ASSIGN (":="),
	TK_NEQ ("!="),
	TK_EXCLAMATION ("!"),
    TK_COLON (":"),
    TK_COMMA (","),
    TK_DOT ("\\."),

    // Operators
    TK_PLUS ("\\+"),
    TK_MINUS ("-"),
    TK_MUL ("\\*"),
    TK_LTE ("<="),
    TK_GTE (">="),
    TK_LT ("<"),
    TK_GT (">"),
    TK_DIV ("/"),
    TK_EQ ("=="),

    // Keywords
    TK_TRUE ("true"),
    TK_FALSE ("false");

    @Getter
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