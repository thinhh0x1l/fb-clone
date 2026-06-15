package com.fb.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity cơ sở với ID kiểu Long để tối ưu hiệu năng
 * UUID tạo index phức tạp và chậm hơn Long
 */
@Data
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    /**
     * Kiểm tra đã bị xóa mềm chưa
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Xóa mềm - không xóa vật lý khỏi database
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}
