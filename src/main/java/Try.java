public class Try {

    public static void main(String[] args) {
        String filePath = "SimpleInput.txt";
        testLexer(filePath);
        testSyntaxAnalyser(filePath);
    }

    public static void testSyntaxAnalyser(String filePath) {
        var lexer = new Lexer(filePath);
        var syntaxAnalyser = new SyntaxAnalyser(lexer);
        System.out.println(syntaxAnalyser.makeTree());
    }

    public static void testLexer(String filePath) {
        Lexer lexer = new Lexer(filePath);

        System.out.println("Lexical Analysis");
        System.out.println("-----------------");
        while (!lexer.isExausthed()) {
            System.out.printf("%-18s :  %s \n",lexer.currentLexema() , lexer.currentToken());
            lexer.moveAhead();
        }

        if (lexer.isSuccessful()) {
            System.out.println("Ok! :D");
        } else {
            System.out.println(lexer.errorMessage());
        }
    }
}