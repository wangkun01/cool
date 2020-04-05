package com.zhss.demo.zuul.gray;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.jmnarloch.spring.cloud.ribbon.support.RibbonFilterContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @ClassName GrayReleaseFilter
 * @Description: 灰度发布的处理Filter
 * @Author xiaoming
 * @Date 2020/4/511:10 下午
 * @Version 1.0.0
 **/
@Slf4j
@Component
public class GrayReleaseFilter extends ZuulFilter {


    @Resource
    private GrayReleaseConfigManager grayReleaseConfigManager;

    @Override
    public String filterType() {
        return null;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     *  zuul每次路由会先进入这个方法
     * @return
     */
    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        // 获取请求的url: 如 http://localhost:9000/order/order?xxxx
        String requestURI = request.getRequestURI();

        // 根据缓存中读取的数据库配置,判断新发布的服务是否需要开启灰度发布
        Map<String, GrayReleaseConfig> grayReleaseConfigs =
                grayReleaseConfigManager.getGrayReleaseConfigs();
        for(String path : grayReleaseConfigs.keySet()) {
            if(requestURI.contains(path)) {
                GrayReleaseConfig grayReleaseConfig = grayReleaseConfigs.get(path);
                if(grayReleaseConfig.getEnableGrayRelease() == 1) {
                    log.info("启用灰度发布功能");
                    return true;
                }
            }
        }

        log.info("不启用灰度发布功能");

        return false;
    }

    /**
     *
     * 如果 shouldFilter return true 才会进入此方法
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {

        /**
         *  真实的项目应该采用此方法.如：1%的流量转发到新服务
         */
        //		Random random = new Random();
//		int seed = random.nextInt() * 100;
//
//        if (seed == 50) {
//            RibbonFilterContextHolder.getCurrentContext().add("version", "new");
//        }  else {
//            RibbonFilterContextHolder.getCurrentContext().add("version", "current");
//        }

        /**
         * 下面的逻辑知识测试用,根据传入的gray标识判断是否路由到新版本
         */
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String gray = request.getParameter("gray");

        //这里的new和current是具体的某个服务在Eureka.instance中配置的自定义元数据
        /**
         *  如：
         *   eureka:
         *   client:
         *     serviceUrl:
         *       defaultZone:http://localhost:8761/eureka/
         *   instance:
         *     metadata-map:
         *       version: new       # 自定义的元数据，key/value都可以随便写。
         */
        if("true".equals(gray)) {
            RibbonFilterContextHolder.getCurrentContext().add("version", "new");
        } else {
            RibbonFilterContextHolder.getCurrentContext().add("version", "current");
        }

        return null;
    }
}
