# 2021 8 10 springcloud

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

1.命名空间，配置环境

2.新建配置，group是项目名

3.克隆项目可以克隆不同环境

4.更多里面可以回滚版本

![1628651526033](2021 8 10 springcloud 学习.assets/1628651526033.png)

开启权限

![1628652030220](2021 8 10 springcloud 学习.assets/1628652030220.png)



改单一配置

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



profile配置

nacos配置中心 ，命名为：服务名-环境.扩展名，的方式。不建议这样，应该用命名空间作区分。

![1628660545958](2021 8 10 springcloud 学习.assets/1628660545958.png)

commons文件 要写在里面。

![1628660661689](2021 8 10 springcloud 学习.assets/1628660661689.png)

分类方式

![1628661791719](2021 8 10 springcloud 学习.assets/1628661791719.png)

修改默认 dataid

data-id下标越大 优先级越高， refresh是是否刷新

![1628662377527](2021 8 10 springcloud 学习.assets/1628662377527.png)

优先级最大的

![1628662505911](2021 8 10 springcloud 学习.assets/1628662505911.png)

@value 无法感知修改之后的值



## sentinel

启动

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

