import java.util.List;

public class SyntaxAnalyser {
    private final ProgramTree tree;
    private final Lexer lexer;

    public SyntaxAnalyser(Lexer lexer) {
        this.lexer = lexer;
        tree = new ProgramTree(SyntaxComponent.PROGRAM);
    }

    public ProgramTree makeTree() throws SyntaxError, LexerError {
        while (lexer.currentPair() != null) {
            tree.addChild(parseClass());
        }
        return tree;
    }

    private void checkToken(ProgramTree currentNode, TokenLexemaPair pair, Token token) {
        if (pair.getToken() != token)
            throw new SyntaxError(pair.getLexema());

        currentNode.addChild(new ProgramTree(pair));
    }

    private void checkToken(ProgramTree currentNode, TokenLexemaPair pair,
                            Token[] tokens) {
        for (Token token : tokens) {
            pair = lexer.nextPair();
            checkToken(currentNode, pair, token);
        }
    }

    // ClassDeclaration
    // : class ClassName [ extends ClassName ] is
    // { MemberDeclaration }
    // end
    private ProgramTree parseClass() {
        var currentNode = new ProgramTree(SyntaxComponent.CLASS_DECLARATION);
        TokenLexemaPair pair = lexer.nextPair();

        checkToken(currentNode, pair, Token.TK_CLASS);
        pair = lexer.nextPair();

        checkToken(currentNode, pair, Token.TK_IDENTIFIER);
        pair = lexer.nextPair();
        Token currentToken = pair.getToken();

        if (currentToken != Token.TK_EXTENDS && currentToken != Token.TK_IS)
            throw new SyntaxError(pair.getLexema());

        currentNode.addChild(new ProgramTree(pair));

        if (currentToken == Token.TK_EXTENDS) {
            pair = lexer.nextPair();

            checkToken(currentNode, pair, Token.TK_IDENTIFIER);
            pair = lexer.nextPair();

            checkToken(currentNode, pair, Token.TK_IS);
        }

        currentNode.addChild(parseMembers());
        pair = lexer.nextPair();

        checkToken(currentNode, pair, Token.TK_END);

        return currentNode;
    }

    private ProgramTree parseMembers() {
        var currentNode = new ProgramTree(SyntaxComponent.MEMBER_DECLARATIONS);
        TokenLexemaPair pair = lexer.currentPair();

        while (pair.getToken() != Token.TK_END) {
            currentNode.addChild(parseMember());
            pair = lexer.currentPair();
        }

        return currentNode;
    }

    private ProgramTree parseMember() {
        var currentNode = new ProgramTree(SyntaxComponent.MEMBER_DECLARATION);
        TokenLexemaPair pair = lexer.currentPair();

        if (pair.getToken() == Token.TK_VAR) {
            currentNode.addChild(parseVariableDeclaration());
        } else if (pair.getToken() == Token.TK_METHOD) {
            currentNode.addChild(parseMethodDeclaration());
        } else if (pair.getToken() == Token.TK_THIS) {
            currentNode.addChild(parseConstructorDeclaration());
        } else
            throw new SyntaxError(pair.getLexema());

        return currentNode;
    }

    private ProgramTree parseVariableDeclaration() {
        var currentNode = new ProgramTree(SyntaxComponent.VARIABLE_DECLARATION);
        TokenLexemaPair pair = null;

        Token[] tokens = {Token.TK_VAR, Token.TK_IDENTIFIER, Token.TK_COLON};
        checkToken(currentNode, pair, tokens);

        currentNode.addChild(parseType());

        return currentNode;
    }

