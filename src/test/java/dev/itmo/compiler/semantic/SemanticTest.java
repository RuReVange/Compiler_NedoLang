package dev.itmo.compiler.semantic;

import dev.itmo.compiler.lexer.Lexer;
import dev.itmo.compiler.lexer.Token;
import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.Parser;

import java.util.List;

public class SemanticTest {
    public static void main(String[] args) {
        String code = "function fact(n) { if (n == 0) return 1; else return n * fact(n - 1); }";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        analyzer.analyze(nodes);

        System.out.println("Semantic analysis completed successfully.");
    }
}
