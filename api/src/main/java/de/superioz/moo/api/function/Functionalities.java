package de.superioz.moo.api.function;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This class registers and calls {@link RegisteredFunctionality}
 *
 * TODO may be redundant
 */
public final class Functionalities {

    private static Map<String, Functionality> nameFunctionalityMap = new HashMap<>();

    public static boolean register(Object classObject) {
        if(classObject == null) return false;

        for(Method m : classObject.getClass().getDeclaredMethods()) {
            if(!m.isAnnotationPresent(RegisteredFunctionality.class)) continue;
            Functionality functionality = new Functionality(classObject, m);
            nameFunctionalityMap.put(functionality.getName(), functionality);
        }
        return true;
    }

    public static <T> T call(String label, Object... params){
        label = label.toLowerCase();
        Functionality functionality = nameFunctionalityMap.get(label);
        if(functionality == null) throw new NullPointerException("This function does not exist! (name=" + label + ")");

        return functionality.execute(params);
    }

}
