package com.easybid.test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.easybid.board.Board;

import jakarta.servlet.http.HttpSession;


//컨트롤러, 프레젠테이션 계층, 웹 요청과 응답을 담당함
@Controller
@RequestMapping("/test")
public class TestController {
    
    @GetMapping("/get")
    public String getMethodName() {
        

        return "test";
    }
    
    // @GetMapping("/login")
    // public String t_login() {
    //     return "member/login";
    // }
    
    //글 목록 페이지
    // @GetMapping("/list")
    // public String getAllBoards(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
    //     Page<Board> boardList = boardService.getBoardList(pageable);
    //     model.addAttribute("boardList", boardList);
    //     return "board/list";
    // }

    //글 작성 페이지, 리다이렉트나 세션 예시
    // @GetMapping("/new")
    // public String createBoardForm(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
    //     
        // if(session.getAttribute("userId") != null) {
        //     System.out.println("글 작성 페이지로 이동 1");
        //     model.addAttribute("board", new Board());
        //     System.out.println("글 작성 페이지로 이동 2");
        //     return "board/new";
        // } else {
        //     // 잘못된 접근이므로 경고와 함께 home으로 보내야함
        //     redirectAttributes.addFlashAttribute("message", "글 작성은 로그인해야 할 수 있습니다.");
        //     redirectAttributes.addFlashAttribute("target", "/user/login");
        //     return "redirect:/remessage";
        // }
    // }
}
