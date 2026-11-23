package com.tikitaka.domain.auth.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tikitaka.domain.auth.jwt.AccessTokenProvider;
import com.tikitaka.domain.auth.jwt.AccessTokenProvider.AccessTokenInfo;
import com.tikitaka.domain.auth.jwt.JwtProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JwtAuthenticationFilter 역할
 *  - JWT 검증 및 인증 정보 등록
 *    1. 쿠키에서 Access Token 추출
 *    2. Token 검증
 *    3. 검증 성공 시 Token 정보 기반 로그인 정보 등록
 *    4. 다음 필터로 요청 전달 (토큰이 없거나 잘못돼도)
 *
 * [인증 성공 흐름]
 *  1. JwtAuthenticationFilter (Access Token 검증 기반 로그인 사용자로 등록)
 *  2. 스프링 시큐리티 권한 검사 (통과)
 *  3. 컨트롤러
 *
 * [인증 실패 흐름]
 * 1. JwtAuthenticationFilter (Access Token 검증 실패로 로그인 사용자 미등록)
 * 2. 스프링 시큐리티 권한 검사 (실패 - 로그인 사용자 등록 부재)
 * 3. ExceptionTranslationFilter (SecurityConfig에 등록된 AuthEntryPoint.commence 호출)
 *
 * OncePerRequestFilter 추상 클래스
 * - 하나의 HTTP 요청당 해당 필터의 내부 로직이 중복 실행되지 않도록 보장
 * - 불필요한 중복 필터 처리 방지
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AccessTokenProvider accessTokenProvider;
    private final JwtProperties jwtProperties;

    public JwtAuthenticationFilter(AccessTokenProvider accessTokenProvider, JwtProperties jwtProperties) {
        this.accessTokenProvider = accessTokenProvider;
        this.jwtProperties = jwtProperties;
    }


    /** 필터 처리 - 요청 쿠키의 Access Token 인증 처리 후, 다음 필터로 요청 전달
     *  request 인자: 요청 정보 확인용
     *  response 인자: 응답 제어용 (ex: 토큰 유효하지 않아 401응답해야 하는 경우)
     *  filterChain 인자: 다음 필터로 요청 전달용 */

    // 예외처리 throws 구조 파악하기!!!! (global/securityConfig에도 throws 있던데 파악하기 )
    // ServletException, IOException 이 예외가 무엇이고, 이런 예외 발생 가능성을 어떻게 파악하고 넣었는지
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 쿠키에서 추출한 Access 토큰
        String token = resolveToken(request);
        // 토큰 검증 및 인증 사용자 SecurityContext 등록
        accessTokenProvider.parseAccessToken(token).ifPresent(this::setAuthentication);
        // 다음 필터로 요청 전달 (설정한 필터 순서에 따라)
        filterChain.doFilter(request, response);
    }


    /** 쿠키에서 Access Token 값 추출 */
    private String resolveToken(HttpServletRequest request) {
        // 요청에 포함된 쿠키 추출
        Cookie[] cookies = request.getCookies();
        if (cookies == null) { return null; }

        // Access Token 쿠키 반환
        for (Cookie cookie : cookies) {
            if (jwtProperties.getAccessCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /** 인증 회원 SecurityContext에 등록 */
    private void setAuthentication(AccessTokenInfo tokenInfo) {
        // 검증된 회원 정보 기반 로그인 사용자 생성
        LoginUser loginUser = new LoginUser(tokenInfo.userId(), tokenInfo.role());

        // Spring Security가 제공하는 인증 객체 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    loginUser,      // 인증된 사용자 정보 (로그인 사용자 정보)
                    null,           // 인증에 사용한 자격증명 (pw 등 별도 보관 X)
                    List.of(new SimpleGrantedAuthority(tokenInfo.role().name()))); // 인증된 사용자의 자격 권한

        // SecurityContext에 인증 정보 등록
        SecurityContextHolder.  // 	SecurityContext에 접근하는 클래스
                getContext().   // 현재 요청의 SecurityContext 가져오기
                setAuthentication(authentication); // Context에 인증 객체 등록
    }
}
