package com.musicserver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.MultipartConfigElement;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * Web配置类
 * <p>
 * 配置Web相关设置，包括：
 * 1. 静态资源映射
 * 2. 文件上传配置
 * 3. Jackson序列化配置
 * 4. HTTP消息转换器
 *
 * @author Music Server Development Team
 * @version 1.0.0
 * @since 2025-09-01
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 文件上传路径
     */
    @Value("${app.file.upload-path:/data/music-server/uploads/}")
    private String uploadPath;

    /**
     * 静态资源访问路径
     */
    @Value("${app.file.static-access-path:/static/**}")
    private String staticAccessPath;

    /**
     * 静态资源映射路径
     */
    @Value("${app.file.static-location:file:/data/music-server/uploads/}")
    private String staticLocation;

    /**
     * 最大文件大小
     */
    @Value("${app.file.max-size:100}")
    private long maxFileSize;

    /**
     * 配置静态资源处理器
     * 将静态资源URL映射到文件系统路径
     *
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置文件上传的静态资源访问
        registry.addResourceHandler(staticAccessPath)
                .addResourceLocations(staticLocation)
                .setCachePeriod(3600); // 缓存1小时

        // 配置Knife4j静态资源
        registry.addResourceHandler("doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        log.info("静态资源映射配置完成 - 访问路径: {}, 映射路径: {}", staticAccessPath, staticLocation);
    }

    /**
     * 配置HTTP消息转换器
     * 自定义Jackson的序列化配置
     *
     * @param converters HTTP消息转换器列表
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建Jackson转换器
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter =
                new MappingJackson2HttpMessageConverter();

        // 设置自定义的ObjectMapper
        jackson2HttpMessageConverter.setObjectMapper(objectMapper());

        // 添加到转换器列表的首位，优先使用
        converters.addFirst(jackson2HttpMessageConverter);

        log.info("HTTP消息转换器配置完成");
    }

    /**
     * 自定义ObjectMapper配置
     * 配置JSON序列化和反序列化规则
     *
     * @return 配置好的ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册Java8时间模块
        mapper.registerModule(new JavaTimeModule());

        // 禁用将日期序列化为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 设置日期时间格式
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 设置时区
        mapper.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        // 忽略未知属性
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 忽略空Bean转JSON的错误
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 忽略null值字段
        mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

        log.info("ObjectMapper配置完成");
        return mapper;
    }

    /**
     * 文件上传配置
     * 设置文件上传的大小限制和临时目录
     *
     * @return 文件上传配置元素
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // 设置单个文件最大大小
        factory.setMaxFileSize(DataSize.ofMegabytes(maxFileSize));

        // 设置总上传数据最大大小
        factory.setMaxRequestSize(DataSize.ofMegabytes(maxFileSize * 2));

        // 设置缓冲区大小
        factory.setFileSizeThreshold(DataSize.ofKilobytes(2));

        // 设置临时目录
        factory.setLocation(uploadPath + "temp/");

        log.info("文件上传配置完成 - 最大文件大小: {}MB, 上传路径: {}", maxFileSize, uploadPath);
        return factory.createMultipartConfig();
    }
}