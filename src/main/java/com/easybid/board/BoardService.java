package com.easybid.board;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

//서비스 계층, 자바 로직 처리
@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    public Optional<Board> getBoard(Long id) {
        return boardRepository.findById(id);
    }

    //글 작성
    public Board createBoard(Board board) {
        return boardRepository.save(board);
    }

    //글 수정
    public Board updateBoard(Long id, Board boardDetails) {
        Board board = boardRepository.findById(id).orElseThrow(null);
        board.setTitle(boardDetails.getTitle());
        board.setUser(boardDetails.getUser());
        board.setBody(boardDetails.getBody());
        return boardRepository.save(board);
    }

    //글 삭제
    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }

    //조회수 증가
    public Board increaseView(Long id) {
        Board board = boardRepository.findById(id).orElseThrow(null);

        board.setView(board.getView() + 1);
        return boardRepository.save(board);
    }
    
    //페이징 처리
    public Page<Board> getBoardList(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }
}
