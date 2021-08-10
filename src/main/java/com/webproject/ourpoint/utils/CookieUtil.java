package com.webproject.ourpoint.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtil {

    public Cookie createCookie(String cookieName, String value) {
        Cookie token = new Cookie(cookieName,value);
        token.setHttpOnly(true);
        token.setMaxAge( (int)(3600 * 1_000L * 24 * 21) );
        token.setPath("/");
        return token;
    }

    public Cookie getTokenCookie(HttpServletRequest req, String cookieName) {
        final Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName))
                    return cookie;
            }
        }
        return null;
    }

}
