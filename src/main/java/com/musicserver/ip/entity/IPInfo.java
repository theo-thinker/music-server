package com.musicserver.ip.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IP信息实体类
 * <p>
 * 封装完整的IP地理位置信息和相关元数据
 * 支持缓存和持久化存储
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 国家
     */
    private String country;

    /**
     * 地区/省份
     */
    private String region;

    /**
     * 城市
     */
    private String city;

    /**
     * 运营商
     */
    private String isp;

    /**
     * 完整地址描述
     */
    private String location;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 时区
     */
    private String timezone;

    /**
     * 语言代码
     */
    private String language;

    /**
     * 货币代码
     */
    private String currency;

    /**
     * IP类型：IPv4/IPv6
     */
    private String ipType;

    /**
     * 是否为内网IP
     */
    private Boolean isPrivate;

    /**
     * 是否为移动网络
     */
    private Boolean isMobile;

    /**
     * 是否为代理IP
     */
    private Boolean isProxy;

    /**
     * 安全等级：1-安全，2-可疑，3-危险
     */
    private Integer securityLevel;

    /**
     * 威胁类型
     */
    private String threatType;

    /**
     * 查询来源：ip2region、cache等
     */
    private String source;

    /**
     * 查询耗时（毫秒）
     */
    private Long queryTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 缓存过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 获取简短位置描述
     *
     * @return 位置描述
     */
    public String getShortLocation() {
        StringBuilder sb = new StringBuilder();

        if (country != null && !country.isEmpty() && !"0".equals(country)) {
            sb.append(country);
        }

        if (region != null && !region.isEmpty() && !"0".equals(region)) {
            if (!sb.isEmpty()) sb.append("-");
            sb.append(region);
        }

        if (city != null && !city.isEmpty() && !"0".equals(city)) {
            if (!sb.isEmpty()) sb.append("-");
            sb.append(city);
        }

        return !sb.isEmpty() ? sb.toString() : "未知地区";
    }

    /**
     * 获取详细位置描述
     *
     * @return 详细位置描述
     */
    public String getDetailLocation() {
        StringBuilder sb = new StringBuilder();

        if (country != null && !country.isEmpty() && !"0".equals(country)) {
            sb.append(country);
        }

        if (region != null && !region.isEmpty() && !"0".equals(region)) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(region);
        }

        if (city != null && !city.isEmpty() && !"0".equals(city)) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(city);
        }

        if (isp != null && !isp.isEmpty() && !"0".equals(isp)) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append("[").append(isp).append("]");
        }

        return !sb.isEmpty() ? sb.toString() : "未知地区";
    }

    /**
     * 检查是否为有效的地理位置信息
     *
     * @return 是否有效
     */
    public boolean isValidLocation() {
        return (country != null && !country.isEmpty() && !"0".equals(country)) ||
                (region != null && !region.isEmpty() && !"0".equals(region)) ||
                (city != null && !city.isEmpty() && !"0".equals(city));
    }

    /**
     * 检查是否为国内IP
     *
     * @return 是否为国内IP
     */
    public boolean isDomesticIp() {
        return "中国".equals(country) || "China".equalsIgnoreCase(country) || "CN".equalsIgnoreCase(country);
    }

    /**
     * 获取安全等级描述
     *
     * @return 安全等级描述
     */
    public String getSecurityLevelDesc() {
        if (securityLevel == null) {
            return "未知";
        }

        return switch (securityLevel) {
            case 1 -> "安全";
            case 2 -> "可疑";
            case 3 -> "危险";
            default -> "未知";
        };
    }
}