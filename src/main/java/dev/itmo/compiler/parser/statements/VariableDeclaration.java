package dev.itmo.compiler.parser.statements;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;

public class VariableDeclaration implements ASTNode {
    public final String name;
    public final ASTNode initializer;

    public VariableDeclaration(String name, ASTNode initializer) {
        this.name = name;
        this.initializer = initializer;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}


