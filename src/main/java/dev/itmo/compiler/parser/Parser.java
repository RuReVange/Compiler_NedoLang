package dev.itmo.compiler.parser;

import dev.itmo.compiler.lexer.Token;
import dev.itmo.compiler.lexer.TokenType;
import dev.itmo.compiler.parser.experessions.BinaryExpression;
import dev.itmo.compiler.parser.experessions.FunctionCallExpression;
import dev.itmo.compiler.parser.experessions.NumberExpression;
import dev.itmo.compiler.parser.experessions.VariableExpression;
import dev.itmo.compiler.parser.statements.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int pos = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<ASTNode> parse() {
        List<ASTNode> nodes = new ArrayList<>();
        while (!isAtEnd()) {
            nodes.add(parseDeclaration());
        }
        return nodes;
    }

    private ASTNode parseDeclaration() {
        if (match(TokenType.FUNCTION)) {
            return parseFunctionDeclaration();
        } else if (match(TokenType.VAR)) {
            return parseVariableDeclaration();
        } else {
            return parseStatement();
        }
    }

    private FunctionDeclaration parseFunctionDeclaration() {
        String name = consume(TokenType.IDENTIFIER, "Expected function name").lexeme;
        consume(TokenType.LPAREN, "Expected '(' after function name");
        List<String> parameters = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                parameters.add(consume(TokenType.IDENTIFIER, "Expected parameter name").lexeme);
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RPAREN, "Expected ')' after parameters");
        ASTNode body = parseBlock();
        return new FunctionDeclaration(name, parameters, body);
    }

    private VariableDeclaration parseVariableDeclaration() {
        String name = consume(TokenType.IDENTIFIER, "Expected variable name").lexeme;
        consume(TokenType.ASSIGN, "Expected '=' after variable name");
        ASTNode initializer = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration");
        return new VariableDeclaration(name, initializer);
    }

    private ASTNode parseStatement() {
        if (match(TokenType.IF)) {
            return parseIfStatement();
        } else if (match(TokenType.WHILE)) {
            return parseWhileStatement();
        } else if (match(TokenType.RETURN)) {
            return parseReturnStatement();
        } else if (match(TokenType.PRINT)) {
            return parsePrintStatement();
        } else {
            return parseExpressionStatement();
        }
    }

    private PrintStatement parsePrintStatement() {
        ASTNode expression = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after print statement");
        return new PrintStatement(expression);
    }

    private IfStatement parseIfStatement() {
        consume(TokenType.LPAREN, "Expected '(' after 'if'");
        ASTNode condition = parseExpression();
        consume(TokenType.RPAREN, "Expected ')' after condition");
        ASTNode thenBranch = parseStatementOrBlock();
        ASTNode elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = parseStatementOrBlock();
        }
        return new IfStatement(condition, thenBranch, elseBranch);
    }

    private WhileStatement parseWhileStatement() {
        consume(TokenType.LPAREN, "Expected '(' after 'while'");
        ASTNode condition = parseExpression();
        consume(TokenType.RPAREN, "Expected ')' after condition");
        ASTNode body = parseStatementOrBlock();
        return new WhileStatement(condition, body);
    }

    private ReturnStatement parseReturnStatement() {
        ASTNode value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = parseExpression();
        }
        consume(TokenType.SEMICOLON, "Expected ';' after return value");
        return new ReturnStatement(value);
    }

    private Block parseBlock() {
        List<ASTNode> statements = new ArrayList<>();
        consume(TokenType.LBRACE, "Expected '{' to start block");
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(parseDeclaration());
        }
        consume(TokenType.RBRACE, "Expected '}' to end block");
        return new Block(statements);
    }

    private ASTNode parseStatementOrBlock() {
        if (check(TokenType.LBRACE)) {
            return parseBlock();
        } else {
            return parseStatement();
        }
    }

    private ASTNode parseExpressionStatement() {
        ASTNode expr = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression");
        return expr;
    }

    private ASTNode parseExpression() {
        return parseAssignment();
    }

    private ASTNode parseAssignment() {
        ASTNode expr = parseEquality();
        if (match(TokenType.ASSIGN)) {
            Token equals = previous();
            ASTNode value = parseAssignment();
            if (expr instanceof VariableExpression) {
                String name = ((VariableExpression) expr).name;
                return new Assignment(name, value);
            } else if (expr instanceof ArrayAccess) {
                return new ArrayAssignment(
                        ((ArrayAccess) expr).array,
                        ((ArrayAccess) expr).index,
                        value
                );
            } else {
                error(equals, "Invalid assignment target");
            }
        }
        return expr;
    }

    private ASTNode parseEquality() {
        ASTNode expr = parseComparison();
        while (match(TokenType.EQUALS, TokenType.NOT_EQUALS)) {
            Token operator = previous();
            ASTNode right = parseComparison();
            expr = new BinaryExpression(expr, operator.lexeme, right);
        }
        return expr;
    }

    private ASTNode parseComparison() {
        ASTNode expr = parseAddition();
        while (match(TokenType.LESS_THAN, TokenType.LESS_EQUALS, TokenType.GREATER_THAN, TokenType.GREATER_EQUALS)) {
            Token operator = previous();
            ASTNode right = parseAddition();
            expr = new BinaryExpression(expr, operator.lexeme, right);
        }
        return expr;
    }

    private ASTNode parseAddition() {
        ASTNode expr = parseMultiplication();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = previous();
            ASTNode right = parseMultiplication();
            expr = new BinaryExpression(expr, operator.lexeme, right);
        }
        return expr;
    }

    private ASTNode parseMultiplication() {
        ASTNode expr = parseUnary();
        while (match(TokenType.MUL, TokenType.DIV)) {
            Token operator = previous();
            ASTNode right = parseUnary();
            expr = new BinaryExpression(expr, operator.lexeme, right);
        }
        return expr;
    }

    private ASTNode parseUnary() {
        if (match(TokenType.MINUS)) {
            Token operator = previous();
            ASTNode right = parseUnary();
            return new BinaryExpression(new NumberExpression(0), operator.lexeme, right);
        }
        return parsePrimary();
    }

    private ASTNode parsePrimary() {
        if (match(TokenType.NUMBER)) {
            return new NumberExpression(Long.parseLong(previous().lexeme));
        }
        if (match(TokenType.IDENTIFIER)) {
            String name = previous().lexeme;
            ASTNode expr = new VariableExpression(name);
            while (true) {
                if (match(TokenType.LPAREN)) {
                    // Функциональный вызов
                    List<ASTNode> arguments = new ArrayList<>();
                    if (!check(TokenType.RPAREN)) {
                        do {
                            arguments.add(parseExpression());
                        } while (match(TokenType.COMMA));
                    }
                    consume(TokenType.RPAREN, "Expected ')' after arguments");
                    expr = new FunctionCallExpression(name, arguments);
                } else if (match(TokenType.LBRACKET)) {
                    // Доступ к элементу массива
                    ASTNode index = parseExpression();
                    consume(TokenType.RBRACKET, "Expected ']' after index");
                    expr = new ArrayAccess(expr, index);
                } else {
                    break;
                }
            }
            return expr;
        }
        if (match(TokenType.LBRACKET)) {
            // Литерал массива
            List<ASTNode> elements = new ArrayList<>();
            if (!check(TokenType.RBRACKET)) {
                do {
                    elements.add(parseExpression());
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RBRACKET, "Expected ']' after array elements");
            return new ArrayLiteral(elements);
        }
        if (match(TokenType.LPAREN)) {
            ASTNode expr = parseExpression();
            consume(TokenType.RPAREN, "Expected ')' after expression");
            return expr;
        }
        if (isAtEnd()) {
            error(previous(), "Unexpected end of input");
        } else {
            error(peek(), "Expected expression, found '" + peek().lexeme + "'");
        }
        return null;
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) pos++;
        return previous();
    }

    private boolean isAtEnd() {
        return pos >= tokens.size() || peek().type == TokenType.EOF;
    }

    private Token peek() {
        if (pos >= tokens.size()) {
            // Возвращаем EOF токен, если вышли за пределы списка
            Token lastToken = tokens.get(tokens.size() - 1);
            return new Token(TokenType.EOF, "", lastToken.line, lastToken.column);
        }
        return tokens.get(pos);
    }

    private Token previous() {
        return tokens.get(pos - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        error(peek(), message);
        return null; // Не достигнуто
    }

    private void error(Token token, String message) {
        throw new RuntimeException("Parser error at line " + token.line + ", column " + token.column + ": " + message);
    }
}