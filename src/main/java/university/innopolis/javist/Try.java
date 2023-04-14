package university.innopolis.javist;

import university.innopolis.javist.errors.SyntaxError;
import university.innopolis.javist.lexer.Lexer;
import university.innopolis.javist.symantic.SemanticAnalyzer;
import university.innopolis.javist.syntax.ProgramTree;
import university.innopolis.javist.syntax.SyntaxAnalyser;

public class Try {

    public static void main(String[] args) {
//         testSemanticAnalyzer("src/main/resources/sources/SemanticTests/Test1.txt");
         testSemanticAnalyzer("src/main/resources/sources/SemanticTests/Test2.txt");
//         testSemanticAnalyzer("src/main/resources/sources/SemanticTests/Test3.txt");
//         testSemanticAnalyzer("src/main/resources/sources/SemanticTests/Test4.txt");
    }

    public static void testSyntaxAnalyser(String filePath) {
        var lexer = new Lexer(filePath);
        var syntaxAnalyser = new SyntaxAnalyser(lexer);
        printAST(syntaxAnalyser.makeTree(), 0);
    }

    public static void testSemanticAnalyzer(String filepath) {
        Lexer lexer = new Lexer(filepath);
        SyntaxAnalyser syntaxAnalyser = new SyntaxAnalyser(lexer);
        SemanticAnalyzer semanticAnalyzer;
        try{
            semanticAnalyzer = new SemanticAnalyzer(syntaxAnalyser.makeTree());
        } catch (SyntaxError e){
            System.out.println("SyntaxError: " + e.getMessage());
            return;
        }
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