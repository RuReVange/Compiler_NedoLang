package dev.itmo.compiler.parser.experessions;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;

public class VariableExpression implements ASTNode {
    public final String name;

    public VariableExpression(String name) {
        this.name = name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

