package university.innopolis.javist.symantic;

import university.innopolis.javist.errors.Constants;
import university.innopolis.javist.errors.SemanticError;
import university.innopolis.javist.errors.SymbolNotFoundException;
import university.innopolis.javist.lexer.Lexer;
import university.innopolis.javist.lexer.Token;
import university.innopolis.javist.symantic.symbol.*;
import university.innopolis.javist.syntax.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The SemanticAnalyzer class is responsible for performing semantic analysis on a program tree.
 * It analyzes the structure and validity of the program's syntax and symbols.
 */
public class SemanticAnalyzer {
    private final ProgramTree root;

    private final SymbolTable symbolTable = new SymbolTable();

    /**
     * Constructs a SemanticAnalyzer object with the specified program tree.
     *
     * @param root The root of the program tree to be analyzed.
     */
    public SemanticAnalyzer(ProgramTree root) {
        this.root = root.clone();
    }

    /**
     * Performs semantic analysis on the program tree.
     *
     * @throws SemanticError if a semantic error is encountered during analysis.
     */
    public void analyze() throws SemanticError {
        try {
            if (root.getValue() == SyntaxComponent.PROGRAM) {
                analyzeProgram(root);
            } else {
                throw new SemanticError("Root of program tree is invalid", root.getLine(), root.getColumn());
            }
        } catch (SemanticError e) {
            System.out.println("Semantic Error: " + e.getMessage());
        }
    }

    /**
     * Analyzes the predefined libraries specified by the given path.
     *
     * @param pathToPredefinedLibraries The path to the predefined libraries.
     * @throws SemanticError if a semantic error is encountered during analysis.
     */
    public void analyzePredefinedLibraries(String pathToPredefinedLibraries) {
        Lexer lexer = new Lexer(pathToPredefinedLibraries);
        ProgramTree predefinedAST = new SyntaxAnalyser(lexer).makeTree();
        if (predefinedAST.getValue() == SyntaxComponent.PROGRAM) {
            analyzeProgram(predefinedAST);
        } else {
            throw new SemanticError("Root of program tree is invalid", predefinedAST.getLine(), predefinedAST.getColumn());
        }
    }

    /**
     * Analyzes the program represented by the given program tree.
     *
     * @param node The program tree node representing the program.
     * @throws SemanticError if a semantic error is encountered during analysis.
     */
    private void analyzeProgram(ProgramTree node) throws SemanticError {
        fetchTypes(node);
        for (ProgramTree child : node.getChildren()) {
            if (child.getValue() == SyntaxComponent.CLASS_DECLARATION) {
                analyzeClassDeclaration(child);
            } else {
                throw new SemanticError(Constants.CLASS_DECLARATION_NOT_FOUND, child.getLine(), child.getColumn());
            }
        }
    }

    /**
     * Fetches the types defined in the program represented by the given program tree.
     *
     * @param node The program tree node representing the program.
     */
    private void fetchTypes(ProgramTree node) {
        for (ProgramTree child : node.getChildren()) {
            String className = null;
            String parentName = null;
            if (child.getValue() == SyntaxComponent.CLASS_DECLARATION) {
                Integer namePosition = findFirstInChildren(child, SyntaxComponent.CLASS_NAME);
                Integer parentNamePosition = findFirstInChildren(child.getChild(namePosition), SyntaxComponent.CLASS_NAME);
                className = ((TokenLexemaPair) child.getChild(namePosition).getChild(0).getValue()).getLexema();
                if (parentNamePosition != null) {
                    parentName = ((TokenLexemaPair) child.getChild(namePosition).getChild(parentNamePosition).getChild(0).getValue()).getLexema();
                }
                if (symbolTable.get(className) != null) {
                    throw new SemanticError(String.format(Constants.CLASS_ALREADY_EXISTS, className), child.getLine(), child.getColumn());
                }
                symbolTable.put(className, new ClassSymbol(className, parentName));
            } else {
                throw new SemanticError(Constants.CLASS_DECLARATION_NOT_FOUND, child.getLine(), child.getColumn());
            }
        }
    }

