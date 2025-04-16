package com.easybid.repository;

import com.easybid.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
    // 기본적인 CRUD는 JpaRepository로 충분합니다.
}
