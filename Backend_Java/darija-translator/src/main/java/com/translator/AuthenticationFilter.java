package com.translator;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.StringTokenizer;

@Provider 
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_PREFIX = "Basic ";

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        List<String> authHeader = requestContext.getHeaders().get(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.size() > 0) {
            String token = authHeader.get(0);
            token = token.replaceFirst(AUTHORIZATION_PREFIX, ""); 

            String decodedString = new String(Base64.getDecoder().decode(token));
            StringTokenizer tokenizer = new StringTokenizer(decodedString, ":");
            String username = tokenizer.nextToken();
            String password = tokenizer.nextToken();

            if (USERNAME.equals(username) && PASSWORD.equals(password)) {
                return; 
            }
        }

        Response unauthorizedStatus = Response
                .status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Accès refusé. Authentification requise (admin/admin).\"}")
                .header("WWW-Authenticate", "Basic realm=\"Darija Translator\"")
                .build();

        requestContext.abortWith(unauthorizedStatus);
    }
}