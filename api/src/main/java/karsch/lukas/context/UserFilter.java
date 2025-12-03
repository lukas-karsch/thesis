package karsch.lukas.context;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.UUID;

/**
 * Custom request filter that adds user ID and user type ("professor" or "student") to the request.
 * Note that this filter does not check whether the user actually exists.
 */
@RequiredArgsConstructor
public class UserFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(UserFilter.class);

    public static final String CUSTOM_AUTH_HEADER_NAME = "customAuth";

    private final RequestContext requestContext;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        setRequestContext(request);
        chain.doFilter(request, response);
    }

    private void setRequestContext(ServletRequest request) {
        if (!(request instanceof HttpServletRequest httpServletRequest)) {
            return;
        }

        final String headerValue = httpServletRequest.getHeader(CUSTOM_AUTH_HEADER_NAME);

        if (headerValue == null || headerValue.isBlank()) {
            return;
        }

        // expected format: "prof_<uuid>"
        var parts = headerValue.split("_");
        if (parts.length != 2) {
            return;
        }

        try {
            var type = parts[0];
            Assert.isTrue(
                    "professor".equals(type) || "student".equals(type),
                    "Authorization type must be either 'professor' or 'student'"
            );
            var id = UUID.fromString(parts[1]);

            requestContext.setUserType(type);
            requestContext.setUserId(id);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID parsed from custom authorization header", e);
        }
    }
}
