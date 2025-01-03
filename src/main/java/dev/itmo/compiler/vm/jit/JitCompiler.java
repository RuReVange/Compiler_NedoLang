package dev.itmo.compiler.vm.jit;

import dev.itmo.compiler.vm.VirtualMachine;
import dev.itmo.compiler.vm.bytecode.Instruction;
import java.util.*;

public class JitCompiler {
    private static final int HOT_THRESHOLD = 10;
    private final Map<Integer, Integer> executionCount = new HashMap<>();
    private final Map<Integer, OptimizedBlock> optimizedBlocks = new HashMap<>();
    private VirtualMachine vm;

    private long totalExecutions = 0;
    private long optimizedExecutions = 0;

    public static class OptimizedBlock {
        private final List<Instruction> originalInstructions;
        private final OptimizedOperation operation;

        public OptimizedBlock(List<Instruction> instructions, OptimizedOperation operation) {
            this.originalInstructions = instructions;
            this.operation = operation;
        }

        public int execute(VirtualMachine vm) {
            return operation.execute(vm);
        }

        public int getLength() {
            return originalInstructions.size();
        }
    }

    public void incrementCount(int ip) {
        totalExecutions++;
        executionCount.merge(ip, 1, Integer::sum);

        if (executionCount.get(ip) >= HOT_THRESHOLD && !optimizedBlocks.containsKey(ip)) {
//            System.out.println("Optimizing block at IP " + ip +
//                    " (execution count: " + executionCount.get(ip) + ")");
            tryOptimize(ip);
        }
    }

    private void tryOptimize(int ip) {
        List<Instruction> instructions = vm.getInstructions();

        if (ip + 1 < instructions.size()) {
            Instruction current = instructions.get(ip);
            Instruction next = instructions.get(ip + 1);

            if (current.opCode == Instruction.OpCode.LOAD_CONST &&
                    next.opCode == Instruction.OpCode.BINARY_ADD) {
                optimizeConstantAddition(ip, current, next);
            }
            else if (current.opCode == Instruction.OpCode.LOAD_CONST &&
                    next.opCode == Instruction.OpCode.COMPARE_OP) {
                optimizeConstantComparison(ip, current, next);
            }
        }
    }

    private void optimizeConstantAddition(int ip, Instruction loadConst, Instruction add) {
        OptimizedOperation operation = vm -> {
            Object constant = loadConst.operand;
            Object value = vm.pop();

            if (constant instanceof Long && value instanceof Long) {
                vm.push((Long)value + (Long)constant);
                return ip + 2;
            }

            // Откат к обычному выполнению если типы не совместимы
            vm.push(value);
            vm.push(constant);
            return ip;
        };

        optimizedBlocks.put(ip, new OptimizedBlock(
                Arrays.asList(loadConst, add),
                operation
        ));
    }

    private void optimizeConstantComparison(int ip, Instruction loadConst, Instruction compare) {
        OptimizedOperation operation = vm -> {
            Object constant = loadConst.operand;
            Object value = vm.pop();
            String op = (String)compare.operand;

            boolean result;
            if (constant instanceof Long && value instanceof Long) {
                long constVal = (Long)constant;
                long val = (Long)value;

                switch(op) {
                    case "==": result = val == constVal; break;
                    case "!=": result = val != constVal; break;
                    case "<":  result = val < constVal;  break;
                    case "<=": result = val <= constVal; break;
                    case ">":  result = val > constVal;  break;
                    case ">=": result = val >= constVal; break;
                    default: return ip;
                }
                vm.push(result);
                return ip + 2;
            }

            vm.push(value);
            vm.push(constant);
            return ip;
        };

        optimizedBlocks.put(ip, new OptimizedBlock(
                Arrays.asList(loadConst, compare),
                operation
        ));
    }

    public OptimizedBlock getOptimizedBlock(int ip) {
        OptimizedBlock block = optimizedBlocks.get(ip);
        if (block != null) {
            optimizedExecutions++;
        }
        return block;
    }

    public void setVM(VirtualMachine vm) {
        this.vm = vm;
    }

    public void printStatistics() {
        System.out.println("\n========== JIT Stats ===============");
        System.out.println("Total execs: " + totalExecutions);
        System.out.println("Optimized execs: " + optimizedExecutions);


        System.out.println("Optimized blocks: " + optimizedBlocks.size());
        System.out.println("Hot spots:");
        executionCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .forEach(e -> System.out.println("ip " + e.getKey() + "=> " + e.getValue() + " execs"));
    }
}