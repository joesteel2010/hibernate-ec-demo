package uk.co.jste.echibernate.misc;

import org.eclipse.collections.impl.collection.mutable.AbstractCollectionAdapter;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Simple utility class to unwrap the delegate from an EC adapter type
 */
public final class EcAdapterUnwrapper {

  private static Method getDelegateMethod = null;

  @SuppressWarnings("unchecked")
  public static <T, I extends Iterable<T>> I unwrap(I input) {
    if (!(input instanceof AbstractCollectionAdapter<?>)) {
      return input;
    }

    if (getDelegateMethod == null) {
      getDelegateMethod = ReflectionUtils.findMethod(AbstractCollectionAdapter.class, "getDelegate");
      Objects.requireNonNull(getDelegateMethod, "getDelegate method not found on AbstractCollectionAdapter");
      ReflectionUtils.makeAccessible(getDelegateMethod);
    }

    return (I) ReflectionUtils.invokeMethod(getDelegateMethod, input);
  }
}
