package com.fb.auth.repository;

import com.fb.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý truy cập dữ liệu cho thực thể User.
 * Cung cấp các phương thức tìm kiếm và kiểm tra người dùng.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tìm người dùng theo địa chỉ email.
     * @param email địa chỉ email cần tìm
     * @return Optional chứa người dùng nếu tìm thấy
     */
    Optional<User> findByEmail(String email);

    /**
     * Tìm người dùng theo tên đăng nhập.
     * @param username tên đăng nhập cần tìm
     * @return Optional chứa người dùng nếu tìm thấy
     */
    Optional<User> findByUsername(String username);

    /**
     * Kiểm tra xem email đã tồn tại trong hệ thống chưa.
     * @param email địa chỉ email cần kiểm tra
     * @return true nếu email đã tồn tại, false nếu chưa
     */
    boolean existsByEmail(String email);

    /**
     * Kiểm tra xem tên đăng nhập đã tồn tại trong hệ thống chưa.
     * @param username tên đăng nhập cần kiểm tra
     * @return true nếu tên đăng nhập đã tồn tại, false nếu chưa
     */
    boolean existsByUsername(String username);

    /**
     * Tìm người dùng theo tên đăng nhập hoặc tên hiển thị (tìm kiếm mờ).
     * Chỉ trả về những người dùng chưa bị xóa (deletedAt IS NULL).
     * @param query từ khóa tìm kiếm
     * @return danh sách người dùng phù hợp
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND " +
           "(LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchByUsernameOrDisplayName(@Param("query") String query);
}
