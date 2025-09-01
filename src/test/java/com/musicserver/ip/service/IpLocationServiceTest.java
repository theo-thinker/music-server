package com.musicserver.ip.service;

import com.musicserver.ip.config.IpLocationProperties;
import com.musicserver.ip.entity.IPInfo;
import com.musicserver.ip.entity.IPLocation;
import com.musicserver.ip.exception.IpValidationException;
import com.musicserver.ip.service.impl.IpLocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lionsoul.ip2region.xdb.Searcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * IP定位服务测试类
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@ExtendWith(MockitoExtension.class)
class IpLocationServiceTest {

    @Mock
    private Searcher ip2regionSearcher;

    @Mock
    private IpLocationProperties properties;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private IpLocationService ipLocationService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(properties.getEnableCache()).thenReturn(true);
        when(properties.getCacheKeyPrefix()).thenReturn("ip_location:");
        when(properties.getCacheExpireSeconds()).thenReturn(3600L);
        
        ipLocationService = new IpLocationServiceImpl(
                ip2regionSearcher, properties, redisTemplate, null);
    }

    @Test
    void testGetLocation_ValidIP() throws Exception {
        // Given
        String ip = "8.8.8.8";
        String regionResult = "美国|0|加利福尼亚|山景城|Google";
        
        when(ip2regionSearcher.search(ip)).thenReturn(regionResult);
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // When
        IPLocation result = ipLocationService.getLocation(ip);
        
        // Then
        assertNotNull(result);
        assertEquals(ip, result.getIp());
        assertEquals("美国", result.getCountry());
        assertEquals("加利福尼亚", result.getRegion());
        assertEquals("山景城", result.getCity());
        assertEquals("Google", result.getIsp());
        
        verify(ip2regionSearcher).search(ip);
    }

    @Test
    void testGetLocation_InvalidIP() {
        // Given
        String invalidIp = "invalid.ip";
        
        // When & Then
        assertThrows(IpValidationException.class, () -> {
            ipLocationService.getLocation(invalidIp);
        });
    }

    @Test
    void testGetLocation_NullIP() {
        // Given
        String nullIp = null;
        
        // When & Then
        assertThrows(IpValidationException.class, () -> {
            ipLocationService.getLocation(nullIp);
        });
    }

    @Test
    void testGetLocation_EmptyIP() {
        // Given
        String emptyIp = "";
        
        // When & Then
        assertThrows(IpValidationException.class, () -> {
            ipLocationService.getLocation(emptyIp);
        });
    }

    @Test
    void testGetIPInfo_ValidIP() throws Exception {
        // Given
        String ip = "192.168.1.1";
        String regionResult = "中国|0|北京|北京|电信";
        
        when(ip2regionSearcher.search(ip)).thenReturn(regionResult);
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // When
        IPInfo result = ipLocationService.getIPInfo(ip);
        
        // Then
        assertNotNull(result);
        assertEquals(ip, result.getIp());
        assertEquals("中国", result.getCountry());
        assertEquals("北京", result.getRegion());
        assertEquals("北京", result.getCity());
        assertEquals("电信", result.getIsp());
        assertEquals("IPv4", result.getIpType());
        assertTrue(result.getIsPrivate());
    }

    @Test
    void testIsValidIP() {
        // Valid IPv4
        assertTrue(ipLocationService.isValidIP("192.168.1.1"));
        assertTrue(ipLocationService.isValidIP("8.8.8.8"));
        assertTrue(ipLocationService.isValidIP("127.0.0.1"));
        
        // Valid IPv6
        assertTrue(ipLocationService.isValidIP("2001:db8::1"));
        assertTrue(ipLocationService.isValidIP("::1"));
        
        // Invalid IPs
        assertFalse(ipLocationService.isValidIP("invalid.ip"));
        assertFalse(ipLocationService.isValidIP("256.256.256.256"));
        assertFalse(ipLocationService.isValidIP(""));
        assertFalse(ipLocationService.isValidIP(null));
    }

    @Test
    void testIsPrivateIP() {
        // Private IPv4 addresses
        assertTrue(ipLocationService.isPrivateIP("192.168.1.1"));
        assertTrue(ipLocationService.isPrivateIP("10.0.0.1"));
        assertTrue(ipLocationService.isPrivateIP("172.16.0.1"));
        assertTrue(ipLocationService.isPrivateIP("127.0.0.1"));
        
        // Public IPv4 addresses
        assertFalse(ipLocationService.isPrivateIP("8.8.8.8"));
        assertFalse(ipLocationService.isPrivateIP("1.1.1.1"));
        
        // Invalid IPs
        assertFalse(ipLocationService.isPrivateIP("invalid.ip"));
    }

    @Test
    void testIsIPv6() {
        // IPv6 addresses
        assertTrue(ipLocationService.isIPv6("2001:db8::1"));
        assertTrue(ipLocationService.isIPv6("::1"));
        assertTrue(ipLocationService.isIPv6("fe80::1"));
        
        // IPv4 addresses
        assertFalse(ipLocationService.isIPv6("192.168.1.1"));
        assertFalse(ipLocationService.isIPv6("8.8.8.8"));
        
        // Invalid IPs
        assertFalse(ipLocationService.isIPv6("invalid.ip"));
    }

    @Test
    void testGetIPType() {
        assertEquals("IPv4", ipLocationService.getIPType("192.168.1.1"));
        assertEquals("IPv4", ipLocationService.getIPType("8.8.8.8"));
        assertEquals("IPv6", ipLocationService.getIPType("2001:db8::1"));
        assertEquals("IPv6", ipLocationService.getIPType("::1"));
        assertEquals("INVALID", ipLocationService.getIPType("invalid.ip"));
        assertEquals("INVALID", ipLocationService.getIPType(""));
        assertEquals("INVALID", ipLocationService.getIPType(null));
    }

    @Test
    void testRecordAccess() {
        // Given
        String ip = "192.168.1.1";
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
        String requestUri = "/api/test";
        Long userId = 1L;
        
        when(properties.getEnableStatistics()).thenReturn(true);
        
        // When
        assertDoesNotThrow(() -> {
            ipLocationService.recordAccess(ip, userAgent, requestUri, userId);
        });
        
        // Then
        // 验证记录方法被调用但不抛出异常
        verify(properties).getEnableStatistics();
    }

    @Test
    void testClearCache() {
        // When
        assertDoesNotThrow(() -> {
            ipLocationService.clearCache();
        });
        
        // Then
        // 验证方法执行成功
        verify(redisTemplate, atLeastOnce()).keys(anyString());
    }

    @Test
    void testGetHealthStatus() {
        // When
        var healthStatus = ipLocationService.getHealthStatus();
        
        // Then
        assertNotNull(healthStatus);
        assertTrue(healthStatus.containsKey("ip2regionStatus"));
        assertTrue(healthStatus.containsKey("redisStatus"));
        assertTrue(healthStatus.containsKey("localCacheStatus"));
        assertTrue(healthStatus.containsKey("configValid"));
    }
}