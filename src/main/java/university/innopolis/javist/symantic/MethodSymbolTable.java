package university.innopolis.javist.symantic;

import university.innopolis.javist.errors.SymbolNotFoundException;

import java.util.*;

public class MethodSymbolTable {
    private final String methodName;
    private final String returnType;
    private final Map<String, String> parameterTypes;
    private final Map<String, String> localVariables;

    public MethodSymbolTable(String methodName, String returnType, Map<String, String> parameterTypes) {
        this.methodName = methodName;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.localVariables = new HashMap<>();
    }

    public String getMethodName() {
        return methodName;
    }

    public String getReturnType() {
        return returnType;
    }

    public Map<String, String> getParameterTypes() {
        return parameterTypes;
    }

    public void addLocalVariable(String varName, String varType) {
        localVariables.put(varName, varType);
    }

    public String getLocalVariableType(String varName) throws SymbolNotFoundException {
        if (localVariables.containsKey(varName)) {
            return localVariables.get(varName);
        } else {
            throw new SymbolNotFoundException("Local variable not found: " + varName);
        }
    }

    public String getParameterType(String paramName) throws SymbolNotFoundException {
        if (parameterTypes.containsKey(paramName)) {
            return parameterTypes.get(paramName);
        } else {
            throw new SymbolNotFoundException("Parameter not found: " + paramName);
        }
    }
}
