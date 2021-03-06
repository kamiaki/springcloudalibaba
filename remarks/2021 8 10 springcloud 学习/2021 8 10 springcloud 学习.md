# 2021 8 10 springcloud

## 版本一定要对应上

```
<spring-boot.version>2.3.2.RELEASE</spring-boot.version>
<spring-cloud.version>Hoxton.SR8</spring-cloud.version>
<spring-cloud-alibaba.version>2.2.3.RELEASE</spring-cloud-alibaba.version>
```





## idea 构建 阿里云想到初始化方式

![1628566349579](2021 8 10 springcloud 学习.assets/1628566349579.png)



## 安装 nacos

```shell
#Clone 项目 https://nacos.io/zh-cn/index.html
git clone https://github.com/nacos-group/nacos-docker.git
cd nacos-docker

#单机模式 Derby
#将文件里面的版本改为 latest
docker-compose -f example/standalone-derby.yaml up

#其他模式
# https://nacos.io/zh-cn/docs/quick-start-docker.html

#登录页面
http://192.168.80.128:8848/nacos/#/login

```

springboot参数

保护阈值：你设置0.5 ，健康实例 / 总实例 < 0.5 ,他会把不健康的实例也拿来用。0的话就是不会把不健康的拿来用。

![1628581325081](2021 8 10 springcloud 学习.assets/1628581325081.png)

```yaml
server:
  port: 8010
spring:
  application:
    # nacos服务名
    name: app1-service
  cloud:
    nacos:
      server-addr: 192.168.80.128:8848
      discovery:
        username: nacos
        password: nacos
        # 服务列表 命名空间
        namespace: public
        # 永久实例 false 宕机了也不会删除实例 不是临时实例
        ephemeral: false
```



## nacos集群搭建

```shell
# 修改example/cluster-hostname.yaml 里面的版本号  latest
# 此配置是本机集群
docker-compose -f example/cluster-hostname.yaml up 

# 此配置是跨ip集群
docker-compose -f example/cluster-ip.yaml up 
#配置 互相发现ip 在这里
cat ../env/nacos-ip.env 


# 启动nginx
#docker-compose
version: "3.1"
services:
  nginx_nacos:
    container_name: "nginx_nacos"
    image: nginx
    ports:
      - 8838:8838
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    network_mode: "host"

#nginx.conf
worker_processes  1;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    client_max_body_size 500m;
    keepalive_timeout  1200;

	#后端地址
	upstream nacos{

		server 192.168.80.128:8848  weight=1 max_fails=2 fail_timeout=10s;
		server 192.168.80.128:8849  weight=1 max_fails=2 fail_timeout=10s;
		server 192.168.80.128:8850  weight=1 max_fails=2 fail_timeout=10s;

	}


    server{
        listen  8838;
        server_name 192.168.80.128;
        location / {
            proxy_pass http://nacos;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header REMOTE-HOST $remote_addr;
            add_header X-Cache $upstream_cache_status;
            add_header Cache-Control no-cache;
        }
    }
}
```



## openFeign

日志

![1628590373122](2021 8 10 springcloud 学习.assets/1628590373122.png)

契约配置

超时配置

自定义拦截



## nacos配置中心

### 配置流程

1.定义命名空间，命名空间ID 和 命名空间名都要设置成命名空间名，如dev，描述可以随便写。

2.新建配置，group是项目名，dataid是服务名相同 ，项目启动会先拿到applicationname，在启动bootstarp.yml

3.如果想配置多环境，就克隆项目配置

4.更多按钮，里面可以回滚配置文件的版本

![1628651526033](2021 8 10 springcloud 学习.assets/1628651526033.png)

### 开启权限  这个文件里可以改开启权限，在nacos容器里改

![1628652030220](2021 8 10 springcloud 学习.assets/1628652030220.png)



### 改一个配置文件的配置

nacos配置

![1628660782663](2021 8 10 springcloud 学习.assets/1628660782663.png)

