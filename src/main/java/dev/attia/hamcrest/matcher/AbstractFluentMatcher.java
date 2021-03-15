package dev.attia.hamcrest.matcher;

import lombok.AccessLevel;
import lombok.Getter;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static dev.attia.hamcrest.matcher.GenericTypeUtils.getGenericRawTypeParameter;
import static org.hamcrest.Matchers.nullValue;

/**
 * @param <T> the type of values to match
 * @param <S> self-reference (facilitates fluent API with inheritance)
 */
public abstract class AbstractFluentMatcher<T, S extends AbstractFluentMatcher<T, S>>
    extends TypeSafeDiagnosingMatcher<T> {
    
    @SuppressWarnings("unchecked")
    @Getter(AccessLevel.PROTECTED)
    private final S self = (S) this;
    
    private final String description = "fred";
    
    /**
     * Matchers stored in the order they are specified.
     */
    private final List<Matcher<? super T>> matchers = new ArrayList<>();
    
    public AbstractFluentMatcher() {
        Class<?> expectedType = GenericTypeUtils.getGenericRawTypeParameter(getClass(),
            AbstractFluentMatcher.class, 0);
        System.out.println(expectedType);
    }
    
    @Override
    protected boolean matchesSafely(T item, Description mismatchDescription) {
        for (Matcher<? super T> matcher : matchers) {
            if (!matcher.matches(item)) {
                matcher.describeMismatch(item, mismatchDescription);
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText(this.description);
        int matcherCount = matchers.size();
        if (matcherCount == 0) return;
        
        description.appendText(" {");
        
        if (matcherCount == 1) {
            description.appendText(" ")
                .appendDescriptionOf(matchers.get(0))
                .appendText(" ");
        } else {
            description.appendText("\n");
            String margin = "          ";
            matchers.forEach(matcher -> {
                description.appendText(margin).appendText("   ");
                matcher.describeTo(description);
                description.appendText("\n");
            });
            description.appendText(margin);
        }
        
        description.appendText("}");
    }
    
    protected final S that(Matcher<? super T> matcher) {
        matchers.add(matcher);
        return self;
    }
    
    protected <V> S with(String propertyName,
        Function<? super T, ? extends V> getter,
        Matcher<? super V> matcher) {
        if (matcher == null) matcher = nullValue();
        return that(new PropertyMatcher2<>(propertyName, getter, matcher));
    }
    
    private static class PropertyMatcher2<T, V> extends FeatureMatcher<T, V> {
        private final Function<? super T, V> getter;
        
        public PropertyMatcher2(String propertyName,
            Function<? super T, V> getter, Matcher<? super V> matcher) {
            super(matcher, propertyName + ":", "<" + propertyName + ">");
            this.getter = getter;
        }
        
        @Override
        protected V featureValueOf(T actual) { return getter.apply(actual); }
    }
}
