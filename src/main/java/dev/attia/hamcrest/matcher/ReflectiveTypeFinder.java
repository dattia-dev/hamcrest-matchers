package dev.attia.hamcrest.matcher;

import java.lang.reflect.Method;

public class ReflectiveTypeFinder {
    private final String methodName;
    private final int expectedNumberOfParameters;
    private final int typedParameter;
    
    public ReflectiveTypeFinder(String methodName, int expectedNumberOfParameters, int typedParameter) {
        this.methodName = methodName;
        this.expectedNumberOfParameters = expectedNumberOfParameters;
        this.typedParameter = typedParameter;
    }
    
    public Class<?> findExpectedType(Class<?> fromClass) {
        for (Class<?> c = fromClass; c != Object.class; c = c.getSuperclass()) {
            for (Method method : c.getDeclaredMethods()) {
                if (canObtainExpectedTypeFrom(method)) {
                    return expectedTypeFrom(method);
                }
            }
        }
        throw new Error("Cannot determine correct type for " + methodName + "() method.");
    }
    
    /**
     * @param method The method to examine.
     * @return true if this method references the relevant type
     */
    private boolean canObtainExpectedTypeFrom(Method method) {
        return method.getName().equals(methodName)
            && method.getParameterTypes().length == expectedNumberOfParameters
            && !method.isSynthetic();
    }
    
    
    /**
     * @param method The method from which to extract
     * @return The type we're looking for
     */
    private Class<?> expectedTypeFrom(Method method) {
        return method.getParameterTypes()[typedParameter];
    }
}
