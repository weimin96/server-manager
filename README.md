<div align="center">
    <img alt="logo" src="image/banner.png" style="width: 600px">
</div>

<div align="center">
    <h2>Spring Boot Starter Server Manager</h2>
    <h4>基于springboot的分布式的服务监控管理组件</h4>
</div>

<div align="center">
<a href = "https://github.com/weimin96/spring-boot-starter-server-manager/actions/workflows/ci.yml"><img src="https://github.com/weimin96/spring-boot-starter-server-manager/actions/workflows/ci.yml/badge.svg" alt="Java CI"></a>
<a href = "https://github.com/weimin96/spring-boot-starter-server-manager/releases/"><img src="https://img.shields.io/github/v/release/weimin96/spring-boot-starter-server-manager" alt="GitHub Release"></a>
<a href = "https://repo1.maven.org/maven2/io/github/weimin96/spring-boot-starter-server-manager/"><img src="https://img.shields.io/maven-central/v/io.github.weimin96/spring-boot-starter-server-manager" alt="Maven Central Version"></a>
<a href = "https://github.com/weimin96/spring-boot-starter-server-manager/releases/"><img src="https://img.shields.io/github/repo-size/weimin96/spring-boot-starter-server-manager" alt="GitHub repo size"></a>
<a href = "https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/:license-apache-brightgreen.svg" alt="License"></a>
<a href = "https://github.com/weimin96/spring-boot-starter-server-manager"><img src="https://img.shields.io/github/last-commit/weimin96/spring-boot-starter-server-manager.svg" alt="Last Commit"></a>
<a href = "https://github.com/weimin96/spring-boot-starter-server-manager"><img src="https://img.shields.io/github/commit-activity/m/weimin96/spring-boot-starter-server-manager.svg" alt="GitHub commit activity"></a>
</div>

## 功能特性

- 支持nacos连接Server端；
- 支持druid，查看sql记录；
- 支持查看各服务历史日志文件；
- 支持查看健康信息、jvm详情；
- ......

## 如何使用

### 服务端
1、引入依赖
```
<dependency>
    <groupId>io.github.weimin96</groupId>
    <artifactId>spring-boot-starter-manager-server</artifactId>
    <version>1.1.1</version>
</dependency>
```

2、`Application`启动类中添加注解
```
@EnableManagerServer
```

3、添加配置
```
spring:
  boot:
    manager:
      server:
        authority:
          # 是否启用授权 建议选择true
          enabled: true
          # 设置登录密码  
          default-password: your_password
          # 设置用户名
          default-user-name: your_username
        # 设置管理页面上下文  
        context-path: /admin
```

### 客户端

1、引入依赖
```
<dependency>
    <groupId>io.github.weimin96</groupId>
    <artifactId>spring-boot-starter-manager-client</artifactId>
    <version>1.1.1</version>
</dependency>
```

2、添加配置
```
spring:
  boot:
    manager:
      client:
        # 对应server的用户名
        username: your_username
        # 对应client的密码
        password: your_password

management:
  endpoints:
    web:
      exposure:
        # 暴漏端点，按需选择
        include: druid,logdir,logcontent,health,env,metrics,httptrace,threaddump,jolokia,info,logfile,refresh,heapdump,loggers,auditevents,mappings,scheduledtasks,configprops,beans
  endpoint:
    health:
      # health 默认只展示是否在线，增加该配置后会展示更多信息
      show-details: ALWAYS
```

单例
```
spring:
  boot:
    manager:
      client:
        # server地址
        url: http://{your_server_ip}:{your_server_port}/{your_server_context_path}      
```

nacos
```
spring:
  boot:
    manager:
      client:
        # 对应server application name
        server-application-name: your_server_application_name
        # 对应server context path  
        server-context-path: your_server_context_path  
```


## 端点解释
| 端点                | 解释                                                                             |
|-------------------|--------------------------------------------------------------------------------|
| health            | 显示应用程序运行状况信息(必须)                                                               |
| druid             | 展示`durid`记录的`sql`，需要设置`spring.datasource.druid.stat-view-servlet.enabled=true` |
| logdir、logcontent | 展示日志列表及内容，需要设置`logging.file.path`                                              |
| logfile           | 展示当前日志信息，需要设置`logging.file.name`                                               |
| info              | 显示任意应用程序信息                                                                     |
| env               | 公开 Spring 的 ConfigurableEnvironment 中的属性                                       |
| metrics           | 显示当前应用程序的“指标”信息                                                                |
| httptrace         | 显示 HTTP 跟踪信息（默认情况下，为最近 100 次 HTTP 请求-响应交换）。需要 HttpTraceRepository bean         |
| threaddump        | 执行线程转储                                                                         |
| heapdump          | 返回堆转储文件。在 HotSpot JVM 上，返回 HPROF 格式文件。在 OpenJ9 JVM 上，返回 PHD 格式文件               |
| jolokia           | 当 Jolokia 位于类路径上时，通过 HTTP 公开 JMX bean（不适用于 WebFlux）。需要依赖 jolokia-core          |
| loggers           | 显示和修改应用程序中记录器的配置                                                               |
| auditevents       | 公开当前应用程序的审核事件信息。需要 AuditEventRepository bean                                   |
| mappings          | 显示所有 @RequestMapping 路径的排序列表                                                   |
| scheduledtasks    | 显示应用程序中的计划任务                                                                   |
| configprops       | 显示所有 @ConfigurationProperties 的排序列表                                            |
| beans             | 显示应用程序中所有 Spring Bean 的完整列表                                                    |

## 参考案例

[demo](https://github.com/weimin96/spring-boot-starter-server-manager/tree/main/spring-boot-starter-manager-sample)


