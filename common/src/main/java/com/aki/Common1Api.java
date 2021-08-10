package com.aki;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Map;

@FeignClient(value = ApiUrlCommon.providerName_api1)
public interface Common1Api {
    /**
     * 获取边界和镜头
     *
     * @param params
     * @return
     */
    @RequestMapping(value = ApiUrlCommon.testApi1Method)
    Map testApi1Method(@RequestBody Map params);
}
