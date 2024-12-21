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

        globalEnv.define("true", true);
        globalEnv.define("false", false);

        // Встроенные функции
        globalEnv.define("print", new BuiltInFunction("print", (args) -> {
            for (Object arg : args) {
                System.out.println(arg);
            }
            return null;
        }));

        // Новая функция length для массивов
        globalEnv.define("length", new BuiltInFunction("length", (args) -> {
            if (args.size() != 1 || !(args.get(0) instanceof List)) {
                throw new RuntimeException("length() expects one array argument");
            }
            return (long) ((List<?>) args.get(0)).size();
        }));


        globalEnv.define("push", new BuiltInFunction("push", (args) -> {
            if (args.size() != 2 || !(args.get(0) instanceof List)) {
                throw new RuntimeException("push() expects array and value arguments");
            }
            ((List<Object>) args.get(0)).add(args.get(1));
            return null;
        }));

        globalEnv.define("randomInt", new BuiltInFunction("randomInt", (args) -> {
            if (args.size() != 2 || !(args.get(0) instanceof Long) || !(args.get(1) instanceof Long)) {
                throw new RuntimeException("randomInt() expects two integer arguments");
            }
            long min = (Long) args.get(0);
            long max = (Long) args.get(1);
            return min + (long)(Math.random() * ((max - min) + 1));
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

        if (left instanceof Boolean && right instanceof Boolean) {
            boolean l = (Boolean) left;
            boolean r = (Boolean) right;
            switch (node.operator) {
                case "==": return l == r;
                case "!=": return l != r;
                default: throw new RuntimeException("Invalid operator for boolean values: " + node.operator);
            }
        }
        // Обработка конкатенации массивов
        if (node.operator.equals("+")) {
            if (left instanceof List && right instanceof List) {
                List<Object> result = new ArrayList<>();
                result.addAll((List<?>) left);
                result.addAll((List<?>) right);
                return result;
            }
            // Добавляем обработку случая, когда один из операндов - массив
            if (left instanceof List) {
                List<Object> result = new ArrayList<>();
                result.addAll((List<?>) left);
                if (right instanceof Object[]) {
                    result.addAll(Arrays.asList((Object[]) right));
                } else {
                    result.add(right);
                }
                return result;
            }
            if (right instanceof List) {
                List<Object> result = new ArrayList<>();
                if (left instanceof Object[]) {
                    result.addAll(Arrays.asList((Object[]) left));
                } else {
                    result.add(left);
                }
                result.addAll((List<?>) right);
                return result;
            }
        }

        // Существующая обработка числовых операций
        if (left instanceof Long && right instanceof Long) {
            long l = (Long) left;
            long r = (Long) right;

            switch (node.operator) {
                case "+": return l + r;
                case "-": return l - r;
                case "*": return l * r;
                case "/": return l / r;
                case "==": return l == r;
                case "!=": return l != r;
                case "<": return l < r;
                case "<=": return l <= r;
                case ">": return l > r;
                case ">=": return l >= r;
                default: throw new RuntimeException("Unknown operator: " + node.operator);
            }
        }
        throw new RuntimeException("Operands must be longs or arrays");
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
        Object array = node.array.accept(this);
        Object index = node.index.accept(this);

        if (!(array instanceof List)) {
            throw new RuntimeException("Cannot access non-array type");
        }

        List<Object> list = (List<Object>) array;
        int idx;

        if (index instanceof Long) {
            idx = ((Long) index).intValue();
        } else if (index instanceof Integer) {
            idx = (Integer) index;
        } else {
            throw new RuntimeException("Array index must be a number");
        }

        if (idx < 0 || idx >= list.size()) {
            throw new RuntimeException("Array index out of bounds");
        }

        return list.get(idx);
    }

    @Override
    public Object visit(ArrayAssignment node) {
        Object array = node.array.accept(this);
        Object index = node.index.accept(this);
        Object value = node.value.accept(this);

        if (!(array instanceof List)) {
            throw new RuntimeException("Cannot assign to non-array type");
        }

        List<Object> list = (List<Object>) array;
        int idx;

        if (index instanceof Long) {
            idx = ((Long) index).intValue();
        } else if (index instanceof Integer) {
            idx = (Integer) index;
        } else {
            throw new RuntimeException("Array index must be a number");
        }

        if (idx < 0) {
            throw new RuntimeException("Array index cannot be negative");
        }

        // Расширяем массив при необходимости
        while (list.size() <= idx) {
            list.add(null);
        }

        list.set(idx, value);
        return null;
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Integer || value instanceof Long) return (Long) value != 0;
        return true;
    }
}
