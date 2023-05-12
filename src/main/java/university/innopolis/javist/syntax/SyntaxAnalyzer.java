package university.innopolis.javist.syntax;

import university.innopolis.javist.errors.LexerError;
import university.innopolis.javist.errors.SyntaxError;
import university.innopolis.javist.lexer.Lexer;
import university.innopolis.javist.lexer.Token;

public class SyntaxAnalyzer {
    private final ProgramTree tree;
    private final Lexer lexer;

    public SyntaxAnalyzer(Lexer lexer) {
        this.lexer = lexer;
        tree = new ProgramTree(SyntaxComponent.PROGRAM, 0, 0);
    }

    /**
     * The main method which executes the whole process.
     * It gets the chain of the methods started to work and returns the resulting AST
     * @return The resulting AST.
     * @throws SyntaxError if any of the Syntax rules are broken.
     * @throws LexerError if there are some typos.
     */
    public ProgramTree makeTree() throws SyntaxError, LexerError {
        while (lexer.currentPair() != null) {
            tree.addChild(parseClass());
        }
        return tree;
    }

    /**
     * Checks if pair contains the token provided.
     * If not, it throws an error. Adds the pair to the node as a child otherwise.
     * @param currentNode The node of the tree being modified.
     * @param pair        The pair of token and lexeme got from Lexer.
     * @param token       Expected token.
     */
    private void checkToken(ProgramTree currentNode, TokenLexemaPair pair, Token token) {
        if (pair.getToken() != token)
            throw new SyntaxError(pair.getLexema(), pair.getLine(), pair.getPosition());

        currentNode.addChild(new ProgramTree(pair, pair.getLine(), pair.getPosition()));
    }

    /**
     * Does the same thing as checkToken(ProgramTree currentNode, TokenLexemaPair pair, Token token),
     * but for the array of tokens. Checks the whole provided sequence at a time.
     * @param currentNode The node of the tree being modified.
     * @param pair        The pair of token and lexeme got from Lexer.
     * @param tokens      Expected sequence of tokens.
     */
    private void checkToken(ProgramTree currentNode, TokenLexemaPair pair,
                            Token[] tokens) {
        for (Token token : tokens) {
            pair = lexer.nextPair();
            checkToken(currentNode, pair, token);
        }
    }

    /**
     * Parses one class according to its structure for adding it and its components to the AST.
     * @return An AST piece containing the class.
     */
    private ProgramTree parseClass() {
        TokenLexemaPair pair = lexer.nextPair();
        var currentNode = new ProgramTree(SyntaxComponent.CLASS_DECLARATION, pair.getLine(), pair.getPosition());

        checkToken(currentNode, pair, Token.TK_CLASS);
        currentNode.addChild(parseClassName());

        pair = lexer.nextPair();
        Token currentToken = pair.getToken();

        if (currentToken != Token.TK_EXTENDS && currentToken != Token.TK_IS)
            throw new SyntaxError(pair.getLexema(), pair.getLine(), pair.getPosition());

        currentNode.addChild(new ProgramTree(pair, pair.getLine(), pair.getPosition()));

        if (currentToken == Token.TK_EXTENDS) {
            currentNode.addChild(parseClassName());

            pair = lexer.nextPair();
            checkToken(currentNode, pair, Token.TK_IS);
        }

        currentNode.addChild(parseMembers());
        pair = lexer.nextPair();

        checkToken(currentNode, pair, Token.TK_END);

        return currentNode;
    }

    /**
     * Parses class name
     * @return An AST piece containing the class name.
     */
    private ProgramTree parseClassName() {
        TokenLexemaPair pair = lexer.nextPair();
        var currentNode = new ProgramTree(SyntaxComponent.CLASS_NAME, pair.getLine(), pair.getPosition());

        if (pair.getToken() == Token.TK_INTEGER ||
                pair.getToken() == Token.TK_REAL ||
                pair.getToken() == Token.TK_ARRAY) {
            checkToken(currentNode, pair, pair.getToken());
        } else
            checkToken(currentNode, pair, Token.TK_IDENTIFIER);

        pair = lexer.currentPair();
        if (pair.getToken() == Token.TK_OPEN_BRACKET) {
            pair = lexer.nextPair();
            currentNode.addChild(new ProgramTree(pair, pair.getLine(), pair.getPosition()));
            currentNode.addChild(parseClassName());

            pair = lexer.nextPair();
            checkToken(currentNode, pair, Token.TK_CLOSE_BRACKET);
        }

        return currentNode;
    }

    /**
     * Parses fields and methods of the class.
     * @return Returns the AST piece containing all of them.
     */
    private ProgramTree parseMembers() {
        TokenLexemaPair pair = lexer.currentPair();
        var currentNode = new ProgramTree(SyntaxComponent.MEMBER_DECLARATIONS, pair.getLine(), pair.getPosition());

        while (pair.getToken() != Token.TK_END) {
            currentNode.addChild(parseMember());
            pair = lexer.currentPair();
        }

        return currentNode;
    }

