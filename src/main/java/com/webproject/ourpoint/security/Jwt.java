package com.webproject.ourpoint.security;

import java.util.Date;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Jwt {

    private final String issuer;

    //Rel3Bjce2MajBo09qgkNgYaTuzvJe8iwnBFhsDS5
    private final String clientSecret;

    //3600
    private final int expirySeconds;

    private final Algorithm algorithm;

    private final JWTVerifier jwtVerifier;

    public Jwt(String issuer, String clientSecret, int expirySeconds) {
        this.issuer = issuer;
        this.clientSecret = clientSecret;
        this.expirySeconds = expirySeconds;
        this.algorithm = Algorithm.HMAC512(clientSecret);
        this.jwtVerifier = com.auth0.jwt.JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }

    //새로운 액세스토큰을 만든다
    public String newAccessToken(Claims claims) {
        Date now = new Date();
        JWTCreator.Builder builder = com.auth0.jwt.JWT.create();
        builder.withIssuer(issuer);
        builder.withIssuedAt(now);
        if (expirySeconds > 0) { // 0이면 만료이기 때문에
            builder.withExpiresAt(new Date(now.getTime() + expirySeconds * 1_000L)); //1000L 곱하므로 1시간
        }
        builder.withClaim("userKey", claims.userKey);
        builder.withClaim("name", claims.name);
        builder.withClaim("email", claims.email);
        builder.withArrayClaim("roles", claims.roles);
        return builder.sign(algorithm);
    }

    public String newRefreshToken(Claims claims) {
        Date now = new Date();
        JWTCreator.Builder builder = com.auth0.jwt.JWT.create();
        builder.withIssuer(issuer);
        builder.withIssuedAt(now);
        if (expirySeconds > 0) { // 0이면 만료이기 때문에
            builder.withExpiresAt(new Date(now.getTime() + expirySeconds * 1_000L * 24 * 21)); //1000L * 24 * 24 곱하므로 3주
        }
        builder.withClaim("userKey", claims.userKey);
        builder.withClaim("name", claims.name);
        builder.withClaim("email", claims.email);
        builder.withArrayClaim("roles", claims.roles);
        return builder.sign(algorithm);
    }

    //만료된 또는 만료 직전의 토큰을 리프레쉬한다.
    public String refreshAccessToken(String token) throws JWTVerificationException {
        Claims claims = verify(token);
        claims.eraseIat();
        claims.eraseExp();
        return newAccessToken(claims);
    }

    public String refreshRefreshToken(String token) throws JWTVerificationException {
        Claims claims = verify(token);
        claims.eraseIat();
        claims.eraseExp();
        return newRefreshToken(claims);
    }

    public Claims verify(String token) throws JWTVerificationException {
        return new Claims(jwtVerifier.verify(token));
    }

    static public class Claims {
        Long userKey;
        String name;
        String email;
        String[] roles;
        Date iat;
        Date exp;

        private Claims() {
        }

        Claims(DecodedJWT decodedJWT) {
            Claim userKey = decodedJWT.getClaim("userKey");
            if (!userKey.isNull())
                this.userKey = userKey.asLong();
            Claim name = decodedJWT.getClaim("name");
            if (!name.isNull())
                this.name = name.asString();
            Claim email = decodedJWT.getClaim("email");
            if (!email.isNull())
                this.email = email.asString();
            Claim roles = decodedJWT.getClaim("roles");
            if (!roles.isNull())
                this.roles = roles.asArray(String.class);
            this.iat = decodedJWT.getIssuedAt();
            this.exp = decodedJWT.getExpiresAt();
        }

        public static Claims of(long userKey, String name, String email, String[] roles) {
            Claims claims = new Claims();
            claims.userKey = userKey;
            claims.name = name;
            claims.email = email;
            claims.roles = roles;
            return claims;
        }

        long iat() {
            return iat != null ? iat.getTime() : -1;
        }

        long exp() {
            return exp != null ? exp.getTime() : -1;
        }

        void eraseIat() {
            iat = null;
        }

        void eraseExp() {
            exp = null;
        }

    }
}
