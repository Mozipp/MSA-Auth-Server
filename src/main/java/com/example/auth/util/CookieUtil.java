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
        logger.info("Set cookie");
        Cookie cookie = new Cookie(name, value);
        logger.info("Set HttpOnly");
//        cookie.setHttpOnly(true);
//        cookie.setSecure(true);
        logger.info("Set Path");
        cookie.setPath("/");
        logger.info("Set MaxAge");
        cookie.setMaxAge(60*60*24);
        logger.info("Set Add Cookie");
        response.addCookie(cookie);
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