package university.innopolis.javist;

import org.objectweb.asm.*;
import university.innopolis.javist.lexer.Token;
import university.innopolis.javist.syntax.NodeValue;
import university.innopolis.javist.syntax.ProgramTree;
import university.innopolis.javist.syntax.SyntaxComponent;
import university.innopolis.javist.syntax.TokenLexemaPair;

import java.io.*;
import java.util.List;

public class JVMByteCodeGenerator {
    private static final String CLASS_NAME = "Something";
    private static final String SUPER_CLASS = "java/lang/Object";

    public static void run(ProgramTree tree) throws Exception {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(51, Opcodes.ACC_PUBLIC, CLASS_NAME, null, SUPER_CLASS, null);

        for (ProgramTree classDeclaration : tree.getChildren()) {
            generateClassDeclaration(classDeclaration, cw);
        }

        cw.visitEnd();

        FileOutputStream fos = new FileOutputStream("Something.class");
        fos.write(cw.toByteArray());
        fos.close();
    }

    public static void generateClassDeclaration(ProgramTree classDeclaration, ClassWriter classWriter) throws Exception {
        Integer classBodyPosition = findFirstInChildren(classDeclaration, SyntaxComponent.MEMBER_DECLARATIONS);
        for (ProgramTree memberDeclaration : classDeclaration.getChild(classBodyPosition).getChildren()) {
            if (memberDeclaration.getChild(0).getValue() == SyntaxComponent.VARIABLE_DECLARATION) {
                generateVariableDeclaration(memberDeclaration.getChild(0), classWriter);
            } else if (memberDeclaration.getChild(0).getValue() == SyntaxComponent.METHOD_DECLARATION) {
                generateMethodDeclaration(memberDeclaration.getChild(0), classWriter);
            } else if (memberDeclaration.getChild(0).getValue() == SyntaxComponent.CONSTRUCTOR_DECLARATION) {
                generateConstructorDeclaration(memberDeclaration.getChild(0), classWriter);
            }
        }
    }

    public static void generateVariableDeclaration(ProgramTree variableDeclaration, ClassWriter classWriter) throws Exception {
        FieldVisitor fv = classWriter.visitField(Opcodes.ACC_PRIVATE, ((TokenLexemaPair) variableDeclaration.getChild(findFirstInChildren(variableDeclaration, Token.TK_IDENTIFIER)).getValue()).getLexema(), getTypeDescriptorExpression(variableDeclaration.getChild(findFirstInChildren(variableDeclaration, SyntaxComponent.EXPRESSION))), null, null);
        fv.visitEnd();
    }

    public static void generateMethodDeclaration(ProgramTree methodDeclaration, ClassWriter classWriter) throws Exception {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, ((TokenLexemaPair) methodDeclaration.getChild(findFirstInChildren(methodDeclaration, Token.TK_IDENTIFIER)).getValue()).getLexema(), getMethodDescriptor(methodDeclaration), null, null);
        mv.visitCode();
        Integer statementsPosition = findFirstInChildren(methodDeclaration, SyntaxComponent.STATEMENTS);
        for (ProgramTree statement : methodDeclaration.getChild(statementsPosition).getChildren()) {
            generateBySyntaxComponent(mv, statement);
        }

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static void generateConstructorDeclaration(ProgramTree constructorDeclaration, ClassWriter classWriter) throws Exception {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", getMethodDescriptor(constructorDeclaration), null, null);
        mv.visitCode();

        Integer statementsPosition = findFirstInChildren(constructorDeclaration, SyntaxComponent.STATEMENTS);
        for (ProgramTree statement : constructorDeclaration.getChild(statementsPosition).getChildren()) {
            generateBySyntaxComponent(mv, statement);
        }

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static void generateAssignment(ProgramTree assignment, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        Integer expressionPosition = findFirstInChildren(assignment, SyntaxComponent.EXPRESSION);
        generateExpression(assignment.getChild(expressionPosition), methodVisitor);
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, ((TokenLexemaPair) assignment.getChild(findFirstInChildren(assignment, Token.TK_IDENTIFIER)).getValue()).getLexema(), getTypeDescriptorExpression(assignment.getChild(expressionPosition)));
    }

    public static void generateWhileLoop(ProgramTree whileLoop, MethodVisitor methodVisitor) throws Exception {
        Label startLabel = new Label();
        Label endLabel = new Label();

        methodVisitor.visitLabel(startLabel);
        Integer expressionPosition = findFirstInChildren(whileLoop, SyntaxComponent.EXPRESSION);
        Integer statementsPosition = findFirstInChildren(whileLoop, SyntaxComponent.STATEMENTS);
        generateExpression(whileLoop.getChild(expressionPosition), methodVisitor);
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, endLabel);

