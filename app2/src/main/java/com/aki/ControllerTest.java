package com.aki;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ControllerTest{
//    @Autowired
//    private Common1Api common1Api;

    @RequestMapping("test1")
    public Map test1(Map params) {
        Map map = new HashMap();
        map.put("调用者", "APP2");
//        Map returnMap = common1Api.testApi1Method(map);
        return map;
    }
}
