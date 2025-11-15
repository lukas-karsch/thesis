package karsch.lukas.mappers;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public interface Mapper<From, To> {
    To map(From from);

    default Set<To> map(Collection<From> from) {
        return from.stream().map(this::map).collect(Collectors.toSet());
    }
}
