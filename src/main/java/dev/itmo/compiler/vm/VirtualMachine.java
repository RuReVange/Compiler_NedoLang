package dev.itmo.compiler.vm;

import dev.itmo.compiler.vm.bytecode.Bytecode;
import dev.itmo.compiler.vm.bytecode.FunctionObject;
import dev.itmo.compiler.vm.bytecode.Instruction;

import java.util.*;

public class VirtualMachine {
    private final List<Instruction> instructions;
    private final Map<String, FunctionObject> functionTable;
    private final Stack<Object> stack = new Stack<>();
    private final Map<String, Object> globals = new HashMap<>();
    private final Stack<Frame> frames = new Stack<>();
    private int ip = 0; // Instruction pointer

    public VirtualMachine(Bytecode bytecode) {
        this.instructions = bytecode.getInstructions();
        this.functionTable = bytecode.getFunctionTable();

        // Инициализируем фрейм глобального контекста
        frames.push(new Frame(-1, new HashMap<>()));

        // Инициализируем встроенные функции
        initializeBuiltInFunctions();
        initializeGlobalVariables();
    }

    private void initializeBuiltInFunctions() {
        globals.put("print", (NativeFunction) (vm, args) -> {
            for (Object arg : args) {
                System.out.println(arg);
            }
            return null;
        });

        globals.put("length", (NativeFunction) (vm, args) -> {
            if (args.size() != 1 || !(args.get(0) instanceof List)) {
                throw new RuntimeException("length() expects one array argument");
            }
            return (long) ((List<?>) args.get(0)).size();
        });

        globals.put("push", (NativeFunction) (vm, args) -> {
            if (args.size() != 2 || !(args.get(0) instanceof List)) {
                throw new RuntimeException("push() expects array and value arguments");
            }
            ((List<Object>) args.get(0)).add(args.get(1));
            return null;
        });

        globals.put("randomInt", (NativeFunction) (vm, args) -> {
            if (args.size() != 2 || !(args.get(0) instanceof Long) || !(args.get(1) instanceof Long)) {
                throw new RuntimeException("randomInt() expects two integer arguments");
            }
            long min = (Long) args.get(0);
            long max = (Long) args.get(1);
            return min + (long) (Math.random() * ((max - min) + 1));
        });
    }

    private void initializeGlobalVariables() {
        globals.put("true", true);
        globals.put("false", false);
    }

    public void run() {
        while (ip < instructions.size()) {
            Instruction instr = instructions.get(ip);
            execute(instr);
            ip++;
        }
    }

