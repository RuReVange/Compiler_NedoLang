package dev.itmo.compiler.interpreter;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment parent;
    private final Map<String, Object> variables = new HashMap<>();

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public void define(String name, Object value) {
        variables.put(name, value);
    }

    public void assign(String name, Object value) {
        if (variables.containsKey(name)) {
            variables.put(name, value);
        } else if (parent != null) {
            parent.assign(name, value);
        } else {
            throw new RuntimeException("Undefined variable: " + name);
        }
    }

    public Object get(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (parent != null) {
            return parent.get(name);
        } else {
            throw new RuntimeException("Undefined variable: " + name);
        }
    }
}
