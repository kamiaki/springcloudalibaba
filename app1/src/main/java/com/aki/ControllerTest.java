package com.aki;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
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
    private static final String degradeRule1 = "degradeRule1";
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
        } catch (BlockException e1) {
            System.out.println("block!");
            Map map2 = new HashMap();
            map2.put("block", "block");
            return map2;
        } catch (Exception ex) {
            Tracer.traceEntry(ex, entry);
        } finally {
            if (entry != null) {
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
    public String test2() {
        return "我是测试2";
    }

    /**
     * 方法 必须在同类中 必须是public 返回值和原方法保持一致
     * 可以加参数， 可以在别的类中，要写那个类为参数，方法必须为静态
     *
     * @param ex
     * @return
     */
    public String blockHandlerForTest2(BlockException ex) {
        ex.printStackTrace();
        return "流控了";
    }

    // 测试降级
    /**
     * PS：这里有个需要注意的知识点，就是 SphU.entry 方法的第二个参数 EntryType 说的是这次请求的流量类型，共有两种类型：IN 和 OUT 。
     * IN：是指进入我们系统的入口流量，比如 http 请求或者是其他的 rpc 之类的请求。
     * OUT：是指我们系统调用其他第三方服务的出口流量。
     * 入口、出口流量只有在配置了系统规则时才有效。
     * 设置 Type 为 IN 是为了统计整个系统的流量水平，防止系统被打垮，用以自我保护的一种方式。
     * @return
     */
    @RequestMapping(value = "test3")
    @SentinelResource(value = degradeRule1, blockHandler = "blockHandlerForTest3", entryType = EntryType.IN)
    public String test3() {
        if (true) throw new RuntimeException("降级异常");
        return "我是测试3";
    }

    public String blockHandlerForTest3(BlockException ex) {
        ex.printStackTrace();
        return "降级了";
    }

    @PostConstruct
    private static void initFlowRules() {
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

    //降级规则
    @PostConstruct
    public void initDegradeRule() {
        List<DegradeRule> degradeRules = new ArrayList<>();
        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource(degradeRule1);
        // 异常数降级
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        // 发生异常数
        degradeRule.setCount(2);
        // 多长时间内出现异常
        degradeRule.setStatIntervalMs(60 * 1000);
        // 时间窗口10s 这个时间内都降级，之后如果第一次就出错直接降级 不会根据条件判断了
        degradeRule.setTimeWindow(10);
        // 触发熔断的最少请求数
        degradeRule.setMinRequestAmount(2);
        degradeRules.add(degradeRule);
        DegradeRuleManager.loadRules(degradeRules);

    }
}
