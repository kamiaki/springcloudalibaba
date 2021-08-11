package com.aki;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FallBack implements Common1Api{
    @Override
    public Map testApi1Method(Map params) {
        Map map = new HashMap();
        map.put("降级了", "降级了");
        return map;
    }
}
