package com.aki;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient(value = ApiUrlCommon.providerName_api2, fallbackFactory = BackFall2Api.class)
public interface Common2Api {
    /**
     * 获取边界和镜头
     *
     * @param params
     * @return
     */
    @RequestMapping(value = ApiUrlCommon.testApi2Method)
    Map testApi2Method(@RequestBody Map params);
}
