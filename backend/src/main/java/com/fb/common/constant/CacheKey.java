package com.fb.common.constant;

/**
 * Các hằng số khóa bộ nhớ đệm Redis (Cache Key Constants).
 *
 * Định nghĩa tất cả các tiền tố khóa được sử dụng trong Redis
 * để quản lý bộ nhớ đệm một cách tập trung và nhất quán.
 */
public final class CacheKey {

    private CacheKey() {}

    // ==================== Luồng tin tức (Feed) ====================
    /** Tiền tố khóa cho luồng tin tức */
    public static final String FEED_PREFIX = "feed:";
    /** Khóa phân phối bài viết đến người theo dõi */
    public static final String FEED_FANOUT = "feed:fanout:";
    /** Khóa luồng tin tức cho người nổi tiếng (người theo dõi > 10.000) */
    public static final String FEED_CELEBRITY = "feed:celebrity:";
    /** Khóa thời gian hết hạn của bài viết trong luồng tin tức */
    public static final String FEED_TTL_PREFIX = "feed:ttl:";

    // ==================== Người dùng (User) ====================
    /** Tiền tố khóa cho người dùng */
    public static final String USER_PREFIX = "user:";
    /** Khóa ánh xạ tên đăng nhập -> mã người dùng */
    public static final String USER_USERNAME_PREFIX = "user:username:";
    /** Khóa phiên đăng nhập của người dùng */
    public static final String USER_SESSION_PREFIX = "user:session:";
    /** Khóa trạng thái hiện diện (trực tuyến/ngoại tuyến) */
    public static final String USER_PRESENCE_PREFIX = "user:presence:";
    /** Tập hợp mã tất cả người dùng đang trực tuyến */
    public static final String USER_ONLINE_SET = "users:online";
    /** Khóa feed cá nhân của người dùng */
    public static final String USER_FEED = "feed:user:";
    /** Khóa hồ sơ người dùng */
    public static final String USER_PROFILE = "user:profile:";
    /** Thời gian hết hạn mặc định (30 phút, đơn vị: giây) */
    public static final int DEFAULT_TTL_SECONDS = 1800;

    // ==================== Bài viết (Post) ====================
    /** Tiền tố khóa cho bài viết */
    public static final String POST_PREFIX = "post:";
    /** Khóa thống kê bài viết (lượt thích, lượt xem, v.v.) */
    public static final String POST_STATS_PREFIX = "post:stats:";
    /** Khóa lưu trạng thái liked của người dùng cho bài viết */
    public static final String POST_LIKE_PREFIX = "post:like:";
    /** Khóa lưu trạng thái đã xem của người dùng cho bài viết */
    public static final String POST_VIEW_PREFIX = "post:view:";

    // ==================== Bạn bè (Friend) ====================
    /** Tiền tố khóa danh sách bạn bè */
    public static final String FRIENDS_PREFIX = "friends:";
    /** Tiền tố khóa lời mời kết bạn đang chờ xử lý */
    public static final String FRIEND_REQUEST_PREFIX = "friend:request:";
    /** Tiền tố khóa gợi ý bạn bè */
    public static final String FRIEND_SUGGESTION_PREFIX = "friend:suggestion:";
    /** Tiền tố khóa danh sách người theo dõi */
    public static final String FOLLOWERS_PREFIX = "followers:";
    /** Tiền tố khóa danh sách đang theo dõi */
    public static final String FOLLOWING_PREFIX = "following:";

    // ==================== Phản ứng (Reaction) ====================
    /** Khóa bộ đếm số lượng phản ứng cho bài viết */
    public static final String REACTION_COUNT_PREFIX = "reaction:count:";
    /** Khóa lưu trạng thái phản ứng của người dùng cho bài viết */
    public static final String USER_REACTION_PREFIX = "user:reaction:";

