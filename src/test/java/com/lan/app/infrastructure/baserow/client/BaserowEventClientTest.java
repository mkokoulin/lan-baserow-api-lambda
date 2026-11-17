package com.lan.app.infrastructure.baserow.client;

import com.baserow.dto.BaserowListResponse;
import com.lan.app.infrastructure.baserow.dto.BaserowEventRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Plain Mockito mock, not a hand-written class implementing {@link BaserowEventClient} —
 * a concrete implementor would get picked up by Quarkus's build-time Arc bean scanning
 * even without {@code @QuarkusTest}, since augmentation indexes the whole module.
 */
@DisplayName("BaserowEventClient.list() — pagination")
class BaserowEventClientTest {

    private static final int TABLE_ID = 992074;

    private static BaserowEventRow row(int id, String name) {
        return new BaserowEventRow(
            id, UUID.randomUUID(), name,
            null, null, "desc", null, null,
            null, null, true, List.of(), null, 1, true, true, false, null, List.of(), null, null
        );
    }

    @Test
    @DisplayName("walks every page until next() is null, instead of trusting the first 200 rows")
    void list_walksEveryPageUntilNextIsNull() {
        var client = mock(BaserowEventClient.class);
        when(client.list(eq(TABLE_ID))).thenCallRealMethod();
        when(client.listAll(eq(TABLE_ID), eq(1))).thenReturn(
            new BaserowListResponse<>(3, "has-more", null, List.of(row(1, "Event 1"), row(2, "Event 2"))));
        when(client.listAll(eq(TABLE_ID), eq(2))).thenReturn(
            new BaserowListResponse<>(3, null, "page1", List.of(row(3, "Event 3"))));

        var result = client.list(TABLE_ID);

        assertThat(result.results(), hasSize(3));
        assertThat(result.results().stream().map(BaserowEventRow::name).toList(),
            contains("Event 1", "Event 2", "Event 3"));
    }

    @Test
    @DisplayName("still filters out rows with a blank or missing name across all pages")
    void list_filtersOutBlankNames() {
        var client = mock(BaserowEventClient.class);
        when(client.list(eq(TABLE_ID))).thenCallRealMethod();
        when(client.listAll(eq(TABLE_ID), anyInt())).thenReturn(
            new BaserowListResponse<>(2, null, null, List.of(row(1, "Event 1"), row(2, ""), row(3, null))));

        var result = client.list(TABLE_ID);

        assertThat(result.results(), hasSize(1));
        assertThat(result.results().stream().map(BaserowEventRow::name).toList(), contains("Event 1"));
    }

    @Test
    @DisplayName("single page (next() == null on the first response) still works as before")
    void list_singlePage() {
        var client = mock(BaserowEventClient.class);
        when(client.list(eq(TABLE_ID))).thenCallRealMethod();
        when(client.listAll(eq(TABLE_ID), eq(1))).thenReturn(
            new BaserowListResponse<>(1, null, null, List.of(row(1, "Only Event"))));

        var result = client.list(TABLE_ID);

        assertThat(result.results(), hasSize(1));
    }
}
