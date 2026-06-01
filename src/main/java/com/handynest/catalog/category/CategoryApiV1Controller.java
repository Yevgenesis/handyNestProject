package com.handynest.catalog.category;

import com.handynest.common.api.ApiConstants;
import com.handynest.common.i18n.SupportedLocaleResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.API_V1 + "/categories")
@Tag(name = "Categories", description = "Public category catalog")
public class CategoryApiV1Controller {

    private final CategoryQueryService categoryQueryService;

    @GetMapping
    @Operation(summary = "List categories", description = "Returns the public category tree.")
    public List<CategoryResponse> findAll(
            @RequestParam(required = false) String locale,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage
    ) {
        return categoryQueryService.findTree(SupportedLocaleResolver.resolve(locale, acceptLanguage));
    }

    @GetMapping("/{categoryId}")
    @Operation(summary = "Get category", description = "Returns one category by public id.")
    public CategoryResponse findById(
            @PathVariable String categoryId,
            @RequestParam(required = false) String locale,
            @RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage
    ) {
        return categoryQueryService.findByPublicId(
                categoryId,
                SupportedLocaleResolver.resolve(locale, acceptLanguage)
        );
    }
}
