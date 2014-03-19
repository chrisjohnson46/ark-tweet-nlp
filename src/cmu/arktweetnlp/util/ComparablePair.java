package cmu.arktweetnlp.util;
import java.lang.Comparable;

public class ComparablePair<T1, T2> implements Comparable<ComparablePair<T1, T2>> {
    public T1 first;
    public T2 second;
    public ComparablePair(T1 x, T2 y) { first=x; second=y; }
    public int compareTo(ComparablePair<T1, T2> other) {
        int firstComp = ((Comparable)first).compareTo(other.first);
        int secondComp = ((Comparable)second).compareTo(other.second);
        if (firstComp == 0) return secondComp;
        return firstComp;
    }
    public boolean equals(ComparablePair<T1, T2> other) {
        boolean ret = first == other.first; 
        ret &= second == other.second;
        return ret;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", first.toString(), second.toString());
    }
}
