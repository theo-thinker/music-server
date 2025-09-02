package com.musicserver.ip.interceptor;

import com.musicserver.ip.config.IpLocationProperties;
import com.musicserver.ip.service.IpLocationService;
import com.musicserver.ip.util.IpLocationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * IP定位拦截器
 * <p>
 * 自动记录请求IP地址和地理位置信息
 * 支持访问统计和安全检查
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@RequiredArgsConstructor
public class IpLocationInterceptor implements HandlerInterceptor {

    private final IpLocationService ipLocationService;
    private final IpLocationProperties properties;

    /**
     * 请求前处理
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @return 是否继续处理
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {
        try {
            // 获取真实IP地址
            String ip = IpLocationUtil.getRealIP(request);

            if (ip == null || ip.isEmpty()) {
                return true; // 无法获取IP，继续处理
            }

            // 安全检查
            if (!performSecurityCheck(ip, request, response)) {
                return false; // 安全检查失败，阻止请求
            }

            // 记录IP访问信息
            recordIpAccess(ip, request);

            // 将IP信息添加到请求属性中
            request.setAttribute("clientIp", ip);
            request.setAttribute("clientUserAgent", IpLocationUtil.getUserAgent(request));

        } catch (Exception e) {
            log.warn("IP location interceptor error: {}", e.getMessage());
            // 不因为拦截器错误阻止正常请求
        }

        return true;
    }

    /**
     * 执行安全检查
     *
     * @param ip       IP地址
     * @param request  HTTP请求
     * @param response HTTP响应
     * @return 是否通过安全检查
     */
    private boolean performSecurityCheck(String ip, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 检查IP黑名单
            if (ipLocationService.isBlacklistIP(ip)) {
                log.warn("Blocked request from blacklisted IP: {} for {}", ip, request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"Access denied\"}");
                return false;
            }

            // 检查私有IP过滤
            if (properties.getSecurity().getFilterPrivateIp() && ipLocationService.isPrivateIP(ip)) {
                log.debug("Filtered private IP access: {} for {}", ip, request.getRequestURI());
                // 私有IP不记录，但允许访问
                return true;
            }

            // 可以在这里添加更多安全检查逻辑
            // 例如：限流检查、地理位置限制等

        } catch (Exception e) {
            log.error("Security check error for IP: {}", ip, e);
            // 安全检查出错时，默认允许访问（可根据业务需求调整）
        }

        return true;
    }

    /**
     * 记录IP访问信息
     *
     * @param ip      IP地址
     * @param request HTTP请求
     */
    private void recordIpAccess(String ip, HttpServletRequest request) {
        if (!properties.getEnableStatistics()) {
            return;
        }

        try {
            String userAgent = IpLocationUtil.getUserAgent(request);
            String requestUri = request.getRequestURI();

            // 获取用户ID（如果已登录）
            Long userId = getUserIdFromRequest(request);

            // 异步记录访问信息
            ipLocationService.recordAccessAsync(ip, userAgent, requestUri, userId);

            log.debug("Recorded IP access: {} -> {}", ip, requestUri);

        } catch (Exception e) {
            log.warn("Failed to record IP access for: {}", ip, e);
        }
    }

    /**
     * 从请求中获取用户ID
     *
     * @param request HTTP请求
     * @return 用户ID，如果未登录则返回null
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            // 尝试从请求属性中获取用户ID
            Object userId = request.getAttribute("userId");
            if (userId instanceof Long) {
                return (Long) userId;
            }

            // 尝试从请求参数中获取用户ID
            String userIdParam = request.getParameter("userId");
            if (userIdParam != null && !userIdParam.isEmpty()) {
                return Long.parseLong(userIdParam);
            }

            // 可以在这里添加从JWT token中解析用户ID的逻辑
            // 例如：从Authorization头中解析JWT token获取用户ID

        } catch (Exception e) {
            log.debug("Failed to get user ID from request: {}", e.getMessage());
        }

        return null;
    }

    /**
     * 请求完成后处理
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param handler  处理器
     * @param ex       异常（如果有）
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                Exception ex) {
        try {
            String ip = (String) request.getAttribute("clientIp");

            if (ip != null && properties.getEnableStatistics()) {
                // 记录响应状态和处理时间等额外信息
                recordResponseInfo(ip, request, response, ex);
            }

        } catch (Exception e) {
            log.debug("Error in afterCompletion: {}", e.getMessage());
        }
    }

    /**
     * 记录响应信息
     *
     * @param ip       IP地址
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param ex       异常（如果有）
     */
    private void recordResponseInfo(String ip, HttpServletRequest request,
                                    HttpServletResponse response, Exception ex) {
        try {
            // 记录响应状态
            int statusCode = response.getStatus();
            String requestUri = request.getRequestURI();

            // 计算处理时间
            Object startTime = request.getAttribute("startTime");
            long processingTime = 0;
            if (startTime instanceof Long) {
                processingTime = System.currentTimeMillis() - (Long) startTime;
            }

            // 判断是否为错误响应
            boolean isError = statusCode >= 400 || ex != null;

            log.debug("IP {} response: {} {} [{}ms] {}",
                    ip, statusCode, requestUri, processingTime,
                    isError ? "ERROR" : "SUCCESS");

            // 这里可以将详细的响应信息记录到统计服务中
            // 例如：更新错误率、平均响应时间等统计指标

        } catch (Exception e) {
            log.debug("Failed to record response info: {}", e.getMessage());
        }
    }
}