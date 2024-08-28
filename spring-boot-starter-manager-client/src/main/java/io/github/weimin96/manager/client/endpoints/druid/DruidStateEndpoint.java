package io.github.weimin96.manager.client.endpoints.druid;

import com.alibaba.druid.stat.DruidStatManagerFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

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

    public DruidStateEndpoint() {
    }

    /**
     * 获取慢SQL列表
     * @return 慢SQL列表
     */
    @ReadOperation
    public List<Map<String, Object>> getDruidStat() {
        // 获取数据源监控数据，包括SQL的慢SQL信息
        return DruidStatManagerFacade.getInstance().getSqlStatDataList(null);
    }
}
