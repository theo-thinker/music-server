package com.musicserver.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;

/**
 * MyBatis Plus配置类
 * 
 * 配置MyBatis Plus相关功能，包括：
 * 1. 分页插件
 * 2. 乐观锁插件
 * 3. 防止全表更新删除插件
 * 4. 自动填充字段处理器
 * 5. Mapper接口扫描
 * 
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@MapperScan("com.musicserver.mapper")
public class MybatisPlusConfig {

    /**
     * MyBatis Plus拦截器配置
     * 添加分页、乐观锁、防攻击等插件
     * 
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 设置数据库类型
        paginationInnerInterceptor.setDbType(DbType.MYSQL);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInnerInterceptor.setMaxLimit(500L);
        // 溢出总页数后是否进行处理
        paginationInnerInterceptor.setOverflow(false);
        // 生成 countSql 优化掉 join 现在只支持左连接
        paginationInnerInterceptor.setOptimizeJoin(true);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 2. 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 3. 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        log.info("MyBatis Plus拦截器配置完成");
        return interceptor;
    }

    /**
     * 自动填充字段处理器
     * 在插入和更新时自动填充创建时间和更新时间
     */
    @Slf4j
    @Component
    public static class MyMetaObjectHandler implements MetaObjectHandler {

        /**
         * 插入时自动填充
         * 
         * @param metaObject 元对象
         */
        @Override
        public void insertFill(MetaObject metaObject) {
            log.debug("开始插入填充...");
            
            // 自动填充创建时间
            this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
            
            // 自动填充更新时间
            this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
            
            log.debug("插入填充完成");
        }

        /**
         * 更新时自动填充
         * 
         * @param metaObject 元对象
         */
        @Override
        public void updateFill(MetaObject metaObject) {
            log.debug("开始更新填充...");
            
            // 自动填充更新时间
            this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
            
            log.debug("更新填充完成");
        }
    }
}