package karsch.lukas.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import karsch.lukas.uuid.UuidUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserFilterTest {

    @Test
    void testDoFilter_shouldAssignUserTypeAndUserId() throws ServletException, IOException {
        var requestContext = new RequestContext();

        var underTest = new UserFilter(requestContext);
        var request = mock(HttpServletRequest.class);
        var uuid = UuidUtils.randomV7();
        when(request.getHeader(UserFilter.CUSTOM_AUTH_HEADER_NAME)).thenReturn("professor_" + uuid);

        underTest.doFilter(request, null, mock(FilterChain.class));

        assertThat(requestContext.getUserId()).isEqualTo(uuid);
        assertThat(requestContext.getUserType()).isEqualTo("professor");
    }

    @Test
    void testDoFilter_shouldThrow_whenInvalidUserType() {
        var requestContext = new RequestContext();

        var underTest = new UserFilter(requestContext);
        var request = mock(HttpServletRequest.class);
        when(request.getHeader(UserFilter.CUSTOM_AUTH_HEADER_NAME)).thenReturn("wrong_type_1");

        assertThatThrownBy(
                () -> underTest.doFilter(request, null, mock(FilterChain.class))
        ).isInstanceOf(IllegalArgumentException.class);
    }
}
