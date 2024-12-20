package dev.itmo.compiler.parser.statements;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;

public class ArrayAssignment implements ASTNode {
    public final ASTNode array;
    public final ASTNode index;
    public final ASTNode value;

    public ArrayAssignment(ASTNode array, ASTNode index, ASTNode value) {
        this.array = array;
        this.index = index;
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
