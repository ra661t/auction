package com.easybid.test;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public class TestRepository {
    //findAll()
    // findById(id);
    // save(board);
    // deleteById(id);
    // findById(id).orElseThrow(null);
    // 등등 아래의 custom method가 없어도 기본으로 내장되어있는 함수들은 사용 가능합니다.
    
    //Custom Method 예시
    //아래 예시대로 함수를 이곳에 작성하면 그대로 사용할 수 있습니다.

    // findBy 뒤에는 조건을 조합하여 더 복잡한 쿼리를 만들 수 있습니다. 여기에는 And, Or, Like, Is, StartingWith, EndingWith, Containing 등 다양한 조건을 결합할 수 있습니다.
    // name과 userId로 Gallery 검색
    // List<Gallery> findByNameAndUserId(String name, Long userId);

    // description 또는 name으로 Gallery 검색
    // List<Gallery> findByDescriptionOrName(String description, String name);

    // name이 특정 텍스트로 시작하는 Gallery 검색
    // List<Gallery> findByNameStartingWith(String prefix);

    // name에 특정 텍스트가 포함된 Gallery 검색
    // List<Gallery> findByNameContaining(String keyword);

    // name이 특정 텍스트로 끝나는 Gallery 검색
    // List<Gallery> findByNameEndingWith(String suffix);

}
