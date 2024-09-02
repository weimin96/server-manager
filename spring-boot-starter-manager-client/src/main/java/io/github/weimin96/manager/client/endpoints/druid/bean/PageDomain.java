package io.github.weimin96.manager.client.endpoints.druid.bean;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * describe:
 *
 * @author panwm
 * @since 2024/9/2 16:45
 */
@Data
public class PageDomain<T> {

    protected List<T> records;

    protected long total;

    protected long size;

    protected long current;

    private PageDomain(List<T> records, long total, long size, long current) {
        this.records = records;
        this.total = total;
        this.size = size;
        this.current = current;
    }

    public static <T> PageDomain<T> empty(long size, long current) {
        return new PageDomain<>(new ArrayList<>(), 0, size, current);
    }

    public static <T> PageDomain<T> of(List<T> records, long total, long size, long current) {
        return new PageDomain<>(records, total, size, current);
    }

}
