package com.aki;

import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class Provider1 implements Common1Api{

    @Override
    public Map testApi1Method(Map params) {
        Map map = new HashMap();
        map.put("Common1Api params", params.toString());
        return map;
    }
}
