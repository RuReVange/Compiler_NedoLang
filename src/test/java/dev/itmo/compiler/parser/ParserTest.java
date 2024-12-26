package dev.itmo.compiler.parser;

import dev.itmo.compiler.lexer.Lexer;
import dev.itmo.compiler.lexer.Token;
import dev.itmo.compiler.lexer.TokenType;
import dev.itmo.compiler.parser.experessions.BinaryExpression;
import dev.itmo.compiler.parser.experessions.FunctionCallExpression;
import dev.itmo.compiler.parser.statements.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    private Parser parser;
    private List<Token> tokens;

    @BeforeEach
    public void setUp() {
        tokens = new ArrayList<>();
    }

    @Test
    public void testEmptyInput() {
        parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();
        assertTrue(nodes.isEmpty());
    }

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

    @Test
    void testParseArithmetic() {
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.NUMBER, "2", 1, 1));
        tokens.add(new Token(TokenType.PLUS, "+", 1, 2));
        tokens.add(new Token(TokenType.NUMBER, "3", 1, 3));
        tokens.add(new Token(TokenType.MUL, "*", 1, 4));
        tokens.add(new Token(TokenType.NUMBER, "4", 1, 5));
        tokens.add(new Token(TokenType.SEMICOLON, ";", 1, 6));

        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        assertNotNull(nodes);
        assertTrue(nodes.get(0) instanceof BinaryExpression);
    }

    @Test
    void testParseIfStatement() {
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IF, "if", 1, 1));
        tokens.add(new Token(TokenType.LPAREN, "(", 1, 2));
        tokens.add(new Token(TokenType.NUMBER, "1", 1, 3));
        tokens.add(new Token(TokenType.RPAREN, ")", 1, 4));
        tokens.add(new Token(TokenType.LBRACE, "{", 1, 5));
        tokens.add(new Token(TokenType.RBRACE, "}", 1, 6));

        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        assertNotNull(nodes);
        assertTrue(nodes.get(0) instanceof IfStatement);
    }

    @Test
    void testParseArrayOperations() {
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IDENTIFIER, "arr", 1, 1));
        tokens.add(new Token(TokenType.LBRACKET, "[", 1, 2));
        tokens.add(new Token(TokenType.NUMBER, "0", 1, 3));
        tokens.add(new Token(TokenType.RBRACKET, "]", 1, 4));
        tokens.add(new Token(TokenType.ASSIGN, "=", 1, 5));
        tokens.add(new Token(TokenType.NUMBER, "42", 1, 6));
        tokens.add(new Token(TokenType.SEMICOLON, ";", 1, 7));

        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        assertNotNull(nodes);
        assertTrue(nodes.get(0) instanceof ArrayAssignment);
    }

    @Test
    void testParseFunctionCall() {
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.IDENTIFIER, "print", 1, 1));
        tokens.add(new Token(TokenType.LPAREN, "(", 1, 2));
        tokens.add(new Token(TokenType.NUMBER, "42", 1, 3));
        tokens.add(new Token(TokenType.RPAREN, ")", 1, 4));
        tokens.add(new Token(TokenType.SEMICOLON, ";", 1, 5));

        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        assertNotNull(nodes);
        assertTrue(nodes.get(0) instanceof FunctionCallExpression);
    }

    @Test
    void testParseComparison() {
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.NUMBER, "5", 1, 1));
        tokens.add(new Token(TokenType.LESS_THAN, "<", 1, 2));
        tokens.add(new Token(TokenType.NUMBER, "10", 1, 3));
        tokens.add(new Token(TokenType.SEMICOLON, ";", 1, 4));

        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        assertNotNull(nodes);
        assertTrue(nodes.get(0) instanceof BinaryExpression);
        BinaryExpression expr = (BinaryExpression) nodes.get(0);
        assertEquals("<", expr.operator);
    }

    @Test
    void testParseWhileLoop() {
        List<Token> tokens = new ArrayList<>();
        tokens.add(new Token(TokenType.WHILE, "while", 1, 1));
        tokens.add(new Token(TokenType.LPAREN, "(", 1, 2));
        tokens.add(new Token(TokenType.NUMBER, "1", 1, 3));
        tokens.add(new Token(TokenType.RPAREN, ")", 1, 4));
        tokens.add(new Token(TokenType.LBRACE, "{", 1, 5));
        tokens.add(new Token(TokenType.RBRACE, "}", 1, 6));

        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        assertNotNull(nodes);
        assertTrue(nodes.get(0) instanceof WhileStatement);
    }
}

