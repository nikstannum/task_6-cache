package ru.clevertec.web.util.paging;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Util class for converting the page and page size into a dataset for the SQL query
 */
@RequiredArgsConstructor
public class PagingUtil {

    private static final String PAGE = "page";
    private static final String SIZE = "size";
    private final int defaultSize;

    public Paging getPaging(HttpServletRequest req) {
        String sizeStr = req.getParameter(SIZE);
        int limit;
        if (sizeStr == null) {
            limit = defaultSize;
        } else {
            limit = Integer.parseInt(sizeStr);
        }
        if (limit > defaultSize) {
            limit = defaultSize;
        }
        String pageStr = req.getParameter(PAGE);
        long page;
        if (pageStr == null) {
            page = 1;
        } else {
            page = Long.parseLong(pageStr);
        }
        long offset = (page - 1) * limit;
        return new Paging(limit, offset);
    }

    public record Paging(int limit, long offset) {
    }
}