    /**
     * Analyzes a class declaration represented by the given program tree node.
     *
     * @param node The program tree node representing the class declaration.
     * @throws SemanticError if a semantic error is encountered during analysis.
     */
    private void analyzeClassDeclaration(ProgramTree node) throws SemanticError {
        // Get class name
        Integer classNamePosition = findFirstInChildren(node, SyntaxComponent.CLASS_NAME);
        if(classNamePosition == null) {
            throw new SemanticError(Constants.NO_CLASS_FOUND_ERROR, node.getLine(), node.getColumn());
        }
        String className = ((TokenLexemaPair) node.getChild(classNamePosition).getChild(0).getValue()).getLexema();

        // analyze class body
        ProgramTree bodyNode = node.getChild(3);
        analyzeClassBody(bodyNode, className);
    }


    /**
     * Analyzes the body of a class represented by the given program tree node.
     *
     * @param node      The program tree node representing the class body.
     * @param className The name of the class being analyzed.
     * @throws SemanticError if a semantic error is encountered during analysis.
     */
    private void analyzeClassBody(ProgramTree node, String className) throws SemanticError {
        if (node.getValue() == SyntaxComponent.MEMBER_DECLARATIONS) {
            for (ProgramTree child : node.getChildren()) {
                if (child.getChild(0).getValue() == SyntaxComponent.VARIABLE_DECLARATION) {
                    analyzeVariableDeclaration(child.getChild(0), className);
                } else if (child.getChild(0).getValue() == SyntaxComponent.METHOD_DECLARATION) {
                    analyzeMethodDeclaration(child.getChild(0), className);
                } else if (child.getChild(0).getValue() == SyntaxComponent.CONSTRUCTOR_DECLARATION) {
                    analyzeConstructorDeclaration(child.getChild(0), className);
                } else {
                    throw new SemanticError("Class body can contain only variable, method and constructor declarations",
                            child.getLine(), child.getColumn());
                }
            }
        }
    }

    /**
     * Analyzes a variable declaration represented by the given program tree node.
     *
     * @param node      The program tree node representing the variable declaration.
     * @param className The name of the class in which the variable is declared.
     * @throws SemanticError if a semantic error is encountered during analysis.
     */
    private void analyzeVariableDeclaration(ProgramTree node, String className) throws SemanticError {
        ProgramTree nameNode = node.getChild(1);
        if (!(nameNode.getValue() instanceof TokenLexemaPair)) {
            throw new SemanticError(Constants.VARIABLE_IDENTIFIER_NOT_DEFINED, nameNode.getLine(), nameNode.getColumn());
        }

        TokenLexemaPair namePair = (TokenLexemaPair) nameNode.getValue();
        if (namePair.getToken() != Token.TK_IDENTIFIER) {
            throw new SemanticError(Constants.VARIABLE_IDENTIFIER_NOT_DEFINED, namePair.getLine(), namePair.getPosition());
        }

        String variableName = namePair.getLexema();
        String variableType = null;
        ClassSymbol classSymbol = (ClassSymbol) symbolTable.get(className);
        if (classSymbol.isVariableExists(variableName)) {
            throw new SemanticError(String.format(Constants.VARIABLE_ALREADY_EXISTS, variableName), namePair.getLine(), namePair.getPosition());
        }
        if (node.getChild(3).getValue() == SyntaxComponent.EXPRESSION) {
            variableType = parseExpression(node.getChild(3).getChildren(), className, new Scope(), null);
        }
        classSymbol.getVariables().put(variableName, new VariableSymbol(variableName, variableType));
    }

