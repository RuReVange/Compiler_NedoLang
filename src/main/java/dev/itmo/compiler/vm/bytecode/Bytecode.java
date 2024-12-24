package dev.itmo.compiler.vm.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.*;
import java.util.Map;

public class Bytecode implements Serializable {
    private final List<Instruction> instructions = new ArrayList<>();
    private final Map<String, FunctionObject> functionTable = new HashMap<>();

    public void addInstruction(Instruction instr) {
        instructions.add(instr);
    }

    public void addInstructionAt(int index, Instruction instr) {
        instructions.add(index, instr);
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void addFunction(FunctionObject function) {
        functionTable.put(function.name, function);
    }

    public Map<String, FunctionObject> getFunctionTable() {
        return functionTable;
    }

    public void serialize(String fileName) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(this);
        }
    }

    public static Bytecode deserialize(String fileName) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName))) {
            return (Bytecode) in.readObject();
        }
    }
}