    /**
     * Parses one field or method of the class.
     * @return An AST piece containing the member.
     */
    private ProgramTree parseMember() {
        TokenLexemaPair pair = lexer.currentPair();
        var currentNode = new ProgramTree(SyntaxComponent.MEMBER_DECLARATION, pair.getLine(), pair.getPosition());

        if (pair.getToken() == Token.TK_VAR) {
            currentNode.addChild(parseVariableDeclaration());
        } else if (pair.getToken() == Token.TK_METHOD) {
            currentNode.addChild(parseMethodDeclaration());
        } else if (pair.getToken() == Token.TK_THIS) {
            currentNode.addChild(parseConstructorDeclaration());
        } else
            throw new SyntaxError(pair.getLexema(), pair.getLine(), pair.getPosition());

        return currentNode;
    }

    /**
     * Parses a variable declaration.
     * Marks the node with special SyntaxComponent instance.
     * @return AST piece with variable declaration.
     */
    private ProgramTree parseVariableDeclaration() {
        var currentNode = new ProgramTree(SyntaxComponent.VARIABLE_DECLARATION, lexer.currentPair().getLine(), lexer.currentPair().getPosition());
        TokenLexemaPair pair = null;

        Token[] tokens = {Token.TK_VAR, Token.TK_IDENTIFIER, Token.TK_COLON};
        checkToken(currentNode, pair, tokens);

        currentNode.addChild(parseExpression());

        return currentNode;
    }

    /**
     * Parses class constructor.
     * @return AST piece with variable declaration.
     */
    private ProgramTree parseConstructorDeclaration() {
        var currentNode = new ProgramTree(SyntaxComponent.CONSTRUCTOR_DECLARATION, lexer.currentPair().getLine(), lexer.currentPair().getPosition());
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

    /**
     * Parses method declaration and calls parsers for the statements.
     * @return AST piece with method.
     */
    private ProgramTree parseMethodDeclaration() {
        var currentNode = new ProgramTree(SyntaxComponent.METHOD_DECLARATION, 0, 0);
        TokenLexemaPair pair = null;

        checkToken(currentNode, pair, new Token[]
                {Token.TK_METHOD, Token.TK_IDENTIFIER, Token.TK_OPEN_PAREN});

        pair = lexer.currentPair();
        currentNode.setLine(pair.getLine());
        currentNode.setLine(pair.getPosition());

        if (pair.getToken() != Token.TK_CLOSE_PAREN) {
            currentNode.addChild(parseParameters());
        }

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_CLOSE_PAREN);

        if (lexer.currentPair().getToken() == Token.TK_COLON) {
            pair = lexer.nextPair();
            checkToken(currentNode, pair, Token.TK_COLON);
            currentNode.addChild(parseIdentifier());
        }

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_IS);

