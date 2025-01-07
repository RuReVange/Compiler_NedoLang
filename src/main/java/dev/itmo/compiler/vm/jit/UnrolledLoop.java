package dev.itmo.compiler.vm.jit;

import dev.itmo.compiler.vm.bytecode.Instruction;

import java.util.List;

public class UnrolledLoop {
    private final List<Instruction> originalInstructions;
    private final int startIp;
    private final int endIp;
    private final int increment;

    public UnrolledLoop(List<Instruction> instructions, int startIp, int endIp, int increment) {
        this.originalInstructions = instructions;
        this.startIp = startIp;
        this.endIp = endIp;
        this.increment = increment;
    }
}
