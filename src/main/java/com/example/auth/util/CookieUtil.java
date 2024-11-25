// CookieUtil.java
package com.example.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CookieUtil {

    private static final Logger logger = LoggerFactory.getLogger(CookieUtil.class);

    private static final String DOMAIN = ".multi-learn.com"; // 도메인 설정

    public void addCookie(HttpServletResponse response, String name, String value, long maxAge) {
        try {
            logger.debug("Adding cookie: name={}, value={}, maxAge={}", name, value, maxAge);
            StringBuilder cookieBuilder = new StringBuilder();
            cookieBuilder.append(name).append("=").append(value).append(";");
            cookieBuilder.append("Path=/;");
            cookieBuilder.append("Max-Age=").append(maxAge).append(";");
            cookieBuilder.append("HttpOnly;");
            cookieBuilder.append("Secure;");
            cookieBuilder.append("SameSite=None;");
            cookieBuilder.append("Domain=").append(DOMAIN).append(";");

            response.addHeader("Set-Cookie", cookieBuilder.toString());
            logger.debug("Cookie added successfully via Set-Cookie header: {}", cookieBuilder.toString());
        } catch (Exception e) {
            logger.error("Failed to add cookie: name={}, error={}", name, e.getMessage());
            throw e; // 예외를 다시 던져 상위 메서드에서도 처리할 수 있게 함
        }
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        try {
            logger.debug("Deleting cookie: name={}", name);
            StringBuilder cookieBuilder = new StringBuilder();
            cookieBuilder.append(name).append("=;"); // 값은 비웁니다
            cookieBuilder.append("Path=/;");
            cookieBuilder.append("Max-Age=0;");
            cookieBuilder.append("HttpOnly;");
            cookieBuilder.append("Secure;");
            cookieBuilder.append("SameSite=None;");
            cookieBuilder.append("Domain=").append(DOMAIN).append(";");

            response.addHeader("Set-Cookie", cookieBuilder.toString());
            logger.debug("Cookie deleted successfully via Set-Cookie header: {}", cookieBuilder.toString());
        } catch (Exception e) {
            logger.error("Failed to delete cookie: name={}, error={}", name, e.getMessage());
            throw e;
        }
    }

    public String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}