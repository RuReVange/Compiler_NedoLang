package dev.itmo.compiler.vm;

import java.util.List;

@FunctionalInterface
public interface NativeFunction {
    Object call(VirtualMachine vm, List<Object> args);
}