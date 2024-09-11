package io.github.weimin96.manager.client.endpoints.druid;

import com.alibaba.druid.stat.DruidStatManagerFacade;
import com.alibaba.druid.util.MapComparator;
import io.github.weimin96.manager.client.endpoints.bean.PageDomain;
import io.github.weimin96.manager.client.utils.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author panwm
 * @since 2024/8/28 23:40
 */
@Slf4j
@Component
@Endpoint(id = "druid")
public class DruidStateEndpoint {

    private static final int DEFAULT_CURRENT = 1;
    private static final int DEFAULT_PER_PAGE_SIZE = 25;
    private static final String ORDER_TYPE_DESC = "desc";
    private static final String ORDER_TYPE_ASC = "asc";
    private static final String DEFAULT_ORDER_TYPE = ORDER_TYPE_DESC;
    private static final String DEFAULT_ORDER_BY = "LastTime";

    public DruidStateEndpoint() {
    }

    /**
     * 获取慢SQL列表
     *
     * @return 慢SQL列表
     */
    @ReadOperation
    public PageDomain<Map<String, Object>> getDruidStat() {
        HttpServletRequest request = Util.getRequest();
        Map<String, String> parameters = Util.getParametersAsMap(request);
        return getSqlStatDataList(parameters);
    }

    private PageDomain<Map<String, Object>> getSqlStatDataList(Map<String, String> parameters) {
        Integer dataSourceId = null;

        String dataSourceIdParam = parameters.get("dataSourceId");
        if (dataSourceIdParam != null && dataSourceIdParam.length() > 0) {
            dataSourceId = Integer.parseInt(dataSourceIdParam);
        }

        List<Map<String, Object>> array = DruidStatManagerFacade.getInstance().getSqlStatDataList(dataSourceId);
        return comparatorOrderBy(array, parameters);
    }

    private PageDomain<Map<String, Object>> comparatorOrderBy(List<Map<String, Object>> array,
                                                              Map<String, String> parameters) {
        int page = DEFAULT_CURRENT;
        int perPageCount = DEFAULT_PER_PAGE_SIZE;

        String orderBy, orderType;
        if (parameters == null) {
            orderBy = DEFAULT_ORDER_BY;
            orderType = DEFAULT_ORDER_TYPE;
        } else {
            orderBy = parameters.get("orderBy");
            orderType = parameters.get("orderType");
            String pageParam = parameters.get("current");
            if (pageParam != null && pageParam.length() != 0) {
                page = Integer.parseInt(pageParam);
            }
            String pageCountParam = parameters.get("pageSize");
            if (pageCountParam != null && pageCountParam.length() > 0) {
                perPageCount = Integer.parseInt(pageCountParam);
            }
        }
        if (array.isEmpty()) {
            return PageDomain.empty(perPageCount, page);
        }

        orderBy = orderBy == null ? DEFAULT_ORDER_BY : orderBy;
        orderType = orderType == null ? DEFAULT_ORDER_TYPE : orderType;

        if (!ORDER_TYPE_DESC.equals(orderType)) {
            orderType = ORDER_TYPE_ASC;
        }

        if (orderBy.trim().length() != 0) {
            array.sort(new MapComparator<>(orderBy, ORDER_TYPE_DESC.equals(orderType)));
        }

        int fromIndex = (page - 1) * perPageCount;
        int toIndex = page * perPageCount;
        if (toIndex > array.size()) {
            toIndex = array.size();
        }
        List<Map<String, Object>> maps = array.subList(fromIndex, toIndex);
        return PageDomain.of(maps, array.size(), perPageCount, page);
    }
}
