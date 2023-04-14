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

public class SemanticAnalyzer {
    private final ProgramTree root;

    private final SymbolTable symbolTable = new SymbolTable();

    public SemanticAnalyzer(ProgramTree root) {
        this.root = root;
    }

    public void analyze() throws SemanticError {
        try{
            if (root.getValue() == SyntaxComponent.PROGRAM) {
                analyzeProgram(root);
            } else {
                throw new SemanticError("Root of program tree is invalid", root.getLine(), root.getColumn());
            }
        } catch (SemanticError e){
            System.out.println("Semantic Error: "+ e.getMessage());
        }
    }

    public void analyzePredefinedLibraries(String pathToPredefinedLibraries) {
        Lexer lexer = new Lexer(pathToPredefinedLibraries);
        ProgramTree predefinedAST = new SyntaxAnalyser(lexer).makeTree();
        if (predefinedAST.getValue() == SyntaxComponent.PROGRAM) {
            analyzeProgram(predefinedAST);
        } else {
            throw new SemanticError("Root of program tree is invalid", predefinedAST.getLine(), predefinedAST.getColumn());
        }
    }

    private void analyzeProgram(ProgramTree node) throws SemanticError {
        fetchTypes(node);
        for (ProgramTree child : node.getChildren()) {
            if (child.getValue() == SyntaxComponent.CLASS_DECLARATION) {
                analyzeClassDeclaration(child);
            } else {
                throw new SemanticError(Constants.CLASS_DECLARATION_NOT_FOUND, child.getLine(), child.getColumn());
            }
        }
        System.out.println("ok");
    }

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

    private void analyzeClassDeclaration(ProgramTree node) throws SemanticError {
        String className = null;
        for (ProgramTree child : node.getChildren()) {
            if (child.getValue() == SyntaxComponent.CLASS_NAME) {
                className = ((TokenLexemaPair) child.getChild(0).getValue()).getLexema();
                break;
            }
        }
        if (className == null) {
            throw new SemanticError(Constants.NO_CLASS_FOUND_ERROR, node.getLine(), node.getColumn());
        }

        // analyze class body
        ProgramTree bodyNode = node.getChild(3);
        analyzeClassBody(bodyNode, className);
    }

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

    private void analyzeMethodDeclaration(ProgramTree node, String className) throws SemanticError {
        Integer nameNodePosition = findFirstInChildren(node, Token.TK_IDENTIFIER);
        Integer closeParenPosition = findFirstInChildren(node, Token.TK_CLOSE_PAREN);
        Integer parametersPosition = findFirstInChildren(node, SyntaxComponent.PARAMETERS);
        Integer isPosition = findFirstInChildren(node, Token.TK_IS);
        List<ParameterSymbol> parameters;
        if (nameNodePosition == null) {
            throw new SemanticError(Constants.METHOD_SHOULD_HAVE_NAME, node.getLine(), node.getColumn());
        }
        String methodName = ((TokenLexemaPair) node.getChild(nameNodePosition).getValue()).getLexema();
        String returnType = null;
        assert closeParenPosition != null;
        assert isPosition != null;
        if (isPosition - closeParenPosition > 1) {
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
            analyzeStatements(node.getChild(statementsPosition), className, methodSymbol.getScope());
        }
    }

    private void analyzeConstructorDeclaration(ProgramTree node, String className) throws SemanticError {
        ProgramTree nameNode = node.getChild(0);
        TokenLexemaPair namePair = (TokenLexemaPair) nameNode.getValue();
        String constructorName = namePair.getLexema();
        // TODO: args reload
    }

    private void analyzeStatements(ProgramTree node, String className, Scope scope) {
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
        }
    }

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

    private List<ParameterSymbol> parseParameters(ProgramTree node, String className, Scope scope) {
        List<ParameterSymbol> result = new ArrayList<>();
        for (ProgramTree child : node.getChildren()) {
            if (child.getValue() == SyntaxComponent.EXPRESSION) {
                result.add(new ParameterSymbol(this.parseExpression(child.getChildren(), className, scope, null)));
            }
        }
        return result;
    }

    private Integer findFirstInChildren(ProgramTree parent, SyntaxComponent syntaxComponent) {
        for (int i = 0; i < parent.getChildrenCount(); i++) {
            if (parent.getChild(i).getValue() == syntaxComponent) {
                return i;
            }
        }
        return null;
    }

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

    private String checkMethod(String className, String methodName, List<ParameterSymbol> parameters) {
        ClassSymbol classSymbol = (ClassSymbol) this.symbolTable.get(className);
        if (!classSymbol.isMethodExists(methodName, parameters)) {
            throw new SemanticError(String.format(Constants.METHOD_NAME_NOT_DEFINED, methodName), 0, 0);
        }
        MethodSymbol methodSymbol = classSymbol.methodLookup(methodName, parameters);
        return methodSymbol.getReturnType();
    }

}