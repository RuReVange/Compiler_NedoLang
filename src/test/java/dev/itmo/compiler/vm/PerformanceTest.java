package dev.itmo.compiler.vm;

import dev.itmo.compiler.lexer.Lexer;
import dev.itmo.compiler.lexer.Token;
import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.Parser;
import dev.itmo.compiler.semantic.SemanticAnalyzer;
import dev.itmo.compiler.vm.bytecode.Bytecode;
import dev.itmo.compiler.vm.bytecode.Compiler;
import dev.itmo.compiler.vm.bytecode.Instruction;
import dev.itmo.compiler.vm.jit.JitCompiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class PerformanceTest {
    public static void main(String[] args) {

        String code = null;
        try {
            code = new String(Files.readAllBytes(Paths.get("src/main/resources/sortJit.nedolang")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<ASTNode> nodes = parser.parse();
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.analyze(nodes);

        Compiler compiler = new Compiler();
        Bytecode bytecode = compiler.compile(nodes);

        System.out.println("\n=== Running without JIT ===");
        runWithoutJIT(bytecode);

        System.out.println("\n=== Running with JIT ===");
        runWithJIT(bytecode);
    }

    private static void runWithoutJIT(Bytecode bytecode) {
        VirtualMachine vm = new VirtualMachineWithoutJIT(bytecode);
        measurePerformance(() -> vm.run());
    }

    private static void runWithJIT(Bytecode bytecode) {
        VirtualMachine vm = new VirtualMachineWithJIT(bytecode);
        measurePerformance(() -> vm.run());
    }

    private static void measurePerformance(Runnable task) {
        for (int i = 0; i < 3; i++) {
            task.run();
        }

        // Реальные измерения
        long totalTime = 0;
        int runs = 5;

        for (int i = 0; i < runs; i++) {
            long startTime = System.nanoTime();
            task.run();
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            totalTime += duration;
            System.out.printf("Run %d: %.3f ms%n", i + 1, duration / 1_000_000.0);
        }

        double averageTime = totalTime / (double) runs;
        System.out.printf("Average execution time: %.3f ms%n", averageTime / 1_000_000.0);
    }
}

class VirtualMachineWithoutJIT extends VirtualMachine {
    private long instructionCount = 0;

    public VirtualMachineWithoutJIT(Bytecode bytecode) {
        super(bytecode);
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();

        while (ip < instructions.size()) {
            Instruction instr = instructions.get(ip);
            execute(instr);
            instructionCount++;
        }

        long endTime = System.nanoTime();
        double seconds = (endTime - startTime) / 1_000_000_000.0;

        System.out.println("\nExecution Statistics (Without JIT):");
        System.out.printf("Total instructions executed: %d%n", instructionCount);
        System.out.printf("Instructions per second: %.2f%n", instructionCount / seconds);
    }
}

class VirtualMachineWithJIT extends VirtualMachine {
    private final JitCompiler jit;
    private long instructionCount = 0;
    private long optimizedCount = 0;

    public VirtualMachineWithJIT(Bytecode bytecode) {
        super(bytecode);
        this.jit = new JitCompiler();
        this.jit.setVM(this);
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();

        while (ip < instructions.size()) {
            jit.incrementCount(ip);
            JitCompiler.OptimizedBlock optimizedBlock = jit.getOptimizedBlock(ip);

            if (optimizedBlock != null) {
                ip = optimizedBlock.execute(this);
                optimizedCount++;
            } else {
                Instruction instr = instructions.get(ip);
                execute(instr);
            }
            instructionCount++;
        }

        long endTime = System.nanoTime();
        double seconds = (endTime - startTime) / 1_000_000_000.0;

        System.out.println("\nExecution Statistics (With JIT):");
        System.out.printf("Total instructions executed: %d%n", instructionCount);
        System.out.printf("Optimized executions: %d (%.2f%%)%n",
                optimizedCount, (optimizedCount * 100.0) / instructionCount);
        System.out.printf("Instructions per second: %.2f%n", instructionCount / seconds);

        jit.printStatistics();
    }
}

