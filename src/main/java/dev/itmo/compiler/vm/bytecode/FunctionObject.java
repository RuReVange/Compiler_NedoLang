package dev.itmo.compiler.vm.bytecode;

import java.io.Serializable;
import java.util.List;

public class FunctionObject implements Serializable {
    public String name;
    public int address;
    public List<String> parameters;

    public FunctionObject(String name, int address, List<String> parameters) {
        this.name = name;
        this.address = address;
        this.parameters = parameters;
    }
}
