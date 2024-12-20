package dev.itmo.compiler.parser.statements;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;

import java.util.List;

public class ArrayLiteral implements ASTNode {
    public final List<ASTNode> elements;

    public ArrayLiteral(List<ASTNode> elements) {
        this.elements = elements;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

