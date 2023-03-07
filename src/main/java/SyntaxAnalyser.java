import java.util.List;

public class SyntaxAnalyser {
    private ProgramTree tree;
    private Lexer lexer;

    public SyntaxAnalyser(Lexer lexer) {
        this.lexer = lexer;
        tree = new ProgramTree(null, SyntaxComponent.PROGRAM);
    }

    public void makeTree(List<Token> tokens) throws SyntaxError, LexerError {
        for (Token token : tokens) {

        }
    }

    private void checkToken(ProgramTree currentNode, TokenLexemaPair pair, Token token) {
        if (pair.getToken() != token)
            throw new SyntaxError(pair.getLexema());

        currentNode.addChild(new ProgramTree(pair, null));
    }

    private void checkToken(ProgramTree currentNode, TokenLexemaPair pair,
                             Token[] tokens) {
        for (Token token: tokens) {
            checkToken(currentNode, pair, token);
            pair = lexer.nextPair();
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
        var currentNode = new ProgramTree(null, SyntaxComponent.CLASS_DECLARATION);
        TokenLexemaPair pair = lexer.nextPair();

        checkToken(currentNode, pair, Token.TK_CLASS);
        pair = lexer.nextPair();

        checkToken(currentNode, pair, Token.TK_IDENTIFIER);
        pair = lexer.nextPair();
        Token currentToken = pair.getToken();

        if (currentToken != Token.TK_EXTENDS && currentToken != Token.TK_IS)
            throw new SyntaxError(pair.getLexema());

        currentNode.addChild(new ProgramTree(pair, null));

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
        var currentNode = new ProgramTree(null, SyntaxComponent.MEMBER_DECLARATIONS);
        TokenLexemaPair pair = lexer.currentPair();

        while (pair.getToken() != Token.TK_END) {
            currentNode.addChild(parseMember());
            pair = lexer.currentPair();
        }

        return currentNode;
    }

    private ProgramTree parseMember() {
        var currentNode = new ProgramTree(null, SyntaxComponent.MEMBER_DECLARATION);
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
        var currentNode = new ProgramTree(null, SyntaxComponent.VARIABLE_DECLARATION);
        TokenLexemaPair pair = lexer.nextPair();

        Token [] tokens = {Token.TK_VAR, Token.TK_IDENTIFIER, Token.TK_COLON};
        checkToken(currentNode, pair, tokens);

        currentNode.addChild(parseType());

        return currentNode;
    }

    private ProgramTree parseType() {

    }

    private ProgramTree parseMethodDeclaration() {
        var currentNode = new ProgramTree(null, SyntaxComponent.METHOD_DECALRATION);

    }

    private ProgramTree parseConstructorDeclaration() {
        var currentNode = new ProgramTree(null, SyntaxComponent.CONSTRUCTOR_DECLARATION);

    }

}
