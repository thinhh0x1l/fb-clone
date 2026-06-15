package com.fb.infrastructure.messaging;

/**
 * Message queue topics
 */
public final class MessageTopic {

    private MessageTopic() {}

    // ==================== Post Events ====================
    public static final String POST_CREATED = "post.created";
    public static final String POST_UPDATED = "post.updated";
    public static final String POST_DELETED = "post.deleted";
    public static final String POST_REPORTED = "post.reported";

    // ==================== Friend Events ====================
    public static final String FRIEND_REQUEST = "friend.request";
    public static final String FRIEND_ACCEPTED = "friend.accepted";
    public static final String FRIEND_REMOVED = "friend.removed";

    // ==================== Message Events ====================
    public static final String MESSAGE_SENT = "message.sent";
    public static final String MESSAGE_READ = "message.read";

    // ==================== Notification Events ====================
    public static final String NOTIFICATION_PUSH = "notification.push";
    public static final String NOTIFICATION_EMAIL = "notification.email";

    // ==================== Analytics Events ====================
    public static final String ANALYTICS_POST = "analytics.post";
    public static final String ANALYTICS_USER = "analytics.user";
    public static final String ANALYTICS_SEARCH = "analytics.search";

    // ==================== Moderation Events ====================
    public static final String CONTENT_REPORTED = "content.reported";
    public static final String CONTENT_FLAGGED = "content.flagged";
    public static final String USER_BANNED = "user.banned";

    // ==================== Media Events ====================
    public static final String MEDIA_UPLOADED = "media.uploaded";
    public static final String MEDIA_PROCESSED = "media.processed";

    // ==================== Redis Channels ====================
    public static final String PRESENCE_CHANNEL = "channel:presence";
    public static final String TYPING_CHANNEL = "channel:typing";
    public static final String FEED_CHANNEL = "channel:feed";
}
