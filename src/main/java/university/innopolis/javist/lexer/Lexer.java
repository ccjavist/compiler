package university.innopolis.javist.lexer;

import university.innopolis.javist.errors.LexerError;
import university.innopolis.javist.syntax.TokenLexemaPair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Lexer {
    private StringBuilder input = new StringBuilder();

    private Token token;

    private String lexema;

    private boolean exausthed = false;

    private String errorMessage = "";

    private Set<Character> blankChars = new HashSet<Character>();

    private int line;

    private int column;

    private int position;


    public Lexer(String filePath) {
        line = 1;
        column = 0;
        position = 0;

        try (Stream<String> st = Files.lines(Paths.get(filePath))) {
            st.forEach(line -> {
                        input.append(line);
                        input.append('\n');
                    }
            );
            input.deleteCharAt(input.length()-1);
        } catch (IOException ex) {
            exausthed = true;
            errorMessage = "Could not read file: " + filePath;
            return;
        }

        // then we add all the characters that we consider blank
        blankChars.add('\r');
        blankChars.add('\n');
        blankChars.add((char) 8);
        blankChars.add((char) 9);
        blankChars.add((char) 11);
        blankChars.add((char) 12);
        blankChars.add((char) 32);

        moveAhead();
    }

    public void moveAhead() {
        if (exausthed) {
            return;
        }

        if (input.length() == 0) {
            exausthed = true;
            return;
        }

        ignoreWhiteSpaces();

        if (findNextToken()) {
            return;
        }

        exausthed = true;

        if (input.length() > 0) {
            errorMessage = "Unexpected symbol: '" + input.charAt(0) + "'";
        }
    }

    private void ignoreWhiteSpaces() {
        int charsToDelete = 0;

        while (blankChars.contains(input.charAt(charsToDelete))) {
            position++;
            if (input.charAt(charsToDelete) == '\n') {
                column = 0;
                line++;
            }
            column++;
            charsToDelete++;
        }

        if (charsToDelete > 0) {
            input.delete(0, charsToDelete);
        }
    }

    private boolean findNextToken() {
        for (Token t : Token.values()) {
            int end = t.endOfMatch(input.toString());

            if (end != -1) {
                position += end;
                column += end;
                token = t;
                lexema = input.substring(0, end);
                input.delete(0, end);
                return true;
            }
        }

        return false;
    }

    public Token currentToken() {
        return token;
    }

    public String currentLexema() {
        return lexema;
    }

    public boolean isSuccessful() {
        return errorMessage.isEmpty();
    }

    public String errorMessage() {
        return errorMessage;
    }

    public boolean isExausthed() {
        return exausthed;
    }

    public TokenLexemaPair nextPair() throws LexerError {
        if (!isExausthed()) {
            var result = new TokenLexemaPair(currentToken(), currentLexema(), line, column);
            moveAhead();
            return result;
        } else if (!errorMessage.equals(""))
            throw new LexerError(errorMessage);
        else
            return null;
    }

    public TokenLexemaPair currentPair() throws LexerError {
        if (!isExausthed()) {
            return new TokenLexemaPair(currentToken(), currentLexema(), line, column);
        } else if (!errorMessage.equals(""))
            throw new LexerError(errorMessage);
        else
            return null;
    }
}