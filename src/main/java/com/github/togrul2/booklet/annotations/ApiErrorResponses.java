package com.github.togrul2.booklet.annotations;

import com.github.togrul2.booklet.dtos.ErrorDto;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(
        value = {
                @ApiResponse(
                        responseCode = "400",
                        description = "Bad Request",
                        content = @Content(
                                mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)
                        )
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized",
                        content = @Content(
                                mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)
                        )
                ),
                @ApiResponse(
                        responseCode = "403",
                        description = "Forbidden",
                        content = @Content(
                                mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)
                        )
                ),
                @ApiResponse(
                        responseCode = "404",
                        description = "Not Found",
                        content = @Content(
                                mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)
                        )
                ),
                @ApiResponse(
                        responseCode = "405",
                        description = "Method Not Allowed",
                        content = @Content(
                                mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)
                        )
                ),
                @ApiResponse(
                        responseCode = "409",
                        description = "Internal Server Error",
                        content = @Content(
                                mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)
                        )
                ),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal Server Error",
                        content = @Content(
                                mediaType = "application/json", schema = @Schema(implementation = ErrorDto.class)
                        )
                )
        }
)
public @interface ApiErrorResponses {
}
