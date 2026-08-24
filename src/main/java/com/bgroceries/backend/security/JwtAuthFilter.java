package com.bgroceries.backend.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * NOTE: implements {@link Filter} directly (instead of
 * {@code OncePerRequestFilter}) because the {@code org.springframework.security.web.filter}
 * package is missing from the spring-security-web jars available on this network
 * (see RAYU.md "Environment gotchas"). The filter is registered once in the chain
 * via {@code SecurityConfig}, so once-per-request semantics still hold.
 *
 * <p><b>Inactivity auto-logout</b>: every authenticated request resets the
 * activity timer in {@link TokenActivityStore}. If more than 5 minutes pass
 * without any request the next call returns HTTP 401 with reason
 * {@code "SESSION_TIMEOUT"} and the token is evicted from the store so that
 * subsequent requests are also rejected.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements Filter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TokenActivityStore tokenActivityStore;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                if (jwtUtil.isTokenValid(token) && "ACCESS".equals(jwtUtil.extractType(token))) {

                    // ── Inactivity check ──────────────────────────────────────────────
                    if (!tokenActivityStore.isActive(token)) {
                        // Token has gone stale (5-min inactivity) or was never registered
                        // (e.g. issued before this feature was added). Evict & reject.
                        tokenActivityStore.evict(token);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write(
                                "{\"error\":\"SESSION_TIMEOUT\"," +
                                "\"message\":\"Your session expired due to inactivity. Please log in again.\"}");
                        return; // do NOT continue the filter chain
                    }

                    // ── Slide the activity window ─────────────────────────────────────
                    tokenActivityStore.touch(token);

                    // ── Authenticate in Spring Security context ───────────────────────
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        String phoneNumber = jwtUtil.extractPhoneNumber(token);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception ignored) {
                // Invalid/expired token -> request continues unauthenticated
                // and will be rejected downstream for protected endpoints.
            }
        }

        filterChain.doFilter(request, response);
    }
}
