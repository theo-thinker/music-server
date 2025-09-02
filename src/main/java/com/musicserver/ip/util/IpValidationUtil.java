package com.musicserver.ip.util;

import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * IP工具类
 * <p>
 * 提供IP地址相关的工具方法
 * 包括IP验证、类型判断、格式化等功能
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
public class IpValidationUtil {

    /**
     * IPv4正则表达式
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    /**
     * IPv6正则表达式
     */
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
                    "^::(?:[0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,7}:$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}$|" +
                    "^[0-9a-fA-F]{1,4}:(?::[0-9a-fA-F]{1,4}){1,6}$|" +
                    "^:(?::[0-9a-fA-F]{1,4}){1,7}$|" +
                    "^(?:[0-9a-fA-F]{1,4}:){1,7}:$"
    );

    /**
     * 私有构造函数，防止实例化
     */
    private IpValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 验证IP地址是否有效
     *
     * @param ip IP地址
     * @return 是否有效
     */
    public static boolean isValidIP(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }

        return isValidIPv4(ip) || isValidIPv6(ip);
    }

    /**
     * 验证IPv4地址是否有效
     *
     * @param ip IP地址
     * @return 是否为有效的IPv4地址
     */
    public static boolean isValidIPv4(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }

        return IPV4_PATTERN.matcher(ip.trim()).matches();
    }

    /**
     * 验证IPv6地址是否有效
     *
     * @param ip IP地址
     * @return 是否为有效的IPv6地址
     */
    public static boolean isValidIPv6(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }

        String trimmedIp = ip.trim();

        // 处理IPv6地址中可能包含的端口号
        if (trimmedIp.startsWith("[") && trimmedIp.contains("]:")) {
            int endIndex = trimmedIp.indexOf("]:");
            trimmedIp = trimmedIp.substring(1, endIndex);
        }

        return IPV6_PATTERN.matcher(trimmedIp).matches();
    }

    /**
     * 判断是否为内网IP地址
     *
     * @param ip IP地址
     * @return 是否为内网IP
     */
    public static boolean isPrivateIP(String ip) {
        if (!isValidIP(ip)) {
            return false;
        }

        if (isValidIPv6(ip)) {
            return isPrivateIPv6(ip);
        }

        return isPrivateIPv4(ip);
    }

    /**
     * 判断是否为私有IPv4地址
     *
     * @param ip IPv4地址
     * @return 是否为私有IPv4地址
     */
    public static boolean isPrivateIPv4(String ip) {
        if (!isValidIPv4(ip)) {
            return false;
        }

        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isSiteLocalAddress() || addr.isLoopbackAddress() || addr.isLinkLocalAddress();
        } catch (UnknownHostException e) {
            // 使用字符串匹配作为备选方案
            return ip.startsWith("192.168.") ||
                    ip.startsWith("10.") ||
                    ip.startsWith("172.16.") || ip.startsWith("172.17.") ||
                    ip.startsWith("172.18.") || ip.startsWith("172.19.") ||
                    ip.startsWith("172.20.") || ip.startsWith("172.21.") ||
                    ip.startsWith("172.22.") || ip.startsWith("172.23.") ||
                    ip.startsWith("172.24.") || ip.startsWith("172.25.") ||
                    ip.startsWith("172.26.") || ip.startsWith("172.27.") ||
                    ip.startsWith("172.28.") || ip.startsWith("172.29.") ||
                    ip.startsWith("172.30.") || ip.startsWith("172.31.") ||
                    ip.equals("127.0.0.1") || ip.equals("localhost");
        }
    }

    /**
     * 判断是否为私有IPv6地址
     *
     * @param ip IPv6地址
     * @return 是否为私有IPv6地址
     */
    public static boolean isPrivateIPv6(String ip) {
        if (!isValidIPv6(ip)) {
            return false;
        }

        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isSiteLocalAddress() || addr.isLoopbackAddress() || addr.isLinkLocalAddress();
        } catch (UnknownHostException e) {
            // 使用字符串匹配作为备选方案
            String lowerIp = ip.toLowerCase();
            return lowerIp.startsWith("fe80:") ||   // Link-local
                    lowerIp.startsWith("fc00:") ||   // Unique local
                    lowerIp.startsWith("fd00:") ||   // Unique local
                    lowerIp.equals("::1");           // Loopback
        }
    }

    /**
     * 判断是否为本地回环地址
     *
     * @param ip IP地址
     * @return 是否为本地回环地址
     */
    public static boolean isLoopbackIP(String ip) {
        if (!isValidIP(ip)) {
            return false;
        }

        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isLoopbackAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1".equals(ip) || "::1".equals(ip) || "localhost".equalsIgnoreCase(ip);
        }
    }

    /**
     * 判断是否为广播地址
     *
     * @param ip IP地址
     * @return 是否为广播地址
     */
    public static boolean isBroadcastIP(String ip) {
        if (!isValidIPv4(ip)) {
            return false;
        }

        return "255.255.255.255".equals(ip) || ip.endsWith(".255");
    }

    /**
     * 判断是否为多播地址
     *
     * @param ip IP地址
     * @return 是否为多播地址
     */
    public static boolean isMulticastIP(String ip) {
        if (!isValidIP(ip)) {
            return false;
        }

        try {
            InetAddress addr = InetAddress.getByName(ip);
            return addr.isMulticastAddress();
        } catch (UnknownHostException e) {
            if (isValidIPv4(ip)) {
                String[] parts = ip.split("\\.");
                int firstOctet = Integer.parseInt(parts[0]);
                return firstOctet >= 224 && firstOctet <= 239;
            }
            return false;
        }
    }

    /**
     * 获取IP地址类型
     *
     * @param ip IP地址
     * @return IP地址类型：IPv4/IPv6/INVALID
     */
    public static String getIPType(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "INVALID";
        }

        if (isValidIPv4(ip)) {
            return "IPv4";
        } else if (isValidIPv6(ip)) {
            return "IPv6";
        } else {
            return "INVALID";
        }
    }

    /**
     * 将IPv4地址转换为长整型
     *
     * @param ip IPv4地址
     * @return 长整型表示的IP地址
     */
    public static long ipv4ToLong(String ip) {
        if (!isValidIPv4(ip)) {
            throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
        }

        String[] parts = ip.split("\\.");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result = result * 256 + Integer.parseInt(parts[i]);
        }
        return result;
    }

    /**
     * 将长整型转换为IPv4地址
     *
     * @param ipLong 长整型表示的IP地址
     * @return IPv4地址字符串
     */
    public static String longToIPv4(long ipLong) {
        if (ipLong < 0 || ipLong > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("Invalid IP long value: " + ipLong);
        }

        return String.format("%d.%d.%d.%d",
                (ipLong >> 24) & 0xFF,
                (ipLong >> 16) & 0xFF,
                (ipLong >> 8) & 0xFF,
                ipLong & 0xFF);
    }

    /**
     * 格式化IP地址
     *
     * @param ip IP地址
     * @return 格式化后的IP地址
     */
    public static String formatIP(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "";
        }

        String trimmedIp = ip.trim();

        if (isValidIPv4(trimmedIp)) {
            return trimmedIp;
        }

        if (isValidIPv6(trimmedIp)) {
            // 标准化IPv6地址格式
            try {
                InetAddress addr = InetAddress.getByName(trimmedIp);
                return addr.getHostAddress();
            } catch (UnknownHostException e) {
                return trimmedIp;
            }
        }

        return trimmedIp;
    }

    /**
     * 掩码IP地址（隐私保护）
     *
     * @param ip IP地址
     * @return 掩码后的IP地址
     */
    public static String maskIP(String ip) {
        if (!isValidIP(ip)) {
            return ip;
        }

        if (isValidIPv4(ip)) {
            String[] parts = ip.split("\\.");
            return String.format("%s.%s.%s.***", parts[0], parts[1], parts[2]);
        }

        if (isValidIPv6(ip)) {
            String[] parts = ip.split(":");
            if (parts.length >= 4) {
                return String.format("%s:%s:%s:%s:****:****:****:****",
                        parts[0], parts[1], parts[2], parts[3]);
            }
        }

        return ip;
    }

    /**
     * 检查IP是否在指定的CIDR范围内
     *
     * @param ip   IP地址
     * @param cidr CIDR格式的网络地址（如：192.168.1.0/24）
     * @return 是否在范围内
     */
    public static boolean isInCIDRRange(String ip, String cidr) {
        if (!isValidIP(ip) || !StringUtils.hasText(cidr)) {
            return false;
        }

        try {
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                return false;
            }

            String networkIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            if (!isValidIPv4(ip) || !isValidIPv4(networkIp)) {
                return false; // 目前只支持IPv4
            }

            long ipLong = ipv4ToLong(ip);
            long networkLong = ipv4ToLong(networkIp);

            long mask = (-1L) << (32 - prefixLength);

            return (ipLong & mask) == (networkLong & mask);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取IP地址的网络部分
     *
     * @param ip           IP地址
     * @param prefixLength 前缀长度
     * @return 网络部分的IP地址
     */
    public static String getNetworkAddress(String ip, int prefixLength) {
        if (!isValidIPv4(ip) || prefixLength < 0 || prefixLength > 32) {
            throw new IllegalArgumentException("Invalid IP or prefix length");
        }

        long ipLong = ipv4ToLong(ip);
        long mask = (-1L) << (32 - prefixLength);
        long networkLong = ipLong & mask;

        return longToIPv4(networkLong);
    }
}