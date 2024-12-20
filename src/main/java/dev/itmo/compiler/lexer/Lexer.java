package dev.itmo.compiler.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private final String input;
    private int pos;
    private int line;
    private int column;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("function", TokenType.FUNCTION);
        keywords.put("return", TokenType.RETURN);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("var", TokenType.VAR);
        keywords.put("int", TokenType.INT);
        keywords.put("array", TokenType.ARRAY);
        keywords.put("print", TokenType.PRINT);
    }

    public Lexer(String input) {
        this.input = input;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < input.length()) {
            char current = peek(0);
            if (Character.isWhitespace(current)) {
                consumeWhitespace();
            } else if (Character.isLetter(current) || current == '_') {
                tokens.add(readIdentifierOrKeyword());
            } else if (Character.isDigit(current)) {
                tokens.add(readNumber());
            } else {
                switch (current) {
                    case '+':
                        tokens.add(new Token(TokenType.PLUS, "+", line, column));
                        advance();
                        break;
                    case '-':
                        tokens.add(new Token(TokenType.MINUS, "-", line, column));
                        advance();
                        break;
                    case '*':
                        tokens.add(new Token(TokenType.MUL, "*", line, column));
                        advance();
                        break;
                    case '/':
                        tokens.add(new Token(TokenType.DIV, "/", line, column));
                        advance();
                        break;
                    case '=':
                        if (peek(1) == '=') {
                            tokens.add(new Token(TokenType.EQUALS, "==", line, column));
                            advance();
                            advance();
                        } else {
                            tokens.add(new Token(TokenType.ASSIGN, "=", line, column));
                            advance();
                        }
                        break;
                    case '!':
                        if (peek(1) == '=') {
                            tokens.add(new Token(TokenType.NOT_EQUALS, "!=", line, column));
                            advance();
                            advance();
                        } else {
                            error("Unexpected character: " + current);
                        }
                        break;
                    case '<':
                        if (peek(1) == '=') {
                            tokens.add(new Token(TokenType.LESS_EQUALS, "<=", line, column));
                            advance();
                            advance();
                        } else {
                            tokens.add(new Token(TokenType.LESS_THAN, "<", line, column));
                            advance();
                        }
                        break;
                    case '>':
                        if (peek(1) == '=') {
                            tokens.add(new Token(TokenType.GREATER_EQUALS, ">=", line, column));
                            advance();
                            advance();
                        } else {
                            tokens.add(new Token(TokenType.GREATER_THAN, ">", line, column));
                            advance();
                        }
                        break;
                    case ';':
                        tokens.add(new Token(TokenType.SEMICOLON, ";", line, column));
                        advance();
                        break;
                    case ',':
                        tokens.add(new Token(TokenType.COMMA, ",", line, column));
                        advance();
                        break;
                    case '.':
                        tokens.add(new Token(TokenType.DOT, ".", line, column));
                        advance();
                        break;
                    case '(':
                        tokens.add(new Token(TokenType.LPAREN, "(", line, column));
                        advance();
                        break;
                    case ')':
                        tokens.add(new Token(TokenType.RPAREN, ")", line, column));
                        advance();
                        break;
                    case '{':
                        tokens.add(new Token(TokenType.LBRACE, "{", line, column));
                        advance();
                        break;
                    case '}':
                        tokens.add(new Token(TokenType.RBRACE, "}", line, column));
                        advance();
                        break;
                    case '[':
                        tokens.add(new Token(TokenType.LBRACKET, "[", line, column));
                        advance();
                        break;
                    case ']':
                        tokens.add(new Token(TokenType.RBRACKET, "]", line, column));
                        advance();
                        break;
                    default:
                        error("Unexpected character: " + current);
                }
            }
        }
        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    private void consumeWhitespace() {
        while (pos < input.length() && Character.isWhitespace(peek(0))) {
            if (peek(0) == '\n') {
                line++;
                column = 0;
            }
            advance();
        }
    }

    private Token readIdentifierOrKeyword() {
        StringBuilder sb = new StringBuilder();
        int startColumn = column;
        while (pos < input.length() && (Character.isLetterOrDigit(peek(0)) || peek(0) == '_')) {
            sb.append(peek(0));
            advance();
        }
        String lexeme = sb.toString();
        TokenType type = keywords.getOrDefault(lexeme, TokenType.IDENTIFIER);
        return new Token(type, lexeme, line, startColumn);
    }

    private Token readNumber() {
        StringBuilder sb = new StringBuilder();
        int startColumn = column;
        while (pos < input.length() && Character.isDigit(peek(0))) {
            sb.append(peek(0));
            advance();
        }
        return new Token(TokenType.NUMBER, sb.toString(), line, startColumn);
    }

    private char peek(int relativePosition) {
        int position = pos + relativePosition;
        if (position >= input.length()) {
            return '\0';
        }
        return input.charAt(position);
    }

    private void advance() {
        pos++;
        column++;
    }

    private void error(String message) {
        throw new RuntimeException("Lexer error at line " + line + ", column " + column + ": " + message);
    }
}