    /**
     * Analyzes a method declaration represented by the given program tree node.
     *
     * @param node      The program tree node representing the method declaration.
     * @param className The name of the class in which the method is declared.
     * @throws SemanticError if a semantic error is encountered during analysis.
     */
    private void analyzeMethodDeclaration(ProgramTree node, String className) throws SemanticError {
        // Get positions for method key description
        Integer nameNodePosition = findFirstInChildren(node, Token.TK_IDENTIFIER);
        Integer closeParenPosition = findFirstInChildren(node, Token.TK_CLOSE_PAREN);
        Integer parametersPosition = findFirstInChildren(node, SyntaxComponent.PARAMETERS);
        Integer isPosition = findFirstInChildren(node, Token.TK_IS);

        assert closeParenPosition != null;
        assert isPosition != null;
        assert nameNodePosition != null;

        List<ParameterSymbol> parameters;
        String methodName = ((TokenLexemaPair) node.getChild(nameNodePosition).getValue()).getLexema();
        String returnType = null;

        if (isPosition - closeParenPosition > 1) {
            // Not void method
            returnType = ((TokenLexemaPair) node.getChild(isPosition - 1).getValue()).getLexema();
        }
        if (symbolTable.get(returnType) == null) {
            throw new SemanticError(String.format(Constants.TYPE_NOT_DEFINED, returnType), node.getLine(), node.getColumn());
        }
        parameters = parametersPosition == null ? new ArrayList<>() :
                analyzeParameters(node.getChild(parametersPosition));
        ClassSymbol classSymbol = (ClassSymbol) symbolTable.get(className);
        MethodSymbol methodSymbol = new MethodSymbol(methodName, returnType, parameters);
        for (ParameterSymbol parameter : parameters) {
            methodSymbol.getScope().put(parameter.getName(), new VariableSymbol(parameter.getName(), parameter.getType()));
        }
        classSymbol.addMethod(methodSymbol);

        // analyze statements
        Integer statementsPosition = findFirstInChildren(node, SyntaxComponent.STATEMENTS);
        if (statementsPosition != null) {
            analyzeStatements(node.getChild(statementsPosition), className, methodSymbol.getScope(), methodSymbol);
        }
    }

    /**
     * Analyzes a constructor declaration represented by the given program tree node.
     *
     * @param node      The program tree node representing the constructor declaration.
     * @param className The name of the class in which the constructor is declared.
     * @throws SemanticError if a semantic error is encountered during analysis.
     */
    private void analyzeConstructorDeclaration(ProgramTree node, String className) throws SemanticError {
        ProgramTree nameNode = node.getChild(0);
        TokenLexemaPair namePair = (TokenLexemaPair) nameNode.getValue();
        String constructorName = namePair.getLexema();
        // TODO: args reload
    }

    /**
     * Analyzes statements within a method or constructor.
     *
     * @param node         The program tree node representing the statements.
     * @param className    The name of the class in which the statements are analyzed.
     * @param scope        The scope in which the statements are analyzed.
     * @param methodSymbol The method symbol representing the method or constructor.
     */
    private void analyzeStatements(ProgramTree node, String className, Scope scope, MethodSymbol methodSymbol) {
        ClassSymbol classSymbol = (ClassSymbol) symbolTable.get(className);
        for (ProgramTree statement : node.getChildren()) {

            if (statement.getChild(0).getValue() == SyntaxComponent.VARIABLE_DECLARATION) {
                VariableSymbol variableSymbol = analyzeVariableInScope(statement.getChild(0), className, scope);
                scope.put(variableSymbol.getName(), variableSymbol);
            }

            if (statement.getChild(0).getValue() == SyntaxComponent.ASSIGNMENT) {
                VariableSymbol variableSymbol = analyzeAssigmentInScope(statement.getChild(0), className, scope);
                VariableSymbol originalVariable = (VariableSymbol) scope.variableLookup(variableSymbol.getName());
                if (originalVariable == null) {
                    originalVariable = classSymbol.variableLookup(variableSymbol.getName());
                }
                if (originalVariable == null) {
                    throw new SemanticError(String.format(Constants.VARIABLE_NAME_NOT_DEFINED, variableSymbol.getName()),
                            statement.getLine(), statement.getColumn());
                }
                if (!originalVariable.getType().equals(variableSymbol.getType())) {
                    throw new SemanticError(String.format(Constants.UNEXPECTED_VARIABLE_TYPE, variableSymbol.getType(),
                            variableSymbol.getName(), originalVariable.getType()),
                            statement.getLine(), statement.getColumn());
                }
            }

            if (statement.getChild(0).getValue() == SyntaxComponent.RETURN_STATEMENT) {
                String type = parseExpression(statement.getChild(0).getChild(1).getChildren(), className, scope, null);
                if ((methodSymbol.getReturnType() == null && type != null) ||
                        !methodSymbol.getReturnType().equals(type)) {
                    throw new SemanticError(String.format(Constants.INVALID_RETURN_STATEMENT, type,
                            methodSymbol.getReturnType() == null ? "void" : methodSymbol.getReturnType()),
                            statement.getChild(0).getLine(), statement.getChild(0).getColumn());
                }

            }

            if(statement.getChild(0).getValue() == SyntaxComponent.WHILE_LOOP){
                analyzeLoop(statement.getChild(0), className, scope, methodSymbol);
            }

            if(statement.getChild(0).getValue() == SyntaxComponent.IF_STATEMENT){
                analyzeIf(statement.getChild(0), className, scope, methodSymbol);
            }
        }
    }

