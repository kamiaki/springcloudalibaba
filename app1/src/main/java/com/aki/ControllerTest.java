package com.aki;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ControllerTest {
    private static final String ruleName1 = "ruleName1";
    private static final String ruleName2 = "ruleName2";
    @Autowired
    private Common2Api common2Api;

    @RequestMapping("test1")
    public Map test1(Map params) {
        Entry entry = null;
        try {
            entry = SphU.entry(ruleName1);
            String str = "hello world";
            System.out.println("=====-==-----==");
            System.out.println(str);
        }catch (BlockException e1) {
            System.out.println("block!");
            Map map2 = new HashMap();
            map2.put("block", "block");
            return map2;
        }catch (Exception ex) {
            Tracer.traceEntry(ex, entry);
        }finally {
            if (entry != null){
                entry.exit();
            }
        }


        Map map = new HashMap();
        map.put("调用者", "APP1");
        Map returnMap = common2Api.testApi2Method(map);
        return returnMap;
    }

    @RequestMapping(value = "test2")
    @SentinelResource(value = ruleName2, blockHandler = "blockHandlerForTest2")
    public String test2(){
        return "我是测试2";
    }

    /**
     * 方法 必须在同类中 必须是public 返回值和原方法保持一致
     * 可以加参数， 可以在别的类中，要写那个类为参数，方法必须为静态
     * @param ex
     * @return
     */
    public String blockHandlerForTest2(BlockException ex){
        ex.printStackTrace();
        return "流控了";
    }


    @PostConstruct
    private static void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();
        //流控
        FlowRule rule = new FlowRule();
        rule.setResource(ruleName1);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(1);
        rules.add(rule);

        FlowRule rule2 = new FlowRule();
        rule2.setResource(ruleName2);
        rule2.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule2.setCount(1);
        rules.add(rule2);

        FlowRuleManager.loadRules(rules);
    }
}
