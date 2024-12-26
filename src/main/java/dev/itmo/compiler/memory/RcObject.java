package dev.itmo.compiler.memory;

import java.util.Stack;

public abstract class RcObject {
    protected int refCount = 0;
    protected boolean markedForDeletion = false;

    public void incRef() {
        refCount++;
    }

    public void decRef() {
        if(refCount <= 0){
            throw new IllegalStateException("refCount already 0");
        }
        refCount--;
        if(refCount == 0) {
            if(markedForDeletion) {
                collectGarbage();
                markedForDeletion = false;
            } else {
                markedForDeletion = true;
            }
        }
    }

    private void collectGarbage(){
        Stack<RcObject> st = new Stack<>();
        st.push(this);
        while(!st.isEmpty()){
            RcObject obj = st.pop();
            obj.destroy(st);
        }
    }

    public int getRefCount() {
        return refCount;
    }

    protected abstract void destroy(Stack<RcObject> toDestroy);
}



