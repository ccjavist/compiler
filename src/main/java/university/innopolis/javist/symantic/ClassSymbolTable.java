package university.innopolis.javist.symantic;

import java.util.HashMap;
import java.util.Map;

public class ClassSymbolTable {

    private final Map<String, ClassInfo> classes = new HashMap<>();

    public boolean addClass(String name, ClassInfo info) {
        if (classes.containsKey(name)) {
            return false;
        }
        classes.put(name, info);
        return true;
    }

    public ClassInfo getClassInfo(String name) {
        return classes.get(name);
    }

    public boolean containsClass(String name) {
        return classes.containsKey(name);
    }

    public static class ClassInfo {
        private final String name;
        private final String parentName;
        private final Map<String, VariableInfo> variables;
        private final Map<String, MethodInfo> methods;

        public ClassInfo(String name, String parentName) {
            this.name = name;
            this.parentName = parentName;
            variables = new HashMap<>();
            methods = new HashMap<>();
        }

        public boolean addVariable(String name, VariableInfo info) {
            if (variables.containsKey(name)) {
                return false;
            }
            variables.put(name, info);
            return true;
        }

        public VariableInfo getVariableInfo(String name) {
            return variables.get(name);
        }

        public boolean containsVariable(String name) {
            return variables.containsKey(name);
        }

        public boolean addMethod(String name, MethodInfo info) {
            if (methods.containsKey(name)) {
                return false;
            }
            methods.put(name, info);
            return true;
        }

        public MethodInfo getMethodInfo(String name) {
            return methods.get(name);
        }

        public boolean containsMethod(String name) {
            return methods.containsKey(name);
        }

        public String getName() {
            return name;
        }

        public String getParentName() {
            return parentName;
        }

        public Map<String, VariableInfo> getVariables() {
            return variables;
        }

        public Map<String, MethodInfo> getMethods() {
            return methods;
        }
    }

    public static class VariableInfo {
        private final String name;
        private final String typeName;

        public VariableInfo(String name, String typeName) {
            this.name = name;
            this.typeName = typeName;
        }

        public String getName() {
            return name;
        }

        public String getTypeName() {
            return typeName;
        }
    }

    public static class MethodInfo {
        private final String name;
        private final String returnType;
        private final Map<String, String> parameterTypes;

        public MethodInfo(String name, String returnType) {
            this.name = name;
            this.returnType = returnType;
            parameterTypes = new HashMap<>();
        }

        public boolean addParameter(String name, String typeName) {
            if (parameterTypes.containsKey(name)) {
                return false;
            }
            parameterTypes.put(name, typeName);
            return true;
        }

        public String getName() {
            return name;
        }

        public String getReturnType() {
            return returnType;
        }

        public Map<String, String> getParameterTypes() {
            return parameterTypes;
        }
    }

}
