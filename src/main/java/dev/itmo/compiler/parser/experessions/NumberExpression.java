package dev.itmo.compiler.parser.experessions;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;

public class NumberExpression implements ASTNode {
    public final long value;  // Изменено с int на long

    public NumberExpression(long value) {  // Изменено с int на long
        this.value = value;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
