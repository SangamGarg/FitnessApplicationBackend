package com.fitnessapp.DeveloperAndTestingService.devUserAndAuthDevService.devAuthInterceptorConfig;

import com.fitnessapp.DeveloperAndTestingService.devUserAndAuthDevService.DevUserEntity;
import com.fitnessapp.DeveloperAndTestingService.devUserAndAuthDevService.DevUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

//This is the interceptor which validates that the developer is authenticated so that it can perform the necessary actions that is done by the developer service -- "api/v1/dev" end points


@Component
@Slf4j
public class DevApiInterceptor implements HandlerInterceptor {

    private final DevUserService userTestingService;

    public DevApiInterceptor(DevUserService userTestingService) {
        this.userTestingService = userTestingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String name = request.getHeader("X-NAME");
        String apiKey = request.getHeader("X-API-KEY");

        if (name == null || apiKey == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing X-NAME or X-API-KEY in headers");
            return false;
        }

        List<DevUserEntity> users = userTestingService.getUsersForDevAndTestForInterceptor();

        if (users.isEmpty()) {
            log.error("Users is empty");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Users list is empty");
            return false;
        } else {
            log.info("Users >>>>>", users);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        // Check if user exists with matching name and key
        boolean validUser = users.stream()
                .anyMatch(user -> user.getDevName().equalsIgnoreCase(name)
                        &&
                        encoder.matches(apiKey, user.getKey()) // apiKey = raw, user.getKey() = hashed
                );

        if (validUser) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Invalid API key or user name");
        return false;
    }


//
//    //I have to change this in the application.properties
//    @Value("${user.creation.secret.key}")
//    private String REQUIRED_API_KEY; // Set your desired key

//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String apiKey = request.getHeader("X-API-KEY"); // or use request.getParameter("key")
//
//        if (REQUIRED_API_KEY.equals(apiKey)) {
//            return true; // Allow the request to proceed
//        }
//
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.getWriter().write("Invalid or missing API Key");
//        return false;
//    }


}
