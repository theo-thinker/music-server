package com.musicserver.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Web工具类
 * 
 * 提供Web相关的实用工具方法
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
public class WebUtils {

    private static final String[] IP_HEADER_NAMES = {
        "X-Forwarded-For",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR",
        "X-Real-IP"
    };

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * 获取客户端真实IP地址
     * 
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ipAddress = null;

        // 尝试从各种可能的Header中获取IP
        for (String headerName : IP_HEADER_NAMES) {
            ipAddress = request.getHeader(headerName);
            if (isValidIpAddress(ipAddress)) {
                break;
            }
        }

        // 如果都没有获取到，使用request.getRemoteAddr()
        if (!isValidIpAddress(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (LOCALHOST_IPV6.equals(ipAddress)) {
                ipAddress = LOCALHOST_IPV4;
            }
        }

        // 对于通过了多级代理的情况，X-Forwarded-For的值可能是多个IP用逗号分隔，第一个IP为客户端真实IP
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        log.debug("获取客户端IP地址: {}", ipAddress);
        return ipAddress;
    }

    /**
     * 验证IP地址是否有效
     * 
     * @param ipAddress IP地址
     * @return 是否有效
     */
    private static boolean isValidIpAddress(String ipAddress) {
        return ipAddress != null && 
               !ipAddress.isEmpty() && 
               !UNKNOWN.equalsIgnoreCase(ipAddress);
    }

    /**
     * 获取用户代理信息
     * 
     * @param request HTTP请求对象
     * @return 用户代理字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }

    /**
     * 获取请求的完整URL
     * 
     * @param request HTTP请求对象
     * @return 完整URL
     */
    public static String getFullRequestUrl(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString != null && !queryString.isEmpty()) {
            requestURL.append("?").append(queryString);
        }

        return requestURL.toString();
    }

    /**
     * 判断是否为AJAX请求
     * 
     * @param request HTTP请求对象
     * @return 是否为AJAX请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }

    /**
     * 判断是否为移动端请求
     * 
     * @param request HTTP请求对象
     * @return 是否为移动端请求
     */
    public static boolean isMobileRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String userAgent = getUserAgent(request);
        if (userAgent == null) {
            return false;
        }

        userAgent = userAgent.toLowerCase();
        return userAgent.contains("mobile") || 
               userAgent.contains("android") || 
               userAgent.contains("iphone") || 
               userAgent.contains("ipad") || 
               userAgent.contains("blackberry") || 
               userAgent.contains("webos");
    }
}