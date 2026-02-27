package com.sbsolutions.components;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoCacheFilterTest {

  @Mock HttpServletRequest  request;
  @Mock HttpServletResponse response;
  @Mock FilterChain         chain;

  private final NoCacheFilter filter = new NoCacheFilter();

  @Test
  void doFilter_setsCacheControlHeader() throws Exception {
    filter.doFilter(request, response, chain);

    verify(response).setHeader("Cache-Control",
        "no-store, no-cache, must-revalidate, max-age=0");
  }

  @Test
  void doFilter_setsPragmaHeader() throws Exception {
    filter.doFilter(request, response, chain);

    verify(response).setHeader("Pragma", "no-cache");
  }

  @Test
  void doFilter_setsExpiresHeaderToZero() throws Exception {
    ArgumentCaptor<Long> expiresCaptor = ArgumentCaptor.forClass(Long.class);

    filter.doFilter(request, response, chain);

    verify(response).setDateHeader(eq("Expires"), expiresCaptor.capture());
    assertThat(expiresCaptor.getValue()).isEqualTo(0L);
  }

  @Test
  void doFilter_alwaysCallsChain() throws Exception {
    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  void doFilter_chainsEvenAfterHeadersSet() throws Exception {
    // Verify ordering: headers are set before chain is called
    var order = inOrder(response, chain);

    filter.doFilter(request, response, chain);

    order.verify(response).setHeader(eq("Cache-Control"), anyString());
    order.verify(response).setHeader(eq("Pragma"), anyString());
    order.verify(response).setDateHeader(eq("Expires"), anyLong());
    order.verify(chain).doFilter(request, response);
  }
}
