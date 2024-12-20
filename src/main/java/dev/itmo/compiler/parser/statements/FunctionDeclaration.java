package dev.itmo.compiler.parser.statements;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;

import java.util.List;

public class FunctionDeclaration implements ASTNode {
    public final String name;
    public final List<String> parameters;
    public final ASTNode body;

    public FunctionDeclaration(String name, List<String> parameters, ASTNode body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

