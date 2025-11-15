package karsch.lukas.auth;

import karsch.lukas.context.RequestContext;
import karsch.lukas.context.UserFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthFilter {
    private final RequestContext requestContext;

    @Bean
    public UserFilter userFilter() {
        return new UserFilter(requestContext);
    }
}
