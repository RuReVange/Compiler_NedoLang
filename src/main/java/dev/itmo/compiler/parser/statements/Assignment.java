package dev.itmo.compiler.parser.statements;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;

public class Assignment implements ASTNode {
    public final String name;
    public final ASTNode value;

    public Assignment(String name, ASTNode value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

