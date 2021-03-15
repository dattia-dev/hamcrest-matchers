package dev.attia.hamcrest.matcher;

import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.stream.Stream;

final class GenericTypeUtils {
    private GenericTypeUtils() {}
    
    static <T> Type[] getGenericTypeParameters(Class<? extends T> of, Class<T> to) {
        Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(of, to);
        if (typeArguments == null) {
            throw new IllegalArgumentException(String.format("" +
                "unable to determine generic parameters of %s to %s",
                of.getName(), to.getName()));
        }
        
        return Stream.of(to.getTypeParameters())
            .map(typeArguments::get)
            .toArray(Type[]::new);
    }
    
    static <T> Class<?>[] getGenericRawTypeParameter(Class<? extends T> of, Class<T> to) {
        return Stream.of(getGenericRawTypeParameter(of, to))
            .map(type -> TypeUtils.getRawType(type, of))
            .toArray(Class<?>[]::new);
    }
    
    @SuppressWarnings("unchecked")
    static <T, S> Class<S> getGenericRawTypeParameter(Class<? extends T> of, Class<T> to, int index) {
        return (Class<S>) getGenericRawTypeParameter(of, to)[index];
    }
}
