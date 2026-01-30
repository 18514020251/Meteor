package com.meteor.message.constants;

/**
 * @author Programmer
 * @date 2026-01-30 19:03
 */
public final class MessageConstants {

    private MessageConstants() {}

    public static final int SOURCE_SYSTEM = 0;
    public static final int SOURCE_EVENT  = 1;

    public static final int READ_UNREAD = 0;
    public static final int READ_READ   = 1;

    public static final int DELETED_NO  = 0;
    public static final int DELETED_YES = 1;

    public static final String BIZ_KEY_EVENT_PREFIX = "evt:";
    public static final String PLACEHOLDER_OCCURRED_AT = "{occurredAt}";
}
