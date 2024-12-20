package dev.itmo.compiler.interpreter;

import java.util.List;

public class BuiltInFunction implements Function {
    private final String name;
    private final java.util.function.Function<List<Object>, Object> implementation;

    public BuiltInFunction(String name, java.util.function.Function<List<Object>, Object> implementation) {
        this.name = name;
        this.implementation = implementation;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return implementation.apply(arguments);
    }
}

