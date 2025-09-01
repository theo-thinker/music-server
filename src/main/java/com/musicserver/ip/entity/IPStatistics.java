package com.musicserver.ip.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * IP访问统计实体类
 * 
 * 记录和分析IP访问的统计信息
 * 支持多维度统计分析
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IPStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 统计ID
     */
    private Long id;

    /**
     * IP地址
     */
    private String ip;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 访问次数
     */
    private Long accessCount;

    /**
     * 首次访问时间
     */
    private LocalDateTime firstAccessTime;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 访问的API数量
     */
    private Integer apiCount;

    /**
     * 访问的用户数量
     */
    private Integer userCount;

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
     * 是否为内网IP
     */
    private Boolean isPrivate;

    /**
     * 是否为移动网络
     */
    private Boolean isMobile;

    /**
     * 风险等级：1-安全，2-可疑，3-危险
     */
    private Integer riskLevel;

    /**
     * 是否被封禁
     */
    private Boolean isBanned;

    /**
     * 封禁原因
     */
    private String banReason;

    /**
     * 封禁时间
     */
    private LocalDateTime banTime;

    /**
     * 解封时间
     */
    private LocalDateTime unbanTime;

    /**
     * 访问设备类型统计
     */
    private Map<String, Integer> deviceTypes;

    /**
     * 访问浏览器统计
     */
    private Map<String, Integer> browsers;

    /**
     * 访问操作系统统计
     */
    private Map<String, Integer> operatingSystems;

    /**
     * 小时访问分布
     */
    private Map<Integer, Integer> hourlyDistribution;

    /**
     * API访问分布
     */
    private Map<String, Integer> apiDistribution;

    /**
     * 错误请求次数
     */
    private Long errorCount;

    /**
     * 成功请求次数
     */
    private Long successCount;

    /**
     * 平均响应时间（毫秒）
     */
    private Double avgResponseTime;

    /**
     * 总流量（字节）
     */
    private Long totalTraffic;

    /**
     * 上传流量（字节）
     */
    private Long uploadTraffic;

    /**
     * 下载流量（字节）
     */
    private Long downloadTraffic;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 扩展属性
     */
    private Map<String, Object> extraAttributes;

    /**
     * 计算访问频率（次/小时）
     * 
     * @return 访问频率
     */
    public Double getAccessFrequency() {
        if (firstAccessTime == null || lastAccessTime == null || accessCount == null || accessCount <= 1) {
            return 0.0;
        }
        
        long minutes = java.time.Duration.between(firstAccessTime, lastAccessTime).toMinutes();
        if (minutes <= 0) {
            return 0.0;
        }
        
        return (double) accessCount / (minutes / 60.0);
    }

    /**
     * 计算成功率
     * 
     * @return 成功率
     */
    public Double getSuccessRate() {
        if (accessCount == null || accessCount <= 0) {
            return 0.0;
        }
        
        long success = successCount != null ? successCount : 0;
        return (double) success / accessCount * 100;
    }

    /**
     * 计算错误率
     * 
     * @return 错误率
     */
    public Double getErrorRate() {
        if (accessCount == null || accessCount <= 0) {
            return 0.0;
        }
        
        long error = errorCount != null ? errorCount : 0;
        return (double) error / accessCount * 100;
    }

    /**
     * 判断是否为高风险IP
     * 
     * @return 是否为高风险IP
     */
    public boolean isHighRisk() {
        // 高访问频率
        if (getAccessFrequency() > 1000) {
            return true;
        }
        
        // 高错误率
        if (getErrorRate() > 50) {
            return true;
        }
        
        // 风险等级
        if (riskLevel != null && riskLevel >= 3) {
            return true;
        }
        
        // 已被封禁
        if (Boolean.TRUE.equals(isBanned)) {
            return true;
        }
        
        return false;
    }

    /**
     * 判断是否为可疑IP
     * 
     * @return 是否为可疑IP
     */
    public boolean isSuspicious() {
        // 中等访问频率
        if (getAccessFrequency() > 100 && getAccessFrequency() <= 1000) {
            return true;
        }
        
        // 中等错误率
        if (getErrorRate() > 20 && getErrorRate() <= 50) {
            return true;
        }
        
        // 风险等级
        if (riskLevel != null && riskLevel == 2) {
            return true;
        }
        
        return false;
    }

    /**
     * 获取风险等级描述
     * 
     * @return 风险等级描述
     */
    public String getRiskLevelDesc() {
        if (isHighRisk()) {
            return "高风险";
        } else if (isSuspicious()) {
            return "可疑";
        } else {
            return "正常";
        }
    }

    /**
     * 格式化流量大小
     * 
     * @param bytes 字节数
     * @return 格式化后的流量大小
     */
    public static String formatTraffic(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "0 B";
        }
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        double size = bytes;
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.2f %s", size, units[unitIndex]);
    }

    /**
     * 获取格式化的总流量
     * 
     * @return 格式化的总流量
     */
    public String getFormattedTotalTraffic() {
        return formatTraffic(totalTraffic);
    }

    /**
     * 获取格式化的上传流量
     * 
     * @return 格式化的上传流量
     */
    public String getFormattedUploadTraffic() {
        return formatTraffic(uploadTraffic);
    }

    /**
     * 获取格式化的下载流量
     * 
     * @return 格式化的下载流量
     */
    public String getFormattedDownloadTraffic() {
        return formatTraffic(downloadTraffic);
    }
}