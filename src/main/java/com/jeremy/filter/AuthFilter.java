package com.jeremy.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Key;
import java.util.HashMap;

/**
 * Created by Jeremy on 2017/2/24.
 */
public class AuthFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${login-CheckURL}")
    private String loginCheckURL;
    @Value("${login-PageURL}")
    private String loginPageURL;

    private HashMap keyMap;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("filter init");
        keyMap = new HashMap();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        System.out.println("TestFilter");

        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        String uri = request.getRequestURI();
        if("/web".equals(uri)){
            try {
                String key = String.valueOf(keyMap.get(request.getParameter("username")));
                String token = String.valueOf(keyMap.get(request.getParameter("token")));
                Jwts.parser().setSigningKey(key).parseClaimsJws(token);

                //OK, we can trust this JWT
                System.out.println("suc");

                request.setAttribute("token",token);

            } catch (SignatureException e) {
                System.out.println("fail");
                request.removeAttribute("token");
                //don't trust the JWT!
            }
        }else if("/login".equals(uri)){
            Key key = MacProvider.generateKey();

            logger.debug("username:"+request.getParameter("fId"));

            String token = Jwts.builder()
                    .setSubject(request.getParameter("fId"))
                    .signWith(SignatureAlgorithm.HS512, key)
                    .compact();

            request.setAttribute("token",token);
            System.out.println(token);
        }
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {
        System.out.println("filter destory");

    }
}
