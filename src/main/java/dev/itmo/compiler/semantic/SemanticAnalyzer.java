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
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private boolean inFunction = false;

    public void analyze(List<ASTNode> nodes) {
        enterScope();

        declareVariable("true");
        declareVariable("false");

        for (ASTNode node : nodes) {
            if (node instanceof FunctionDeclaration) {
                FunctionDeclaration func = (FunctionDeclaration) node;
                if (functions.containsKey(func.name)) {
                    throw new SemanticError("Function '" + func.name + "' is already defined");
                }
                functions.put(func.name, func);
            }
        }

        addBuiltinFunction("push", 2);
        addBuiltinFunction("length", 1);
        addBuiltinFunction("randomInt", 2);

        for (ASTNode node : nodes) {
            node.accept(this);
        }

        exitScope();
    }

    private void addBuiltinFunction(String name, int paramCount) {
        functions.put(name, new FunctionDeclaration(name,
                Collections.nCopies(paramCount, "param"), null));
    }

    private void enterScope() {
        scopes.push(new HashMap<>());
    }

    private void exitScope() {
        scopes.pop();
    }

    private void declareVariable(String name) {
        if (scopes.peek().containsKey(name)) {
            throw new SemanticError("Variable '" + name + "' is already declared in this scope");
        }
        scopes.peek().put(name, true);
    }

    private void checkVariable(String name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name)) {
                return;
            }
        }
        throw new SemanticError("Undefined variable '" + name + "'");
    }

    @Override
    public Void visit(NumberExpression node) {
        return null;
    }

    @Override
    public Void visit(VariableExpression node) {
        checkVariable(node.name);
        return null;
    }

    @Override
    public Void visit(BinaryExpression node) {
        node.left.accept(this);
        node.right.accept(this);
        return null;
    }

    @Override
    public Void visit(FunctionCallExpression node) {
        FunctionDeclaration func = functions.get(node.functionName);
        if (func == null) {
            throw new SemanticError("Undefined function '" + node.functionName + "'");
        }

        if (func.parameters.size() != node.arguments.size()) {
            throw new SemanticError("Function '" + node.functionName + "' expects " +
                    func.parameters.size() + " arguments, but got " + node.arguments.size());
        }

        for (ASTNode arg : node.arguments) {
            arg.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration node) {
        enterScope();
        inFunction = true;

        for (String param : node.parameters) {
            declareVariable(param);
        }
        node.body.accept(this);

        inFunction = false;
        exitScope();
        return null;
    }

    @Override
    public Void visit(VariableDeclaration node) {
        declareVariable(node.name);
        if (node.initializer != null) {
            node.initializer.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Assignment node) {
        checkVariable(node.name);
        node.value.accept(this);
        return null;
    }

    @Override
    public Void visit(IfStatement node) {
        node.condition.accept(this);

        enterScope();
        node.thenBranch.accept(this);
        exitScope();

        if (node.elseBranch != null) {
            enterScope();
            node.elseBranch.accept(this);
            exitScope();
        }
        return null;
    }

    @Override
    public Void visit(WhileStatement node) {
        node.condition.accept(this);

        enterScope();
        node.body.accept(this);
        exitScope();
        return null;
    }

    @Override
    public Void visit(ReturnStatement node) {
        if (!inFunction) {
            throw new SemanticError("Return statement outside of function");
        }
        if (node.value != null) {
            node.value.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Block node) {
        enterScope();
        for (ASTNode statement : node.statements) {
            statement.accept(this);
        }
        exitScope();
        return null;
    }

    @Override
    public Void visit(PrintStatement node) {
        node.expression.accept(this);
        return null;
    }

    @Override
    public Void visit(ArrayLiteral node) {
        for (ASTNode element : node.elements) {
            element.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ArrayAccess node) {
        node.array.accept(this);
        node.index.accept(this);
        return null;
    }

    @Override
    public Void visit(ArrayAssignment node) {
        node.array.accept(this);
        node.index.accept(this);
        node.value.accept(this);
        return null;
    }
}