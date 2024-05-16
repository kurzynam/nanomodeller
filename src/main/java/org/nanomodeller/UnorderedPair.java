package org.nanomodeller;


public class UnorderedPair<K, V> {
    final K first;
    final V second;

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    public UnorderedPair(K first, V second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UnorderedPair))
            return false;
        UnorderedPair<K, V> up = (UnorderedPair<K, V>) o;
        return (up.first == this.first && up.second == this.second) ||
                (up.first == this.second && up.second == this.first);
    }

    @Override
    public int hashCode() {
        int hashFirst = first.hashCode();
        int hashSecond = second.hashCode();
        int maxHash = Math.max(hashFirst, hashSecond);
        int minHash = Math.min(hashFirst, hashSecond);
        return minHash * 31 + maxHash;
    }
}