        generateBody(whileLoop.getChild(statementsPosition).getChildren(), methodVisitor);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, startLabel);

        methodVisitor.visitLabel(endLabel);
    }

    public static void generateIfStatement(ProgramTree ifStatement, MethodVisitor methodVisitor) throws Exception {
        Label elseLabel = new Label();
        Label endLabel = new Label();

        Integer expressionPosition = findFirstInChildren(ifStatement, SyntaxComponent.EXPRESSION);
        Integer statementsPosition = findFirstInChildren(ifStatement, SyntaxComponent.STATEMENTS);

        generateExpression(ifStatement.getChild(expressionPosition), methodVisitor);
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, elseLabel);

        generateBody(ifStatement.getChild(statementsPosition).getChildren(), methodVisitor);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, endLabel);


        methodVisitor.visitLabel(endLabel);
    }

    public static void generateReturnStatement(ProgramTree returnStatement, MethodVisitor methodVisitor) throws Exception {
        Integer expressionPosition = findFirstInChildren(returnStatement, SyntaxComponent.EXPRESSION);
        generateExpression(returnStatement.getChild(expressionPosition), methodVisitor);
        methodVisitor.visitInsn(Opcodes.IRETURN);
    }

    public static void generateBody(List<ProgramTree> body, MethodVisitor methodVisitor) throws Exception {
        for (ProgramTree statement : body) {
            generateBySyntaxComponent(methodVisitor, statement);
        }
    }

    private static void generateBySyntaxComponent(MethodVisitor methodVisitor, ProgramTree statement) throws Exception {
        if (statement.getChild(0).getValue() == SyntaxComponent.ASSIGNMENT) {
            generateAssignment(statement.getChild(0), methodVisitor);
        } else if (statement.getChild(0).getValue() == SyntaxComponent.WHILE_LOOP) {
            generateWhileLoop(statement.getChild(0), methodVisitor);
        } else if (statement.getChild(0).getValue() == SyntaxComponent.IF_STATEMENT) {
            generateIfStatement(statement.getChild(0), methodVisitor);
        } else if (statement.getChild(0).getValue() == SyntaxComponent.RETURN_STATEMENT) {
            generateReturnStatement(statement.getChild(0), methodVisitor);
        }
    }

    public static void generateExpression(ProgramTree expression, MethodVisitor methodVisitor) throws Exception {
        generatePrimary(expression.getChild(0), methodVisitor);
    }

    public static void generatePrimary(ProgramTree primary, MethodVisitor methodVisitor) {
        if (primary.getValue() instanceof TokenLexemaPair) {
            if (((TokenLexemaPair) primary.getValue()).getToken() == Token.TK_THIS) {
                methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
            }
            else if (((TokenLexemaPair) primary.getValue()).getToken() == Token.TK_BOOLEAN_LITERAL) {
                methodVisitor.visitLdcInsn(Boolean.valueOf(((TokenLexemaPair) primary.getValue()).getLexema()));
            } else if (((TokenLexemaPair) primary.getValue()).getToken() == Token.TK_REAL_LITERAL) {
                methodVisitor.visitLdcInsn(Double.valueOf(((TokenLexemaPair) primary.getValue()).getLexema()));
            } else if (((TokenLexemaPair) primary.getValue()).getToken() == Token.TK_INTEGER_LITERAL) {
                methodVisitor.visitLdcInsn(Integer.valueOf(((TokenLexemaPair) primary.getValue()).getLexema()));
            }
        }
    }

    public static void generateArguments(ProgramTree arguments, MethodVisitor methodVisitor) throws Exception {
        for (int i = 0; i < arguments.getChildren().size(); i++) {
            Integer expressionPosition = findFirstInChildren(arguments.getChild(i), SyntaxComponent.EXPRESSION);
            generateExpression(arguments.getChild(i).getChild(expressionPosition), methodVisitor);
        }
    }

    public static String getMethodDescriptor(ProgramTree methodDeclaration) throws Exception {
        StringBuilder descriptor = new StringBuilder("(");
        Integer parametersPosition = findFirstInChildren(methodDeclaration, SyntaxComponent.PARAMETERS);
        for (ProgramTree parameter : methodDeclaration.getChild(parametersPosition).getChildren()) {
            if (parameter.getValue() != SyntaxComponent.VARIABLE_DECLARATION) {
                continue;
            }
            Integer classNamePosition = findFirstInChildren(parameter, SyntaxComponent.CLASS_NAME);
            descriptor.append(getTypeDescriptorExpression(parameter.getChild(classNamePosition)));
        }
        descriptor.append(")");
        Integer returnTypePosition = findFirstInChildren(methodDeclaration, Token.TK_COLON) + 1;
        descriptor.append(getTypeDescriptorExpression(methodDeclaration.getChild(returnTypePosition)));
        return descriptor.toString();
    }

    public static String getTypeDescriptorExpression(ProgramTree expression) throws Exception {
        if (expression.getChildrenCount() == 0) {
            if (expression.getValue() instanceof TokenLexemaPair) {
                return ((TokenLexemaPair) expression.getValue()).getLexema();
            }
            return "";
        }
        if (expression.getChild(0).getValue() instanceof TokenLexemaPair && ((TokenLexemaPair) expression.getChild(0).getValue()).getToken() == Token.TK_BOOLEAN_LITERAL) {
            return "Z";
        } else if (expression.getChild(0).getValue() instanceof TokenLexemaPair && ((TokenLexemaPair) expression.getChild(0).getValue()).getToken() == Token.TK_INTEGER_LITERAL) {
            return "I";
        } else if (expression.getChild(0).getValue() instanceof TokenLexemaPair && ((TokenLexemaPair) expression.getChild(0).getValue()).getToken() == Token.TK_REAL_LITERAL) {
            return "D";
        } else if (expression.getChild(0).getValue() instanceof TokenLexemaPair && ((TokenLexemaPair) expression.getChild(0).getValue()).getToken() == Token.TK_THIS) {
            return "L" + CLASS_NAME + ";";
        } else if (expression.getChild(0).getValue() instanceof TokenLexemaPair && ((TokenLexemaPair) expression.getChild(0).getValue()).getToken() == Token.TK_IDENTIFIER) {
            return "L" + ((TokenLexemaPair) expression.getChild(0).getValue()).getLexema() + ";";
        }
        return "";
    }

    private static Integer findFirstInChildren(ProgramTree parent, Token token) {
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

    private static Integer findFirstInChildren(ProgramTree parent, SyntaxComponent syntaxComponent) {
        for (int i = 0; i < parent.getChildrenCount(); i++) {
            if (parent.getChild(i).getValue() == syntaxComponent) {
                return i;
            }
        }
        return null;
    }
}