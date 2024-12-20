package dev.itmo.compiler.interpreter;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;
import dev.itmo.compiler.parser.experessions.BinaryExpression;
import dev.itmo.compiler.parser.experessions.FunctionCallExpression;
import dev.itmo.compiler.parser.experessions.NumberExpression;
import dev.itmo.compiler.parser.experessions.VariableExpression;
import dev.itmo.compiler.parser.statements.*;

import java.util.*;

public class Interpreter implements ASTVisitor<Object> {
    private Environment globalEnv;
    private Environment currentEnv;

    public Interpreter() {
        this.globalEnv = new Environment(null);
        this.currentEnv = globalEnv;

        // Встроенные функции
        globalEnv.define("print", new BuiltInFunction("print", (args) -> {
            for (Object arg : args) {
                System.out.println(arg);
            }
            return null;
        }));
    }

    public void interpret(List<ASTNode> nodes) {
        for (ASTNode node : nodes) {
            node.accept(this);
        }
    }

    @Override
    public Object visit(NumberExpression node) {
        return node.value;
    }

    @Override
    public Object visit(VariableExpression node) {
        return currentEnv.get(node.name);
    }

    @Override
    public Object visit(BinaryExpression node) {
        Object left = node.left.accept(this);
        Object right = node.right.accept(this);

        if (left instanceof Integer && right instanceof Integer) {
            int l = (Integer) left;
            int r = (Integer) right;

            switch (node.operator) {
                case "+":
                    return l + r;
                case "-":
                    return l - r;
                case "*":
                    return l * r;
                case "/":
                    return l / r;
                case "==":
                    return l == r;
                case "!=":
                    return l != r;
                case "<":
                    return l < r;
                case "<=":
                    return l <= r;
                case ">":
                    return l > r;
                case ">=":
                    return l >= r;
                default:
                    throw new RuntimeException("Unknown operator: " + node.operator);
            }
        } else {
            throw new RuntimeException("Operands must be integers");
        }
    }

    @Override
    public Object visit(FunctionCallExpression node) {
        Object function = currentEnv.get(node.functionName);
        if (function instanceof Function) {
            List<Object> arguments = new ArrayList<>();
            for (ASTNode argNode : node.arguments) {
                arguments.add(argNode.accept(this));
            }
            return ((Function) function).call(this, arguments);
        } else {
            throw new RuntimeException("Attempt to call non-function: " + node.functionName);
        }
    }

    @Override
    public Object visit(VariableDeclaration node) {
        Object value = node.initializer.accept(this);
        currentEnv.define(node.name, value);
        return null;
    }

    @Override
    public Object visit(Assignment node) {
        Object value = node.value.accept(this);
        currentEnv.assign(node.name, value);
        return null;
    }

    @Override
    public Object visit(IfStatement node) {
        Object condition = node.condition.accept(this);
        if (isTruthy(condition)) {
            node.thenBranch.accept(this);
        } else if (node.elseBranch != null) {
            node.elseBranch.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(WhileStatement node) {
        while (isTruthy(node.condition.accept(this))) {
            node.body.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(Block node) {
        executeBlock(node.statements, new Environment(currentEnv));
        return null;
    }

    public void executeBlock(List<ASTNode> statements, Environment environment) {
        Environment previous = currentEnv;
        try {
            currentEnv = environment;
            for (ASTNode statement : statements) {
                statement.accept(this);
            }
        } finally {
            currentEnv = previous;
        }
    }

    @Override
    public Object visit(FunctionDeclaration node) {
        Function function = new UserDefinedFunction(node, currentEnv);
        currentEnv.define(node.name, function);
        return null;
    }

    @Override
    public Object visit(ReturnStatement node) {
        Object value = null;
        if (node.value != null) {
            value = node.value.accept(this);
        }
        throw new Return(value);
    }

    @Override
    public Object visit(PrintStatement node) {
        Object value = node.expression.accept(this);
        System.out.println(value);
        return null;
    }

    @Override
    public Object visit(ArrayLiteral node) {
        List<Object> elements = new ArrayList<>();
        for (ASTNode elementNode : node.elements) {
            elements.add(elementNode.accept(this));
        }
        return new ArrayList<>(elements);
    }

    @Override
    public Object visit(ArrayAccess node) {
        Object arrayObject = node.array.accept(this);
        Object indexObject = node.index.accept(this);
        if (arrayObject instanceof List && indexObject instanceof Integer) {
            List<Object> array = (List<Object>) arrayObject;
            int index = (Integer) indexObject;
            if (index < 0 || index >= array.size()) {
                throw new RuntimeException("Array index out of bounds");
            }
            return array.get(index);
        } else {
            throw new RuntimeException("Invalid array access");
        }
    }

    @Override
    public Object visit(ArrayAssignment node) {
        Object arrayObject = node.array.accept(this);
        Object indexObject = node.index.accept(this);
        Object value = node.value.accept(this);
        if (arrayObject instanceof List && indexObject instanceof Integer) {
            List<Object> array = (List<Object>) arrayObject;
            int index = (Integer) indexObject;
            if (index < 0 || index >= array.size()) {
                throw new RuntimeException("Array index out of bounds");
            }
            array.set(index, value);
            return null;
        } else {
            throw new RuntimeException("Invalid array assignment");
        }
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Integer) return (Integer) value != 0;
        return true;
    }
}
