package com.zhss.demo.zuul.gray;

import lombok.Data;

/**
 * @ClassName GrayReleaseConfig
 * @Description: 灰度发布实体类,和数据库一一对应
 * @Author xiaoming
 * @Date 2020/4/511:04 下午
 * @Version 1.0.0
 **/
@Data
public class GrayReleaseConfig {

    private int id;
    private String serviceId;
    private String path;
    /**
     * 是否启用灰度发布
     */
    private int enableGrayRelease;
}