    private void execute(Instruction instr) {
        switch (instr.opCode) {
            case LOAD_CONST:
                stack.push(instr.operand);
                break;
            case LOAD_NAME: {
                String name = (String) instr.operand;
                Object value = loadName(name);
                stack.push(value);
            }
            break;
            case STORE_NAME: {
                String name = (String) instr.operand;
                Object value = stack.pop();
                storeName(name, value);
            }
            break;
            case BINARY_ADD: {
                Object b = stack.pop();
                Object a = stack.pop();
                if (a instanceof Long && b instanceof Long) {
                    stack.push((Long) a + (Long) b);
                } else if (a instanceof List && b instanceof List) {
                    List<Object> result = new ArrayList<>((List<Object>) a);
                    result.addAll((List<Object>) b);
                    stack.push(result);
                } else {
                    throw new RuntimeException("Unsupported types for addition");
                }
            }
            break;
            case BINARY_SUBTRACT: {
                Object b = stack.pop();
                Object a = stack.pop();
                stack.push((Long) a - (Long) b);
            }
            break;
            case BINARY_MULTIPLY: {
                Object b = stack.pop();
                Object a = stack.pop();
                stack.push((Long) a * (Long) b);
            }
            break;
            case BINARY_DIVIDE: {
                Object b = stack.pop();
                Object a = stack.pop();
                stack.push((Long) a / (Long) b);
            }
            break;
            case BINARY_MODULO: {
                Object b = stack.pop();
                Object a = stack.pop();
                stack.push((Long) a % (Long) b);
            }
            break;
            case COMPARE_OP: {
                String op = (String) instr.operand;
                Object b = stack.pop();
                Object a = stack.pop();
                boolean result;
                switch (op) {
                    case "==":
                        result = a.equals(b);
                        break;
                    case "!=":
                        result = !a.equals(b);
                        break;
                    case "<":
                        result = ((Long) a) < ((Long) b);
                        break;
                    case ">":
                        result = ((Long) a) > ((Long) b);
                        break;
                    case "<=":
                        result = ((Long) a) <= ((Long) b);
                        break;
                    case ">=":
                        result = ((Long) a) >= ((Long) b);
                        break;
                    default:
                        throw new RuntimeException("Unknown comparison operator: " + op);
                }
                stack.push(result);
            }
            break;
            case POP_JUMP_IF_FALSE: {
                Object value = stack.pop();
                if (value instanceof Boolean && !(Boolean) value) {
                    ip = (Integer) instr.operand - 1;
                }
            }
            break;
            case JUMP_FORWARD: {
                ip = (Integer) instr.operand - 1;
            }
            break;
            case JUMP_ABSOLUTE: {
                ip = (Integer) instr.operand - 1;
            }
            break;
            case CALL_FUNCTION:
            {
                String functionName = (String) instr.operand;
                int argCount = (Integer) instr.operand2;

                FunctionObject function = functionTable.get(functionName);
                if (function == null) {
                    throw new RuntimeException("Undefined function: " + functionName);
                }

                if (argCount != function.parameters.size()) {
                    throw new RuntimeException("Function " + functionName + " expects " + function.parameters.size() + " arguments, got " + argCount);
                }

                // Извлекаем аргументы со стека
                List<Object> args = new ArrayList<>();
                for (int i = 0; i < argCount; i++) {
                    args.add(0, stack.pop()); // Добавляем в начало списка
                }

                // Сопоставляем аргументы с параметрами
                Map<String, Object> newLocals = new HashMap<>();
                for (int i = 0; i < argCount; i++) {
                    String paramName = function.parameters.get(i);
                    Object argValue = args.get(i);
                    newLocals.put(paramName, argValue);
                }

                // Сохраняем текущий IP и создаём новый фрейм
                int returnAddress = ip;
                frames.push(new Frame(returnAddress, newLocals));

                // Переходим к адресу функции
                ip = function.address - 1;
            }
            break;
            case CALL_NATIVE: {
                String functionName = (String) instr.operand;
                NativeFunction function = (NativeFunction) globals.get(functionName);
                if (function == null) {
                    throw new RuntimeException("Unknown native function: " + functionName);
                }
                List<Object> args = new ArrayList<>();
                // Получаем количество аргументов функции
                int argCount = getArgumentCount(functionName);
                for (int i = 0; i < argCount; i++) {
                    args.add(0, stack.pop());
                }
                Object result = function.call(this, args);
                if (result != null) {
                    stack.push(result);
                }
            }
            break;
            case RETURN_VALUE:
            {
                Object returnValue = stack.pop();
                Frame currentFrame = frames.pop();
                if (frames.isEmpty()) {
                    // Программа завершила работу
                    ip = instructions.size();
                } else {
                    ip = currentFrame.returnAddress;


                    stack.push(returnValue);
                }
            }
            break;
            case BUILD_LIST: {
                int count = (Integer) instr.operand;
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    list.add(0, stack.pop());
                }
                stack.push(list);
            }
            break;
            case SUBSCR_LOAD: {
                Object index = stack.pop();
                Object list = stack.pop();
                if (!(list instanceof List)) {
                    throw new RuntimeException("SUBSCR_LOAD expects list");
                }
                if (!(index instanceof Long)) {
                    throw new RuntimeException("SUBSCR_LOAD expects integer index");
                }
                List<Object> array = (List<Object>) list;
                int idx = ((Long) index).intValue();
                stack.push(array.get(idx));
            }
            break;
            case SUBSCR_STORE: {
                Object value = stack.pop();
                Object index = stack.pop();
                Object list = stack.pop();
                if (!(list instanceof List)) {
                    throw new RuntimeException("SUBSCR_STORE expects list");
                }
                if (!(index instanceof Long)) {
                    throw new RuntimeException("SUBSCR_STORE expects integer index");
                }
                List<Object> array = (List<Object>) list;
                int idx = ((Long) index).intValue();
                // Расширяем массив при необходимости
                while (array.size() <= idx) {
                    array.add(null);
                }
                array.set(idx, value);
            }
            break;
            case PRINT:
                System.out.println(stack.pop());
                break;
            default:
                throw new RuntimeException("Unknown opcode: " + instr.opCode);
        }
    }

    private Object getFunctionAddress(String functionName) {
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instr = instructions.get(i);
            if (instr.opCode == Instruction.OpCode.LOAD_CONST && functionName.equals(instr.operand)) {
                return i + 1;
            }
            if (instr.opCode == Instruction.OpCode.STORE_NAME && functionName.equals(instr.operand)) {
                return i + 1;
            }
            if (instr.opCode == Instruction.OpCode.LOAD_NAME && functionName.equals(instr.operand)) {
                return i + 1;
            }
        }
        return null;
    }

    private int getArgumentCount(String functionName) {
        // Для упрощения возвращаем фиксированное количество аргументов для встроенных функций
        switch (functionName) {
            case "print":
                return 1;
            case "length":
                return 1;
            case "push":
                return 2;
            case "randomInt":
                return 2;
            default:
                return 0; // Для пользовательских функций аргументы уже на стеке
        }
    }

    private Object loadName(String name) {
        // Ищем переменную в текущем фрейме
        for (int i = frames.size() - 1; i >= 0; i--) {
            Frame frame = frames.get(i);
            if (frame.locals.containsKey(name)) {
                return frame.locals.get(name);
            }
        }
        // Если не найдена, ищем в глобалах
        if (globals.containsKey(name)) {
            return globals.get(name);
        }
        throw new RuntimeException("Name '" + name + "' is not defined");
    }

    private void storeName(String name, Object value) {
        // Сохраняем переменную в текущем фрейме
        frames.peek().locals.put(name, value);
    }

    private static class Frame {
        Map<String, Object> locals;
        int returnAddress;

        Frame(int returnAddress, Map<String, Object> locals) {
            this.returnAddress = returnAddress;
            this.locals = locals;
        }
    }


    @FunctionalInterface
    public interface NativeFunction {
        Object call(VirtualMachine vm, List<Object> args);
    }
}