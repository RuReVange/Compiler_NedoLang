package dev.itmo.compiler.vm;

import dev.itmo.compiler.memory.RcList;
import dev.itmo.compiler.vm.bytecode.Bytecode;
import dev.itmo.compiler.vm.bytecode.Instruction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReferenceCountingTest {
    private VirtualMachine vm;
    private Bytecode bytecode;

    @BeforeEach
    void setUp() {
        bytecode = new Bytecode();
        vm = new VirtualMachine(bytecode);
    }

    @Test
    void testInitialRefCount() {
        RcList list = new RcList();
        assertEquals(0, list.getRefCount());
    }

    @Test
    void testRefCountAfterStoreLoad() {
        RcList list = new RcList();

        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, list));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.STORE_NAME, "myList"));
        vm.run();

        assertEquals(1, list.getRefCount());

        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_NAME, "myList"));
        vm.run();

        assertEquals(2, list.getRefCount());
    }

    @Test
    void testRefCountInNestedLists() {
        RcList outer = new RcList();
        RcList inner = new RcList();

        assertEquals(0, outer.getRefCount());
        assertEquals(0, inner.getRefCount());

        outer.add(inner);
        assertEquals(1, inner.getRefCount());

        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, outer));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.STORE_NAME, "outer"));
        vm.run();

        assertEquals(1, outer.getRefCount());
        assertEquals(1, inner.getRefCount());
    }

    @Test
    void testRefCountAfterReassignment() {
        RcList list1 = new RcList();
        RcList list2 = new RcList();

        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, list1));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.STORE_NAME, "myList"));
        vm.run();

        assertEquals(1, list1.getRefCount());
        assertEquals(0, list2.getRefCount());

        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, list2));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.STORE_NAME, "myList"));
        vm.run();

        assertEquals(0, list1.getRefCount());
        assertEquals(1, list2.getRefCount());
    }

    @Test
    void testRefCountWithListOperations() {
        RcList list = new RcList();
        RcList element = new RcList();

        list.add(element);
        assertEquals(1, element.getRefCount());

        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, list));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.STORE_NAME, "list"));
        vm.run();

        assertEquals(1, list.getRefCount());
        assertEquals(1, element.getRefCount());

        list.set(0, null);
        assertEquals(0, element.getRefCount());
    }

    @Test
    void testRefCountWithMultipleReferences() {
        RcList list = new RcList();

        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, list));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.STORE_NAME, "list1"));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, list));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.STORE_NAME, "list2"));
        vm.run();

        assertEquals(2, list.getRefCount());
    }

    @Test
    void testRefCountInStack() {
        RcList list = new RcList();
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, list));
        vm.run();

        assertEquals(1, list.getRefCount());
    }

    @Test
    void testRefCountWithNestedStructures() {
        RcList outer = new RcList();
        RcList middle = new RcList();
        RcList inner = new RcList();

        middle.add(inner);
        outer.add(middle);

        assertEquals(1, middle.getRefCount());
        assertEquals(1, inner.getRefCount());

        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, outer));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.STORE_NAME, "outer"));
        vm.run();

        assertEquals(1, outer.getRefCount());
        assertEquals(1, middle.getRefCount());
        assertEquals(1, inner.getRefCount());
    }

    @Test
    void testRefCountWithSubscrOperations() {
        RcList list = new RcList();
        RcList element = new RcList();
        list.add(element);

        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, list));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.LOAD_CONST, 0L));
        bytecode.addInstruction(new Instruction(Instruction.OpCode.SUBSCR_LOAD));
        vm.run();

        assertEquals(2, element.getRefCount());
    }
}
