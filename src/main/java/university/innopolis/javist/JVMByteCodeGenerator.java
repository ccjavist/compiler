package university.innopolis.javist;

import org.objectweb.asm.*;
import university.innopolis.javist.syntax.ProgramTree;

import java.io.*;

public class JVMByteCodeGenerator {
    private static final String CLASS_NAME = "Something";
    private static final String SUPER_CLASS = "java/lang/Object";

    public static void run(ProgramTree tree) throws Exception {
        ClassWriter cw  = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(51, Opcodes.ACC_PUBLIC, CLASS_NAME, null, SUPER_CLASS, null);

        for (ClassDeclaration classDeclaration : tree.getClassDeclarations()) {
            generateClassDeclaration(classDeclaration, cw);
        }

        cw.visitEnd();

        FileOutputStream fos = new FileOutputStream("Something.class");
        fos.write(cw.toByteArray());
        fos.close();
    }

    public static void generateClassDeclaration(ClassDeclaration classDeclaration, ClassWriter classWriter) throws Exception {
        for (MemberDeclaration memberDeclaration : classDeclaration.getDeclarations()) {
            if (memberDeclaration instanceof VariableDeclaration) {
                generateVariableDeclaration((VariableDeclaration) memberDeclaration, classWriter);
            } else if (memberDeclaration instanceof MethodDeclaration) {
                generateMethodDeclaration((MethodDeclaration) memberDeclaration, classWriter);
            } else if (memberDeclaration instanceof ConstructorDeclaration) {
                generateConstructorDeclaration((ConstructorDeclaration) memberDeclaration, classWriter);
            }
        }
    }

    public static void generateVariableDeclaration(VariableDeclaration variableDeclaration, ClassWriter classWriter) throws Exception {
        FieldVisitor fv = classWriter.visitField(Opcodes.ACC_PRIVATE, variableDeclaration.getIdentifier(), getTypeDescriptor(variableDeclaration.getExpression()), null, null);
        fv.visitEnd();
    }

    public static void generateMethodDeclaration(MethodDeclaration methodDeclaration, ClassWriter classWriter) throws Exception {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, methodDeclaration.getIdentifier(), getMethodDescriptor(methodDeclaration), null, null);
        mv.visitCode();

        for (Statement statement : methodDeclaration.getBody()) {
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

    public static void generateConstructorDeclaration(ConstructorDeclaration constructorDeclaration, ClassWriter classWriter) throws Exception {
        MethodVisitor mv = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", getMethodDescriptor(constructorDeclaration), null, null);
        mv.visitCode();

        for (Statement statement : constructorDeclaration.getBody()) {
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

    public static void generateAssignment(Assignment assignment, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        generateExpression(assignment.getExpression(), methodVisitor);
        methodVisitor.visitFieldInsn(Opcodes.PUTFIELD, CLASS_NAME, assignment.getIdentifier(), getTypeDescriptor(assignment.getExpression()));
    }

    public static void generateWhileLoop(WhileLoop whileLoop, MethodVisitor methodVisitor) throws Exception {
        Label startLabel = new Label();
        Label endLabel = new Label();

        methodVisitor.visitLabel(startLabel);

        generateExpression(whileLoop.getExpression(), methodVisitor);
        methodVisitor.visitJumpInsn(Opcodes.IFEQ, endLabel);

        generateBody(whileLoop.getBody(), methodVisitor);
        methodVisitor.visitJumpInsn(Opcodes.GOTO, startLabel);

        methodVisitor.visitLabel(endLabel);
    }

    public static void generateIfStatement(IfStatement ifStatement, MethodVisitor methodVisitor) throws Exception {
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

    public static void generateReturnStatement(ReturnStatement returnStatement, MethodVisitor methodVisitor) throws Exception {
        generateExpression(returnStatement.getExpression(), methodVisitor);
        methodVisitor.visitInsn(Opcodes.IRETURN);
    }

    public static void generateBody(List<Statement> body, MethodVisitor methodVisitor) throws Exception {
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

    public static void generateExpression(Expression expression, MethodVisitor methodVisitor) throws Exception {
        if (expression instanceof Primary) {
            generatePrimary((Primary) expression, methodVisitor);
        }
    }

    public static void generatePrimary(Primary primary, MethodVisitor methodVisitor) throws Exception {
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

    public static void generateIntegerLiteral(IntegerLiteral integerLiteral, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitLdcInsn(integerLiteral.getValue());
    }

    public static void generateRealLiteral(RealLiteral realLiteral, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitLdcInsn(realLiteral.getValue());
    }

    public static void generateBooleanLiteral(BooleanLiteral booleanLiteral, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitLdcInsn(booleanLiteral.getValue());
    }

    public static void generateThis(This thisObject, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
    }

    public static void generateClassName(ClassName className, MethodVisitor methodVisitor) throws Exception {
        methodVisitor.visitTypeInsn(Opcodes.NEW, className.getName());
        methodVisitor.visitInsn(Opcodes.DUP);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, className.getName(), "<init>", "()V", false);
    }

    public static String getTypeDescriptor(Expression expression) {
        if (expression instanceof Primary) {
            return getTypeDescriptor((Primary) expression);
        }

        return "V";
    }

    public static String getMethodDescriptor(MethodDeclaration methodDeclaration) {
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        for (ParameterDeclaration parameterDeclaration : methodDeclaration.getParameters()) {
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

    public static String getMethodDescriptor(ConstructorDeclaration constructorDeclaration) {
        StringBuilder sb = new StringBuilder();

        sb.append("(");
        for (ParameterDeclaration parameterDeclaration : constructorDeclaration.getParameters()) {
            sb.append(getTypeDescriptor(parameterDeclaration.getClassName()));
        }
        sb.append(")V");

        return sb.toString();
    }

    public static String getTypeDescriptor(Primary primary) {
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

    public static String getTypeDescriptor(ClassName className) {
        return "L" + className.getName() + ";";
    }
}