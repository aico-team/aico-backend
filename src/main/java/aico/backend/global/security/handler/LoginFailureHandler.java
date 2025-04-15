package aico.backend.global.security.handler;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String errorMsg;

        if (exception instanceof UsernameNotFoundException) {
            errorMsg = "user does not exist";
        } else if (exception instanceof BadCredentialsException) {
            errorMsg = "wrong email or password";
        } else {
            errorMsg = "unknown error";
        }

        response.getWriter().write(errorMsg);
    }
}
