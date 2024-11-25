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

    public void addCookie(HttpServletResponse response, String name, String value, long maxAge) {
        logger.info("Adding cookie: {} with value: {} and maxAge: {}", name, value, maxAge);

        StringBuilder cookieBuilder = new StringBuilder();
        cookieBuilder.append(name).append("=").append(value).append("; ");
        cookieBuilder.append("HttpOnly; ");
        cookieBuilder.append("Path=/; ");
        cookieBuilder.append("Max-Age=").append(60*60*24).append("; ");
        cookieBuilder.append("SameSite=Lax; "); // 또는 "Strict"
        // HTTPS를 사용하지 않으므로 Secure 플래그 제거
        // if (isProduction) {
        //     cookieBuilder.append("Secure; ");
        // }

        response.addHeader("Set-Cookie", cookieBuilder.toString());
        logger.info("Set-Cookie header added: {}", cookieBuilder.toString());
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
//        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
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