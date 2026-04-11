package com.lan.app.flows.meetingroom;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lan.app.session.Session;

import java.util.HashMap;
import java.util.Map;

/**
 * Typed accessors for meeting-related data stored in session.payloadJson.
 */
final class MeetingSession {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static String getDate(Session s)  { return get(s, MeetingFlowDef.KEY_DATE); }
    static String getStart(Session s) { return get(s, MeetingFlowDef.KEY_START); }
    static String getEnd(Session s)   { return get(s, MeetingFlowDef.KEY_END); }

    static void setDate(Session s, String v)  { put(s, MeetingFlowDef.KEY_DATE, v); }
    static void setStart(Session s, String v) { put(s, MeetingFlowDef.KEY_START, v); }
    static void setEnd(Session s, String v)   { put(s, MeetingFlowDef.KEY_END, v); }

    static void clear(Session s) {
        Map<String, String> data = load(s);
        data.remove(MeetingFlowDef.KEY_DATE);
        data.remove(MeetingFlowDef.KEY_START);
        data.remove(MeetingFlowDef.KEY_END);
        save(s, data);
    }

    private static String get(Session s, String key) {
        return load(s).get(key);
    }

    private static void put(Session s, String key, String value) {
        Map<String, String> data = load(s);
        data.put(key, value);
        save(s, data);
    }

    private static Map<String, String> load(Session s) {
        try {
            String json = s.getPayloadJson();
            if (json == null || json.isBlank() || json.equals("{}")) return new HashMap<>();
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static void save(Session s, Map<String, String> data) {
        try {
            s.setPayloadJson(MAPPER.writeValueAsString(data));
        } catch (Exception ignored) {}
    }

    private MeetingSession() {}
}