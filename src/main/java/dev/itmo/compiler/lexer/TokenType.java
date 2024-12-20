package dev.itmo.compiler.lexer;

public enum TokenType {
    // Ключевые слова
    FUNCTION, RETURN, IF, ELSE, WHILE, VAR, PRINT,

    // Типы данных
    INT, ARRAY,

    // Литералы и идентификаторы
    IDENTIFIER, NUMBER,

    // Операторы
    PLUS, MINUS, MUL, DIV,
    ASSIGN,
    EQUALS, NOT_EQUALS, LESS_THAN, GREATER_THAN, LESS_EQUALS, GREATER_EQUALS,

    // Разделители
    SEMICOLON, COMMA, DOT,
    LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET,

    // конец файла
    EOF
}