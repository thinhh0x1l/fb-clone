package com.fb.common.enums;

/**
 * Các loại tin nhắn trong ứng dụng nhắn tin.
 *
 * Định nghĩa các định dạng nội dung tin nhắn
 * được hỗ trợ trong hệ thống.
 */
public enum MessageType {
    /** Tin nhắn văn bản thuần */
    TEXT,
    /** Tin nhắn chứa hình ảnh */
    IMAGE,
    /** Tin nhắn chứa tệp đính kèm */
    FILE,
    /** Tin nhắn chứa liên kết */
    LINK
}
