package com.lan.app.flows.meetingroom;

public final class MeetingFlowDef {
    public static final String FLOW             = "meetingroom";
    public static final String STEP_PROMPT      = "meeting:prompt";
    public static final String STEP_WAIT_DATE   = "meeting:wait_date";
    public static final String STEP_WAIT_START  = "meeting:wait_start";
    public static final String STEP_WAIT_END    = "meeting:wait_end";
    public static final String STEP_WAIT_CONTACT = "meeting:wait_contact";
    public static final String STEP_DONE        = "meeting:done";

    // callback_data prefixes
    public static final String CB_DATE_PFX  = "meet:date:";
    public static final String CB_START_PFX = "meet:start:";
    public static final String CB_END_PFX   = "meet:end:";

    static final int SLOT_START_HOUR = 10;
    static final int SLOT_END_HOUR   = 22;
    static final int SLOT_STEP_MIN   = 30;

    static final String KEY_DATE    = "meeting.date";
    static final String KEY_START   = "meeting.start";
    static final String KEY_END     = "meeting.end";

    private MeetingFlowDef() {}
}