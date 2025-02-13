package com.example.springboot.common;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 接口限流操作
 */

@Component
@Slf4j
public class MyFilter implements Filter {
    // 时间窗口

    // 1 秒之内允许通过 door 个请求

    private static volatile long startTime = System.currentTimeMillis();

    private static final long windowTime = 1000L;

    private static final int door = 100;

    private static final AtomicInteger bear = new AtomicInteger(0); // 原子计数器

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        int count = bear.incrementAndGet();  // 有一个请求，就+1
        if (count == 1) {   // 并发安全的
            startTime = System.currentTimeMillis();
        }
        // 发生了请求
        long now = System.currentTimeMillis();
//        log.info("拦截了请求， count： {}", count);
        log.info("时间窗口: {}ms, count: {}", (now - startTime), count);
        if (now - startTime <= windowTime) {
            if (count > door) {  // 超过了阈值
                // 限流操作
                log.info("拦截了请求, count: {}", count);
                HttpServletResponse response = (HttpServletResponse) servletResponse;
                response.setStatus(HttpStatus.OK.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
                response.getWriter().print(JSONUtil.toJsonStr(Result.error("402", "接口请求太频繁"))); // 设置返回值
                return;   // 返回，不放行
            }
        } else {
            // 重新进入下一个窗口
            startTime = System.currentTimeMillis();
            bear.set(1);
        }
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        filterChain.doFilter(servletRequest, servletResponse);  //  恭喜你，可以正常通过
        log.info("接口请求的路径：{}", request.getServletPath());
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

}
