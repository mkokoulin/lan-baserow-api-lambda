package com.lan.app.infrastructure.baserow.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BaserowFile(
    String url,
    Map<String, Thumbnail> thumbnails,
    @JsonProperty("visible_name") String visibleName,
    String name,
    long size,
    @JsonProperty("mime_type") String mimeType,
    @JsonProperty("is_image") boolean isImage,
    @JsonProperty("image_width") Integer imageWidth,
    @JsonProperty("image_height") Integer imageHeight,
    @JsonProperty("uploaded_at") OffsetDateTime uploadedAt
) {}