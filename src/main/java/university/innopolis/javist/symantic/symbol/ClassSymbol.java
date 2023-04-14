package university.innopolis.javist.symantic.symbol;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class ClassSymbol extends Symbol {

    @Setter
    private String parentClassName;

    @Setter
    private ClassSymbol parentClass = null;

    private final Map<String, Set<MethodSymbol>> methods = new HashMap<>();
    private final Set<ConstructorSymbol> constructors = new HashSet<>();
    private final Map<String, VariableSymbol> variables = new HashMap<>();

    public ClassSymbol(String name, String parentClass) {
        super(name);
        this.parentClassName = parentClass;
    }

    public boolean isMethodExists(String methodName, List<ParameterSymbol> parameters) {
        if (!this.methods.containsKey(methodName)) {
            if (this.parentClass != null) {
                return this.parentClass.isMethodExists(methodName, parameters);
            }
            return false;
        }
        for (MethodSymbol method : methods.get(methodName)) {
            if (method.equals(methodName, parameters)) {
                return true;
            }
        }
        if (this.parentClass != null) {
            this.parentClass.isMethodExists(methodName, parameters);
        }
        return false;
    }

    public MethodSymbol methodLookup(String methodName, List<ParameterSymbol> parameters) {
        if (!this.methods.containsKey(methodName)) {
            if (this.parentClass != null) {
                return this.parentClass.methodLookup(methodName, parameters);
            }
            return null;
        }
        for (MethodSymbol method : methods.get(methodName)) {
            if (method.equals(methodName, parameters)) {
                return method;
            }
        }
        if (this.parentClass != null) {
            return this.parentClass.methodLookup(methodName, parameters);
        }
        return null;
    }

    public boolean isConstructorExists(List<ParameterSymbol> parameters) {
        if (this.constructors.contains(new ConstructorSymbol(parameters))) {
            return true;
        }
        if (this.parentClass != null) {
            return this.parentClass.isConstructorExists(parameters);
        }
        return false;
    }

    public ConstructorSymbol constructorLookup(List<ParameterSymbol> parameters) {
        for (ConstructorSymbol constructor : this.constructors) {
            if (constructor.getParameters().equals(parameters)) {
                return constructor;
            }
        }
        if (this.parentClass != null) {
            return this.parentClass.constructorLookup(parameters);
        }
        return null;
    }

    public boolean isVariableExists(String variableName) {
        if (!this.variables.containsKey(variableName)) {
            if (this.parentClass != null) {
                return this.parentClass.isVariableExists(variableName);
            }
            return false;
        }
        return true;
    }

    public VariableSymbol variableLookup(String variableName) {
        if (this.variables.get(variableName) == null) {
            if (this.parentClass != null) {
                return this.parentClass.variableLookup(variableName);
            }
        }
        return this.variables.get(variableName);
    }

    public void addMethod(MethodSymbol method) {
        Set<MethodSymbol> methods = this.methods.computeIfAbsent(method.getName(), k -> new HashSet<>());
        methods.add(method);
    }
}