    private void analyzeLoop(ProgramTree node, String className, Scope scope, MethodSymbol methodSymbol){
        scope = new Scope(scope);
        ProgramTree condition = node.getChild(1);
        String type = parseExpression(condition.getChildren(), className, scope, null);
        if(!type.equals("Boolean")){
            throw new SemanticError(String.format(Constants.INVALID_LOOP_CONDITION, type),
                    condition.getLine(), condition.getColumn());
        }
        analyzeStatements(node.getChild(3), className, scope, methodSymbol);
    }

    private void analyzeIf (ProgramTree node, String className, Scope scope, MethodSymbol methodSymbol){
        scope = new Scope(scope);
        ProgramTree condition = node.getChild(1);
        String type = parseExpression(condition.getChildren(), className, scope, null);
        if(!type.equals("Boolean")){
            throw new SemanticError(String.format(Constants.INVALID_IF_CONDITION, type),
                    condition.getLine(), condition.getColumn());
        }
        analyzeStatements(node.getChild(3), className, scope, methodSymbol);
    }

    /**
     * Analyzes a variable declaration within a scope.
     *
     * @param node      The program tree node representing the variable declaration.
     * @param className The name of the class in which the variable is declared.
     * @param scope     The scope in which the variable is declared.
     * @return The variable symbol representing the analyzed variable.
     * @throws SemanticError if a semantic error is encountered during analysis.
     */
    private VariableSymbol analyzeVariableInScope(ProgramTree node, String className, Scope scope) {
        Integer variableNamePosition = findFirstInChildren(node, Token.TK_IDENTIFIER);
        Integer expressionPosition = findFirstInChildren(node, SyntaxComponent.EXPRESSION);
        String variableName = ((TokenLexemaPair) node.getChild(variableNamePosition).getValue()).getLexema();
        String type = null;
        if (scope.isExist(variableName) || ((ClassSymbol) symbolTable.get(className)).isVariableExists(variableName)) {
            throw new SemanticError(String.format(Constants.VARIABLE_ALREADY_EXISTS, variableName),
                    node.getLine(), node.getColumn());
        }
        if (expressionPosition != null) {
            type = parseExpression(node.getChild(expressionPosition).getChildren(), className, scope, null);
        }
        // TODO: analyze all kinds of expressions
        return new VariableSymbol(variableName, type);
    }