```yaml
#yaml
# 配置中心的配置写在这里
spring:
  cloud:
    nacos:
      server-addr: 192.168.80.128:8848
      username: nacos
      password: nacos
      config:
        namespace: dev
        # 扩展名
        file-extension: yaml
        # 禁用和启用 配置中心
        enabled: true


#java
// 测试gitee提交1
@SpringBootApplication
// nacos 客户端
@EnableDiscoveryClient
@EnableFeignClients //feign
public class App1Application {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(App1Application.class, args);
        String property = run.getEnvironment().getProperty("myParameter.value1");
        System.out.println("=================");
        System.out.println(property);
    }
}
```



### profile配置----------不推荐

必须写 yaml扩展名

nacos配置中心 ，命名为：服务名-环境.扩展名，的方式。不建议这样，应该用命名空间作区分。

![1628660545958](2021 8 10 springcloud 学习.assets/1628660545958.png)

这个是 app2-service-dev.yaml 是这样写的，里面要有commons的引用

![1628660661689](2021 8 10 springcloud 学习.assets/1628660661689.png)

分类方式

![1628661791719](2021 8 10 springcloud 学习.assets/1628661791719.png)



### 修改默认 dataid 自定义 dataid

实际例子 必须写 .yaml

![1630774104306](2021 8 10 springcloud 学习.assets/1630774104306.png)

```
# 配置中心的配置写在这里 bootstarp.yml
spring:
  cloud:
    nacos:
      server-addr: 192.168.80.128:8848
      username: nacos
      password: nacos
      config:
        namespace: dev
        group: changsha
        file-extension: yaml
        # 禁用和启用 配置中心
        enabled: true
        shared-configs[0]:
          data-id: myconfig.yaml
          refresh: true
          group: changsha

```



### 优先级 

文件一定要有扩展名！ 比如 yaml 或 properties

data-id下标越大 优先级越高， refresh是是否刷新 

![1628662377527](2021 8 10 springcloud 学习.assets/1628662377527.png)

优先级最大的

![1628662505911](2021 8 10 springcloud 学习.assets/1628662505911.png)

@value 无法感知修改之后的值



## sentinel

### docker 启动

```yaml
#docker run
docker run --name sentinel --restart=always -p 8858:8858 -d bladex/sentinel-dashboard 

#docker-compose
version: "3.1"
services:
  sentinel:
    container_name: "sentinel"
    image: bladex/sentinel-dashboard
    restart: always
    ports:
      - 8858:8858
```

 访问

http://192.168.80.128:8858/#/login

账号密码sentinel



### 被调用方 被调用之后出现在界面上

![1628668127332](2021 8 10 springcloud 学习.assets/1628668127332.png)



### 流控方式

1.qps流控方式

代码方式

```java
#pom
    <!--sentinel-->
        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-core</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.csp</groupId>
            <artifactId>sentinel-annotation-aspectj</artifactId>
        </dependency>
        
    
#java  直接定义
@RestController
public class ControllerTest {
    private static final String ruleName = "test1";
    @Autowired
    private Common2Api common2Api;

    @RequestMapping("test1")
    public Map test1(Map params) {
        Entry entry = null;
        try {
            entry = SphU.entry(ruleName);
            String str = "hello world";
            System.out.println("=====-==-----==");
            System.out.println(str);
        }catch (BlockException e1) {
        // 限流了
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

    @PostConstruct
    private static void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();
        //流控
        FlowRule rule = new FlowRule();
        rule.setResource(ruleName);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(1);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }
}

```



### 注解

