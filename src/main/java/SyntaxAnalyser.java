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
        for (Token token: tokens) {
            pair = lexer.nextPair();
            checkToken(currentNode, pair, token);
        }
    }

    private ProgramTree parseExpr() {
        return null;
    }

    private ProgramTree parseTerm() {
        return null;
    }

//    public ProgramTree parseFactor() {
//        ProgramTree res;
//        if ( tk=get(), tk==tkLParen )
//        {
//            res = parseExpr();
//            get(); // skip ‘)’
//        } else
//        res = mkUnaryTree(parseId());
//
//        return res;
//    }

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

        Token [] tokens = {Token.TK_VAR, Token.TK_IDENTIFIER, Token.TK_COLON};
        checkToken(currentNode, pair, tokens);

        currentNode.addChild(parseType());

        return currentNode;
    }

    private ProgramTree parseType() {
        TokenLexemaPair pair = lexer.nextPair();

        if (pair.getToken() != Token.TK_INTEGER &&
                pair.getToken() != Token.TK_REAL &&
                pair.getToken() != Token.TK_ARRAY)
            throw new SyntaxError(pair.getLexema());

        return new ProgramTree(pair);
    }

    private ProgramTree parseMethodDeclaration() {
        var currentNode = new ProgramTree(SyntaxComponent.METHOD_DECALRATION);
        TokenLexemaPair pair = null;

        Token [] tokens = {Token.TK_METHOD, Token.TK_IDENTIFIER, Token.TK_OPEN_PAREN};
        checkToken(currentNode, pair, tokens);

        pair = lexer.currentPair();

        if (pair.getToken() != Token.TK_CLOSE_PAREN) {
            currentNode.addChild(parseParameters());
        }

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_CLOSE_PAREN);

        if (lexer.currentPair().getToken() == Token.TK_COLON) {
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

        Token [] tokens = {Token.TK_IDENTIFIER, Token.TK_COLON};
        checkToken(currentNode, pair, tokens);

        currentNode.addChild(parseType());

        return currentNode;
    }

    private ProgramTree parseStatements() {
        return null;
    }

    private ProgramTree parseConstructorDeclaration() {
        var currentNode = new ProgramTree(SyntaxComponent.CONSTRUCTOR_DECLARATION);
        return null;
    }

}
