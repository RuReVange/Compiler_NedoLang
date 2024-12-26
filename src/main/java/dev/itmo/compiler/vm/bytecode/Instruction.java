package dev.itmo.compiler.vm.bytecode;

import java.io.Serializable;

public class Instruction implements Serializable {
    public enum OpCode {
        LOAD_CONST(0x01),
        LOAD_NAME(0x02),
        STORE_NAME(0x03),
        POP_TOP(0x04),

        BINARY_ADD(0x10),
        BINARY_SUBTRACT(0x11),
        BINARY_MULTIPLY(0x12),
        BINARY_DIVIDE(0x13),
        BINARY_MODULO(0x14),

        COMPARE_OP(0x20),

        JUMP_FORWARD(0x30),
        JUMP_ABSOLUTE(0x31),
        POP_JUMP_IF_FALSE(0x32),
        RETURN_VALUE(0x33),
        CALL_FUNCTION(0x34),

        BUILD_LIST(0x40),
        SUBSCR_LOAD(0x41),
        SUBSCR_STORE(0x42),

        CALL_NATIVE(0x50),

        PRINT(0x60);

        private final byte code;

        OpCode(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }

        public static OpCode fromCode(byte code) {
            for (OpCode op : values()) {
                if (op.code == code) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Invalid OpCode: " + code);
        }
    }

    public OpCode opCode;
    public Object operand;
    public Object operand2;

    public Instruction(OpCode opCode) {
        this(opCode, null, null);
    }

    public Instruction(OpCode opCode, Object operand) {
        this(opCode, operand, null);
    }

    public Instruction(OpCode opCode, Object operand, Object operand2) {
        this.opCode = opCode;
        this.operand = operand;
        this.operand2 = operand2;
    }

    @Override
    public String toString() {
        return "Instruction{" +
                "opCode=" + opCode +
                ", operand=" + operand +
                ", operand2=" + operand2 +
                '}';
    }
}