package dev.attia.hamcrest.matcher;

import lombok.Value;
import lombok.val;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public abstract class ReflectiveMatcher<T> extends TypeSafeDiagnosingMatcher<T> {
    @Override
    protected boolean matchesSafely(T item, Description mismatchDescription) {
        for (MatcherField matcherField : getMatcherFields()) {
            String name = matcherField.field.getName();
            BeanInfo beanInfo = getBeanInfo(item.getClass());
            
            Object value = Stream.of(beanInfo.getPropertyDescriptors())
                .filter(propertyDescriptor -> propertyDescriptor.getName().equals(name))
                .findFirst()
                .map(PropertyDescriptor::getReadMethod)
                .map(method -> invoke(method, item))
                .orElseThrow(() -> new RuntimeException(String.format(
                    "failed to read %s of %s", name, item)));
            
            Matcher<?> matcher = matcherField.matcher;
            if (matcher.matches(value)) continue;
            
            mismatchDescription.appendText("<").appendText(name).appendText("> ");
            matcher.describeMismatch(value, mismatchDescription);
            return false;
        }
        
        return true;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText("Bean");
        val matcherFields = getMatcherFields();
        switch (matcherFields.size()) {
            case 0:
                return;
            case 1:
                description.appendText(" { ")
                    .appendDescriptionOf(matcherFields.get(0))
                    .appendText(" ");
                break;
            default:
                val margin = "          ";
                description.appendText(" {\n").appendText(margin);
                for (MatcherField matcherField : matcherFields) {
                    description.appendText("   ")
                        .appendDescriptionOf(matcherField)
                        .appendText("\n").appendText(margin);
                }
        }
        description.appendText("}");
    }
    
    private List<MatcherField> getMatcherFields() {
        List<MatcherField> result = new ArrayList<>();
        for (Field field : getClass().getDeclaredFields()) {
            if (!Matcher.class.isAssignableFrom(field.getType())) continue;
            Matcher<?> matcher = getValue(field, this);
            if (matcher != null) result.add(new MatcherField(matcher, field));
        }
        return result;
    }
    
    @Value
    private static class MatcherField implements SelfDescribing {
        Matcher<?> matcher;
        Field field;
    
        @Override
        public void describeTo(Description description) {
            description.appendText(field.getName())
                .appendText(": ").appendDescriptionOf(matcher);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T getValue(Field field, Object object) {
        boolean accessible = field.isAccessible();
        if (!accessible) field.setAccessible(true);
        try {
            return (T) field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } finally {
            if (!accessible) field.setAccessible(false);
        }
    }
    
    private static Object invoke(Method method, Object object, Object... args) {
        try {
            return method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(String.format(
                "failed method invocation:\n method=%s\n object=%s,\n args=%s",
                method, object, Arrays.toString(args)
            ));
        }
    }
    
    private static BeanInfo getBeanInfo(Class<?> type) {
        try {
            return Introspector.getBeanInfo(type);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
    }
}
