package com.hmall.utils;
import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.hmall.common.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.bouncycastle.jcajce.BCFKSLoadStoreParameter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtTool {
    private final JWTSigner jwtSigner;

    public JwtTool(KeyPair keyPair) {
        this.jwtSigner = JWTSignerUtil.createSigner("rs256", keyPair);
    }

    /**
     * 创建 access-token
     *
     * @param  用户信息
     * @return access-token
     */
    private static SecretKey secretKey = Keys.hmacShaKeyFor("nVZ7X83)vOho*ABc0b]My#2J9./4k1Ww".getBytes());
    public String createToken(Long userId, Duration ttl) {
        //官方字段用set,自己定义的用claim
        String jwt = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 3600000*24))       //设置的是过期的时间点，而不是有效时长,所以可用当前时间加上有效时长
                .setSubject("LOGIN_USER")        //主题
                .claim("userId", userId)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }

    /**
     * 解析token
     *
     * @param token token
     * @return 解析刷新token得到的用户信息
     */
    public Claims parseToken(String token) {
        // 1.校验token是否为空
        if (token == null) {
            throw new UnauthorizedException("未登录");
        }
        try{
            //密码要匹配
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(secretKey).build();
            Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);  //这个对象能够获取到jwt中的payload
            return claimsJws.getBody();
        }catch (ExpiredJwtException e){
            throw new UnauthorizedException("无效的token");
        }catch (JwtException e){
            throw new UnauthorizedException("无效的token");
        }

    }
}