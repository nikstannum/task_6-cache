package ru.clevertec.controller.util.paging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Util class for converting the page and page size into a dataset for the SQL query
 */
@RequiredArgsConstructor
public class PagingUtil {

    private final int defaultSize;

    public Paging getPaging(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        int limit;
        if (size < 1 || size > defaultSize) {
            limit = defaultSize;
        } else {
            limit = size;
        }
        long offset = (long) (page - 1) * limit;
        return new Paging(limit, offset);
    }

    @Getter
    public static class Paging {
        private final int limit;
        private final long offset;

        public Paging(int limit, long offset) {
            this.limit = limit;
            this.offset = offset;
        }
    }
}
