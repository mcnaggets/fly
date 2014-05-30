package by.fly.ui.control;

/**
 * A simple interface to get a string representation of a given data.
 *
 * @param <T> the data type
 */
@FunctionalInterface
public interface DataToString<T> {

    /**
     * Returns a string representation of the specified object.
     *
     * @param item the object.
     * @return the specified object string representation.
     */
    String toString(T item);
}
