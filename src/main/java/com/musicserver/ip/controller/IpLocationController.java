package com.musicserver.ip.controller;

import com.musicserver.common.Result;
import com.musicserver.ip.entity.IPInfo;
import com.musicserver.ip.entity.IPLocation;
import com.musicserver.ip.entity.IPStatistics;
import com.musicserver.ip.service.IpLocationService;
import com.musicserver.ip.util.IpLocationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * IP定位控制器
 * <p>
 * 提供IP地理位置查询和统计相关的REST API接口
 * 支持单个查询、批量查询、统计分析等功能
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/ip")
@RequiredArgsConstructor
@Tag(name = "IP定位服务", description = "IP地理位置查询和统计API")
public class IpLocationController {

    private final IpLocationService ipLocationService;

    @GetMapping("/current")
    @Operation(summary = "获取当前请求IP的地理位置信息", description = "自动获取当前请求的真实IP地址并返回地理位置信息")
    public Result<IPInfo> getCurrentIPLocation(HttpServletRequest request) {
        try {
            String ip = IpLocationUtil.getRealIP(request);
            IPInfo ipInfo = ipLocationService.getIPInfo(ip);

            log.debug("Current IP location query: {} -> {}", ip, ipInfo.getLocation());
            return Result.success(ipInfo);

        } catch (Exception e) {
            log.error("Failed to get current IP location", e);
            return Result.error("获取当前IP位置信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/location/{ip}")
    @Operation(summary = "查询指定IP的地理位置信息", description = "根据IP地址查询详细的地理位置信息")
    public Result<IPLocation> getIPLocation(
            @Parameter(description = "IP地址", example = "8.8.8.8")
            @PathVariable @NotBlank String ip) {
        try {
            IPLocation location = ipLocationService.getLocation(ip);
            return Result.success(location);

        } catch (Exception e) {
            log.error("Failed to get IP location for: {}", ip, e);
            return Result.error("查询IP位置信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/info/{ip}")
    @Operation(summary = "查询指定IP的详细信息", description = "根据IP地址查询完整的IP信息，包括地理位置、ISP、安全等级等")
    public Result<IPInfo> getIPInfo(
            @Parameter(description = "IP地址", example = "8.8.8.8")
            @PathVariable @NotBlank String ip) {
        try {
            IPInfo ipInfo = ipLocationService.getIPInfo(ip);
            return Result.success(ipInfo);

        } catch (Exception e) {
            log.error("Failed to get IP info for: {}", ip, e);
            return Result.error("查询IP详细信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/location/{ip}/async")
    @Operation(summary = "异步查询IP地理位置信息", description = "异步方式查询IP地理位置信息，适用于批量查询场景")
    public CompletableFuture<Result<IPLocation>> getIPLocationAsync(
            @Parameter(description = "IP地址", example = "8.8.8.8")
            @PathVariable @NotBlank String ip) {
        return ipLocationService.getLocationAsync(ip)
                .thenApply(Result::success)
                .exceptionally(throwable -> {
                    log.error("Failed to get IP location async for: {}", ip, throwable);
                    return Result.error("异步查询IP位置信息失败: " + throwable.getMessage());
                });
    }

    @PostMapping("/batch/locations")
    @Operation(summary = "批量查询IP地理位置信息", description = "批量查询多个IP地址的地理位置信息")
    public Result<Map<String, IPLocation>> getIPLocationsBatch(
            @Parameter(description = "IP地址列表")
            @RequestBody List<String> ips) {
        try {
            if (ips == null || ips.isEmpty()) {
                return Result.error("IP地址列表不能为空");
            }

            if (ips.size() > 100) {
                return Result.error("批量查询IP数量不能超过100个");
            }

            Map<String, IPLocation> locations = ipLocationService.getLocationsBatch(ips);
            return Result.success(locations);

        } catch (Exception e) {
            log.error("Failed to get IP locations batch", e);
            return Result.error("批量查询IP位置信息失败: " + e.getMessage());
        }
    }

    @PostMapping("/batch/infos")
    @Operation(summary = "批量查询IP详细信息", description = "批量查询多个IP地址的详细信息")
    public Result<Map<String, IPInfo>> getIPInfosBatch(
            @Parameter(description = "IP地址列表")
            @RequestBody List<String> ips) {
        try {
            if (ips == null || ips.isEmpty()) {
                return Result.error("IP地址列表不能为空");
            }

            if (ips.size() > 100) {
                return Result.error("批量查询IP数量不能超过100个");
            }

            Map<String, IPInfo> infos = ipLocationService.getIPInfosBatch(ips);
            return Result.success(infos);

        } catch (Exception e) {
            log.error("Failed to get IP infos batch", e);
            return Result.error("批量查询IP详细信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/validate/{ip}")
    @Operation(summary = "验证IP地址格式", description = "验证IP地址格式是否正确")
    public Result<Map<String, Object>> validateIP(
            @Parameter(description = "IP地址", example = "192.168.1.1")
            @PathVariable @NotBlank String ip) {
        try {
            Map<String, Object> result = Map.of(
                    "ip", ip,
                    "valid", ipLocationService.isValidIP(ip),
                    "type", ipLocationService.getIPType(ip),
                    "private", ipLocationService.isPrivateIP(ip),
                    "ipv6", ipLocationService.isIPv6(ip)
            );
            return Result.success(result);

        } catch (Exception e) {
            log.error("Failed to validate IP: {}", ip, e);
            return Result.error("验证IP地址失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/{ip}")
    @Operation(summary = "获取IP访问统计信息", description = "获取指定IP的访问统计信息")
    public Result<IPStatistics> getIPStatistics(
            @Parameter(description = "IP地址", example = "8.8.8.8")
            @PathVariable @NotBlank String ip,
            @Parameter(description = "开始日期", example = "2025-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期", example = "2025-01-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            IPStatistics statistics = ipLocationService.getStatistics(ip, startDate, endDate);
            return Result.success(statistics);

        } catch (Exception e) {
            log.error("Failed to get IP statistics for: {}", ip, e);
            return Result.error("获取IP统计信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/{ip}/today")
    @Operation(summary = "获取IP今日统计信息", description = "获取指定IP今日的访问统计信息")
    public Result<IPStatistics> getTodayIPStatistics(
            @Parameter(description = "IP地址", example = "8.8.8.8")
            @PathVariable @NotBlank String ip) {
        try {
            IPStatistics statistics = ipLocationService.getTodayStatistics(ip);
            return Result.success(statistics);

        } catch (Exception e) {
            log.error("Failed to get today IP statistics for: {}", ip, e);
            return Result.error("获取IP今日统计信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/top")
    @Operation(summary = "获取热门IP列表", description = "获取访问量最高的IP地址列表")
    public Result<List<IPStatistics>> getTopIPs(
            @Parameter(description = "返回数量限制", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
            @Parameter(description = "统计日期", example = "2025-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            if (date == null) {
                date = LocalDate.now();
            }

            List<IPStatistics> topIPs = ipLocationService.getTopIPs(limit, date);
            return Result.success(topIPs);

        } catch (Exception e) {
            log.error("Failed to get top IPs", e);
            return Result.error("获取热门IP列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/statistics/risk")
    @Operation(summary = "获取风险IP列表", description = "获取具有风险的IP地址列表")
    public Result<List<IPStatistics>> getRiskIPs(
            @Parameter(description = "返回数量限制", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
            @Parameter(description = "风险等级", example = "2")
            @RequestParam(required = false) @Min(1) @Max(3) Integer riskLevel) {
        try {
            List<IPStatistics> riskIPs = ipLocationService.getRiskIPs(limit, riskLevel);
            return Result.success(riskIPs);

        } catch (Exception e) {
            log.error("Failed to get risk IPs", e);
            return Result.error("获取风险IP列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/blacklist/{ip}")
    @Operation(summary = "添加IP到黑名单", description = "将指定IP地址添加到黑名单")
    public Result<Void> addToBlacklist(
            @Parameter(description = "IP地址", example = "1.2.3.4")
            @PathVariable @NotBlank String ip,
            @Parameter(description = "封禁原因")
            @RequestParam @NotBlank String reason,
            @Parameter(description = "封禁时长（小时）", example = "24")
            @RequestParam(required = false) Integer durationHours) {
        try {
            ipLocationService.addToBlacklist(ip, reason, durationHours);
            return Result.success();

        } catch (Exception e) {
            log.error("Failed to add IP to blacklist: {}", ip, e);
            return Result.error("添加IP到黑名单失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/blacklist/{ip}")
    @Operation(summary = "从黑名单移除IP", description = "将指定IP地址从黑名单中移除")
    public Result<Void> removeFromBlacklist(
            @Parameter(description = "IP地址", example = "1.2.3.4")
            @PathVariable @NotBlank String ip) {
        try {
            ipLocationService.removeFromBlacklist(ip);
            return Result.success();

        } catch (Exception e) {
            log.error("Failed to remove IP from blacklist: {}", ip, e);
            return Result.error("从黑名单移除IP失败: " + e.getMessage());
        }
    }

    @GetMapping("/security/check/{ip}")
    @Operation(summary = "检查IP安全状态", description = "检查IP地址是否在白名单或黑名单中")
    public Result<Map<String, Object>> checkIPSecurity(
            @Parameter(description = "IP地址", example = "8.8.8.8")
            @PathVariable @NotBlank String ip) {
        try {
            Map<String, Object> result = Map.of(
                    "ip", ip,
                    "whitelist", ipLocationService.isWhitelistIP(ip),
                    "blacklist", ipLocationService.isBlacklistIP(ip),
                    "private", ipLocationService.isPrivateIP(ip)
            );
            return Result.success(result);

        } catch (Exception e) {
            log.error("Failed to check IP security for: {}", ip, e);
            return Result.error("检查IP安全状态失败: " + e.getMessage());
        }
    }

    @PostMapping("/cache/clear")
    @Operation(summary = "清空缓存", description = "清空所有IP位置信息的缓存")
    public Result<Void> clearCache() {
        try {
            ipLocationService.clearCache();
            return Result.success();

        } catch (Exception e) {
            log.error("Failed to clear cache", e);
            return Result.error("清空缓存失败: " + e.getMessage());
        }
    }

    @PostMapping("/cache/clear/{ip}")
    @Operation(summary = "清空指定IP的缓存", description = "清空指定IP地址的位置信息缓存")
    public Result<Void> clearIPCache(
            @Parameter(description = "IP地址", example = "8.8.8.8")
            @PathVariable @NotBlank String ip) {
        try {
            ipLocationService.clearCache(ip);
            return Result.success();

        } catch (Exception e) {
            log.error("Failed to clear cache for IP: {}", ip, e);
            return Result.error("清空IP缓存失败: " + e.getMessage());
        }
    }

    @PostMapping("/cache/warmup")
    @Operation(summary = "预热缓存", description = "预热指定IP列表的缓存")
    public Result<Void> warmupCache(
            @Parameter(description = "IP地址列表")
            @RequestBody List<String> ips) {
        try {
            if (ips == null || ips.isEmpty()) {
                return Result.error("IP地址列表不能为空");
            }

            if (ips.size() > 1000) {
                return Result.error("预热IP数量不能超过1000个");
            }

            ipLocationService.warmupCache(ips);
            return Result.success();

        } catch (Exception e) {
            log.error("Failed to warmup cache", e);
            return Result.error("预热缓存失败: " + e.getMessage());
        }
    }

    @GetMapping("/cache/stats")
    @Operation(summary = "获取缓存统计信息", description = "获取IP位置缓存的统计信息")
    public Result<Map<String, Object>> getCacheStats() {
        try {
            Map<String, Object> stats = ipLocationService.getCacheStats();
            return Result.success(stats);

        } catch (Exception e) {
            log.error("Failed to get cache stats", e);
            return Result.error("获取缓存统计信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    @Operation(summary = "获取服务健康状态", description = "获取IP定位服务的健康状态信息")
    public Result<Map<String, Object>> getHealthStatus() {
        try {
            Map<String, Object> status = ipLocationService.getHealthStatus();
            return Result.success(status);

        } catch (Exception e) {
            log.error("Failed to get health status", e);
            return Result.error("获取服务健康状态失败: " + e.getMessage());
        }
    }

    @PostMapping("/statistics/export")
    @Operation(summary = "导出统计数据", description = "导出指定时间范围的IP统计数据")
    public Result<String> exportStatistics(
            @Parameter(description = "开始日期", example = "2025-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期", example = "2025-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "导出格式", example = "CSV")
            @RequestParam(defaultValue = "CSV") String format) {
        try {
            if (startDate.isAfter(endDate)) {
                return Result.error("开始日期不能晚于结束日期");
            }

            String filePath = ipLocationService.exportStatistics(startDate, endDate, format);
            return Result.success(filePath);

        } catch (Exception e) {
            log.error("Failed to export statistics", e);
            return Result.error("导出统计数据失败: " + e.getMessage());
        }
    }
}