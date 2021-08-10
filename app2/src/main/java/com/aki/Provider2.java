package com.aki;

import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class Provider2 implements Common2Api{

    @Override
    public Map testApi2Method(Map params) {
        Map map = new HashMap();
        map.put("Common2Api params", params.toString());
        return map;
    }
}
