package dev.itmo.compiler.interpreter;

import dev.itmo.compiler.parser.statements.Block;
import dev.itmo.compiler.parser.statements.FunctionDeclaration;

import java.util.List;

public class UserDefinedFunction implements Function {
    private final FunctionDeclaration declaration;
    private final Environment closure;

    public UserDefinedFunction(FunctionDeclaration declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.parameters.size(); i++) {
            environment.define(declaration.parameters.get(i), arguments.get(i));
        }
        try {
            interpreter.executeBlock(((Block) declaration.body).statements, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }
}

