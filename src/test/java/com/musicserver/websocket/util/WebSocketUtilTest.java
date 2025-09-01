package com.musicserver.websocket.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.musicserver.websocket.constant.WebSocketConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

/**
 * WebSocket工具类单元测试
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@ExtendWith(MockitoExtension.class)
class WebSocketUtilTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WebSocketUtil webSocketUtil;

    private Long testUserId;
    private String testDestination;
    private String testMessage;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        testDestination = "/queue/test";
        testMessage = "test message";
    }

    @Test
    void testSendToUser() {
        // 执行测试
        webSocketUtil.sendToUser(testUserId, testDestination, testMessage);

        // 验证消息发送
        verify(messagingTemplate).convertAndSendToUser(
                eq(testUserId.toString()),
                eq(testDestination),
                eq(testMessage)
        );
    }

    @Test
    void testSendWebSocketMessage() {
        // 执行测试
        webSocketUtil.sendWebSocketMessage(testUserId, testDestination, "TEST_TYPE", testMessage);

        // 验证消息发送
        verify(messagingTemplate).convertAndSendToUser(
                eq(testUserId.toString()),
                eq(testDestination),
                any()
        );
    }

    @Test
    void testBroadcast() {
        // 执行测试
        webSocketUtil.broadcast(testDestination, testMessage);

        // 验证消息广播
        verify(messagingTemplate).convertAndSend(eq(testDestination), eq(testMessage));
    }

    @Test
    void testBroadcastWebSocketMessage() {
        // 执行测试
        webSocketUtil.broadcastWebSocketMessage(testDestination, "TEST_TYPE", testMessage);

        // 验证消息广播
        verify(messagingTemplate).convertAndSend(eq(testDestination), any(Object.class));
    }

    @Test
    void testSendSuccessResponse() {
        // 执行测试
        webSocketUtil.sendSuccessResponse(testUserId, testDestination, "TEST_TYPE", testMessage);

        // 验证消息发送
        verify(messagingTemplate).convertAndSendToUser(
                eq(testUserId.toString()),
                eq(testDestination),
                any()
        );
    }

    @Test
    void testSendErrorResponse() {
        // 执行测试
        webSocketUtil.sendErrorResponse(testUserId, testDestination, "TEST_TYPE", "error message");

        // 验证消息发送
        verify(messagingTemplate).convertAndSendToUser(
                eq(testUserId.toString()),
                eq(testDestination),
                any()
        );
    }

    @Test
    void testSendHeartbeatResponse() {
        // 执行测试
        webSocketUtil.sendHeartbeatResponse(testUserId);

        // 验证消息发送
        verify(messagingTemplate).convertAndSendToUser(
                eq(testUserId.toString()),
                eq(WebSocketConstants.Destinations.QUEUE_HEARTBEAT),
                any()
        );
    }

    @Test
    void testSendSystemNotificationToUser() {
        // 执行测试 - 发送给指定用户
        webSocketUtil.sendSystemNotification(
                testUserId, 
                "Test Title", 
                "Test Content", 
                "TEST_TYPE", 
                "INFO"
        );

        // 验证消息发送
        verify(messagingTemplate).convertAndSendToUser(
                eq(testUserId.toString()),
                eq(WebSocketConstants.Destinations.QUEUE_NOTIFICATIONS),
                any()
        );
    }

    @Test
    void testSendSystemNotificationBroadcast() {
        // 执行测试 - 广播给所有用户
        webSocketUtil.sendSystemNotification(
                null, 
                "Test Title", 
                "Test Content", 
                "TEST_TYPE", 
                "INFO"
        );

        // 验证消息广播
        verify(messagingTemplate).convertAndSend(
                eq(WebSocketConstants.Destinations.TOPIC_SYSTEM_NOTIFICATIONS),
                any(Object.class)
        );
    }

    @Test
    void testSendForceLogoutNotification() {
        // 执行测试
        webSocketUtil.sendForceLogoutNotification(testUserId, "Force logout reason");

        // 验证消息发送
        verify(messagingTemplate).convertAndSendToUser(
                eq(testUserId.toString()),
                eq(WebSocketConstants.Destinations.QUEUE_SYSTEM_FORCE_LOGOUT),
                any()
        );
    }

    @Test
    void testBuildRoomDestination() {
        // 执行测试
        String roomId = "room123";
        String destination = webSocketUtil.buildRoomDestination(roomId);

        // 验证结果
        assertThat(destination).isEqualTo(WebSocketConstants.Destinations.TOPIC_MUSIC_ROOM + roomId);
    }

    @Test
    void testBuildChatRoomDestination() {
        // 执行测试
        String chatRoomId = "chat123";
        String destination = webSocketUtil.buildChatRoomDestination(chatRoomId);

        // 验证结果
        assertThat(destination).isEqualTo(WebSocketConstants.Destinations.TOPIC_CHAT_ROOM + chatRoomId);
    }

    @Test
    void testExtractRoomIdFromDestination() {
        // 执行测试
        String roomId = "room123";
        String destination = WebSocketConstants.Destinations.TOPIC_MUSIC_ROOM + roomId;
        String extractedRoomId = webSocketUtil.extractRoomIdFromDestination(destination);

        // 验证结果
        assertThat(extractedRoomId).isEqualTo(roomId);
    }

    @Test
    void testExtractChatRoomIdFromDestination() {
        // 执行测试
        String chatRoomId = "chat123";
        String destination = WebSocketConstants.Destinations.TOPIC_CHAT_ROOM + chatRoomId;
        String extractedChatRoomId = webSocketUtil.extractChatRoomIdFromDestination(destination);

        // 验证结果
        assertThat(extractedChatRoomId).isEqualTo(chatRoomId);
    }

    @Test
    void testFormatFileSize() {
        // 测试不同大小的文件
        assertThat(webSocketUtil.formatFileSize(0)).isEqualTo("0 B");
        assertThat(webSocketUtil.formatFileSize(1024)).isEqualTo("1.0 KB");
        assertThat(webSocketUtil.formatFileSize(1024 * 1024)).isEqualTo("1.0 MB");
        assertThat(webSocketUtil.formatFileSize(1024 * 1024 * 1024)).isEqualTo("1.0 GB");
    }

    @Test
    void testGenerateMessageId() {
        // 执行测试
        String messageId = webSocketUtil.generateMessageId("test", testUserId);

        // 验证结果
        assertThat(messageId).startsWith("test_");
        assertThat(messageId).contains("_" + testUserId);
    }

    @Test
    void testHasPermission() {
        // 执行测试（当前实现总是返回true）
        boolean hasPermission = webSocketUtil.hasPermission(testUserId, "TEST_PERMISSION");

        // 验证结果
        assertTrue(hasPermission);
    }

    @Test
    void testGetClientIpAddress() {
        // 准备测试数据
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("REMOTE_ADDRESS", "192.168.1.100");

        // 执行测试
        String ipAddress = webSocketUtil.getClientIpAddress(sessionAttributes);

        // 验证结果
        assertThat(ipAddress).isEqualTo("192.168.1.100");
    }

    @Test
    void testGetClientIpAddressUnknown() {
        // 准备测试数据
        Map<String, Object> sessionAttributes = new HashMap<>();

        // 执行测试
        String ipAddress = webSocketUtil.getClientIpAddress(sessionAttributes);

        // 验证结果
        assertThat(ipAddress).isEqualTo("unknown");
    }

    @Test
    void testGetDeviceType() {
        // 测试不同设备类型
        assertThat(webSocketUtil.getDeviceType("Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X)"))
                .isEqualTo(WebSocketConstants.DeviceType.MOBILE);
        
        assertThat(webSocketUtil.getDeviceType("Mozilla/5.0 (iPad; CPU OS 14_6 like Mac OS X)"))
                .isEqualTo(WebSocketConstants.DeviceType.TABLET);
        
        assertThat(webSocketUtil.getDeviceType("Mozilla/5.0 (Windows NT 10.0; Win64; x64)"))
                .isEqualTo(WebSocketConstants.DeviceType.WEB);
        
        assertThat(webSocketUtil.getDeviceType(""))
                .isEqualTo(WebSocketConstants.DeviceType.UNKNOWN);
        
        assertThat(webSocketUtil.getDeviceType(null))
                .isEqualTo(WebSocketConstants.DeviceType.UNKNOWN);
    }
}