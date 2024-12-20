package dev.itmo.compiler.parser.experessions;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;

public class BinaryExpression implements ASTNode {
    public final ASTNode left;
    public final String operator;
    public final ASTNode right;

    public BinaryExpression(ASTNode left, String operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

