package com.aki;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ControllerTest {
    @Autowired
    private Common2Api common2Api;

    @RequestMapping("test1")
    public Map test1(Map params) {
        Map map = new HashMap();
        map.put("调用者", "APP1");
        Map returnMap = common2Api.testApi2Method(map);
        return returnMap;
    }
}