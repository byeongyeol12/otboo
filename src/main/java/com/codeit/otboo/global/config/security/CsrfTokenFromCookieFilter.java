package com.codeit.otboo.global.config.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CsrfTokenFromCookieFilter extends OncePerRequestFilter {

	private static final String CSRF_HEADER = "X-XSRF-TOKEN";
	private static final String CSRF_COOKIE = "XSRF-TOKEN";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		if (request.getHeader(CSRF_HEADER) == null && request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (CSRF_COOKIE.equals(cookie.getName())) {
					final String tokenValue = cookie.getValue();
					HttpServletRequestWrapper wrappedRequest = new HttpServletRequestWrapper(request) {

						@Override
						public String getHeader(String name) {
							if (CSRF_HEADER.equalsIgnoreCase(name)) {
								return tokenValue;
							}
							return super.getHeader(name);
						}

						@Override
						public Enumeration<String> getHeaders(String name) {
							if (CSRF_HEADER.equalsIgnoreCase(name)) {
								return Collections.enumeration(Collections.singletonList(tokenValue));
							}
							return super.getHeaders(name);
						}

						@Override
						public Enumeration<String> getHeaderNames() {
							Enumeration<String> original = super.getHeaderNames();
							return Collections.enumeration(
								Collections.list(original).stream()
									.filter(h -> !h.equalsIgnoreCase(CSRF_HEADER))
									.collect(Collectors.toList()) // 기존 헤더에서 X-XSRF-TOKEN 제외하고 다시 추가
							);
						}
					};
					filterChain.doFilter(wrappedRequest, response);
					return;
				}
			}
		}

		filterChain.doFilter(request, response);
	}
}

