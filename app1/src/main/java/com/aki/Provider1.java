package com.aki;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class Provider1 implements Common1Api{
    @Value("${myParameter.value1}")
    private String value1;

    @Override
    public Map testApi1Method(Map params) {
        Map map = new HashMap();
        map.put("从调用者传来的参数", params.toString());
        map.put("myParameter.value1", value1);
        return map;
    }
}
