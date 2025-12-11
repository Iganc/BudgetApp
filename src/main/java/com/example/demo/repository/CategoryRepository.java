package com.example.demo.repository;

import com.example.demo.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.user u WHERE c.isDefault = true OR u.id = :userId ORDER BY c.name ASC")
    List<Category> findDefaultAndUserCategories(@Param("userId") Long userId);

    Optional<Category> findByNameAndUserId(String name, Long userId);

    Optional<Category> findByNameAndIsDefault(String name, boolean isDefault);
}