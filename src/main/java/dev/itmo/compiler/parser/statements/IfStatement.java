package dev.itmo.compiler.parser.statements;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;

public class IfStatement implements ASTNode {
    public final ASTNode condition;
    public final ASTNode thenBranch;
    public final ASTNode elseBranch;

    public IfStatement(ASTNode condition, ASTNode thenBranch, ASTNode elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

