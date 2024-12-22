package dev.itmo.compiler.vm.bytecode;

import java.io.Serializable;

public class Instruction implements Serializable {
    public enum OpCode {
        // Стековые операции
        LOAD_CONST,
        LOAD_NAME,
        STORE_NAME,
        POP_TOP,

        // Арифметические операции
        BINARY_ADD,
        BINARY_SUBTRACT,
        BINARY_MULTIPLY,
        BINARY_DIVIDE,
        BINARY_MODULO,

        // Логические операции
        COMPARE_OP,

        // Управление потоком
        JUMP_FORWARD,
        JUMP_ABSOLUTE,
        POP_JUMP_IF_FALSE,
        RETURN_VALUE,
        CALL_FUNCTION,

        // Работа с последовательностями
        BUILD_LIST,
        LIST_APPEND,
        SUBSCR_LOAD,
        SUBSCR_STORE,
        DUP_TOP,

        // Встроенные функции
        CALL_NATIVE,

        // Прочие операции
        PRINT,
    }

    public OpCode opCode;
    public Object operand;
    public Object operand2;

    public Instruction(OpCode opCode) {
        this.opCode = opCode;
        this.operand = null;
        this.operand2 = null;
    }

    public Instruction(OpCode opCode, Object operand) {
        this.opCode = opCode;
        this.operand = operand;
        this.operand2 = null;
    }

    public Instruction(OpCode opCode, Object operand, Object operand2) {
        this.opCode = opCode;
        this.operand = operand;
        this.operand2 = operand2;
    }
}