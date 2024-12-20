package dev.itmo.compiler.parser.statements;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;

public class ArrayAccess implements ASTNode {
    public final ASTNode array;
    public final ASTNode index;

    public ArrayAccess(ASTNode array, ASTNode index) {
        this.array = array;
        this.index = index;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