        currentNode.addChild(parseStatements());

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_END);

        return currentNode;
    }

    /**
     * Checks if the token is type-word or just an identifier
     * @return Node with the pair.
     */
    private ProgramTree parseIdentifier() {
        TokenLexemaPair pair = lexer.nextPair();

        if (pair.getToken() == Token.TK_INTEGER ||
                pair.getToken() == Token.TK_REAL ||
                pair.getToken() == Token.TK_ARRAY ||
                pair.getToken() == Token.TK_IDENTIFIER)
            return new ProgramTree(pair, pair.getLine(), pair.getPosition());

        throw new SyntaxError(pair.getLexema(), pair.getLine(), pair.getPosition());
    }

    /**
     * Not used in the Syntax analysis. But that's one of the approaches to parse identifier as a type
     * @return node with type.
     */
    private ProgramTree parseType() {
        TokenLexemaPair pair = lexer.nextPair();

        if (pair.getToken() == Token.TK_INTEGER ||
                pair.getToken() == Token.TK_REAL)
            return new ProgramTree(pair, pair.getLine(), pair.getPosition());

        if (pair.getToken() != Token.TK_ARRAY)
            throw new SyntaxError(pair.getLexema(), pair.getLine(), pair.getPosition());

        var result = new ProgramTree(SyntaxComponent.ARRAY_TYPE, pair.getLine(), pair.getPosition());
        result.addChild(new ProgramTree(pair, pair.getLine(), pair.getPosition()));

        pair = lexer.nextPair();
        checkToken(result, pair, Token.TK_OPEN_BRACKET);
        result.addChild(parseType());

        pair = lexer.nextPair();
        checkToken(result, pair, Token.TK_CLOSE_BRACKET);

        return result;
    }

    /**
     * Parses parameters of a method declaration.
     * @return node containing all the parameters of a method.
     */
    private ProgramTree parseParameters() {
        TokenLexemaPair pair = lexer.currentPair();
        var currentNode = new ProgramTree(SyntaxComponent.PARAMETERS, pair.getLine(), pair.getPosition());

        while (pair.getToken() != Token.TK_CLOSE_PAREN) {
            currentNode.addChild(parseParameter());

            pair = lexer.currentPair();

            if (pair.getToken() == Token.TK_COMMA) {
                currentNode.addChild(new ProgramTree(pair, pair.getLine(), pair.getPosition()));
                pair = lexer.nextPair();
            }
        }

        return currentNode;
    }

    /**
     * Parses one parameter of a method declaration.
     * @return AST piece with a parameter.
     */
    public ProgramTree parseParameter() {
        var currentNode = new ProgramTree(SyntaxComponent.VARIABLE_DECLARATION, lexer.currentPair().getLine(), lexer.currentPair().getPosition());
        TokenLexemaPair pair = null;

        Token[] tokens = {Token.TK_IDENTIFIER, Token.TK_COLON};
        checkToken(currentNode, pair, tokens);

        currentNode.addChild(parseClassName());

        return currentNode;
    }


    /**
     * Parses statements inside a method.
     * @return AST piece with all statements in a method.
     */
    private ProgramTree parseStatements() {
        TokenLexemaPair pair = lexer.currentPair();
        var currentNode = new ProgramTree(SyntaxComponent.STATEMENTS, pair.getLine(), pair.getPosition());

        while (pair.getToken() != Token.TK_END &&
                pair.getToken() != Token.TK_ELSE) {
            currentNode.addChild(parseStatement());
            pair = lexer.currentPair();
        }

        return currentNode;
    }

    /**
     * Parses one statement.
     * @return AST piece with a statement.
     */
    private ProgramTree parseStatement() {
        TokenLexemaPair pair = lexer.currentPair();
        var currentNode = new ProgramTree(SyntaxComponent.STATEMENT, pair.getLine(), pair.getPosition());

        if (pair.getToken() == Token.TK_VAR) {
            ProgramTree varDecl = parseVariableDeclaration();

//            if (varDecl.getChild(3).getValue() instanceof syntax.SyntaxComponent) {
//                syntax.ProgramTree arrayType = varDecl.getChild(3);
//                pair = lexer.nextPair();
//                checkToken(arrayType, pair, lexer.Token.TK_OPEN_PAREN);
//
//                arrayType.addChild(parseExpression());
//
//                pair = lexer.nextPair();
//                checkToken(arrayType, pair, lexer.Token.TK_CLOSE_PAREN);
//            }

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

    /**
     * Parses assignment. It consists of a keyword, identifier, ":=" and expression.
     * @return Node containing the parts of an assignment.
     */
    private ProgramTree parseAssignment() {
        var currentNode = new ProgramTree(SyntaxComponent.ASSIGNMENT, lexer.currentPair().getLine(), lexer.currentPair().getPosition());
        TokenLexemaPair pair = null;

        checkToken(currentNode, pair, new Token[]
                {Token.TK_IDENTIFIER, Token.TK_ASSIGN});

        currentNode.addChild(parseExpression());

        return currentNode;
    }

    /**
     * Parses if-statement. It consists of a keyword "if", expression, "is", body and "end".
     * @return Node containing the if-statement.
     */
    private ProgramTree parseIf() {
        TokenLexemaPair pair = lexer.nextPair();
        var currentNode = new ProgramTree(SyntaxComponent.IF_STATEMENT, pair.getLine(), pair.getPosition());

        checkToken(currentNode, pair, Token.TK_IF);

        currentNode.addChild(parseExpression());

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_THEN);

        currentNode.addChild(parseStatements());

        pair = lexer.nextPair();

        if (pair.getToken() == Token.TK_END)
            currentNode.addChild(new ProgramTree(pair, pair.getLine(), pair.getPosition()));
        else {
            checkToken(currentNode, pair, Token.TK_ELSE);
            currentNode.addChild(parseStatements());

            pair = lexer.nextPair();
            checkToken(currentNode, pair, Token.TK_END);
        }

        return currentNode;
    }

    /**
     * Parses while loop. It consists of a keyword "while", expression, "loop", body and "end".
     * @return Node containing the while loop.
     */
    private ProgramTree parseWhile() {
        TokenLexemaPair pair = lexer.nextPair();
        var currentNode = new ProgramTree(SyntaxComponent.WHILE_LOOP, pair.getLine(), pair.getPosition());

        checkToken(currentNode, pair, Token.TK_WHILE);

        currentNode.addChild(parseExpression());

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_LOOP);

        currentNode.addChild(parseStatements());

        pair = lexer.nextPair();
        checkToken(currentNode, pair, Token.TK_END);

        return currentNode;
    }

    /**
     * Parses return statement. It consists of a keyword "return" and expression.
     * @return Node containing the parts of a return statement.
     */
    private ProgramTree parseReturn() {
        TokenLexemaPair pair = lexer.nextPair();
        var currentNode = new ProgramTree(SyntaxComponent.RETURN_STATEMENT, pair.getLine(), pair.getPosition());

        checkToken(currentNode, pair, Token.TK_RETURN);
        currentNode.addChild(parseExpression());

        return currentNode;
    }

    /**
     * Parses expression. It consists of a primary and possible method calls.
     * @return Node containing the expression.
     */
    private ProgramTree parseExpression() {
        var currentNode = new ProgramTree(SyntaxComponent.EXPRESSION, 0, 0);
        currentNode.addChild(parsePrimary());

        TokenLexemaPair pair = lexer.currentPair();
        currentNode.setLine(pair.getLine());
        currentNode.setColumn(pair.getPosition());

        if(pair.getToken() == Token.TK_OPEN_PAREN) {
            currentNode.addChild(parseArguments());
            return currentNode;
        }

        while (pair.getToken() == Token.TK_DOT) {
            pair = lexer.nextPair();
            checkToken(currentNode, pair, Token.TK_DOT);

            currentNode.addChild(parseIdentifier());
            currentNode.addChild(parseArguments());
            pair = lexer.currentPair();
        }

        return currentNode;
    }

    private ProgramTree parsePrimary() {
        TokenLexemaPair pair = lexer.currentPair();

        if (pair.getToken() == Token.TK_INTEGER_LITERAL ||
                pair.getToken() == Token.TK_REAL_LITERAL ||
                pair.getToken() == Token.TK_BOOLEAN_LITERAL ||
                pair.getToken() == Token.TK_THIS) {
            pair = lexer.nextPair();
            return new ProgramTree(pair, pair.getLine(), pair.getPosition());
        }

        return parseClassName();
    }

    private ProgramTree parseArguments() {
        TokenLexemaPair pair = lexer.nextPair();
        var currentNode = new ProgramTree(SyntaxComponent.ARGUMENTS, pair.getLine(), pair.getPosition());
        checkToken(currentNode, pair, Token.TK_OPEN_PAREN);
        pair = lexer.currentPair();
        if (pair.getToken() != Token.TK_CLOSE_PAREN)
            currentNode.addChild(parseExpression());

        pair = lexer.nextPair();
        while (pair.getToken() == Token.TK_COMMA) {
            currentNode.addChild(parseExpression());
            pair = lexer.nextPair();
        }

        checkToken(currentNode, pair, Token.TK_CLOSE_PAREN);

        return currentNode;
    }

    private ProgramTree parseExpression1() {
        ProgramTree left = parseTerm();

        TokenLexemaPair pair = lexer.currentPair();
        var currentNode = new ProgramTree(SyntaxComponent.EXPRESSION, pair.getLine(), pair.getPosition());

        while (pair.getToken() == Token.TK_PLUS ||
                pair.getToken() == Token.TK_MINUS) {
            pair = lexer.nextPair();
            ProgramTree temp = new ProgramTree(pair, pair.getLine(), pair.getPosition());
            temp.addChild(left);
            temp.addChild(parseTerm());
            left = temp;

            pair = lexer.currentPair();
        }

        currentNode.addChild(left);

        return currentNode;
    }

    private ProgramTree parseTerm() {
        ProgramTree left = parseFactor();

        TokenLexemaPair pair = lexer.currentPair();
        var currentNode = new ProgramTree(SyntaxComponent.TERM, pair.getLine(), pair.getPosition());

        while (pair.getToken() == Token.TK_MUL ||
                pair.getToken() == Token.TK_DIV) {
            pair = lexer.nextPair();
            ProgramTree temp = new ProgramTree(pair, pair.getLine(), pair.getPosition());
            temp.addChild(left);
            temp.addChild(parseFactor());
            left = temp;

            pair = lexer.currentPair();
        }

        currentNode.addChild(left);

        return currentNode;
    }

    private ProgramTree parseFactor() {
        TokenLexemaPair pair = lexer.nextPair();
        var currentNode = new ProgramTree(SyntaxComponent.FACTOR, pair.getLine(), pair.getPosition());

        if (pair.getToken() == Token.TK_OPEN_PAREN) {
            currentNode.addChild(parseExpression());

            pair = lexer.nextPair();
            if (pair.getToken() != Token.TK_CLOSE_PAREN)
                throw new SyntaxError(pair.getLexema(), pair.getLine(), pair.getPosition());
        } else
            checkToken(currentNode, pair, Token.TK_IDENTIFIER);

        return currentNode;
    }
}
