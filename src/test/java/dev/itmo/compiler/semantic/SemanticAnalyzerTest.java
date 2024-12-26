package dev.itmo.compiler.semantic;

import dev.itmo.compiler.lexer.Lexer;
import dev.itmo.compiler.lexer.Token;
import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.Parser;
import dev.itmo.compiler.parser.experessions.FunctionCallExpression;
import dev.itmo.compiler.parser.experessions.NumberExpression;
import dev.itmo.compiler.parser.experessions.VariableExpression;
import dev.itmo.compiler.parser.statements.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SemanticAnalyzerTest {
    private SemanticAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new SemanticAnalyzer();
    }

    @Test
    void testSimpleVariableDeclaration() {
        List<ASTNode> nodes = Arrays.asList(
                new VariableDeclaration("x", new NumberExpression(42))
        );
        assertDoesNotThrow(() -> analyzer.analyze(nodes));
    }

    @Test
    void testUndefinedVariableUsage() {
        List<ASTNode> nodes = Arrays.asList(
                new Assignment("x", new NumberExpression(42))
        );

        SemanticError error = assertThrows(SemanticError.class,
                () -> analyzer.analyze(nodes));
        assertTrue(error.getMessage().contains("Undefined variable 'x'"));
    }

    @Test
    void testDuplicateVariableDeclaration() {
        List<ASTNode> nodes = Arrays.asList(
                new VariableDeclaration("x", new NumberExpression(42)),
                new VariableDeclaration("x", new NumberExpression(10))
        );

        SemanticError error = assertThrows(SemanticError.class,
                () -> analyzer.analyze(nodes));
        assertTrue(error.getMessage().contains("already declared"));
    }

    @Test
    void testFunctionDeclarationAndCall() {
        List<ASTNode> nodes = Arrays.asList(
                new FunctionDeclaration("test",
                        Arrays.asList("a", "b"),
                        new Block(Collections.singletonList(
                                new ReturnStatement(new NumberExpression(1))
                        ))
                ),
                new FunctionCallExpression("test",
                        Arrays.asList(new NumberExpression(1), new NumberExpression(2)))
        );

        assertDoesNotThrow(() -> analyzer.analyze(nodes));
    }

    @Test
    void testIncorrectFunctionArgumentCount() {
        List<ASTNode> nodes = Arrays.asList(
                new FunctionDeclaration("test",
                        Arrays.asList("a", "b"),
                        new Block(Collections.singletonList(
                                new ReturnStatement(new NumberExpression(1))
                        ))
                ),
                new FunctionCallExpression("test",
                        Collections.singletonList(new NumberExpression(1)))
        );

        SemanticError error = assertThrows(SemanticError.class,
                () -> analyzer.analyze(nodes));
        assertTrue(error.getMessage().contains("expects 2 arguments"));
    }

    @Test
    void testReturnOutsideFunction() {
        List<ASTNode> nodes = Collections.singletonList(
                new ReturnStatement(new NumberExpression(1))
        );

        SemanticError error = assertThrows(SemanticError.class,
                () -> analyzer.analyze(nodes));
        assertTrue(error.getMessage().contains("Return statement outside of function"));
    }

    @Test
    void testArrayOperations() {
        List<ASTNode> nodes = Arrays.asList(
                new VariableDeclaration("arr", new ArrayLiteral(Arrays.asList(
                        new NumberExpression(1),
                        new NumberExpression(2)
                ))),
                new ArrayAssignment(
                        new VariableExpression("arr"),
                        new NumberExpression(0),
                        new NumberExpression(42)
                ),
                new PrintStatement(new ArrayAccess(
                        new VariableExpression("arr"),
                        new NumberExpression(0)
                ))
        );

        assertDoesNotThrow(() -> analyzer.analyze(nodes));
    }

    @Test
    void testBuiltinFunctions() {
        List<ASTNode> nodes = Arrays.asList(
                new VariableDeclaration("arr", new ArrayLiteral(Collections.emptyList())),
                new FunctionCallExpression("push", Arrays.asList(new VariableExpression("arr"),
                        new NumberExpression(1)
                )),
                new FunctionCallExpression("length", Collections.singletonList(
                        new VariableExpression("arr")
                )),
                new FunctionCallExpression("randomInt", Arrays.asList(
                        new NumberExpression(0),
                        new NumberExpression(100)
                ))
        );

        assertDoesNotThrow(() -> analyzer.analyze(nodes));
    }

    @Test
    void testScopeRules() {
        List<ASTNode> nodes = Collections.singletonList(
                new Block(Arrays.asList(
                        new VariableDeclaration("x", new NumberExpression(1)),
                        new Block(Arrays.asList(
                                new VariableDeclaration("y", new NumberExpression(2)),
                                new Assignment("x", new NumberExpression(3))
                        )),
                        new Assignment("y", new NumberExpression(4))
                ))
        );

        SemanticError error = assertThrows(SemanticError.class,
                () -> analyzer.analyze(nodes));
        assertTrue(error.getMessage().contains("Undefined variable 'y'"));
    }

    @Test
    void testBooleanLiterals() {
        List<ASTNode> nodes = Arrays.asList(
                new VariableDeclaration("flag", new VariableExpression("true")),
                new IfStatement(
                        new VariableExpression("false"),
                        new Block(Collections.emptyList()),
                        null
                )
        );

        assertDoesNotThrow(() -> analyzer.analyze(nodes));
    }

    @Test
    void testSemanticAnalysis() {
        String code = "function test(a) { var b = a + 1; return b; }";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        SemanticAnalyzer analyzer = new SemanticAnalyzer();

        assertDoesNotThrow(() -> analyzer.analyze(nodes));
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
        assertThrows(RuntimeException.class, () -> analyzer.analyze(nodes));
    }
}
