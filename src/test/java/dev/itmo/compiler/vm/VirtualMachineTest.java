package dev.itmo.compiler.vm;

import dev.itmo.compiler.memory.RcList;
import dev.itmo.compiler.vm.bytecode.Bytecode;
import dev.itmo.compiler.vm.bytecode.Instruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VirtualMachineTest {
    private VirtualMachine vm;
    private Bytecode bytecode;

    @BeforeEach
    void setUp() {
        bytecode = new Bytecode();
        vm = new VirtualMachine(bytecode);
    }

    @Test
    void testBinaryAdd() {
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 5L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 3L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.BINARY_ADD));
        vm.run();

        assertEquals(8L, (Long)vm.getTopOfStack());
    }

    @Test
    void testBinarySubtract() {
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 10L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 4L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.BINARY_SUBTRACT));
        vm.run();

        assertEquals(6L, (Long)vm.getTopOfStack());
    }

    @Test
    void testBinaryMultiply() {
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 6L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 7L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.BINARY_MULTIPLY));
        vm.run();

        assertEquals(42L, (Long)vm.getTopOfStack());
    }

    @Test
    void testBinaryDivide() {
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 20L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 5L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.BINARY_DIVIDE));
        vm.run();

        assertEquals(4L, (Long)vm.getTopOfStack());
    }

    @Test
    void testCompareEqual() {
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 5L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 5L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.COMPARE_OP, "=="));
        vm.run();

        assertEquals(true, vm.getTopOfStack());
    }

    @Test
    void testBuildList() {
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 1L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 2L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 3L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.BUILD_LIST, 3));
        vm.run();

        RcList result = (RcList) vm.getTopOfStack();
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0));
        assertEquals(2L, result.get(1));
        assertEquals(3L, result.get(2));
    }

    @Test
    void testSubscrLoad() {
        RcList list = new RcList();
        list.add(10L);
        list.add(20L);

        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, list));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 1L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.SUBSCR_LOAD));
        vm.run();

        assertEquals(20L, (Long)vm.getTopOfStack());
    }

    @Test
    void testSubscrStore() {
        RcList list = new RcList();
        list.add(10L);

        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, list));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 0L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 20L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.SUBSCR_STORE));
        vm.run();

        assertEquals(20L, list.get(0));
    }

    @Test
    void testStoreAndLoadName() {
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 42L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.STORE_NAME, "x"));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_NAME, "x"));
        vm.run();

        assertEquals(42L, (Long)vm.getTopOfStack());
    }

    @Test
    void testReferenceCount() {
        RcList list = new RcList();
        assertEquals(0, list.getRefCount());

        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, list));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.STORE_NAME, "test"));
        vm.run();

        assertEquals(1, list.getRefCount());
    }

    @Test
    void testNestedListReferenceCount() {
        RcList outer = new RcList();
        RcList inner = new RcList();
        assertEquals(0, inner.getRefCount());

        outer.add(inner);
        assertEquals(1, inner.getRefCount());

        outer.set(0, null);
        assertEquals(0, inner.getRefCount());
    }
}
