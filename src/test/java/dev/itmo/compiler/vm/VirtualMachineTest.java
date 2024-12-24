package dev.itmo.compiler.vm;

import dev.itmo.compiler.parser.ASTNode;
import dev.itmo.compiler.parser.experessions.NumberExpression;
import dev.itmo.compiler.parser.statements.PrintStatement;
import dev.itmo.compiler.vm.bytecode.Bytecode;
import dev.itmo.compiler.vm.bytecode.Compiler;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VirtualMachineTest {
    @Test
    public void testSimpleScriptExecution() {
        // Создаём байткод для вывода числа 42
        Bytecode bytecode = new Bytecode();
        Compiler compiler = new Compiler();

        // Создаём AST
        List<ASTNode> nodes = new ArrayList<>();
        nodes.add(new PrintStatement(new NumberExpression(42L)));

        // Компилируем AST
        bytecode = compiler.compile(nodes);

        // Запускаем виртуальную машину
        VirtualMachine vm = new VirtualMachine(bytecode);

        // Перехватываем вывод
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));

        try {
            vm.run();
        } finally {
            System.setOut(originalOut);
        }

        // Проверяем, что выводится 42
        assertEquals("42\n", outputStream.toString());
    }
}
