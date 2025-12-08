package karsch.lukas.mappers;

import java.util.*;
import java.util.stream.Collectors;

public interface Mapper<From, To> {
    To map(From from);

    default Set<To> map(Collection<From> from) {
        return from.stream().map(this::map).collect(Collectors.toSet());
    }

    default SortedSet<To> mapToSortedSet(SortedSet<From> from, Comparator<? super To> comparator) {
        return from.stream().map(this::map).collect(Collectors.toCollection(() -> new TreeSet<>(comparator)));
    }

    default List<To> mapToList(Collection<From> from) {
        return from.stream().map(this::map).toList();
    }

    default List<To> mapToList(SortedSet<From> from) {
        return from.stream().map(this::map).toList();
    }
}