    /**
     * Analyzes an assignment within a scope.
     *
     * @param node      The program tree node representing the assignment.
     * @param className The name of the class in which the assignment occurs.
     * @param scope     The scope in which the assignment occurs.
     * @return The variable symbol representing the assigned variable.
     */
    private VariableSymbol analyzeAssigmentInScope(ProgramTree node, String className, Scope scope) {
        String variableName = null;
        String variableType = null;
        Integer variableNamePosition = findFirstInChildren(node, Token.TK_IDENTIFIER);
        Integer expressionPosition = findFirstInChildren(node, SyntaxComponent.EXPRESSION);
        ClassSymbol classSymbol = (ClassSymbol) this.symbolTable.get(className);
        if (variableNamePosition == null) {
            throw new SemanticError(String.format(Constants.NOT_EMPTY_VARIABLE_NAME),
                    node.getLine(), node.getColumn());
        }
        if (expressionPosition != null) {
            variableType = parseExpression(node.getChild(expressionPosition).getChildren(), className, scope, null);
        }
        variableName = ((TokenLexemaPair) node.getChild(variableNamePosition).getValue()).getLexema();

        return new VariableSymbol(variableName, variableType);
    }

    /**
     * Parses an expression recursively.
     *
     * @param nodes       The list of program tree nodes representing the expression.
     * @param className   The name of the current class.
     * @param scope       The current scope.
     * @param currentType The current type being evaluated in the expression.
     * @return The type of the expression.
     * @throws SemanticError if a semantic error is encountered during parsing.
     */
    private String parseExpression(List<ProgramTree> nodes, String className, Scope scope, String currentType) {
        if (nodes.size() == 0 && currentType == null) {
            throw new SemanticError(Constants.INVALID_EXPRESSION, 0, 0);
        } else if (nodes.size() == 0) {
            return currentType;
        }
        ClassSymbol classSymbol = (ClassSymbol) this.symbolTable.get(className);
        ProgramTree currentNode = nodes.get(0);
        nodes.remove(0);
        if (currentNode.getValue() == SyntaxComponent.CLASS_NAME) {
            String returnType = ((TokenLexemaPair) currentNode.getChild(0).getValue()).getLexema();
            VariableSymbol variableSymbol = (VariableSymbol) scope.variableLookup(returnType);
            if (variableSymbol == null) {
                variableSymbol = classSymbol.variableLookup(returnType);
            }
            if (symbolTable.get(returnType) != null) {
                return parseExpression(nodes, className, scope, returnType);
            }
            if (variableSymbol == null) {
                throw new SemanticError(String.format(Constants.VARIABLE_NAME_NOT_DEFINED, returnType),
                        currentNode.getLine(), currentNode.getColumn());
            }
            return parseExpression(nodes, className, scope, variableSymbol.getType());
        }
        if (currentNode.getValue() instanceof TokenLexemaPair) {
            String literalType = switch (((TokenLexemaPair) currentNode.getValue()).getToken()) {
                case TK_BOOLEAN_LITERAL -> "Boolean";
                case TK_INTEGER_LITERAL -> "Integer";
                case TK_REAL_LITERAL -> "Real";
                default -> null;
            };
            if (literalType != null) {
                return parseExpression(nodes, className, scope, literalType);
            }
        }
        String resultType = new String(currentType);
        while (currentNode.getValue() instanceof TokenLexemaPair
                && ((TokenLexemaPair) currentNode.getValue()).getToken() == Token.TK_DOT) {
            if (resultType == null) {
                throw new SemanticError(String.format(Constants.CANNOT_RESOLVE_METHOD,
                        ((TokenLexemaPair) nodes.get(0).getValue()).getLexema()),
                        currentNode.getLine(), currentNode.getColumn());
            }
            ClassSymbol currentTypeClass = (ClassSymbol) symbolTable.get(resultType);

            currentNode = nodes.get(0);
            nodes.remove(0);
            String methodName = ((TokenLexemaPair) currentNode.getValue()).getLexema();

            if (nodes.size() > 0) {
                currentNode = nodes.get(0);
                nodes.remove(0);
            }
            List<ParameterSymbol> parameters;
            if (currentNode.getValue() == SyntaxComponent.ARGUMENTS) {
                parameters = parseParameters(currentNode, className, scope);
                if (nodes.size() > 0) {
                    currentNode = nodes.get(0);
                    nodes.remove(0);
                }
            } else {
                parameters = new ArrayList<>();
            }
            MethodSymbol methodSymbol = currentTypeClass.methodLookup(methodName, parameters);
            if (methodSymbol == null) {
                throw new SemanticError(String.format(Constants.CANNOT_RESOLVE_METHOD, methodName),
                        currentNode.getLine(), currentNode.getColumn());
            }
            resultType = methodSymbol.getReturnType();
        }
        return parseExpression(nodes, className, scope, resultType);
    }

