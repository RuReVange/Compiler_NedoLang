package dev.itmo.compiler.vm.jit;

import dev.itmo.compiler.memory.RcList;
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

            if (current.opCode == Instruction.OpCode.LOAD_CONST && next.opCode == Instruction.OpCode.BINARY_ADD) {
                optimizeConstantArithmetic(ip, current, next, "+");
            } else if (current.opCode == Instruction.OpCode.LOAD_CONST && next.opCode == Instruction.OpCode.BINARY_SUBTRACT) {
                optimizeConstantArithmetic(ip, current, next, "-");
            } else if (current.opCode == Instruction.OpCode.LOAD_CONST && next.opCode == Instruction.OpCode.BINARY_MULTIPLY) {
                optimizeConstantArithmetic(ip, current, next, "*");
            } else if (current.opCode == Instruction.OpCode.LOAD_CONST && next.opCode == Instruction.OpCode.BINARY_DIVIDE) {
                optimizeConstantArithmetic(ip, current, next, "/");
            } else if (current.opCode == Instruction.OpCode.LOAD_CONST && next.opCode == Instruction.OpCode.BINARY_MODULO) {
                optimizeConstantArithmetic(ip, current, next, "%");
            } else if (current.opCode == Instruction.OpCode.LOAD_CONST && next.opCode == Instruction.OpCode.COMPARE_OP) {
                optimizeConstantComparison(ip, current, next);
            } else if (current.opCode == Instruction.OpCode.LOAD_NAME && next.opCode == Instruction.OpCode.STORE_NAME && current.operand.equals(next.operand)) {
                optimizeLoadStore(ip, current, next);
            }
        }
        if (ip + 2 < instructions.size()) {
            Instruction current = instructions.get(ip);
            Instruction next = instructions.get(ip + 1);
            Instruction nextNext = instructions.get(ip + 2);

            if (current.opCode == Instruction.OpCode.LOAD_NAME && next.opCode == Instruction.OpCode.LOAD_NAME &&
                    (nextNext.opCode == Instruction.OpCode.BINARY_ADD ||
                            nextNext.opCode == Instruction.OpCode.BINARY_SUBTRACT ||
                            nextNext.opCode == Instruction.OpCode.BINARY_MULTIPLY ||
                            nextNext.opCode == Instruction.OpCode.BINARY_DIVIDE)) {
                optimizeBinaryOperation(ip, current, next, nextNext);
            }
        }
    }

    private void optimizeBinaryOperation(int ip, Instruction load1, Instruction load2, Instruction binaryOp) {
        OptimizedOperation operation = vm -> {
            try {
                String name1 = (String)load1.operand;
                String name2 = (String)load2.operand;

                Object val1 = vm.loadName(name1);
                Object val2 = vm.loadName(name2);

                if (val1 == null || val2 == null) {
                    return ip;
                }

                Object result = null;

                if (val1 instanceof Long && val2 instanceof Long) {
                    Long longVal1 = (Long)val1;
                    Long longVal2 = (Long)val2;

                    switch (binaryOp.opCode) {
                        case BINARY_ADD:
                            result = longVal1 + longVal2;
                            break;
                        case BINARY_SUBTRACT:
                            result = longVal1 - longVal2;
                            break;
                        case BINARY_MULTIPLY:
                            result = longVal1 * longVal2;
                            break;
                        case BINARY_DIVIDE:
                            if (longVal2 != 0) {
                                result = longVal1 / longVal2;
                            }
                            break;
                    }
                } else if ((val1 instanceof RcList || val1 instanceof List) &&
                        (val2 instanceof RcList || val2 instanceof List) &&
                        binaryOp.opCode == Instruction.OpCode.BINARY_ADD) {
                    RcList resultList = new RcList();

                    List<?> list1 = (val1 instanceof RcList) ?
                            ((RcList)val1).getItems() : (List<?>)val1;
                    List<?> list2 = (val2 instanceof RcList) ?
                            ((RcList)val2).getItems() : (List<?>)val2;

                    for (Object item : list1) {
                        resultList.add(item);
                    }
                    for (Object item : list2) {
                        resultList.add(item);
                    }

                    result = resultList;
                }

                if (result != null) {
                    vm.push(result);
                    return ip + 3;
                }

                return ip;
            } catch (Exception e) {
                return ip;
            }
        };

        optimizedBlocks.put(ip, new OptimizedBlock(
                Arrays.asList(load1, load2, binaryOp),
                operation
        ));
    }

    // constantFolding
    private void optimizeConstantArithmetic(int ip, Instruction loadConst, Instruction arithmetic, String operator) {
        OptimizedOperation operation = vm -> {
            Object constant = loadConst.operand;
            Object value = vm.pop();
            if (constant instanceof Long && value instanceof Long) {
                long constVal = (Long) constant;
                long val = (Long) value;
                long result;
                switch (operator) {
                    case "+": result = val + constVal; break;
                    case "-": result = val - constVal; break;
                    case "*": result = val * constVal; break;
                    case "/": result = val / constVal; break;
                    case "%": result = val % constVal; break;
                    default: throw new RuntimeException("Unknown operator: " + operator);
                }

                vm.push(result);
                return ip + 2;
            }
            vm.push(value);
            vm.push(constant);
            return ip;
        };
        optimizedBlocks.put(ip, new OptimizedBlock(Arrays.asList(loadConst, arithmetic), operation));

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

    private void optimizeLoadStore(int ip, Instruction loadName, Instruction storeName) {
        OptimizedOperation operation = vm -> ip + 2;
        optimizedBlocks.put(ip, new OptimizedBlock(Arrays.asList(loadName, storeName), operation));
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
        System.out.println("\n========== JIT Stats ================");
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