package dev.itmo.compiler.parser;

public interface ASTNode {
    <T> T accept(ASTVisitor<T> visitor);
}
