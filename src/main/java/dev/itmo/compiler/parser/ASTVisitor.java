package dev.itmo.compiler.parser;

import dev.itmo.compiler.parser.experessions.BinaryExpression;
import dev.itmo.compiler.parser.experessions.FunctionCallExpression;
import dev.itmo.compiler.parser.experessions.NumberExpression;
import dev.itmo.compiler.parser.experessions.VariableExpression;
import dev.itmo.compiler.parser.statements.*;

public interface ASTVisitor<T> {
    T visit(NumberExpression node);
    T visit(VariableExpression node);
    T visit(BinaryExpression node);
    T visit(FunctionCallExpression node);
    T visit(FunctionDeclaration node);
    T visit(VariableDeclaration node);
    T visit(Assignment node);
    T visit(IfStatement node);
    T visit(WhileStatement node);
    T visit(ReturnStatement node);
    T visit(Block node);
    T visit(PrintStatement node);
    T visit(ArrayLiteral node);
    T visit(ArrayAccess node);
    T visit(ArrayAssignment node);
}


