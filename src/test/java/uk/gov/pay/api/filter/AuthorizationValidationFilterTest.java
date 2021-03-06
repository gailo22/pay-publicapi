package uk.gov.pay.api.filter;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;
import static uk.gov.pay.api.utils.ApiKeyGenerator.apiKeyValueOf;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationValidationFilterTest {

    private static final String SECRET_KEY = "mysupersecret";
    private AuthorizationValidationFilter authorizationValidationFilter;

    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockFilterChain;

    @Before
    public void setup() {
        authorizationValidationFilter = new AuthorizationValidationFilter(SECRET_KEY);
    }

    @Test
    public void shouldProcessFilterChain_whenAuthorizationHeaderIsValid() throws Exception {

        String validToken = "asdfghdasd";
        String authorization = "Bearer " + apiKeyValueOf(validToken, SECRET_KEY);

        when(mockRequest.getHeader("Authorization")).thenReturn(authorization);

        authorizationValidationFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void shouldRejectRequest_with401ResponseError_whenAuthorizationHeaderIsInvalid() throws Exception {

        String invalidApiKey = "asdfghdasdakjshdkjwhdjweghrhjgwerguweurweruhiweuiweriuui";
        String authorization = "Bearer " + invalidApiKey;

        when(mockRequest.getHeader("Authorization")).thenReturn(authorization);

        authorizationValidationFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verifyZeroInteractions(mockFilterChain);
        verify(mockResponse).sendError(401, "Unauthorized");
    }

    @Test
    public void shouldRejectRequest_with401ResponseError_whenAuthorizationHeaderIsNotPresent() throws Exception {

        when(mockRequest.getHeader("Authorization")).thenReturn(null);

        authorizationValidationFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verifyZeroInteractions(mockFilterChain);
        verify(mockResponse).sendError(401, "Unauthorized");
    }

    @Test
    public void shouldRejectRequest_with401ResponseError_whenAuthorizationHeaderHasInvalidFormat() throws Exception {

        String validToken = "asdfghdasd";
        String authorization = "Bearer" + apiKeyValueOf(validToken, SECRET_KEY);

        when(mockRequest.getHeader("Authorization")).thenReturn(authorization);

        authorizationValidationFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verifyZeroInteractions(mockFilterChain);
        verify(mockResponse).sendError(401, "Unauthorized");
    }

    @Test
    public void shouldRejectRequest_with401ResponseError_whenAuthorizationHeaderHasNotMinimumLengthExpected() throws Exception {

        String apiKey = RandomStringUtils.randomAlphanumeric(32);
        String authorization = "Bearer " + apiKey;

        when(mockRequest.getHeader("Authorization")).thenReturn(authorization);

        authorizationValidationFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verifyZeroInteractions(mockFilterChain);
        verify(mockResponse).sendError(401, "Unauthorized");
    }
}
