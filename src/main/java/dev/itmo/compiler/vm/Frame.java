package dev.itmo.compiler.vm;

import dev.itmo.compiler.memory.RcList;

import java.util.Map;

public class Frame {
    public int returnAddress;
    public Map<String,Object> locals;

    public Frame(int returnAddress, Map<String,Object> locals){
        this.returnAddress = returnAddress;
        this.locals = locals;
    }

    @Override
    public String toString() {
        return "Frame{" +
                "returnAddress=" + returnAddress +
                ", locals=" + locals.entrySet().stream()
                .map(entry -> {
                    Object value = entry.getValue();
                    if (value instanceof RcList list) {
                        return entry.getKey() + list.getItems().stream().limit(5).toList();
                    } else {
                        return entry.getKey() + "=" + value;
                    }
                })
                .limit(7) // Обрезаем вывод до 7 элементов в карте
                .toList() +
                '}';
    }
}




