package ru.clevertec.web.util.paging;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Util class for converting the page and page size into a dataset for the SQL query
 */
@RequiredArgsConstructor
@Component
public class PagingUtil {

    @Value("${pagination.defaultSize}")
    private String defaultSize;

    public Paging getPaging(Integer page, Integer size) {
        int limit;
        int defSize = Integer.parseInt(defaultSize);
        if (size == null || size > defSize) {
            limit = defSize;
        } else {
            limit = size;
        }
        if (page == null || page < 1) {
            page = 1;
        }
        long offset = (long) (page - 1) * limit;
        return new Paging(limit, offset);
    }

    public record Paging(int limit, long offset) {
    }
}
