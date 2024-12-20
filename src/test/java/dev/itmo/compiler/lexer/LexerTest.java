package dev.itmo.compiler.lexer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LexerTest {

    @Test
    void testSimpleTokens() {
        String code = "var x = 10;";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertEquals(6, tokens.size());
        assertEquals(TokenType.VAR, tokens.get(0).type);
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type);
        assertEquals("x", tokens.get(1).lexeme);
        assertEquals(TokenType.ASSIGN, tokens.get(2).type);
        assertEquals(TokenType.NUMBER, tokens.get(3).type);
        assertEquals("10", tokens.get(3).lexeme);
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type);
        assertEquals(TokenType.EOF, tokens.get(5).type);
    }

    @Test
    void testFunctionDefinition() {
        String code = "function test(a, b) { return a + b; }";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertEquals(15, tokens.size());
        assertEquals(TokenType.FUNCTION, tokens.get(0).type);
        assertEquals("test", tokens.get(1).lexeme);
    }

    @Test
    void codeRaw() {
        String code = "function fact(n) { if (n == 0) return 1; else return n * fact(n - 1); }";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        System.out.println(tokens);

        String res = "[FUNCTION 'function' at 1:1, " +
                "IDENTIFIER 'fact' at 1:10, " +
                "LPAREN '(' at 1:14, " +
                "IDENTIFIER 'n' at 1:15, " +
                "RPAREN ')' at 1:16, " +
                "LBRACE '{' at 1:18, " +
                "IF 'if' at 1:20, " +
                "LPAREN '(' at 1:23, " +
                "IDENTIFIER 'n' at 1:24, " +
                "EQUALS '==' at 1:26, " +
                "NUMBER '0' at 1:29, " +
                "RPAREN ')' at 1:30, " +
                "RETURN 'return' at 1:32, " +
                "NUMBER '1' at 1:39, " +
                "SEMICOLON ';' at 1:40, " +
                "ELSE 'else' at 1:42, " +
                "RETURN 'return' at 1:47, " +
                "IDENTIFIER 'n' at 1:54, " +
                "MUL '*' at 1:56, " +
                "IDENTIFIER 'fact' at 1:58, " +
                "LPAREN '(' at 1:62, " +
                "IDENTIFIER 'n' at 1:63, " +
                "MINUS '-' at 1:65, " +
                "NUMBER '1' at 1:67, " +
                "RPAREN ')' at 1:68, " +
                "SEMICOLON ';' at 1:69, " +
                "RBRACE '}' at 1:71, " +
                "EOF '' at 1:72]";

        assertEquals(tokens.get(0), new Token(TokenType.FUNCTION, "function", 1, 1));
        assertEquals(tokens.get(1), new Token(TokenType.IDENTIFIER, "fact", 1, 10));
        assertEquals(tokens.get(2), new Token(TokenType.LPAREN, "(", 1, 14));
        assertEquals(tokens.get(3), new Token(TokenType.IDENTIFIER, "n", 1, 15));
        assertEquals(tokens.get(4), new Token(TokenType.RPAREN, ")", 1, 16));
        assertEquals(tokens.toString(), res);
    }
}