    private ProgramTree parseConstructorDeclaration() {
        var currentNode = new ProgramTree(SyntaxComponent.CONSTRUCTOR_DECLARATION);
        TokenLexemaPair pair = null;

        checkToken(currentNode, pair, new Token[]
                {Token.TK_THIS, Token.TK_OPEN_PAREN});

        pair = lexer.currentPair();

        if (pair.getToken() != Token.TK_CLOSE_PAREN) {
            currentNode.addChild(parseParameters());
        }

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_CLOSE_PAREN);

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_IS);

        currentNode.addChild(parseStatements());

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_END);

        return currentNode;
    }

    private ProgramTree parseMethodDeclaration() {
        var currentNode = new ProgramTree(SyntaxComponent.METHOD_DECALRATION);
        TokenLexemaPair pair = null;

        checkToken(currentNode, pair, new Token[]
                {Token.TK_METHOD, Token.TK_IDENTIFIER, Token.TK_OPEN_PAREN});

        pair = lexer.currentPair();

        if (pair.getToken() != Token.TK_CLOSE_PAREN) {
            currentNode.addChild(parseParameters());
        }

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_CLOSE_PAREN);

        if (lexer.currentPair().getToken() == Token.TK_COLON) {
            pair = lexer.nextPair();
            checkToken(currentNode, pair, Token.TK_COLON);
            currentNode.addChild(parseType());
        }

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_IS);

        currentNode.addChild(parseStatements());

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_END);

        return currentNode;
    }

    private ProgramTree parseType() {
        TokenLexemaPair pair = lexer.nextPair();

        if (pair.getToken() == Token.TK_INTEGER ||
                pair.getToken() == Token.TK_REAL)
            return new ProgramTree(pair);

        if (pair.getToken() != Token.TK_ARRAY)
            throw new SyntaxError(pair.getLexema());

        var result = new ProgramTree(SyntaxComponent.ARRAY_TYPE);
        result.addChild(new ProgramTree(pair));

        pair = lexer.nextPair();
        checkToken(result, pair, Token.TK_OPEN_BRACKET);
        result.addChild(parseType());

        pair = lexer.nextPair();
        checkToken(result, pair, Token.TK_CLOSE_BRACKET);

        return result;
    }

    private ProgramTree parseParameters() {
        var currentNode = new ProgramTree(SyntaxComponent.PARAMETERS);
        TokenLexemaPair pair = lexer.currentPair();

        while (pair.getToken() != Token.TK_CLOSE_PAREN) {
            currentNode.addChild(parseParameter());

            pair = lexer.currentPair();

            if (pair.getToken() == Token.TK_COMMA) {
                currentNode.addChild(new ProgramTree(pair));
                pair = lexer.nextPair();
            }
        }

        return currentNode;
    }

    public ProgramTree parseParameter() {
        var currentNode = new ProgramTree(SyntaxComponent.VARIABLE_DECLARATION);
        TokenLexemaPair pair = null;

        Token[] tokens = {Token.TK_IDENTIFIER, Token.TK_COLON};
        checkToken(currentNode, pair, tokens);

        currentNode.addChild(parseType());

        return currentNode;
    }

    private ProgramTree parseStatements() {
        var currentNode = new ProgramTree(SyntaxComponent.STATEMENTS);
        TokenLexemaPair pair = lexer.currentPair();

        while (pair.getToken() != Token.TK_END &&
                pair.getToken() != Token.TK_ELSE) {
            currentNode.addChild(parseStatement());
            pair = lexer.currentPair();
        }

        return currentNode;
    }

    private ProgramTree parseStatement() {
        var currentNode = new ProgramTree(SyntaxComponent.STATEMENT);
        TokenLexemaPair pair = lexer.currentPair();

        if (pair.getToken() == Token.TK_VAR) {
            ProgramTree varDecl = parseVariableDeclaration();

            if (varDecl.getChild(3).getValue() instanceof SyntaxComponent) {
                ProgramTree arrayType = varDecl.getChild(3);
                pair = lexer.nextPair();
                checkToken(arrayType, pair, Token.TK_OPEN_PAREN);

                arrayType.addChild(parseExpression());

                pair = lexer.nextPair();
                checkToken(arrayType, pair, Token.TK_CLOSE_PAREN);
            }

            currentNode.addChild(varDecl);
        } else if (pair.getToken() == Token.TK_WHILE) {
            currentNode.addChild(parseWhile());
        } else if (pair.getToken() == Token.TK_IF) {
            currentNode.addChild(parseIf());
        } else if (pair.getToken() == Token.TK_RETURN) {
            currentNode.addChild(parseReturn());
        } else {
            currentNode.addChild(parseAssignment());
        }

        return currentNode;
    }

    private ProgramTree parseAssignment() {
        var currentNode = new ProgramTree(SyntaxComponent.ASSIGNMENT);
        TokenLexemaPair pair = null;

        checkToken(currentNode, pair, new Token[]
                {Token.TK_IDENTIFIER, Token.TK_ASSIGN});

        currentNode.addChild(parseExpression());

        return currentNode;
    }

    private ProgramTree parseIf() {
        var currentNode = new ProgramTree(SyntaxComponent.IF_STATEMENT);
        TokenLexemaPair pair = lexer.nextPair();

        checkToken(currentNode, pair, Token.TK_IF);

        currentNode.addChild(parseExpression());

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_THEN);

        currentNode.addChild(parseStatements());

        pair = lexer.nextPair();

        if (pair.getToken() == Token.TK_END)
            currentNode.addChild(new ProgramTree(pair));
        else {
            checkToken(currentNode, pair, Token.TK_ELSE);
            currentNode.addChild(parseStatements());

            pair = lexer.nextPair();
            checkToken(currentNode, pair, Token.TK_END);
        }

        return currentNode;
    }

    private ProgramTree parseWhile() {
        var currentNode = new ProgramTree(SyntaxComponent.WHILE_LOOP);
        TokenLexemaPair pair = lexer.nextPair();

        checkToken(currentNode, pair, Token.TK_WHILE);

        currentNode.addChild(parseExpression());

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_LOOP);

        currentNode.addChild(parseStatements());

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_END);

        return currentNode;
    }

    private ProgramTree parseReturn() {
        var currentNode = new ProgramTree(SyntaxComponent.RETURN_STATEMENT);
        TokenLexemaPair pair = lexer.nextPair();

        checkToken(currentNode, pair, Token.TK_RETURN);
        currentNode.addChild(parseExpression());

        return currentNode;
    }

    private ProgramTree parseExpression() {
        var currentNode = new ProgramTree(SyntaxComponent.EXPRESSION);
        ProgramTree left = parseTerm();

        TokenLexemaPair pair = lexer.currentPair();

        while (pair.getToken() == Token.TK_PLUS ||
                pair.getToken() == Token.TK_MINUS) {
            pair = lexer.nextPair();
            ProgramTree temp = new ProgramTree(pair);
            temp.addChild(left);
            temp.addChild(parseTerm());
            left = temp;

            pair = lexer.currentPair();
        }

        currentNode.addChild(left);

        return currentNode;
    }

    private ProgramTree parseTerm() {
        var currentNode = new ProgramTree(SyntaxComponent.TERM);
        ProgramTree left = parseFactor();

        TokenLexemaPair pair = lexer.currentPair();

        while (pair.getToken() == Token.TK_MUL ||
                pair.getToken() == Token.TK_DIV) {
            pair = lexer.nextPair();
            ProgramTree temp = new ProgramTree(pair);
            temp.addChild(left);
            temp.addChild(parseFactor());
            left = temp;

            pair = lexer.currentPair();
        }

        currentNode.addChild(left);

        return currentNode;
    }

    private ProgramTree parseFactor() {
        var currentNode = new ProgramTree(SyntaxComponent.FACTOR);
        TokenLexemaPair pair = lexer.nextPair();

        if (pair.getToken() == Token.TK_OPEN_PAREN) {
            currentNode.addChild(parseExpression());

            pair = lexer.nextPair();
            if (pair.getToken() != Token.TK_CLOSE_PAREN)
                throw new SyntaxError(pair.getLexema());
        }
        else
            checkToken(currentNode, pair, Token.TK_IDENTIFIER);

        return currentNode;
    }
}
