package dev.itmo.compiler.vm;

import dev.itmo.compiler.memory.RcList;
import dev.itmo.compiler.memory.RcObject;
import dev.itmo.compiler.vm.bytecode.Bytecode;
import dev.itmo.compiler.vm.bytecode.FunctionObject;
import dev.itmo.compiler.vm.bytecode.Instruction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualMachine {
    private final List<Instruction> instructions;
    private final Map<String, FunctionObject> functionTable;
    private final Stack<Object> stack = new Stack<>();
    private final Map<String, Object> globals = new HashMap<>();
    private final Stack<Frame> frames = new Stack<>();
    private int ip = 0;

    public VirtualMachine(Bytecode bytecode) {
        this.instructions = bytecode.getInstructions();
        this.functionTable = bytecode.getFunctionTable();

        frames.push(new Frame(-1, new HashMap<>()));
        initBuiltins();
    }

    private void initBuiltins() {
        globals.put("print", (NativeFunction) (vm, args) -> {
            for (Object arg : args) {
                System.out.println(arg);
            }
            return null;
        });
        globals.put("length", (NativeFunction) (vm, args) -> {
            if (args.size()!=1 || !(args.get(0) instanceof RcList)) {
                throw new RuntimeException("length() expects 1 list argument");
            }
            return (long)((RcList)args.get(0)).size();
        });
        globals.put("push", (NativeFunction) (vm, args) -> {
            if (args.size()!=2 || !(args.get(0) instanceof RcList)) {
                throw new RuntimeException("push expects (list, value)");
            }
            RcList lst = (RcList) args.get(0);
            Object val = args.get(1);
            lst.add(val);
            return null;
        });
        globals.put("randomInt", (NativeFunction) (vm, args) -> {
            if(args.size()!=2) {
                throw new RuntimeException("randomInt(min, max)");
            }
            long min = (long)args.get(0);
            long max = (long)args.get(1);
            return min + (long)(Math.random()*(max-min+1));
        });
        globals.put("true", true);
        globals.put("false", false);
    }

    public void run() {
        while (ip < instructions.size()) {
            Instruction instr = instructions.get(ip);
//            System.out.println(instr);
            try {
                execute(instr);
            } catch (Exception e) {
                System.err.println("Error executing instruction: " + e.getMessage());
                e.printStackTrace();
                break;
            }
//            AtomicInteger counter = new AtomicInteger(0);
//            System.out.println("Stack: " + stack.stream()
//                    .takeWhile(e -> counter.incrementAndGet() < 7)
//                    .toList());
//            System.out.println("Frames: " + frames);
//            System.out.println("IP: " + ip);
        }
        System.out.println("Program finished");
    }

    private void execute(Instruction instr) {
        switch(instr.opCode) {
            case LOAD_CONST: {
                push(instr.operand);
                ip++;
                break;
            }
            case LOAD_NAME: {
                String name = (String) instr.operand;
                Object val = loadName(name);
                if(val == null){
                    throw new RuntimeException("Name '"+name+"' not defined");
                }
                push(val);
                ip++;
                break;
            }
            case STORE_NAME: {
                String name = (String)instr.operand;
                Object value = pop();
                storeName(name, value);
                ip++;
                break;
            }
            case BINARY_ADD: {
                Object b = pop();
                Object a = pop();
//                System.out.println("BINARY_ADD:");
//                System.out.println("a = " + a);
//                System.out.println("b = " + b);

                if(a instanceof Long && b instanceof Long) {
                    push((Long)a + (Long)b);
                } else if(a instanceof String || b instanceof String) {
                    push(String.valueOf(a) + String.valueOf(b));
                } else if((a instanceof RcList || a instanceof List) &&
                        (b instanceof RcList || b instanceof List)) {
                    RcList result = new RcList();

                    // Преобразуем входные данные в List
                    List<?> listA = (a instanceof RcList) ? ((RcList)a).getItems() : (List<?>)a;
                    List<?> listB = (b instanceof RcList) ? ((RcList)b).getItems() : (List<?>)b;

//                    System.out.println("Concatenating lists:");
//                    System.out.println("List A: " + listA);
//                    System.out.println("List B: " + listB);

                    for(Object item : listA) {
                        result.add(item);
                    }

                    for(Object item : listB) {
                        result.add(item);
                    }

                    System.out.println("Result: " + result.getItems());
                    push(result);
                } else {
                    throw new RuntimeException("Unsupported types for +: " +
                            (a != null ? a.getClass() : "null") + " and " +
                            (b != null ? b.getClass() : "null"));
                }
                ip++;
                break;
            }
            case BINARY_SUBTRACT: {
                Object b = pop();
                Object a = pop();
                push((Long)a - (Long)b);
                ip++;
                break;
            }
            case BINARY_MULTIPLY: {
                Object b = pop();
                Object a = pop();
                push((Long)a * (Long)b);
                ip++;
                break;
            }
            case BINARY_DIVIDE: {
                Object b = pop();
                Object a = pop();
                push((Long)a / (Long)b);
                ip++;
                break;
            }
            case BINARY_MODULO: {
                Object b = pop();
                Object a = pop();
                push((Long)a % (Long)b);
                ip++;
                break;
            }
            case COMPARE_OP: {
                String op = (String)instr.operand;
                Object b = pop();
                Object a = pop();
                boolean r;
                switch(op) {
                    case "==": r = Objects.equals(a,b); break;
                    case "!=": r = !Objects.equals(a,b); break;
                    case "<":  r = ((Long)a)<((Long)b);  break;
                    case "<=": r = ((Long)a)<=((Long)b); break;
                    case ">":  r = ((Long)a)>((Long)b);  break;
                    case ">=": r = ((Long)a)>=((Long)b); break;
                    default: throw new RuntimeException("Unknown compare op: " + op);
                }
                push(r);
                ip++;
                break;
            }
            case POP_JUMP_IF_FALSE: {
                Object condition = pop();
                if (condition instanceof Boolean && !(Boolean) condition) {
                    ip = (Integer) instr.operand;
                } else {
                    ip++;
                }
                break;
            }
            case JUMP_FORWARD: {
                ip += (Integer) instr.operand;
                break;
            }

            case JUMP_ABSOLUTE: {
                ip = (Integer) instr.operand;
                break;
            }
            case CALL_FUNCTION: {
                String funcName = (String) instr.operand;
                int argCount = (int) instr.operand2;
                FunctionObject fn = functionTable.get(funcName);
                if(fn == null){
                    throw new RuntimeException("Undefined function: "+funcName);
                }
                if(argCount != fn.parameters.size()){
                    throw new RuntimeException("Argument count mismatch for function "+funcName);
                }
                List<Object> args = new ArrayList<>();
                for(int i = 0; i < argCount; i++) {
                    Object ar = pop();
                    if(ar instanceof RcObject) {
                        ((RcObject)ar).incRef();
                    }
                    args.add(0, ar);
                }
                Map<String,Object> newLocals = new HashMap<>();
                for(int i = 0;i < argCount; i++){
                    newLocals.put(fn.parameters.get(i), args.get(i));
                }
                int returnAddress = ip + 1;
                frames.push(new Frame(returnAddress, newLocals));
                ip = fn.address;
                break;
            }
            case CALL_NATIVE: {
                String funcName = (String)instr.operand;
                int argCount = (Integer)instr.operand2;
                Object nativeF = globals.get(funcName);
                if(!(nativeF instanceof NativeFunction)){
                    throw new RuntimeException("Unknown native function: "+funcName);
                }
                NativeFunction nf = (NativeFunction)nativeF;
                List<Object> args = new ArrayList<>();
                for(int i=0;i<argCount;i++){
                    Object ar = pop();
                    if(ar instanceof RcObject){
                        ((RcObject)ar).incRef();
                    }
                    args.add(0,ar);
                }
                Object res = nf.call(this, args);
                if(res!=null){
                    push(res);
                }
                ip++;
                break;
            }
            case RETURN_VALUE: {
                Object returnVal = pop();
                Frame fr = frames.pop();

                // Не уменьшаем счетчик ссылок для возвращаемого значения
                for (Map.Entry<String, Object> entry : fr.locals.entrySet()) {
                    Object v = entry.getValue();
                    if (v instanceof RcObject && v != returnVal) {  // Добавлена проверка
                        ((RcObject) v).decRef();
                    }
                }

                if (frames.isEmpty()) {
                    ip = instructions.size();
                } else {
                    ip = fr.returnAddress;
                    push(returnVal);
                }
                break;
            }
            case BUILD_LIST: {
                int count = (Integer)instr.operand;
                RcList list = new RcList();
                for(int i=0;i<count;i++){
                    Object v = pop();
                    list.addToFront(v);
                }
                push(list);
                ip++;
                break;
            }
            case SUBSCR_LOAD: {
                Object idx = pop();
                Object arr = pop();
                if(!(arr instanceof RcList)){
                    throw new RuntimeException("subscr_load expects a list");
                }
                int i = (int)(long)(Long)idx;
                RcList rlist = (RcList)arr;
                Object val = rlist.get(i);
                push(val);
                ip++;
                break;
            }
            case SUBSCR_STORE: {
                Object value = pop();
                Object indexObj = pop();
                Object arrayObj = pop();
                if(!(arrayObj instanceof RcList)){
                    throw new RuntimeException("subscr_store on non-list");
                }
                RcList list = (RcList)arrayObj;
                int i = (int)(long)(Long)indexObj;

                // Расширяем список если нужно
                while (list.size() <= i) {
                    list.add(null);
                }

                list.set(i, value);
                ip++;
                break;
            }
            case PRINT: {
                Object val = pop();
                System.out.println(val);
                ip++;
                break;
            }
            default:
                throw new RuntimeException("Unknown opcode: "+instr.opCode);
        }
    }

    private Object loadName(String name){
        // Ищем в локальных фреймах (от верхнего к нижнему)
        for(int i = frames.size() - 1; i >= 0; i--){
            Frame f = frames.get(i);
            if(f.locals.containsKey(name)){
                return f.locals.get(name);
            }
        }
        // потом глобальные
        return globals.get(name);
    }

    private void storeName(String name, Object value){
        Frame topFrame = frames.peek();
        Object oldVal = topFrame.locals.put(name, value);
        if(oldVal instanceof RcObject){
            ((RcObject)oldVal).decRef();
        }
        if(value instanceof RcObject){
            ((RcObject)value).incRef();
        }
    }

    private void push(Object obj){
        if(obj instanceof RcObject){
            ((RcObject)obj).incRef();
        }
        stack.push(obj);
    }

    private Object pop(){
        Object top = stack.pop();
        if(top instanceof RcObject){
            ((RcObject)top).decRef();
        }
        return top;
    }

    public Object getTopOfStack() {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }
}