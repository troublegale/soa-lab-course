package itmo.ivank.soa.util;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

public class SortBuilder {

    public static Sort buildSort(List<String> sortParams, String defaultSort) {
        if (sortParams == null || sortParams.isEmpty()) {
            return Sort.by(Sort.Direction.ASC, defaultSort);
        }
        List<Sort.Order> orders = getOrders(sortParams);
        return Sort.by(orders);
    }

    private static List<Sort.Order> getOrders(List<String> sortParams) {
        List<Sort.Order> orders = new ArrayList<>();
        for (String sortParam : sortParams) {
            boolean negative = sortParam.startsWith("-");
            if (negative) sortParam = sortParam.substring(1);
            Sort.Direction direction = negative ? Sort.Direction.DESC : Sort.Direction.ASC;
            orders.add(new Sort.Order(direction, sortParam));
        }
        return orders;
    }

}
