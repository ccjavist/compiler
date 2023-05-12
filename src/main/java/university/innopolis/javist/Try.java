package university.innopolis.javist;

import university.innopolis.javist.errors.SyntaxError;
import university.innopolis.javist.lexer.Lexer;
import university.innopolis.javist.symantic.SemanticAnalyzer;
import university.innopolis.javist.syntax.ProgramTree;
import university.innopolis.javist.syntax.SyntaxAnalyser;

public class Try {

    public static void main(String[] args) throws Exception {
        testSemanticAnalyzer("src/main/resources/sources/Power.txt");
        JVMByteCodeGenerator.run(new SyntaxAnalyser(new Lexer("src/main/resources/sources/Power.txt")).makeTree());
    }

    public static void testSyntaxAnalyser(String filePath) {
        var lexer = new Lexer(filePath);
        var syntaxAnalyser = new SyntaxAnalyser(lexer);
        printAST(syntaxAnalyser.makeTree(), 0);
    }

    public static void testSemanticAnalyzer(String filepath) throws Exception {
        ProgramTree tree;
        Lexer lexer = new Lexer(filepath);
        SyntaxAnalyser syntaxAnalyser = new SyntaxAnalyser(lexer);
        tree =  new SyntaxAnalyser(lexer).makeTree();
        SemanticAnalyzer semanticAnalyzer;
        try{
            semanticAnalyzer = new SemanticAnalyzer(tree);
        } catch (SyntaxError e){
            System.out.println("SyntaxError: " + e.getMessage());
            return;
        }
        semanticAnalyzer.analyzePredefinedLibraries("src/main/resources/sources/Libraries.txt");
        semanticAnalyzer.analyze();
        JVMByteCodeGenerator.run(tree);
    }

    public static void printAST(ProgramTree node, int depth) {
        String indent = "  ".repeat(depth);

        System.out.println(indent + node.toString());
        for (int i = 0; i < node.getChildrenCount(); i++) {
            printAST(node.getChild(i), depth + 1);
        }
    }

    public static void testLexer(String filePath) {
        Lexer lexer = new Lexer(filePath);

        System.out.println("Lexical Analysis");
        System.out.println("-----------------");
        while (!lexer.isExausthed()) {
            System.out.printf("%-18s :  %s \n", lexer.currentLexema(), lexer.currentToken());
            lexer.moveAhead();
        }

        if (lexer.isSuccessful()) {
            System.out.println("Ok! :D");
        } else {
            System.out.println(lexer.errorMessage());
        }
    }
}