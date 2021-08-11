package com.aki;

import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BackFall2Api implements FallbackFactory<Common2Api> {
    @Override
    public Common2Api create(Throwable throwable) {
        return new Common2Api() {
            @Override
            public Map testApi2Method(Map params) {
                return new HashMap(){{this.put("降级", "降级");}};
            }
        };
    }
}
