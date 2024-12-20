package dev.itmo.compiler.parser;

import dev.itmo.compiler.lexer.Lexer;
import dev.itmo.compiler.lexer.Token;
import dev.itmo.compiler.parser.statements.FunctionDeclaration;
import dev.itmo.compiler.parser.statements.VariableDeclaration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @Test
    void testPrinter() {
        String code = """
                function fact(n) {
                    if (n == 0)
                        return 1;
                    else
                        return n * fact(n - 1);
                }
                """;
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        System.out.println(tokens);

        Parser parser = new Parser(tokens);

        List<ASTNode> nodes = assertDoesNotThrow(parser::parse);

        ASTPrinter printer = new ASTPrinter();
        for (ASTNode node : nodes) {
            node.accept(printer);
        }
    }

    @Test
    void testFunctionParsing() {
        String code = "function fact(n) { if (n == 0) { return 1; } else { return n * fact(n - 1); } }";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        System.out.println(tokens);

        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        ASTPrinter printer = new ASTPrinter();
        for (ASTNode node : nodes) {
            node.accept(printer);
        }

        assertEquals(1, nodes.size());
        assertInstanceOf(FunctionDeclaration.class, nodes.get(0));

        FunctionDeclaration func = (FunctionDeclaration) nodes.get(0);
        assertEquals("fact", func.name);
        assertEquals(1, func.parameters.size());
        assertEquals("n", func.parameters.get(0));
    }

    @Test
    void testExpressionParsing() {
        String code = "var x = 5 + 3 * 2;";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        ASTPrinter printer = new ASTPrinter();
        for (ASTNode node : nodes) {
            node.accept(printer);
        }

        assertEquals(1, nodes.size());
        assertInstanceOf(VariableDeclaration.class, nodes.get(0));

        VariableDeclaration varDecl = (VariableDeclaration) nodes.get(0);
        assertEquals("x", varDecl.name);
    }

    @Test
    void testFunctionAdd() {
        String code = "function add(a, b) { return a + b; }";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        ASTPrinter printer = new ASTPrinter();
        for (ASTNode node : nodes) {
            node.accept(printer);
        }

        assertEquals(1, nodes.size());
        assertInstanceOf(FunctionDeclaration.class, nodes.get(0));
    }

    @Test
    void testFunctionWithWhileLoop() {
        String code = "function countDown(n) { while (n > 0) { n = n - 1; } }";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        ASTPrinter printer = new ASTPrinter();
        for (ASTNode node : nodes) {
            node.accept(printer);
        }

        assertEquals(1, nodes.size());
        assertInstanceOf(FunctionDeclaration.class, nodes.get(0));
    }

    @Test
    void testVariableDeclarations() {
        String code = "var x = 10; var y = x * 2;";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        ASTPrinter printer = new ASTPrinter();
        for (ASTNode node : nodes) {
            node.accept(printer);
        }

        assertEquals(2, nodes.size());
        assertInstanceOf(VariableDeclaration.class, nodes.get(0));
        assertInstanceOf(VariableDeclaration.class, nodes.get(1));
    }

    @Test
    void testFunctionWithInlineIf() {
        String code = "function test(n) { if (n > 0) return n; }";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        ASTPrinter printer = new ASTPrinter();
        for (ASTNode node : nodes) {
            node.accept(printer);
        }

        assertEquals(1, nodes.size());
        assertInstanceOf(FunctionDeclaration.class, nodes.get(0));
    }
}

