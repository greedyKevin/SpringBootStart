package com.augurit.service.utils.jwt;


import com.augurit.service.entity.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.util.StringUtils;

/**
 * jwt token生成器
 *
 * @author huang jiahui
 * @date 2021/12/7 13:35
 */
public class JwtUtils {

    private static final Algorithm algorithm = Algorithm.HMAC256("KLJOi3u2904h^&RT%^&RVBJH234202x");
    private static final String ISSUER = "Augur";

    public static String createJwtToken(User user) {

        String token = JWT.create()
                .withIssuer(ISSUER)
                .withClaim("user", user.getId())
                .sign(algorithm);

        return token;
    }


    public static String verifyJwtToken(String token){
        if (token == null) {
            return null;
        }

        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();

        DecodedJWT decodedJWT = verifier.verify(token);

        String result = decodedJWT.getClaim("user").asString();

        if(!StringUtils.hasText(result)){
            return null;
        }

        return result;
    }

}
