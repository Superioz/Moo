package de.superioz.moo.api.function;

import de.superioz.moo.api.utils.ReflectionUtil;
import lombok.Getter;

import java.lang.reflect.Method;

/**
 * Just a wrapper class for a {@link RegisteredFunctionality}, that means the method itself and its annotation
 */
@Getter
public final class Functionality {

    private String name;
    private RegisteredFunctionality function;

    private Object methodsParent;
    private Method method;
    private Class<?>[] parameterTypes;
    private Class<?> returnType;

    public Functionality(Object methodsParent, Method method) {
        this.methodsParent = methodsParent;
        this.method = method;
        this.parameterTypes = method.getParameterTypes();
        this.returnType = method.getReturnType();

        if(!method.isAnnotationPresent(RegisteredFunctionality.class)) {
            return;
        }
        RegisteredFunctionality function = method.getAnnotation(RegisteredFunctionality.class);
        this.function = function;
        this.name = function.name();
        if(this.name.isEmpty()) this.name = method.getName();
        this.name = name.toLowerCase();
    }

    public <T> T execute(Object... params) {
        if(method == null || !checkParameter(params)){
            return null;
        }
        return (T) ReflectionUtil.invokeMethod(method, methodsParent, params);
    }

    private boolean checkParameter(Object... params) {
        if(params.length != parameterTypes.length){
            return false;
        }

        for(int i = 0; i < params.length; i++) {
            Object o = params[i];
            Class<?> c = parameterTypes[i];

            if(!o.getClass().isAssignableFrom(c)) {
                System.out.println(o.getClass() + "; " + c);
                return false;
            }
        }
        return true;
    }

}
