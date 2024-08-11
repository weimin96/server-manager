package io.github.weimin96.manager.server.domain.value;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * @author panwm
 * @since 2024/8/3 22:30
 */
@lombok.EqualsAndHashCode
@lombok.ToString
public class Endpoints implements Iterable<Endpoint>, Serializable {

    private final Map<String, Endpoint> endpoints;

    private static final Endpoints EMPTY = new Endpoints(Collections.emptyList());

    private Endpoints(Collection<Endpoint> endpoints) {
        if (endpoints.isEmpty()) {
            this.endpoints = Collections.emptyMap();
        }
        else {
            this.endpoints = endpoints.stream().collect(toMap(Endpoint::getId, Function.identity()));
        }
    }

    public Optional<Endpoint> get(String id) {
        return Optional.ofNullable(this.endpoints.get(id));
    }

    public boolean isPresent(String id) {
        return this.endpoints.containsKey(id);
    }

    @Override
    public Iterator<Endpoint> iterator() {
        return new UnmodifiableIterator<>(this.endpoints.values().iterator());
    }

    public static Endpoints empty() {
        return EMPTY;
    }

    public static Endpoints single(String id, String url) {
        return new Endpoints(Collections.singletonList(Endpoint.of(id, url)));
    }

    public static Endpoints of(Collection<Endpoint> endpoints) {
        if (endpoints == null || endpoints.isEmpty()) {
            return empty();
        }
        return new Endpoints(endpoints);
    }

    public Endpoints withEndpoint(String id, String url) {
        Endpoint endpoint = Endpoint.of(id, url);
        HashMap<String, Endpoint> newEndpoints = new HashMap<>(this.endpoints);
        newEndpoints.put(endpoint.getId(), endpoint);
        return new Endpoints(newEndpoints.values());
    }

    public Stream<Endpoint> stream() {
        return this.endpoints.values().stream();
    }

    private static final class UnmodifiableIterator<T> implements Iterator<T> {

        private final Iterator<T> delegate;

        private UnmodifiableIterator(Iterator<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return this.delegate.hasNext();
        }

        @Override
        public T next() {
            return this.delegate.next();
        }

    }
}
