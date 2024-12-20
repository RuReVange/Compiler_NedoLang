package dev.itmo.compiler.semantic;

import dev.itmo.compiler.lexer.Lexer;
import dev.itmo.compiler.lexer.Token;
import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class SemanticAnalyzerTest {

    @Test
    void testSemanticAnalysis() {
        String code = "function test(a) { var b = a + 1; return b; }";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        SemanticAnalyzer analyzer = new SemanticAnalyzer();

        Assertions.assertDoesNotThrow(() -> analyzer.analyze(nodes));
    }

    @Test
    void testUndeclaredVariable() {
        String code = "function test() { return x; }";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        SemanticAnalyzer analyzer = new SemanticAnalyzer();

        // из-за необъявленного x
        Assertions.assertThrows(RuntimeException.class, () -> analyzer.analyze(nodes));
    }
}
