package com.musicserver.websocket.service;

import com.musicserver.dto.websocket.UserOnlineStatusMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * 在线用户服务单元测试
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@ExtendWith(MockitoExtension.class)
class OnlineUserServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private OnlineUserService onlineUserService;

    private Long testUserId;
    private String testUsername;
    private String testSessionId;

    @BeforeEach
    void setUp() {
        testUserId = 1L;
        testUsername = "testuser";
        testSessionId = "test-session-123";

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void testAddOnlineUser() {
        // 执行测试
        onlineUserService.addOnlineUser(testUserId, testUsername, testSessionId);

        // 验证用户已添加到在线列表
        assertTrue(onlineUserService.isUserOnline(testUserId));

        // 验证Redis操作
        verify(valueOperations).set(anyString(), any(), anyLong(), any());
        verify(setOperations).add(anyString(), eq(testUserId));
    }

    @Test
    void testRemoveOnlineUser() {
        // 先添加用户
        onlineUserService.addOnlineUser(testUserId, testUsername, testSessionId);

        // 执行移除
        onlineUserService.removeOnlineUser(testUserId, testSessionId);

        // 验证用户已从在线列表移除
        assertFalse(onlineUserService.isUserOnline(testUserId));

        // 验证Redis操作
        verify(redisTemplate).delete(anyString());
        verify(setOperations).remove(anyString(), eq(testUserId));
    }

    @Test
    void testGetOnlineUsers() {
        // 添加测试用户
        onlineUserService.addOnlineUser(testUserId, testUsername, testSessionId);

        // 获取在线用户列表
        List<UserOnlineStatusMessage> onlineUsers = onlineUserService.getOnlineUsers();

        // 验证结果
        assertThat(onlineUsers).isNotEmpty();
        assertThat(onlineUsers.get(0).getUserId()).isEqualTo(testUserId);
        assertThat(onlineUsers.get(0).getUsername()).isEqualTo(testUsername);
    }

    @Test
    void testGetOnlineUserCount() {
        // 初始状态
        assertThat(onlineUserService.getOnlineUserCount()).isEqualTo(0);

        // 添加用户
        onlineUserService.addOnlineUser(testUserId, testUsername, testSessionId);

        // 验证计数
        assertThat(onlineUserService.getOnlineUserCount()).isEqualTo(1);
    }

    @Test
    void testIsUserOnline() {
        // 初始状态
        assertFalse(onlineUserService.isUserOnline(testUserId));

        // 添加用户后
        onlineUserService.addOnlineUser(testUserId, testUsername, testSessionId);
        assertTrue(onlineUserService.isUserOnline(testUserId));
    }

    @Test
    void testGetUserSession() {
        // 添加用户
        onlineUserService.addOnlineUser(testUserId, testUsername, testSessionId);

        // 获取会话信息
        OnlineUserService.UserSession session = onlineUserService.getUserSession(testUserId);

        // 验证会话信息
        assertThat(session).isNotNull();
        assertThat(session.getUserId()).isEqualTo(testUserId);
        assertThat(session.getUsername()).isEqualTo(testUsername);
        assertThat(session.getSessionId()).isEqualTo(testSessionId);
    }

    @Test
    void testGetUserIdBySession() {
        // 添加用户
        onlineUserService.addOnlineUser(testUserId, testUsername, testSessionId);

        // 根据会话ID获取用户ID
        Long userId = onlineUserService.getUserIdBySession(testSessionId);

        // 验证结果
        assertThat(userId).isEqualTo(testUserId);
    }

    @Test
    void testBroadcastUserOnline() {
        // 执行广播
        onlineUserService.broadcastUserOnline(testUserId, testUsername);

        // 验证消息发送
        verify(messagingTemplate).convertAndSend(eq("/topic/online/status"), any(Object.class));
    }

    @Test
    void testBroadcastUserOffline() {
        // 执行广播
        onlineUserService.broadcastUserOffline(testUserId, testUsername);

        // 验证消息发送
        verify(messagingTemplate).convertAndSend(eq("/topic/online/status"), any(Object.class));
    }

    @Test
    void testSendOnlineUsersToUser() {
        // 添加用户
        onlineUserService.addOnlineUser(testUserId, testUsername, testSessionId);

        // 发送在线用户列表
        onlineUserService.sendOnlineUsersToUser(testUserId);

        // 验证消息发送
        verify(messagingTemplate).convertAndSendToUser(
                eq(testUserId.toString()),
                eq("/queue/online/users"),
                any()
        );
    }

    @Test
    void testUpdateUserStatus() {
        // 添加用户
        onlineUserService.addOnlineUser(testUserId, testUsername, testSessionId);

        // 更新状态
        String newStatus = "AWAY";
        onlineUserService.updateUserStatus(testUserId, newStatus);

        // 验证状态更新
        OnlineUserService.UserSession session = onlineUserService.getUserSession(testUserId);
        assertThat(session.getStatus()).isEqualTo(newStatus);

        // 验证Redis操作
        verify(valueOperations, atLeast(2)).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void testUpdateUserActivity() {
        // 添加用户
        onlineUserService.addOnlineUser(testUserId, testUsername, testSessionId);

        // 更新活跃时间
        onlineUserService.updateUserActivity(testUserId);

        // 验证Redis操作
        verify(valueOperations, atLeast(2)).set(anyString(), any(), anyLong(), any());
    }

    @Test
    void testCleanExpiredSessions() {
        // 这个测试需要模拟过期会话，比较复杂
        // 这里只验证方法可以正常调用
        assertDoesNotThrow(() -> onlineUserService.cleanExpiredSessions());
    }
}