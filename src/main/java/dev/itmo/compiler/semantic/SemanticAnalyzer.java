package dev.itmo.compiler.semantic;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;
import dev.itmo.compiler.parser.experessions.BinaryExpression;
import dev.itmo.compiler.parser.experessions.FunctionCallExpression;
import dev.itmo.compiler.parser.experessions.NumberExpression;
import dev.itmo.compiler.parser.experessions.VariableExpression;
import dev.itmo.compiler.parser.statements.*;

import java.util.*;

public class SemanticAnalyzer implements ASTVisitor<Void> {
    private final Map<String, FunctionDeclaration> functions = new HashMap<>();
    private final Deque<Map<String, Boolean>> scopes = new ArrayDeque<>();

    public void analyze(List<ASTNode> nodes) {
        // Первый проход: собираем все функции
        for (ASTNode node : nodes) {
            if (node instanceof FunctionDeclaration) {
                FunctionDeclaration func = (FunctionDeclaration) node;
                functions.put(func.name, func);
            }
        }
        // Второй проход: анализируем функции
        for (ASTNode node : nodes) {
            node.accept(this);
        }
    }

    @Override
    public Void visit(FunctionDeclaration node) {
        beginScope();
        for (String param : node.parameters) {
            declareVariable(param);
        }
        node.body.accept(this);
        endScope();
        return null;
    }

    @Override
    public Void visit(Block node) {
        beginScope();
        for (ASTNode statement : node.statements) {
            statement.accept(this);
        }
        endScope();
        return null;
    }

    @Override
    public Void visit(PrintStatement node) {
        return null;
    }

    @Override
    public Void visit(ArrayLiteral node) {
        return null;
    }

    @Override
    public Void visit(ArrayAccess node) {
        return null;
    }

    @Override
    public Void visit(ArrayAssignment node) {
        return null;
    }

    @Override
    public Void visit(VariableDeclaration node) {
        declareVariable(node.name);
        node.initializer.accept(this);
        return null;
    }

    @Override
    public Void visit(Assignment node) {
        if (!isVariableDeclared(node.name)) {
            error("Variable '" + node.name + "' is not declared");
        }
        node.value.accept(this);
        return null;
    }

    @Override
    public Void visit(VariableExpression node) {
        if (!isVariableDeclared(node.name)) {
            error("Variable '" + node.name + "' is not declared");
        }
        return null;
    }

    @Override
    public Void visit(FunctionCallExpression node) {
        if (!functions.containsKey(node.functionName)) {
            error("Function '" + node.functionName + "' is not declared");
        }
        for (ASTNode arg : node.arguments) {
            arg.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(IfStatement node) {
        node.condition.accept(this);
        node.thenBranch.accept(this);
        if (node.elseBranch != null) {
            node.elseBranch.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(WhileStatement node) {
        node.condition.accept(this);
        node.body.accept(this);
        return null;
    }

    @Override
    public Void visit(ReturnStatement node) {
        if (node.value != null) {
            node.value.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(BinaryExpression node) {
        node.left.accept(this);
        node.right.accept(this);
        return null;
    }

    @Override
    public Void visit(NumberExpression node) {
        return null;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declareVariable(String name) {
        if (scopes.peek().containsKey(name)) {
            error("Variable '" + name + "' is already declared in this scope");
        }
        scopes.peek().put(name, true);
    }

    private boolean isVariableDeclared(String name) {
        for (Map<String, Boolean> scope : scopes) {
            if (scope.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    private void error(String message) {
        throw new RuntimeException("Semantic error: " + message);
    }
}

