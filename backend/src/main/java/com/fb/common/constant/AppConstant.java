package com.fb.common.constant;

/**
 * Các hằng số toàn cục của ứng dụng (Application-wide Constants).
 *
 * Định nghĩa tất cả các hằng số được sử dụng rộng rãi trong ứng dụng,
 * bao gồm giới hạn, thời gian chờ, thuật toán, và thông số kỹ thuật.
 */
public final class AppConstant {

    private AppConstant() {}

    /** Phiên bản API hiện tại */
    public static final String API_VERSION = "/api/v1";

    /** Tên ứng dụng */
    public static final String APP_NAME = "Facebook Clone";

    // ==================== Phân trang (Pagination) ====================
    /** Số lượng phần tử mặc định trên mỗi trang */
    public static final int DEFAULT_PAGE_SIZE = 20;
    /** Số lượng phần tử tối đa trên mỗi trang */
    public static final int MAX_PAGE_SIZE = 100;
    /** Số lượng bài viết trên mỗi trang luồng tin tức */
    public static final int FEED_PAGE_SIZE = 20;

    // ==================== Giới hạn (Limits) ====================
    /** Số lượng bạn bè tối đa */
    public static final int MAX_FRIENDS = 5000;
    /** Số lời mời kết bạn đang chờ xử lý tối đa */
    public static final int MAX_PENDING_REQUESTS = 20;
    /** Độ dài nội dung bài viết tối đa (ký tự) */
    public static final int MAX_POST_LENGTH = 10000;
    /** Độ dài nội dung bình luận tối đa (ký tự) */
    public static final int MAX_COMMENT_LENGTH = 2000;
    /** Độ dài nội dung tin nhắn tối đa (ký tự) */
    public static final int MAX_MESSAGE_LENGTH = 10000;
    /** Số hình ảnh tối đa trong một bài viết */
    public static final int MAX_IMAGES_PER_POST = 20;
    /** Kích thước tệp video tối đa (MB) */
    public static final int MAX_VIDEO_SIZE_MB = 50;
    /** Kích thước tệp hình ảnh tối đa (MB) */
    public static final int MAX_IMAGE_SIZE_MB = 10;
    /** Số lượng thành viên tối đa trong một nhóm */
    public static final int MAX_GROUP_MEMBERS = 50;
    /** Số lượng nhắc đến tối đa trong một bài viết */
    public static final int MAX_MENTIONS_PER_POST = 50;

    // ==================== Thời gian chờ (Timeouts) ====================
    /** Thời gian hết hạn token truy cập JWT (15 phút) */
    public static final int JWT_ACCESS_TOKEN_EXPIRY = 900;
    /** Thời gian hết hạn token làm mới JWT (7 ngày) */
    public static final int JWT_REFRESH_TOKEN_EXPIRY = 604800;
    /** Thời gian hết hạn phiên đăng nhập (30 ngày) */
    public static final int SESSION_EXPIRY = 2592000;
    /** Thời gian hết hạn lời mời kết bạn (30 ngày) */
    public static final int FRIEND_REQUEST_EXPIRY = 2592000;
    /** Thời gian cho phép chỉnh sửa tin nhắn (15 phút) */
    public static final int MESSAGE_EDIT_WINDOW = 900;
    /** Thời gian cho phép xóa tin nhắn (24 giờ) */
    public static final int MESSAGE_DELETE_WINDOW = 86400;

    // ==================== Thuật toán luồng tin tức (Feed Algorithm) ====================
    /** Ngưỡng người theo dõi để được coi là người nổi tiếng (fanout on read) */
    public static final int FEED_FANOUT_THRESHOLD = 10000;
    /** Trọng số yếu tố thời gian trong xếp hạng luồng tin tức */
    public static final double FEED_RECENCY_WEIGHT = 0.2;
    /** Trọng số yếu tố mối quan hệ trong xếp hạng luồng tin tức */
    public static final double FEED_RELATIONSHIP_WEIGHT = 0.4;
    /** Trọng số yếu tố nội dung trong xếp hạng luồng tin tức */
    public static final double FEED_CONTENT_WEIGHT = 0.3;
    /** Trọng số yếu tố đa dạng trong xếp hạng luồng tin tức */
    public static final double FEED_DIVERSITY_WEIGHT = 0.1;

