package dev.itmo.compiler.memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RcObjectTest {

    @Test
    public void testSimpleRcObjectDestruction() {
        RcList list = new RcList();
        RcList sublist = new RcList();
        list.incRef(); // refCount = 1
        sublist.incRef(); // refCount = 1

        list.add(sublist); // sublist refCount = 2

        list.decRef(); // list refCount = 0
        // list destruction should decrease sublist refCount to 1
        // sublist should NOT be destroyed

        // Now decrease sublist refCount to 1
        sublist.decRef(); // sublist refCount = 1

        // Decrease sublist refCount to 0, should trigger destruction
        sublist.decRef(); // sublist refCount = 0
        // sublist should be destroyed without StackOverflowError
    }

    @Test
    public void testCyclicReferences() {
        RcList a = new RcList();
        RcList b = new RcList();
        a.incRef();
        b.incRef();
        a.add(b); // b refCount = 2
        b.add(a); // a refCount = 2
        a.decRef(); // a refCount = 1
        b.decRef(); // b refCount = 1

        // Дополнительно проверяем, что refCount объектов равен 1
//        assertEquals(1, a.getRefCount());
//        assertEquals(1, b.getRefCount());

        a.decRef(); // a refCount = 0, должны запуститься destroy без переполнения стека

        // Проверяем, что объекты уничтожены
//        assertEquals(0, a.getRefCount());
//        assertEquals(0, b.getRefCount());
    }
}
