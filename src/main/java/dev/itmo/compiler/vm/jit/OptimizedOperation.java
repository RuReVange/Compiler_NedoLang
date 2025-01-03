package dev.itmo.compiler.vm.jit;

import dev.itmo.compiler.vm.VirtualMachine;

@FunctionalInterface
public interface OptimizedOperation {
    int execute(VirtualMachine vm);
}



