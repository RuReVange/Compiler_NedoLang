package dev.itmo.compiler.vm.bytecode;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.ASTVisitor;
import dev.itmo.compiler.parser.experessions.BinaryExpression;
import dev.itmo.compiler.parser.experessions.FunctionCallExpression;
import dev.itmo.compiler.parser.experessions.NumberExpression;
import dev.itmo.compiler.parser.experessions.VariableExpression;
import dev.itmo.compiler.parser.statements.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compiler implements ASTVisitor<Void> {
    private Bytecode bytecode = new Bytecode();
    private int currentPosition = 0;
    private int labelCounter = 0;
    private Map<String, Integer> labels = new HashMap<>();
    private Map<String, FunctionObject> functionTable = new HashMap<>();


    public Bytecode compile(List<ASTNode> nodes) {
        String mainLabel = newLabel();

        addInstruction(new Instruction(Instruction.OpCode.JUMP_FORWARD, mainLabel));

        for (ASTNode node : nodes) {
            if (node instanceof FunctionDeclaration) {
                node.accept(this);
            }
        }

        markLabel(mainLabel);

        for (ASTNode node : nodes) {
            if (!(node instanceof FunctionDeclaration)) {
                node.accept(this);
            }
        }

        resolveLabels();

        for (FunctionObject function : functionTable.values()) {
            function.address = labels.get(function.name);
            bytecode.addFunction(function);
        }

        return bytecode;
    }

    private void addInstruction(Instruction instr) {
        bytecode.addInstruction(instr);
        currentPosition++;
    }

    private String newLabel() {
        return "label_" + (labelCounter++);
    }

    private void markLabel(String label) {
        labels.put(label, currentPosition);
    }

    private void resolveLabels() {
        List<Instruction> instructions = bytecode.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instr = instructions.get(i);
            if (instr.opCode == Instruction.OpCode.JUMP_FORWARD ||
                    instr.opCode == Instruction.OpCode.JUMP_ABSOLUTE ||
                    instr.opCode == Instruction.OpCode.POP_JUMP_IF_FALSE) {
                String label = (String) instr.operand;
                Integer position = labels.get(label);
                if (position == null) {
                    throw new RuntimeException("Undefined label: " + label);
                }
                instr.operand = position;
            }
        }
    }

    @Override
    public Void visit(NumberExpression node) {
        addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, node.value));
        return null;
    }

    @Override
    public Void visit(VariableExpression node) {
        addInstruction(new Instruction(Instruction.OpCode.LOAD_NAME, node.name));
        return null;
    }

    @Override
    public Void visit(BinaryExpression node) {
        node.left.accept(this);
        node.right.accept(this);
        switch (node.operator) {
            case "+":
                addInstruction(new Instruction(Instruction.OpCode.BINARY_ADD));
                break;
            case "-":
                addInstruction(new Instruction(Instruction.OpCode.BINARY_SUBTRACT));
                break;
            case "*":
                addInstruction(new Instruction(Instruction.OpCode.BINARY_MULTIPLY));
                break;
            case "/":
                addInstruction(new Instruction(Instruction.OpCode.BINARY_DIVIDE));
                break;
            case "%":
                addInstruction(new Instruction(Instruction.OpCode.BINARY_MODULO));
                break;
            case "==":
            case "!=":
            case "<":
            case "<=":
            case ">":
            case ">=":
                addInstruction(new Instruction(Instruction.OpCode.COMPARE_OP, node.operator));
                break;
            default:
                throw new RuntimeException("Unknown operator: " + node.operator);
        }
        return null;
    }

    @Override
    public Void visit(Assignment node) {
        node.value.accept(this);
        addInstruction(new Instruction(Instruction.OpCode.STORE_NAME, node.name));
        return null;
    }

    @Override
    public Void visit(VariableDeclaration node) {
        node.initializer.accept(this);
        addInstruction(new Instruction(Instruction.OpCode.STORE_NAME, node.name));
        return null;
    }

    @Override
    public Void visit(ReturnStatement node) {
        if (node.value != null) {
            node.value.accept(this);
        } else {
            addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, null));
        }
        addInstruction(new Instruction(Instruction.OpCode.RETURN_VALUE));
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration node) {
        String functionLabel = node.name;
        markLabel(functionLabel);

        FunctionObject function = new FunctionObject(node.name, currentPosition, node.parameters);
        functionTable.put(node.name, function);

        node.body.accept(this);

        if (bytecode.getInstructions().get(currentPosition - 1).opCode != Instruction.OpCode.RETURN_VALUE) {
            addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, null));
            addInstruction(new Instruction(Instruction.OpCode.RETURN_VALUE));
        }

        return null;
    }

    @Override
    public Void visit(FunctionCallExpression node) {
        for (ASTNode arg : node.arguments) {
            arg.accept(this);
        }

        if (isNativeFunction(node.functionName)) {
            addInstruction(new Instruction(Instruction.OpCode.CALL_NATIVE, node.functionName, node.arguments.size()));
        } else {
            addInstruction(new Instruction(Instruction.OpCode.CALL_FUNCTION, node.functionName, node.arguments.size()));
        }
        return null;
    }

    private boolean isNativeFunction(String name) {
        return name.equals("print") || name.equals("length") || name.equals("push") || name.equals("randomInt");
    }

    @Override
    public Void visit(IfStatement node) {
        node.condition.accept(this);
        String elseLabel = newLabel();
        String endLabel = newLabel();

        addInstruction(new Instruction(Instruction.OpCode.POP_JUMP_IF_FALSE, elseLabel));

        node.thenBranch.accept(this);

        addInstruction(new Instruction(Instruction.OpCode.JUMP_ABSOLUTE, endLabel));

        markLabel(elseLabel);
        if (node.elseBranch != null) {
            node.elseBranch.accept(this);
        }

        markLabel(endLabel);

        return null;
    }

    @Override
    public Void visit(WhileStatement node) {
        String startLabel = newLabel();
        String endLabel = newLabel();

        markLabel(startLabel);
        node.condition.accept(this);
        addInstruction(new Instruction(Instruction.OpCode.POP_JUMP_IF_FALSE, endLabel));
        node.body.accept(this);
        addInstruction(new Instruction(Instruction.OpCode.JUMP_ABSOLUTE, startLabel));
        markLabel(endLabel);
        return null;
    }

    @Override
    public Void visit(PrintStatement node) {
        node.expression.accept(this);
        addInstruction(new Instruction(Instruction.OpCode.PRINT));
        return null;
    }

    @Override
    public Void visit(Block node) {
        for (ASTNode statement : node.statements) {
            statement.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(ArrayLiteral node) {
        int elementCount = node.elements.size();
        for (ASTNode element : node.elements) {
            element.accept(this);
        }
        addInstruction(new Instruction(Instruction.OpCode.BUILD_LIST, elementCount));
        return null;
    }

    @Override
    public Void visit(ArrayAccess node) {
        node.array.accept(this);
        node.index.accept(this);
        addInstruction(new Instruction(Instruction.OpCode.SUBSCR_LOAD));
        return null;
    }

    @Override
    public Void visit(ArrayAssignment node) {
        node.array.accept(this);
        node.index.accept(this);
        node.value.accept(this);
        addInstruction(new Instruction(Instruction.OpCode.SUBSCR_STORE));
        return null;
    }
}