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
                """
                        var a = 10;
                        var b = a;
                        function add(x, y) { return x + y; }
                        add(a, b);
                        """


        );
        List<Instruction> instructions = bytecode.getInstructions();

        assertTrue(instructions.size() >= 5);
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);

        FunctionObject addFunction = bytecode.getFunctionTable().get("add");
        assertNotNull(addFunction);
        assertEquals("add", addFunction.name);
        assertEquals(2, addFunction.parameters.size());
    }
    @Test
    void testCompileSimpleFunctionWithoutReturnStatement() {
        Bytecode bytecode = compileSingleExpression(
                "function hello(a) { print(a); }"
        );
        List<Instruction> instructions = bytecode.getInstructions();

        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);
        assertEquals(Instruction.OpCode.RETURN_VALUE, instructions.get(4).opCode);
        assertNull(instructions.get(4).operand);
        assertNull(instructions.get(4).operand2);
    }
    @Test
    void testCompileFunctionWithEqualsExpression() {
        Bytecode bytecode = compileSingleExpression(
                """
                        function add(a, b) { return a + b; }
                        var i = 1;
                        var j = 2;
                        var k = add(i,j);
                        """
        );
        List<Instruction> instructions = bytecode.getInstructions();
    }
    @Test
    void testCompileIfStatement() {
        Bytecode bytecode = compileSingleExpression(
                "if (x > 0) { print(x); } else { print(-x); }"
        );
        List<Instruction> instructions = bytecode.getInstructions();

        assertTrue(instructions.size() >= 2);
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);

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

        assertTrue(instructions.size() >= 9);
        assertEquals(Instruction.OpCode.JUMP_FORWARD, instructions.get(0).opCode);

        assertEquals(3, instructions.stream()
                .filter(i -> i.opCode == Instruction.OpCode.STORE_NAME)
                .count());
    }
        @Test
        public void testInliningMethodAfter200Calls() {
            Bytecode bytecode = compileSingleExpression("""
                var i = 202;
                function println(a) {
                    print(a);
                }
                while (i > 0) {
                    i = i - 1;
                    println(i);
                }
                """);
            var inst = bytecode.getInstructions();
        }
    }