```java
///////////////////////// java 注解
   // 注解支持的配置Bean 
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
    
	//////////////////////////////////////需要 处理的方法
    /**
     * 获取数据到报率可用性-图表和表格
     *
     * @param params
     * @return
     */
    @AddLog
    @RequestMapping(value = "detail1/axiosGetDetail1ChartTable")
    @SentinelResource(value = SentinelConfig.ruleName1, blockHandler = "blockHandlerFor1", fallback = "fallback1",
    blockHandlerClass = {DetailControllerEEE.class}, fallbackClass = {DetailControllerEEE.class})
    public ReturnMsg axiosGetDetail1ChartTable(@RequestBody Map params) {
//        int a = 1/0;
        Map map = detailApi.axiosGetDetail1ChartTable(params);
        ReturnMsg returnMsg = new ReturnMsg(EnumMsg.SUCCESS_200);
        returnMsg.setData(map);
        return returnMsg;
    }

///////////////////   这个是兜底方法
@Component
@Slf4j
public class DetailControllerEEE {

    /**
     * fallback的方法
     * 方法如果在同类，必须是public，方法在别的类，必须是static，并且SentinelResource要有参数， blockHandlerClass = {DetailControllerEEE.class}
     * 方法的参数 和 返回值，要和原方法一直
     *
     * @param ex
     * @return
     */
    public static ReturnMsg fallback1(@RequestBody Map params, Throwable ex) {
        log.error(ex.getMessage());
        ReturnMsg returnMsg = new ReturnMsg(EnumMsg.FAIL_909);
        returnMsg.setMsg("出错了");
        return returnMsg;
    }

    /**
     * blockHandler方法
     * 方法如果在同类，必须是public，方法在别的类，必须是static，并且SentinelResource要有参数，fallbackClass = {DetailControllerEEE.class}
     * 方法的参数 和 返回值，要和原方法一直
     *
     * @param ex
     * @return
     */
    public static ReturnMsg blockHandlerFor1(@RequestBody Map params, BlockException ex) {
        log.error(ex.getMessage());
        ReturnMsg returnMsg = new ReturnMsg(EnumMsg.FAIL_909);
        returnMsg.setMsg("流控了");
        return returnMsg;
    }
}


 /////////////////////配置类 配置规则
@Configuration
public class SentinelConfig {
    public static final String ruleName1 = "ruleName1";
    public static final String ruleName2 = "ruleName2";
    public static final String degradeRule1 = "degradeRule1";

    // 注解支持的配置Bean
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }

    @PostConstruct
    private void initFlowRules() {
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
    private void initDegradeRule() {
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

```







2.并发线程流控方式



降级处理

```java
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
```





### 异常处理 被限流抛异常 熔断统一处理

### blockexception 统一异常处理



各种规则

![1628668825106](2021 8 10 springcloud 学习.assets/1628668825106.png)



### springboot 整合 openfeign 一定注意springcloud版本 SR8

设置好 fallback之后，就是设置兜底方法，就可以在界面上配置了，要用 @SentinelResource(fallback = "fallback1",fallbackClass = {DetailControllerEEE.class}) 报错接口

```yaml
#版本
  <spring-boot.version>2.3.2.RELEASE</spring-boot.version>
        <spring-cloud.version>Hoxton.SR8</spring-cloud.version>
        <spring-cloud-alibaba.version>2.2.3.RELEASE</spring-cloud-alibaba.version>
#feign
feign:
  sentinel:
    enabled: true
    
    
#pom
<!--            maven 版本冲突解决方案-->
<dependency>
	<groupId>org.hibernate</groupId>
	<artifactId>hibernate-validator</artifactId>
	<version>5.2.4.Final</version>
</dependency>

 <!--sentinel-->
 <dependency>
 <groupId>com.alibaba.cloud</groupId>
 <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
 </dependency>
 
 #java
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


package com.aki;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@FeignClient(value = ApiUrlCommon.providerName_api2, fallbackFactory = BackFall2Api.class)
public interface Common2Api {
    /**
     * 获取边界和镜头
     *
     * @param params
     * @return
     */
    @RequestMapping(value = ApiUrlCommon.testApi2Method)
    Map testApi2Method(@RequestBody Map params);
}

```



### 整合 springcloud

```yaml
spring:
  profiles:
    include: commons, database

  cloud:
    sentinel:
      transport:
        dashboard: 192.168.80.128:8858
```

### 注解方式

```java
// 注解支持的配置Bean
@Bean
public SentinelResourceAspect sentinelResourceAspect() {
    return new SentinelResourceAspect();
}
```





持久化

用nacos 来做持久化

![在这里插入图片描述](2021 8 10 springcloud 学习.assets/20201231143703869.png) 

