package university.innopolis.javist;

import university.innopolis.javist.lexer.Lexer;
import university.innopolis.javist.symantic.SemanticAnalyzer;
import university.innopolis.javist.syntax.ProgramTree;
import university.innopolis.javist.syntax.SyntaxAnalyser;

public class Try {

    public static void main(String[] args) {
        String filePath = "src/main/resources/sources/SimpleInput.txt";
//        testLexer(filePath);
        testSyntaxAnalyser(filePath);
         testSemanticAnalyzer(filePath);
    }

    public static void testSyntaxAnalyser(String filePath) {
        var lexer = new Lexer(filePath);
        var syntaxAnalyser = new SyntaxAnalyser(lexer);
        printAST(syntaxAnalyser.makeTree(), 0);
    }

    public static void testSemanticAnalyzer(String filepath) {
        Lexer lexer = new Lexer(filepath);
        SyntaxAnalyser syntaxAnalyser = new SyntaxAnalyser(lexer);
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(syntaxAnalyser.makeTree());
        semanticAnalyzer.analyzePredefinedLibraries("src/main/resources/sources/Libraries.txt");
        semanticAnalyzer.analyze();
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