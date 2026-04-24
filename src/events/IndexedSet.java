package events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An ordered collection, as determined by the collection itself (via use of {@code compareTo}), similar to {@code Set}.
 * The user can view all elements of the list, but can only remove from the head, and all elements added are
 * automatically sorted.
 * @param <E> the type of elements in this list
 */
public class IndexedSet<E> implements Iterable<E>
{
    private final List<E> list = new ArrayList<>();

    /**
     * Add the specified element to this list.  It will be added in the proper position of the ordered list.
     * @param e element to be added to this list
     * @return {@code true} if element was successfully added
     */
    public boolean add(E e)
    {
        if (list.contains(e))
            return false;

        // not found in list
        list.add(e); // TODO better add, into order
        return true;
    }

    /**
     * Retrieve the element at the specified index of this list.
     * @param index index of the element to return
     * @return the element at the specified position in this list
     */
    public E get(int index)
    {
        return list.get(index);
    }

    /**
     * Retrieve the first element of the list.
     * @return the element at the head of the list
     */
    public E peek()
    {
        return list.getFirst();
    }

    /**
     * Return and remove the first element of the list.
     * @return the removed element
     */
    public E poll()
    {
        return list.removeFirst();
    }

    /**
     * Test if the list contains the specified element.
     * @param e the element to test if the list contains
     * @return if the list contains the element
     */
    public boolean contains(E e)
    {
        return list.contains(e);
    }

    /**
     * Retrieve the number of elements in the list.
     * @return number of elements in the list
     */
    public int size()
    {
        return list.size();
    }

    /**
     * Test if the list is empty.
     * @return {@code true} if the list is empty
     */
    public boolean isEmpty()
    {
        return list.isEmpty();
    }

    /**
     * Remove all elements from the list.
     */
    public void clear()
    {
        list.clear();
    }

    @Override
    public Iterator<E> iterator()
    {
        return list.iterator();
    }
}
