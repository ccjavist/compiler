package university.innopolis.javist;

import org.objectweb.asm.*;
import university.innopolis.javist.lexer.Token;
import university.innopolis.javist.syntax.NodeValue;
import university.innopolis.javist.syntax.ProgramTree;
import university.innopolis.javist.syntax.SyntaxComponent;
import university.innopolis.javist.syntax.TokenLexemaPair;

import java.io.*;

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
        FieldVisitor fv = classWriter.visitField(Opcodes.ACC_PRIVATE, (((TokenLexemaPair)variableDeclaration.getChild(findFirstInChildren(variableDeclaration, Token.TK_IDENTIFIER)).getValue()).getLexema(), getTypeDescriptorExpression(variableDeclaration.getChild(findFirstInChildren(variableDeclaration, SyntaxComponent.EXPRESSION))), null, null);
        fv.visitEnd();
    }

    public static void generateMethodDeclaration(ProgramTree methodDeclaration, ClassWriter classWriter) throws Exception {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, (((TokenLexemaPair)methodDeclaration.getChild(findFirstInChildren(methodDeclaration, Token.TK_IDENTIFIER)).getValue()).getLexema(), getMethodDescriptor(methodDeclaration), null, null);
        mv.visitCode();
        Integer statementsPosition = findFirstInChildren(methodDeclaration, SyntaxComponent.STATEMENTS);
        for (ProgramTree statement : methodDeclaration.getChild(statementsPosition).getChildren()) {
            if (statement.getChild(0).getValue() == SyntaxComponent.ASSIGNMENT) {
                generateAssignment(statement.getChild(0), mv);
            } else if (statement.getChild(0).getValue() == SyntaxComponent.WHILE_LOOP) {
                generateWhileLoop(statement.getChild(0), mv);
            } else if (statement.getChild(0).getValue() == SyntaxComponent.IF_STATEMENT) {
                generateIfStatement(statement.getChild(0), mv);
            } else if (statement.getChild(0).getValue() == SyntaxComponent.RETURN_STATEMENT) {
                generateReturnStatement(statement.getChild(0), mv);
            }
        }

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static void generateConstructorDeclaration(ProgramTree constructorDeclaration, ClassWriter classWriter) throws Exception {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", getMethodDescriptor(constructorDeclaration), null, null);
        mv.visitCode();

        for (ProgramTree statement : constructorDeclaration.getBody()) {
            if (statement instanceof Assignment) {
                generateAssignment((Assignment) statement, mv);
            } else if (statement instanceof WhileLoop) {
                generateWhileLoop((WhileLoop) statement, mv);
            } else if (statement instanceof IfStatement) {
                generateIfStatement((IfStatement) statement, mv);
            } else if (statement instanceof ReturnStatement) {
                generateReturnStatement((ReturnStatement) statement, mv);
            }
        }

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    public static void generateAssignment(ProgramTree assignment, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        generateExpression(assignment.getExpression(), methodVisitor);
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, assignment.getIdentifier(), getTypeDescriptor(assignment.getExpression()));
    }

    public static void generateWhileLoop(ProgramTree whileLoop, MethodVisitor methodVisitor) throws Exception {
        Label startLabel = new Label();
        Label endLabel = new Label();

        methodVisitor.visitLabel(startLabel);

        generateExpression(whileLoop.getExpression(), methodVisitor);
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, endLabel);

        generateBody(whileLoop.getBody(), methodVisitor);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, startLabel);

        methodVisitor.visitLabel(endLabel);
    }

    public static void generateIfStatement(ProgramTree ifStatement, MethodVisitor methodVisitor) throws Exception {
        Label elseLabel = new Label();
        Label endLabel = new Label();

        generateExpression(ifStatement.getExpression(), methodVisitor);
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, elseLabel);

        generateBody(ifStatement.getThenBody(), methodVisitor);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, endLabel);

        methodVisitor.visitLabel(elseLabel);
        generateBody(ifStatement.getElseBody(), methodVisitor);

        methodVisitor.visitLabel(endLabel);
    }

    public static void generateReturnStatement(ProgramTree returnStatement, MethodVisitor methodVisitor) throws Exception {
        generateExpression(returnStatement.getExpression(), methodVisitor);
        methodVisitor.visitInsn(Opcodes.IRETURN);
    }

    public static void generateBody(List<ProgramTree> body, MethodVisitor methodVisitor) throws Exception {
        for (Statement statement : body) {
            if (statement instanceof Assignment) {
                generateAssignment((Assignment) statement, methodVisitor);
            } else if (statement instanceof WhileLoop) {
                generateWhileLoop((WhileLoop) statement, methodVisitor);
            } else if (statement instanceof IfStatement) {
                generateIfStatement((IfStatement) statement, methodVisitor);
            } else if (statement instanceof ReturnStatement) {
                generateReturnStatement((ReturnStatement) statement, methodVisitor);
            }
        }
    }

    public static void generateExpression(ProgramTree expression, MethodVisitor methodVisitor) throws Exception {
        if (expression instanceof Primary) {
            generatePrimary((Primary) expression, methodVisitor);
        }
    }

    public static void generatePrimary(ProgramTree primary, MethodVisitor methodVisitor) throws Exception {
        if (primary instanceof IntegerLiteral) {
            generateIntegerLiteral((IntegerLiteral) primary, methodVisitor);
        } else if (primary instanceof RealLiteral) {
            generateRealLiteral((RealLiteral) primary, methodVisitor);
        } else if (primary instanceof BooleanLiteral) {
            generateBooleanLiteral((BooleanLiteral) primary, methodVisitor);
        } else if (primary instanceof This) {
            generateThis((This) primary, methodVisitor);
        } else if (primary instanceof ClassName) {
            generateClassName((ClassName) primary, methodVisitor);
        }
    }

    public static void generateIntegerLiteral(ProgramTree integerLiteral, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitLdcInsn(integerLiteral.getValue());
    }

    public static void generateRealLiteral(ProgramTree realLiteral, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitLdcInsn(realLiteral.getValue());
    }

    public static void generateBooleanLiteral(ProgramTree booleanLiteral, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitLdcInsn(booleanLiteral.getValue());
    }

    public static void generateThis(ProgramTree thisObject, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
    }

    public static void generateClassName(ProgramTree className, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitTypeInsn(Opcodes.NEW, className.getName());
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, className.getName(), "<init>", "()V", false);
    }

    public static String getTypeDescriptorExpression(ProgramTree expression) {
        if (expression instanceof Primary) {
            return getTypeDescriptor((Primary) expression);
        }

        return "V";
    }

    public static String getMethodDescriptor(ProgramTree methodDeclaration) {
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        for (ProgramTree parameterDeclaration : methodDeclaration.getParameters()) {
            sb.append(getTypeDescriptor(parameterDeclaration.getClassName()));
        }
        sb.append(")");

        if (methodDeclaration.getReturnType() != null) {
            sb.append(getTypeDescriptor(methodDeclaration.getReturnType()));
        } else {
            sb.append("V");
        }

        return sb.toString();
    }

    public static String getMethodConstructorDescriptor(ProgramTree constructorDeclaration) {
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        for (ProgramTree parameterDeclaration : constructorDeclaration.getParameters()) {
            sb.append(getTypeDescriptor(parameterDeclaration.getClassName()));
        }
        sb.append(")V");

        return sb.toString();
    }

    public static String getTypeDescriptor(ProgramTree primary) {
        if (primary instanceof IntegerLiteral) {
            return "I";
        } else if (primary instanceof RealLiteral) {
            return "D";
        } else if (primary instanceof BooleanLiteral) {
            return "Z";
        } else if (primary instanceof This) {
            return "L" + CLASS_NAME + ";";
        } else if (primary instanceof ClassName) {
            return "L" + ((ClassName) primary).getName() + ";";
        }

        return "V";
    }

//    public static String getTypeDescriptor(ProgramTree className) {
//        return "L" + className.getName() + ";";
//    }

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