    // ==================== Thông báo (Notification) ====================
    /** Khóa bộ đếm số thông báo chưa đọc */
    public static final String NOTIFICATION_COUNT_PREFIX = "notification:count:";
    /** Khóa đánh dấu có thông báo chưa đọc */
    public static final String NOTIFICATION_UNREAD = "notification:unread:";
    /** Tiền tố khóa thông báo theo người dùng */
    public static final String NOTIFICATION_USER = "notification:user:";

    // ==================== Tìm kiếm (Search) ====================
    /** Khóa lịch sử tìm kiếm của người dùng */
    public static final String SEARCH_HISTORY_PREFIX = "search:history:";
    /** Khóa danh sách chủ đề xu hướng */
    public static final String SEARCH_TRENDING = "search:trending";
    /** Tiền tố khóa gợi ý tự động hoàn thành */
    public static final String AUTOCOMPLETE_PREFIX = "autocomplete:";

    // ==================== Xu hướng (Trending) ====================
    /** Khóa danh sách hashtag xu hướng */
    public static final String TRENDING_HASHTAGS = "trending:hashtags";
    /** Khóa danh sách chủ đề xu hướng */
    public static final String TRENDING_TOPICS = "trending:topics";
    /** Khóa danh sách bài viết xu hướng */
    public static final String TRENDING_POSTS = "trending:posts";

    // ==================== Giới hạn tốc độ (Rate Limiting) ====================
    /** Tiền tố khóa giới hạn tốc độ yêu cầu */
    public static final String RATE_LIMIT_PREFIX = "ratelimit:";

    // ==================== Phiên đăng nhập (Session) ====================
    /** Tiền tố khóa phiên đăng nhập */
    public static final String SESSION_PREFIX = "session:";
    /** Tiền tố khóa danh sách phiên của người dùng */
    public static final String USER_SESSIONS_PREFIX = "user:sessions:";

    // ==================== Đồ thị xã hội (Graph) ====================
    /** Khóa đồ thị kề (liên kết giữa các người dùng) */
    public static final String GRAPH_ADJACENCY = "graph:adjacency:";
    /** Khóa điểm PageRank của người dùng */
    public static final String GRAPH_PAGERANK = "graph:pagerank";
    /** Khóa thông tin cộng đồng xã hội */
    public static final String GRAPH_COMMUNITY = "graph:community:";

    // ==================== Hồ sơ người dùng (User Profile) ====================
    /** Tiền tố khóa sở thích của người dùng */
    public static final String USER_INTERESTS_PREFIX = "user:interests:";
    /** Tiền tố khóa hành vi của người dùng */
    public static final String USER_BEHAVIOR_PREFIX = "user:behavior:";
    /** Tiền tố khóa vector nhúng của người dùng */
    public static final String USER_EMBEDDING_PREFIX = "user:embedding:";

    // ==================== Nội dung (Content) ====================
    /** Khóa độ tương đồng nội dung */
    public static final String CONTENT_SIMILARITY = "content:similarity:";
    /** Tiền tố khóa vector nhúng nội dung */
    public static final String CONTENT_EMBEDDING_PREFIX = "content:embedding:";
    /** Tiền tố khóa điểm chất lượng nội dung */
    public static final String CONTENT_QUALITY_PREFIX = "content:quality:";

    // ==================== Tính năng ML ====================
    /** Tiền tố khóa đặc trưng đầu vào cho mô hình ML */
    public static final String ML_FEATURES_PREFIX = "ml:features:";
    /** Tiền tố khóa kết quả dự đoán từ mô hình ML */
    public static final String ML_PREDICTIONS_PREFIX = "ml:predictions:";

    // ==================== Hằng số thời gian hết hạn (đơn vị: giây) ====================
    /** 5 phút */
    public static final int TTL_SHORT = 300;
    /** 30 phút */
    public static final int TTL_MEDIUM = 1800;
    /** 1 giờ */
    public static final int TTL_LONG = 3600;
    /** 1 ngày */
    public static final int TTL_DAY = 86400;
    /** 7 ngày */
    public static final int TTL_WEEK = 604800;
    /** 30 ngày */
    public static final int TTL_MONTH = 2592000;
}
