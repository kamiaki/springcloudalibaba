package com.aki;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class Provider2 implements Common2Api{
    @Value("${myParameter.value1}")
    private String value1;

    @Override
    public Map testApi2Method(Map params) {
        double random = Math.random() * 10;
        if (random >= 5) throw new RuntimeException("降级降级");
        Map map = new HashMap();
        map.put("从调用者传来的参数", params.toString());
        map.put("myParameter.value1", value1);
        return map;
    }
}
