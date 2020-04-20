一、网关动态路由
    
    1.新增动态路由配置表
    create table cool.gateway_api_route
    (
        id           varchar(50)  not null comment '服务' primary key,
        path         varchar(255) not null comment '服务匹配path',
        service_id   varchar(50)  null comment '服务id',
        url          varchar(255) null,
        retryable    tinyint(1)   null comment '是否允许重试',
        enabled      tinyint(1)   not null comment '是否可用',
        strip_prefix int          null,
        api_name     varchar(255) null
    )
    comment '网关动态路由配置表';
    INSERT INTO gateway_api_route (id, path, service_id, retryable, strip_prefix, url, enabled) VALUES ('order-service', '/order/**', 'order-service',0,1, NULL, 1);
    
    2.新建实体类与配置表一一对应
    GatewayApiRoute
    
    3.新增配置类，初始化ZuulProperties\ZuulProperties\JdbcTemplate
    DynamicRouteConfiguration
    
    4.定时任务每隔5s,发布一个路由刷新事件 
    RefreshRouteTask
        RoutesRefreshedEvent routesRefreshedEvent = new RoutesRefreshedEvent(routeLocator);
        publisher.publishEvent(routesRefreshedEvent);
    
    5.实现动态路由
    (1) 继承SimpleRouteLocator重写locateRoutes方法，从properties文件和数据库读取路由配置
    （2） 实现RefreshableRouteLocator监听路由刷新事件,加载最新的理由配置
    DynamicRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator
    
    
二、灰度发布
1.新建灰度发布配置表
create table cool.gray_release_config
(
    id                  int auto_increment primary key,
    service_id          varchar(255) null comment '服务id',
    path                varchar(255) null comment '服务路径',
    enable_gray_release int          null comment '是否启用灰度发布'
)
comment '服务灰度发布配置表';
2.新建实体类与配置表一一对应
GrayReleaseConfig
3. 灰度发布管理的控制类,定时任务没秒查询数据库，写入缓存map中
GrayReleaseConfigManager
4.灰度发布的处理Filter
GrayReleaseFilter extends ZuulFilter
获取灰度发布控制类bean中的缓存,
override方法：shouldFilter()-->根据缓存判断是否启用灰度发布
如果shouldFilter()返回true-->
override run()方法--> 
判断是路由到新版本的服务,还是旧版本服务
        if("true".equals(gray)) {
            RibbonFilterContextHolder.getCurrentContext().add("version", "new");
        } else {
            RibbonFilterContextHolder.getCurrentContext().add("version", "current");
        }


