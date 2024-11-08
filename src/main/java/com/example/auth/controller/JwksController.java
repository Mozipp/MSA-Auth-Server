    // JwksController.java
    package com.example.auth.controller;

    import com.example.auth.util.JwtUtil;
    import com.nimbusds.jose.jwk.RSAKey;
    import com.nimbusds.jose.jwk.JWKSet;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.RestController;

    import java.security.interfaces.RSAPublicKey;
    import java.util.Map;

    @RestController
    public class JwksController {

        private final RSAKey rsaKey;

        @Autowired
        public JwksController(JwtUtil jwtUtil) {
            RSAPublicKey publicKey = (RSAPublicKey) jwtUtil.getKeyPair().getPublic();
            this.rsaKey = new RSAKey.Builder(publicKey).keyID("rsa-key").build();
        }

        @GetMapping("/.well-known/jwks.json")
        public Map<String, Object> getJwks() {
            JWKSet jwkSet = new JWKSet(rsaKey);
            return jwkSet.toJSONObject();
        }
    }