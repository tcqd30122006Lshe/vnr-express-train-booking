package com.trainbooking.config;

import com.trainbooking.entity.Role;
import com.trainbooking.entity.User;
import com.trainbooking.repository.RoleRepository;
import com.trainbooking.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            // 1. Tạo 3 Role mặc định (ADMIN, STAFF, USER) nếu chưa có trong Database
            Role adminRole = roleRepository.findById("ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").description("Quản trị viên toàn hệ thống").build()));

            roleRepository.findById("STAFF")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("STAFF").description("Nhân viên bán vé & soát vé").build()));

            roleRepository.findById("USER")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("USER").description("Hành khách mua vé").build()));

            // 2. Khởi tạo tài khoản Admin mặc định (admin / admin123) nếu chưa có
            if (!userRepository.existsByUsername("admin")) {
                User adminUser = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .fullName("Hệ Thống Quản Trị")
                        .email("admin@trainbooking.com")
                        .phone("0999999999")
                        .roles(Set.of(adminRole))
                        .build();

                userRepository.save(adminUser);
                log.info(">>> Đã khởi tạo thành công tài khoản ADMIN mặc định: admin / admin123");
            }
        };
    }
}