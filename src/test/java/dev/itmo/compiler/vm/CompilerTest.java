package dev.itmo.compiler.vm;

import dev.itmo.compiler.lexer.Lexer;
import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.Parser;
import dev.itmo.compiler.vm.bytecode.Bytecode;
import dev.itmo.compiler.vm.bytecode.Compiler;
import dev.itmo.compiler.vm.bytecode.FunctionObject;
import dev.itmo.compiler.vm.bytecode.Instruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompilerTest {
    private Compiler compiler;
    private Parser parser;
    private Lexer lexer;

    @BeforeEach
    void setUp() {
        compiler = new Compiler();
    }

    private Bytecode compileSingleExpression(String code) {
        lexer = new Lexer(code);
        parser = new Parser(lexer.tokenize());
        List<ASTNode> nodes = parser.parse();
        return compiler.compile(nodes);
    }

    @Test
    void testCompileNumberLiteral() {
        Bytecode bytecode = compileSingleExpression("42;");
        List<Instruction> instructions = bytecode.getInstructions();

        assertTrue(instructions.size() >= 2);
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);
        assertEquals(Instruction.OpCode.LOAD_CONST, instructions.get(1).opCode);
        assertEquals(42, (Long) instructions.get(1).operand);
    }

    @Test
    void testCompileSimpleAddition() {
        Bytecode bytecode = compileSingleExpression("2 + 3;");
        List<Instruction> instructions = bytecode.getInstructions();

        assertTrue(instructions.size() >= 4);
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);
        assertEquals(Instruction.OpCode.LOAD_CONST, instructions.get(1).opCode);
        assertEquals(Instruction.OpCode.LOAD_CONST, instructions.get(2).opCode);
        assertEquals(Instruction.OpCode.BINARY_ADD, instructions.get(3).opCode);
    }

    @Test
    void testCompileVariableDeclaration() {
        Bytecode bytecode = compileSingleExpression("var x = 10;");
        List<Instruction> instructions = bytecode.getInstructions();

        assertTrue(instructions.size() >= 3);
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);
        assertEquals(Instruction.OpCode.LOAD_CONST, instructions.get(1).opCode);
        assertEquals(Instruction.OpCode.STORE_NAME, instructions.get(2).opCode);
        assertEquals("x", instructions.get(2).operand);
    }

    @Test
    void testCompileSimpleFunction() {
        Bytecode bytecode = compileSingleExpression(
                "function add(a, b) { return a + b; }"
        );
        List<Instruction> instructions = bytecode.getInstructions();

        assertTrue(instructions.size() >= 5);
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);

        // Проверяем наличие функции в таблице функций
        FunctionObject addFunction = bytecode.getFunctionTable().get("add");
        assertNotNull(addFunction);
        assertEquals("add", addFunction.name);
        assertEquals(2, addFunction.parameters.size());
    }

    @Test
    void testCompileIfStatement() {
        Bytecode bytecode = compileSingleExpression(
                "if (x > 0) { print(x); } else { print(-x); }"
        );
        List<Instruction> instructions = bytecode.getInstructions();

        assertTrue(instructions.size() >= 2);
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);

        // Проверяем наличие необходимых инструкций для if-else
        assertTrue(instructions.stream()
                .anyMatch(i -> i.opCode == Instruction.OpCode.POP_JUMP_IF_FALSE));
        assertTrue(instructions.stream()
                .anyMatch(i -> i.opCode == Instruction.OpCode.JUMP_ABSOLUTE));
        assertTrue(instructions.stream()
                .anyMatch(i -> i.opCode == Instruction.OpCode.PRINT));
    }

    @Test
    void testCompileWhileLoop() {
        Bytecode bytecode = compileSingleExpression(
                "while (x > 0) { x = x - 1; }"
        );
        List<Instruction> instructions = bytecode.getInstructions();

        assertTrue(instructions.size() >= 2);
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);

        // Проверяем наличие инструкций для цикла
        assertTrue(instructions.stream()
                .anyMatch(i -> i.opCode == Instruction.OpCode.POP_JUMP_IF_FALSE));
        assertTrue(instructions.stream()
                .anyMatch(i -> i.opCode == Instruction.OpCode.JUMP_ABSOLUTE));
    }

    @Test
    void testCompileArrayOperations() {
        Bytecode bytecode = compileSingleExpression(
                "var arr = [1, 2, 3]; arr[1] = 42;"
        );
        List<Instruction> instructions = bytecode.getInstructions();

        assertTrue(instructions.size() >= 2);
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);

        // Проверяем наличие операций с массивом
        assertTrue(instructions.stream()
                .anyMatch(i -> i.opCode == Instruction.OpCode.BUILD_LIST));
        assertTrue(instructions.stream()
                .anyMatch(i -> i.opCode == Instruction.OpCode.SUBSCR_STORE));
    }

    @Test
    void testCompileFunctionCall() {
        Bytecode bytecode = compileSingleExpression(
                "print(42);"
        );
        List<Instruction> instructions = bytecode.getInstructions();

        assertTrue(instructions.size() >= 3);
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);
        assertEquals(Instruction.OpCode.LOAD_CONST, instructions.get(1).opCode);
        assertEquals(Instruction.OpCode.PRINT, instructions.get(2).opCode);
    }

    @Test
    void testComplexExpression() {
        Bytecode bytecode = compileSingleExpression(
                "var result = (2 + 3) * (4 - 1);"
        );
        List<Instruction> instructions = bytecode.getInstructions();

        assertTrue(instructions.size() >= 8);
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);

        // Проверяем наличие всех арифметических операций
        assertTrue(instructions.stream()
                .anyMatch(i -> i.opCode == Instruction.OpCode.BINARY_ADD));
        assertTrue(instructions.stream()
                .anyMatch(i -> i.opCode == Instruction.OpCode.BINARY_SUBTRACT));
        assertTrue(instructions.stream()
                .anyMatch(i -> i.opCode == Instruction.OpCode.BINARY_MULTIPLY));
    }

    @Test
    void testCompileMultipleStatements() {
        Bytecode bytecode = compileSingleExpression(
                """
                var x = 10;
                var y = 20;
                var z = x + y;
                print(z);
                """
        );
        List<Instruction> instructions = bytecode.getInstructions();

        assertTrue(instructions.size() >= 9); // JUMP_FORWARD + минимум 8 инструкций
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);

        // Проверяем количество операций сохранения переменных
        assertEquals(3, instructions.stream()
                .filter(i -> i.opCode == Instruction.OpCode.STORE_NAME)
                .count());
    }
}

