package com.lan.app.infrastructure.baserow.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BaserowDtoDeserializationTest {

    static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void baserowListResponse_deserializesResults() throws Exception {
        var json = """
            {"count":2,"next":null,"previous":null,"results":[{"id":1,"value":"a"},{"id":2,"value":"b"}]}
            """;
        var type = mapper.getTypeFactory()
            .constructParametricType(BaserowListResponse.class, BaserowSelectOption.class);
        BaserowListResponse<BaserowSelectOption> response = mapper.readValue(json, type);

        assertEquals(2, response.count());
        assertNull(response.next());
        assertNull(response.previous());
        assertEquals(2, response.results().size());
        assertEquals(1, response.results().get(0).id());
        assertEquals("a", response.results().get(0).value());
    }

    @Test
    void baserowListResponse_withNextPage() throws Exception {
        var json = """
            {"count":100,"next":"https://api.baserow.io/api/database/rows/table/1/?page=2","previous":null,"results":[]}
            """;
        var type = mapper.getTypeFactory()
            .constructParametricType(BaserowListResponse.class, BaserowSingleSelect.class);
        BaserowListResponse<BaserowSingleSelect> response = mapper.readValue(json, type);

        assertEquals(100, response.count());
        assertNotNull(response.next());
        assertTrue(response.results().isEmpty());
    }

    @Test
    void baserowSingleSelect_deserializesWithColor() throws Exception {
        var json = """
            {"id":3,"value":"Active","color":"green"}
            """;
        var result = mapper.readValue(json, BaserowSingleSelect.class);

        assertEquals(3, result.id());
        assertEquals("Active", result.value());
        assertEquals("green", result.color());
    }

    @Test
    void baserowSingleSelect_ignoresUnknownFields() throws Exception {
        var json = """
            {"id":1,"value":"X","color":"red","extra_field":"ignored"}
            """;
        assertDoesNotThrow(() -> mapper.readValue(json, BaserowSingleSelect.class));
    }

    @Test
    void baserowSelectOption_deserializesWithoutColor() throws Exception {
        var json = """
            {"id":5,"value":"Option"}
            """;
        var result = mapper.readValue(json, BaserowSelectOption.class);

        assertEquals(5, result.id());
        assertEquals("Option", result.value());
    }

    @Test
    void baserowSelectOption_ignoresUnknownFields() throws Exception {
        var json = """
            {"id":1,"value":"X","color":"blue"}
            """;
        assertDoesNotThrow(() -> mapper.readValue(json, BaserowSelectOption.class));
    }

    @Test
    void baserowLinkToTable_deserializesIdAndValue() throws Exception {
        var json = """
            {"id":42,"value":"Row name"}
            """;
        var result = mapper.readValue(json, BaserowLinkToTable.class);

        assertEquals(42, result.id());
        assertEquals("Row name", result.value());
    }

    @Test
    void baserowFile_deserializesAllFields() throws Exception {
        var json = """
            {
              "url": "https://files.baserow.io/user_files/abc.jpg",
              "thumbnails": {"tiny": {"url": "https://files.baserow.io/thumb.jpg", "width": 21, "height": 21}},
              "visible_name": "photo.jpg",
              "name": "abc.jpg",
              "size": 102400,
              "mime_type": "image/jpeg",
              "is_image": true,
              "image_width": 1024,
              "image_height": 768,
              "uploaded_at": "2024-01-15T10:30:00+00:00"
            }
            """;
        var result = mapper.readValue(json, BaserowFile.class);

        assertEquals("https://files.baserow.io/user_files/abc.jpg", result.url());
        assertEquals("photo.jpg", result.visibleName());
        assertEquals("abc.jpg", result.name());
        assertEquals(102400L, result.size());
        assertEquals("image/jpeg", result.mimeType());
        assertTrue(result.isImage());
        assertEquals(1024, result.imageWidth());
        assertEquals(768, result.imageHeight());
        assertNotNull(result.uploadedAt());
        assertNotNull(result.thumbnails());
        var tiny = result.thumbnails().get("tiny");
        assertNotNull(tiny);
        assertEquals("https://files.baserow.io/thumb.jpg", tiny.url());
        assertEquals(21, tiny.width());
        assertEquals(21, tiny.height());
    }

    @Test
    void baserowFile_ignoresUnknownFields() throws Exception {
        var json = """
            {
              "url": "https://files.baserow.io/f.pdf",
              "visible_name": "doc.pdf",
              "name": "f.pdf",
              "size": 512,
              "mime_type": "application/pdf",
              "is_image": false,
              "uploaded_at": "2024-01-15T10:30:00+00:00",
              "unknown_key": "should be ignored"
            }
            """;
        assertDoesNotThrow(() -> mapper.readValue(json, BaserowFile.class));
    }

    @Test
    void thumbnail_deserializes() throws Exception {
        var json = """
            {"url":"https://files.baserow.io/t.jpg","width":128,"height":128}
            """;
        var result = mapper.readValue(json, Thumbnail.class);

        assertEquals("https://files.baserow.io/t.jpg", result.url());
        assertEquals(128, result.width());
        assertEquals(128, result.height());
    }

    @Test
    void thumbnail_ignoresUnknownFields() throws Exception {
        var json = """
            {"url":"https://x.com/t.jpg","width":64,"height":64,"extra":"value"}
            """;
        assertDoesNotThrow(() -> mapper.readValue(json, Thumbnail.class));
    }

    @Test
    void baserowCollaborator_deserializesIdAndName() throws Exception {
        var json = """
            {"id":7,"name":"Jane Doe"}
            """;
        var result = mapper.readValue(json, BaserowCollaborator.class);

        assertEquals(7, result.id());
        assertEquals("Jane Doe", result.name());
    }

    @Test
    void baserowCollaborator_ignoresUnknownFields() throws Exception {
        var json = """
            {"id":1,"name":"John","email":"john@example.com"}
            """;
        assertDoesNotThrow(() -> mapper.readValue(json, BaserowCollaborator.class));
    }

    @Test
    void baserowErrorResponse_deserializesErrorAndDetail() throws Exception {
        var json = """
            {"error":"ERROR_ROW_DOES_NOT_EXIST","detail":"The row with id 99 does not exist."}
            """;
        var result = mapper.readValue(json, BaserowErrorResponse.class);

        assertEquals("ERROR_ROW_DOES_NOT_EXIST", result.error());
        assertEquals("The row with id 99 does not exist.", result.detail());
    }

    @Test
    void baserowErrorResponse_ignoresUnknownFields() throws Exception {
        var json = """
            {"error":"ERROR_NO_PERMISSION","detail":"No permission.","extra":"x"}
            """;
        assertDoesNotThrow(() -> mapper.readValue(json, BaserowErrorResponse.class));
    }

    @Test
    void baserowListResponse_usedWithCollaborators() throws Exception {
        var json = """
            {"count":1,"next":null,"previous":null,"results":[{"id":1,"name":"Alice"}]}
            """;
        var type = mapper.getTypeFactory()
            .constructParametricType(BaserowListResponse.class, BaserowCollaborator.class);
        BaserowListResponse<BaserowCollaborator> response = mapper.readValue(json, type);

        assertEquals(1, response.count());
        assertEquals("Alice", response.results().get(0).name());
    }

    @Test
    void baserowListResponse_usedWithFiles() throws Exception {
        var json = """
            {"count":1,"next":null,"previous":null,"results":[
              {"url":"https://x.com/f.jpg","visible_name":"f.jpg","name":"f.jpg",
               "size":100,"mime_type":"image/jpeg","is_image":true,
               "uploaded_at":"2024-01-15T10:30:00+00:00"}
            ]}
            """;
        var type = mapper.getTypeFactory()
            .constructParametricType(BaserowListResponse.class, BaserowFile.class);
        BaserowListResponse<BaserowFile> response = mapper.readValue(json, type);

        assertEquals(1, response.count());
        assertEquals("f.jpg", response.results().get(0).name());
    }
}
