package dev.itmo.compiler.interpreter;

import dev.itmo.compiler.lexer.Lexer;
import dev.itmo.compiler.lexer.Token;
import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.Parser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

class InterpreterTest {

    @Test
    void testFunctionDeclarationAndCall() {
        String code = "function add(a, b) { return a + b; } print(add(2, 3));";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(nodes);
        // 5
    }

    @Test
    void testRecursiveFunctionFactorial() {
        String code = "function fact(n) { if(n == 0) { return 1; } else { return n * fact(n - 1); } } print(fact(5));";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(nodes);
        // 120
    }

    @Test
    void testArrayHandling() {
        String code = "var arr = [1, 2, 3]; print(arr);";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(nodes);
        // [1, 2, 3]
    }

    @Test
    void testFunctionCallWithIncorrectArguments() {
        String code = "function foo() { return 1; } print(foo(n));";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();

        Interpreter interpreter = new Interpreter();
        try {
            interpreter.interpret(nodes);
            fail("Expected RuntimeException due to incorrect number of arguments");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
