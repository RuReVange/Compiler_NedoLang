package dev.itmo.compiler.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RcList extends RcObject {
    private final List<Object> items = new ArrayList<>();

    public void add(Object it){
        if(it instanceof RcObject){
            ((RcObject)it).incRef();
        }
        items.add(it);
    }

    public void addToFront(Object it){
        if(it instanceof RcObject){
            ((RcObject)it).incRef();
        }
        items.add(0,it);
    }

    public Object get(int i){
        return items.get(i);
    }

    public void set(int i, Object v){
        while(i>=items.size()){
            items.add(null);
        }
        Object old = items.get(i);
        if(old instanceof RcObject){
            ((RcObject)old).decRef();
        }
        if(v instanceof RcObject){
            ((RcObject)v).incRef();
        }
        items.set(i,v);
    }

    public int size(){
        return items.size();
    }

    public List<Object> getItems(){
        return items;
    }

    @Override
    protected void destroy(Stack<RcObject> toDestroy) {
        for(Object o: items){
            if(o instanceof RcObject){
                RcObject rc = (RcObject)o;
                rc.decRef();
                // если он тоже обнулил счётчик, добавляем в очередь
                if(rc.refCount==0 && !rc.markedForDeletion){
                    rc.markedForDeletion=true;
                    toDestroy.push(rc);
                }
            }
        }
        items.clear();
    }

    @Override
    public String toString(){
        return items.toString();
    }
}
