package com.musicserver.ip.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * IP位置信息实体类
 * 
 * 简化的IP地理位置信息，用于快速查询和缓存
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPLocation implements Serializable {

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
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 根据ip2region查询结果创建IPLocation
     * 
     * @param ip IP地址
     * @param regionResult ip2region查询结果
     * @return IPLocation实例
     */
    public static IPLocation fromRegionResult(String ip, String regionResult) {
        if (regionResult == null || regionResult.trim().isEmpty()) {
            return IPLocation.builder()
                    .ip(ip)
                    .country("未知")
                    .region("未知")
                    .city("未知")
                    .isp("未知")
                    .createdTime(LocalDateTime.now())
                    .build();
        }

        // ip2region格式：国家|区域|省份|城市|ISP
        String[] parts = regionResult.split("\\|");
        
        String country = parts.length > 0 && !"0".equals(parts[0]) ? parts[0] : "未知";
        String region = parts.length > 2 && !"0".equals(parts[2]) ? parts[2] : "未知";
        String city = parts.length > 3 && !"0".equals(parts[3]) ? parts[3] : "未知";
        String isp = parts.length > 4 && !"0".equals(parts[4]) ? parts[4] : "未知";

        return IPLocation.builder()
                .ip(ip)
                .country(country)
                .region(region)
                .city(city)
                .isp(isp)
                .createdTime(LocalDateTime.now())
                .build();
    }

    /**
     * 获取完整地址描述
     * 
     * @return 地址描述
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        
        if (country != null && !country.isEmpty() && !"未知".equals(country)) {
            sb.append(country);
        }
        
        if (region != null && !region.isEmpty() && !"未知".equals(region) && !region.equals(country)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(region);
        }
        
        if (city != null && !city.isEmpty() && !"未知".equals(city) && !city.equals(region)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(city);
        }
        
        if (isp != null && !isp.isEmpty() && !"未知".equals(isp)) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("[").append(isp).append("]");
        }
        
        return sb.length() > 0 ? sb.toString() : "未知地区";
    }

    /**
     * 转换为IPInfo
     * 
     * @return IPInfo实例
     */
    public IPInfo toIPInfo() {
        return IPInfo.builder()
                .ip(this.ip)
                .country(this.country)
                .region(this.region)
                .city(this.city)
                .isp(this.isp)
                .latitude(this.latitude)
                .longitude(this.longitude)
                .location(this.getFullAddress())
                .ipType(getIpType())
                .isPrivate(isPrivateIp())
                .securityLevel(1) // 默认安全等级
                .source("ip2region")
                .createdTime(this.createdTime)
                .updatedTime(LocalDateTime.now())
                .build();
    }

    /**
     * 获取IP类型
     * 
     * @return IP类型
     */
    private String getIpType() {
        if (ip != null && ip.contains(":")) {
            return "IPv6";
        }
        return "IPv4";
    }

    /**
     * 判断是否为内网IP
     * 
     * @return 是否为内网IP
     */
    private Boolean isPrivateIp() {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // 简单的内网IP判断
        return ip.startsWith("192.168.") ||
               ip.startsWith("10.") ||
               ip.startsWith("172.16.") ||
               ip.startsWith("172.17.") ||
               ip.startsWith("172.18.") ||
               ip.startsWith("172.19.") ||
               ip.startsWith("172.20.") ||
               ip.startsWith("172.21.") ||
               ip.startsWith("172.22.") ||
               ip.startsWith("172.23.") ||
               ip.startsWith("172.24.") ||
               ip.startsWith("172.25.") ||
               ip.startsWith("172.26.") ||
               ip.startsWith("172.27.") ||
               ip.startsWith("172.28.") ||
               ip.startsWith("172.29.") ||
               ip.startsWith("172.30.") ||
               ip.startsWith("172.31.") ||
               ip.equals("127.0.0.1") ||
               ip.equals("localhost");
    }
}