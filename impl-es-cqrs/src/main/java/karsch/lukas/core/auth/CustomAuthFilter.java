package karsch.lukas.core.auth;

import karsch.lukas.context.RequestContext;
import karsch.lukas.context.UserFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

// TODO remove duplicate code in this package

@Component
@RequiredArgsConstructor
public class CustomAuthFilter {
    private final RequestContext requestContext;

    @Bean
    public UserFilter userFilter() {
        return new UserFilter(requestContext);
    }
}
