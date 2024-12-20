package dev.itmo.compiler.parser.experessions;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;

import java.util.List;

public class FunctionCallExpression implements ASTNode {
    public final String functionName;
    public final List<ASTNode> arguments;

    public FunctionCallExpression(String functionName, List<ASTNode> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

