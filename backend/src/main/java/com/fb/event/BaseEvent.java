package com.fb.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Lớp sự kiện cơ sở cho tất cả sự kiện trong ứng dụng.
 * Lưu trữ thông tin cơ bản: mã sự kiện, thời điểm xảy ra, và mã người dùng liên quan.
 */
@Getter
public abstract class BaseEvent extends ApplicationEvent {

    /** Mã định danh duy nhất của sự kiện */
    private final Long eventId;

    /** Thời điểm sự kiện xảy ra */
    private final Instant occurredOn;

    /** Mã người dùng liên quan đến sự kiện */
    private final Long userId;

    /**
     * Khởi tạo sự kiện cơ sở.
     *
     * @param source đối tượng phát sinh sự kiện
     * @param userId mã người dùng liên quan
     */
    protected BaseEvent(Object source, Long userId) {
        super(source);
        this.eventId = System.nanoTime();
        this.occurredOn = Instant.now();
        this.userId = userId;
    }
}
