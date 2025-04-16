package com.easybid.board;

import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
// import org.springframework.data.web.PageableDefault;

//컨트롤러, 프레젠테이션 계층, 웹 요청과 응답을 담당함
@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    //글 목록 페이지
    @GetMapping("/list")
    public String getAllBoards(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        Page<Board> boardList = boardService.getBoardList(pageable);
        model.addAttribute("boardList", boardList);
        return "board/list";
    }

    //글 작성 페이지
    @GetMapping("/new")
    public String createBoardForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        model.addAttribute("board", new Board());
        return "board/new";
    }

    //글 작성 완료
    @PostMapping("/upload")
    public String createBoard(@ModelAttribute Board board, HttpSession session, RedirectAttributes redirectAttributes) {
        boardService.createBoard(board);

        redirectAttributes.addFlashAttribute("message", "등록되었습니다.");
        redirectAttributes.addFlashAttribute("target", "/board/list");
        return "redirect:/remessage";
    }

    //게시글 열람
    @GetMapping("/{id}")
    public String getBoard(@PathVariable("id") Long id, Model model) {
        boardService.increaseView(id); //조회수 증가

        model.addAttribute("board", boardService.getBoard(id).orElseThrow(null));
        return "board/detail";
    }

    //글 수정 페이지
    @GetMapping("/{id}/edit")
    public String editBoardForm(@PathVariable("id") Long id, Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Board board = boardService.getBoard(id).orElseThrow(null);
        
        model.addAttribute("board", board);
        return "board/edit";
    }

    //글 수정 확인
    @PostMapping("/{id}/edit")
    public String updateBoard(@PathVariable("id") Long id, HttpSession session, @ModelAttribute Board board, RedirectAttributes redirectAttributes) {

        boardService.updateBoard(id, board);
        
        redirectAttributes.addFlashAttribute("message", "수정되었습니다.");
        redirectAttributes.addFlashAttribute("target", "/board/list");
        return "redirect:/remessage";
    }

    //글 삭제
    @PostMapping("/{id}/delete")
    public String deleteBoard(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        boardService.deleteBoard(id);
        
        redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
        redirectAttributes.addFlashAttribute("target", "/board/list");
        return "redirect:/remessage";
    }
}