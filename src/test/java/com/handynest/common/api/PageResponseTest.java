package com.handynest.common.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

class PageResponseTest {

  @Test
  void fromMapsSpringPageToApiEnvelope() {
    PageRequest pageRequest =
        PageRequest.of(1, 2, Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by("publicId")));
    PageImpl<String> page = new PageImpl<>(List.of("a", "b"), pageRequest, 5);

    PageResponse<String> response = PageResponse.from(page);

    assertEquals(List.of("a", "b"), response.content());
    assertEquals(1, response.page());
    assertEquals(2, response.size());
    assertEquals(5, response.totalElements());
    assertEquals(3, response.totalPages());
    assertFalse(response.first());
    assertFalse(response.last());
    assertEquals("createdAt,DESC;publicId,ASC", response.sort());
  }
}