    /**
     * Parses the parameters of a method or constructor.
     *
     * @param node      The program tree node representing the parameters.
     * @param className The name of the current class.
     * @param scope     The current scope.
     * @return The list of parameter symbols.
     */
    private List<ParameterSymbol> parseParameters(ProgramTree node, String className, Scope scope) {
        List<ParameterSymbol> result = new ArrayList<>();
        for (ProgramTree child : node.getChildren()) {
            if (child.getValue() == SyntaxComponent.EXPRESSION) {
                result.add(new ParameterSymbol(this.parseExpression(child.getChildren(), className, scope, null)));
            }
        }
        return result;
    }

    /**
     * Finds the index of the first child node with the specified syntax component value.
     *
     * @param parent          The parent program tree node.
     * @param syntaxComponent The syntax component value to search for.
     * @return The index of the first matching child node, or null if not found.
     */
    private Integer findFirstInChildren(ProgramTree parent, SyntaxComponent syntaxComponent) {
        for (int i = 0; i < parent.getChildrenCount(); i++) {
            if (parent.getChild(i).getValue() == syntaxComponent) {
                return i;
            }
        }
        return null;
    }

    /**
     * Finds the index of the first child node with the specified token value.
     *
     * @param parent The parent program tree node.
     * @param token  The token value to search for.
     * @return The index of the first matching child node, or null if not found.
     */
    private Integer findFirstInChildren(ProgramTree parent, Token token) {
        for (int i = 0; i < parent.getChildrenCount(); i++) {
            NodeValue current = parent.getChild(i).getValue();
            if (current instanceof TokenLexemaPair) {
                if (((TokenLexemaPair) current).getToken() == token) {
                    return i;
                }
            }
        }
        return null;
    }

    /**
     * Analyzes the parameters of a method or constructor declaration.
     *
     * @param parent The program tree node representing the parameters.
     * @return The list of parameter symbols.
     */
    private List<ParameterSymbol> analyzeParameters(ProgramTree parent) {
        List<ParameterSymbol> result = new ArrayList<>();
        for (ProgramTree child : parent.getChildren()) {
            if (child.getValue() == SyntaxComponent.VARIABLE_DECLARATION) {
                ParameterSymbol parameter = new ParameterSymbol(
                        ((TokenLexemaPair) child.getChild(0).getValue()).getLexema(),
                        ((TokenLexemaPair) child.getChild(2).getChild(0).getValue()).getLexema());
                result.add(parameter);
            }
        }
        return result;
    }

    /**
     * Checks if a method with the given name and parameters exists in a class and returns its return type.
     *
     * @param className  The name of the class.
     * @param methodName The name of the method.
     * @param parameters The list of parameter symbols.
     * @return The return type of the method.
     * @throws SemanticError If the method is not found in the class.
     */
    private String checkMethod(String className, String methodName, List<ParameterSymbol> parameters) {
        ClassSymbol classSymbol = (ClassSymbol) this.symbolTable.get(className);
        if (!classSymbol.isMethodExists(methodName, parameters)) {
            throw new SemanticError(String.format(Constants.METHOD_NAME_NOT_DEFINED, methodName), 0, 0);
        }
        MethodSymbol methodSymbol = classSymbol.methodLookup(methodName, parameters);
        return methodSymbol.getReturnType();
    }

}