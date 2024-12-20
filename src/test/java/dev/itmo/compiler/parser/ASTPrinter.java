package dev.itmo.compiler.parser;

import dev.itmo.compiler.parser.experessions.BinaryExpression;
import dev.itmo.compiler.parser.experessions.FunctionCallExpression;
import dev.itmo.compiler.parser.experessions.NumberExpression;
import dev.itmo.compiler.parser.experessions.VariableExpression;
import dev.itmo.compiler.parser.statements.*;

public class ASTPrinter implements ASTVisitor<Void> {
    private int indent = 0;

    private void printIndent() {
        for (int i = 0; i < indent; i++) System.out.print("  ");
    }

    @Override
    public Void visit(NumberExpression node) {
        printIndent();
        System.out.println("NumberExpression: " + node.value);
        return null;
    }

    @Override
    public Void visit(VariableExpression node) {
        printIndent();
        System.out.println("VariableExpression: " + node.name);
        return null;
    }

    @Override
    public Void visit(BinaryExpression node) {
        printIndent();
        System.out.println("BinaryExpression: " + node.operator);
        indent++;
        node.left.accept(this);
        node.right.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(FunctionCallExpression node) {
        printIndent();
        System.out.println("FunctionCall: " + node.functionName);
        indent++;
        for (ASTNode arg : node.arguments) {
            arg.accept(this);
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(FunctionDeclaration node) {
        printIndent();
        System.out.println("FunctionDeclaration: " + node.name);
        indent++;
        printIndent();
        System.out.println("Parameters: " + node.parameters);
        node.body.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Block node) {
        printIndent();
        System.out.println("Block:");
        indent++;
        for (ASTNode statement : node.statements) {
            statement.accept(this);
        }
        indent--;
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
        printIndent();
        System.out.println("VariableDeclaration: " + node.name);
        indent++;
        node.initializer.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(Assignment node) {
        printIndent();
        System.out.println("Assignment: " + node.name);
        indent++;
        node.value.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(IfStatement node) {
        printIndent();
        System.out.println("IfStatement:");


        indent++;
        printIndent();
        System.out.println("Condition:");
        indent++;
        node.condition.accept(this);
        indent--;
        printIndent();
        System.out.println("Then:");
        node.thenBranch.accept(this);
        if (node.elseBranch != null) {
            printIndent();
            System.out.println("Else:");
            node.elseBranch.accept(this);
        }
        indent--;
        return null;
    }

    @Override
    public Void visit(WhileStatement node) {
        printIndent();
        System.out.println("WhileStatement:");
        indent++;
        printIndent();
        System.out.println("Condition:");
        indent++;
        node.condition.accept(this);
        indent--;
        printIndent();
        System.out.println("Body:");
        node.body.accept(this);
        indent--;
        return null;
    }

    @Override
    public Void visit(ReturnStatement node) {
        printIndent();
        System.out.println("ReturnStatement:");
        indent++;
        if (node.value != null) {
            node.value.accept(this);
        }
        indent--;
        return null;
    }
}