```java
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-datasource-nacos</artifactId>
    <version>1.8.0</version>
</dependency>

spring:
  cloud:
    sentinel:
      datasource:
        # 名字随意
        ds:
          nacos:
            # nacos的访问地址，，根据上面准备工作中启动的实例配置
            server-add: 192.168.22.71:8848
            # nacos中存储规则的groupId
            groupId: DEFAULT_GROUP
            # nacos中存储规则的dataId
            dataId: ${spring.application.name}-rules
            # 用来定义存储的规则类型
            rule-type: flow
            data-type: json

[
    {
        "resource": "/test/test",
        "limitApp": "default",
        "grade": 1,
        "count": 5,
        "strategy": 0,
        "controlBehavior": 0,
        "clusterMode": false
    }
]

流控配置注解
resource: 需要限流的接口路径
limitApp: 流控针对的调用来源，若为 default 则不区分调用来源
grade: 限流阈值类型（QPS 或并发线程数）；0代表根据并发数量来限流，1代表根据QPS来进行流量控制
count: 限流阈值
strategy: 调用关系限流策略
controlBehavior: 流量控制效果（直接拒绝、Warm Up、匀速排队）
clusterMode: 是否为集群模式
到这里为止我们的准备工作都已经做好了，接下来我们可以测试了。测试之前，由于我们添加了很多配置，我们将nacos、sentinel和项目重启一遍，然后准备访问接口
```



## 整合冲突的jar包

```
 
            <dependency>
                <groupId>com.alibaba</groupId>
                <artifactId>fastjson</artifactId>
                <version> 1.2.4</version>
            </dependency>
 

        <dependency>
            <groupId>io.github.swagger2markup</groupId>
            <artifactId>swagger2markup</artifactId>
<version>1.3.3 </version>
        </dependency>
```







## Seata 

```
at模式 逆向sql
tcc模式 try confirm Cancel
```

一：搭建seata-server

   1.下载 seata-server  

​      [https://github.com/seata/seata/tree/1.4.0](https://links.jianshu.com/go?to=https%3A%2F%2Fgithub.com%2Fseata%2Fseata%2Ftree%2F1.4.0)

   2.修改file.confi和registry.conf .  

​                   路径如下

![img](2021 8 10 springcloud 学习.assets/15683428-4c78c1fc1f53ceb2.webp) 

​             2-1.file.conf中的修改内容如下。

![img](2021 8 10 springcloud 学习.assets/15683428-4aa3f9a292dde7bd.webp) 

​              3-1.修改registry 文件

 ![img](2021 8 10 springcloud 学习.assets/15683428-61076e3319ebf826.webp) 

 

每个表添加  undo log 回滚表，逆向sql

![img](2021 8 10 springcloud 学习.assets/15683428-f36ade00fb822a7a.webp) 



配置yml

![1628688042007](2021 8 10 springcloud 学习.assets/1628688042007.png)



java

![1628688056836](2021 8 10 springcloud 学习.assets/1628688056836.png)



原理

生成全局事务 xid 通过xid 串联起所有事务

## gateway 

### gateway一般使用

```java
    <!--sentinel-->
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
        </dependency>
        

	<!--gateway网关的依赖-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        
           
            ////yml
# nacos          
spring:
  cloud:
    nacos:
      server-addr: 192.168.80.128:8848
      discovery:
        username: nacos
        password: nacos
        # 服务列表 命名空间
        namespace: dev
        # 非临时服务
        ephemeral: false
    gateway:
      discovery:
        locator:
          enabled: true  #开启自动代理
          
    sentinel:
      transport:
        dashboard: 192.168.80.128:8858
        port: 8858
          
      # 超时
      httpclient:
        connect-timeout: 6000
        response-timeout: 60s
      # 跨域
      globalcors:
        corsConfigurations:
          '[/**]':
            allowCredentials: true
            allowedOrigins: "*"
            allowedHeaders: "*"
            allowedMethods: "*"
            maxAge: 3628800
      routes:
        - id: consumer-all
          uri: lb://consumer-all
          predicates:
            - Path=/** # 路由的前缀 这里一般只配置一个 */

/// java
package com.hydf.changsha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient  //可以是其他注册中心
public class GateWaylApplication {
    public static void main(String[] args) {
        SpringApplication.run(GateWaylApplication.class, args);
    }
}

```

### 在界面里配置限流







## skywalking  性能监控工具

#### 安装elasticsearch与Skywalking后修改配置文件

改jvm参数 连接skywalking
