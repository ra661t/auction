package com.easybid.board;

import org.springframework.data.jpa.repository.JpaRepository;

//리포지토리, 퍼시스턴스 계층, DB나 파일 같은 외부 I/O 담당
public interface BoardRepository extends JpaRepository<Board, Long> {
}
