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
        FieldVisitor fv = classWriter.visitField(Opcodes.ACC_PRIVATE, ((TokenLexemaPair)variableDeclaration.getChild(findFirstInChildren(variableDeclaration, Token.TK_IDENTIFIER)).getValue()).getLexema(), getTypeDescriptorExpression(variableDeclaration.getChild(findFirstInChildren(variableDeclaration, SyntaxComponent.EXPRESSION))), null, null);
        fv.visitEnd();
    }

    public static void generateMethodDeclaration(ProgramTree methodDeclaration, ClassWriter classWriter) throws Exception {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, ((TokenLexemaPair)methodDeclaration.getChild(findFirstInChildren(methodDeclaration, Token.TK_IDENTIFIER)).getValue()).getLexema(), getMethodDescriptor(methodDeclaration), null, null);
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
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, ((TokenLexemaPair)assignment.getChild(findFirstInChildren(assignment, Token.TK_IDENTIFIER)).getValue()).getLexema(), getTypeDescriptor(assignment.getChild(expressionPosition)));
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
        if (((TokenLexemaPair)expression.getChild(0).getValue()).getToken() == Token.TK_BOOLEAN_LITERAL ||
        ((TokenLexemaPair)expression.getChild(0).getValue()).getToken() == Token.TK_REAL_LITERAL ||
        ((TokenLexemaPair)expression.getChild(0).getValue()).getToken() == Token.TK_INTEGER_LITERAL) {
            generatePrimary(expression.getChild(0), methodVisitor);
        }
        for (int i = 0; i < expression.getIdentifiers().size(); i++) {
            Identifier identifier = expression.getIdentifiers().get(i);
            Arguments arguments = expression.getArguments().get(i);
            generateArguments(arguments, methodVisitor);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, CLASS_NAME, identifier, getMethodDescriptor(arguments));
        }
    }

    public static void generatePrimary(ProgramTree primary, MethodVisitor methodVisitor) throws Exception {
        if (primary instanceof IntegerLiteral) {
            IntegerLiteral integerLiteral = (IntegerLiteral) primary;
            methodVisitor.visitIntInsn(Opcodes.BIPUSH, integerLiteral.getValue());
        } else if (primary instanceof RealLiteral) {
            RealLiteral realLiteral = (RealLiteral) primary;
            methodVisitor.visitLdcInsn(realLiteral.getValue());
        } else if (primary instanceof BooleanLiteral) {
            BooleanLiteral booleanLiteral = (BooleanLiteral) primary;
            methodVisitor.visitInsn(booleanLiteral.getValue() ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        } else if (primary instanceof This) {
            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        } else if (primary instanceof ClassName) {
            ClassName className = (ClassName) primary;
            methodVisitor.visitTypeInsn(Opcodes.NEW, getTypeDescriptor(className));
            methodVisitor.visitInsn(Opcodes.DUP);
            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, getTypeDescriptor(className), "<init>", "()V");
        }
    }

    public static void generateArguments(Arguments arguments, MethodVisitor methodVisitor) throws Exception {
        for (Expression expression : arguments.getExpressions()) {
            generateExpression(expression, methodVisitor);
        }
    }

    public static String getTypeDescriptor(Expression expression) {
        if (expression instanceof IntegerLiteral) {
            return "I";
        } else if (expression instanceof RealLiteral) {
            return "D";
        } else if (expression instanceof BooleanLiteral) {
            return "Z";
        } else if (expression instanceof This) {
            return CLASS_NAME;
        } else if (expression instanceof ClassName) {
            ClassName className = (ClassName) expression;
            return getTypeDescriptor(className);
        }
        return null;
    }

    public static String getTypeDescriptor(ClassName className) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < className.getDimensions(); i++) {
            sb.append("[");
        }
        sb.append(className.getIdentifier());
        return sb.toString();
    }

    public static String getMethodDescriptor(MethodDeclaration methodDeclaration) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (ParameterDeclaration parameterDeclaration : methodDeclaration.getParameters()) {
            sb.append(getTypeDescriptor(parameterDeclaration.getClassName()));
        }
        sb.append(")");
        sb.append(getTypeDescriptor(methodDeclaration.getReturnType()));
        return sb.toString();
    }

    public static String getMethodDescriptor(ConstructorDeclaration constructorDeclaration) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (ParameterDeclaration parameterDeclaration : constructorDeclaration.getParameters()) {
            sb.append(getTypeDescriptor(parameterDeclaration.getClassName()));
        }
        sb.append(")V");
        return sb.toString();
    }

    public static String getMethodDescriptor(Arguments arguments) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Expression expression : arguments.getExpressions()) {
            sb.append(getTypeDescriptor(expression));
        }
        sb.append(")I");
        return sb.toString();
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