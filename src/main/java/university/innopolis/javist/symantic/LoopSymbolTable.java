package university.innopolis.javist.symantic;

import java.util.*;

public class LoopSymbolTable {
    private final Deque<String> loopStack;

    public LoopSymbolTable() {
        loopStack = new ArrayDeque<>();
    }

    public void enterLoop(String loopId) {
        loopStack.push(loopId);
    }

    public void exitLoop() {
        loopStack.pop();
    }

    public boolean isInLoop() {
        return !loopStack.isEmpty();
    }

    public String getCurrentLoopId() {
        return loopStack.peek();
    }
}
