package com.jeremy.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.GenericFilterBean;

public class JwtFilter extends GenericFilterBean {

    @Value("${auth:true}")
    private String auth;

    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;

        String tradeCode = request.getParameter("tradeCode");
        if (!"users.selectByPrimaryKey".equals(tradeCode) && "true".equals(auth)) {
            final String authHeader = request.getParameter("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ServletException("Missing or invalid Authorization header.");
            }

            logger.debug("authHeader:"+authHeader);
            final String token = authHeader.substring(7); // The part after "Bearer "

            try {
                final Claims claims = Jwts.parser().setSigningKey("secretkey")
                    .parseClaimsJws(token).getBody();
                request.setAttribute("claims", claims);
            }
            catch (final SignatureException e) {
                throw new ServletException("Invalid token.");
            }

            logger.debug("jwt filter");
        }


        chain.doFilter(req, res);
    }

}