    // ==================== Gợi ý bạn bè (Friend Suggestion) ====================
    /** Số lượng gợi ý bạn bè tối đa hiển thị */
    public static final int FRIEND_SUGGESTION_LIMIT = 20;
    /** Trọng số bạn bè chung trong thuật toán gợi ý */
    public static final double MUTUAL_FRIEND_WEIGHT = 0.5;
    /** Trọng số cùng nơi làm việc trong thuật toán gợi ý */
    public static final double SAME_WORKPLACE_WEIGHT = 0.15;
    /** Trọng số cùng trường học trong thuật toán gợi ý */
    public static final double SAME_EDUCATION_WEIGHT = 0.1;
    /** Trọng số cùng địa điểm trong thuật toán gợi ý */
    public static final double SAME_LOCATION_WEIGHT = 0.1;
    /** Trọng số cùng sở thích trong thuật toán gợi ý */
    public static final double SAME_INTERESTS_WEIGHT = 0.1;
    /** Trọng số hoạt động gần đây trong thuật toán gợi ý */
    public static final double RECENT_ACTIVITY_WEIGHT = 0.05;

    // ==================== Tìm kiếm (Search) ====================
    /** Số lượng gợi ý tự động hoàn thành tối đa */
    public static final int AUTOCOMPLETE_LIMIT = 10;
    /** Số lượng kết quả tìm kiếm tối đa */
    public static final int SEARCH_RESULTS_LIMIT = 50;
    /** Ngưỡng khớp mờ trong tìm kiếm (0.0 - 1.0) */
    public static final double FUZZY_MATCH_THRESHOLD = 0.6;

    // ==================== Xu hướng (Trending) ====================
    /** Số lượng hashtag xu hướng tối đa hiển thị */
    public static final int TRENDING_HASHTAGS_LIMIT = 100;
    /** Khoảng thời gian tính xu hướng (giờ) */
    public static final int TRENDING_WINDOW_HOURS = 24;
    /** Số bài viết tối thiểu để được xếp vào xu hướng */
    public static final int TRENDING_MIN_POSTS = 10;

    // ==================== Phát hiện spam (Spam Detection) ====================
    /** Giới hạn tốc độ đăng bài để phát hiện spam (bài/giờ) */
    public static final int SPAM_RATE_LIMIT = 10;
    /** Số lần nội dung trùng lặp tối đa trước khi bị gắn cờ spam */
    public static final int SPAM_DUPLICATE_THRESHOLD = 3;
    /** Ngưỡng điểm spam (từ 0.0 đến 1.0, càng cao càng có khả năng là spam) */
    public static final double SPAM_SCORE_THRESHOLD = 0.7;

    // ==================== Kiểm duyệt (Moderation) ====================
    /** Số lượng báo cáo tối đa để tự động gắn cờ nội dung */
    public static final int MODERATION_AUTO_FLAG_THRESHOLD = 5;
    /** Thời hạn khiếu nại kiểm duyệt (ngày) */
    public static final int MODERATION_APPEAL_WINDOW_DAYS = 30;

    // ==================== Thông báo (Notification) ====================
    /** Kích thước lô thông báo khi gửi hàng loạt */
    public static final int NOTIFICATION_BATCH_SIZE = 100;
    /** Thời gian lưu trữ thông báo (ngày) */
    public static final int NOTIFICATION_EXPIRY_DAYS = 90;

    // ==================== Phương tiện (Media) ====================
    /** Kích thước hình thu nhỏ (pixel) */
    public static final int THUMBNAIL_SIZE = 150;
    /** Kích thước hình trung bình (pixel) */
    public static final int MEDIUM_SIZE = 600;
    /** Kích thước hình lớn (pixel) */
    public static final int LARGE_SIZE = 1200;
    /** Các kích thước avatar khả dụng (pixel) */
    public static final int AVATAR_SIZES[] = {170, 320, 640};
    /** Chiều rộng ảnh bìa (pixel) */
    public static final int COVER_WIDTH = 820;
    /** Chiều cao ảnh bìa (pixel) */
    public static final int COVER_HEIGHT = 312;
}
