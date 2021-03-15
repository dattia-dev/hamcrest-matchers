package dev.attia.hamcrest.matcher;

import lombok.RequiredArgsConstructor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.function.Function;

/**
 * @param <T> the owning type
 * @param <V> the value type
 */
@RequiredArgsConstructor
class PropertyMatcher<T, V> extends TypeSafeDiagnosingMatcher<T> {
    
    private final String propertyName;
    
    private final Function<? super T, ? extends V> valueGetter;
    
    private final Matcher<? super V> valueMatcher;
    
    @Override
    protected boolean matchesSafely(T item, Description mismatchDescription) {
        V value = valueGetter.apply(item);
        if (valueMatcher.matches(value)) return true;
    
        mismatchDescription.appendText("<")
            .appendText(propertyName).appendText("> ");
        valueMatcher.describeMismatch(value, mismatchDescription);
        
        return false;
    }
    
    @Override
    public void describeTo(Description description) {
        description.appendText(propertyName).appendText(": ")
            .appendDescriptionOf(valueMatcher);
    }
}
