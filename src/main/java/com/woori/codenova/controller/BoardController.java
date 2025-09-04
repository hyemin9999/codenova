package com.woori.codenova.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.woori.codenova.entity.Board;
import com.woori.codenova.entity.Category;
import com.woori.codenova.entity.Comment;
import com.woori.codenova.entity.SiteUser;
import com.woori.codenova.form.BoardForm;
import com.woori.codenova.form.CommentForm;
import com.woori.codenova.service.BoardService;
import com.woori.codenova.service.CategoryService;
import com.woori.codenova.service.CommentService;
import com.woori.codenova.service.SearchTextService;
import com.woori.codenova.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequestMapping("/board")
@RequiredArgsConstructor
@Controller
public class BoardController {

    private final BoardService boardService;
    private final UserService userService;
    private final CommentService commentService;
    private final SearchTextService searchTextService;
    private final CategoryService categoryService;

    // ===============================================================
    // 목록 페이지
    // ===============================================================
    @GetMapping("/list")
    public String list(Model model, @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw,
                       @RequestParam(value = "field", defaultValue = "all") String field,
                       @RequestParam(value = "size", defaultValue = "10") int size) {

        switch (size) {
            case 10: case 20: case 30: case 50: break;
            default: size = 10;
        }

        Page<Board> paging = this.boardService.getList(0, page, kw, field, size);

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("field", field);
        model.addAttribute("size", size);

        if (!kw.isBlank()) {
            Category citem = categoryService.getItem(0);
            searchTextService.create(kw, citem);
        }

        return "board_list";
    }

    @GetMapping("/list/{cid}")
    public String list(Model model, @PathVariable("cid") Integer cid,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "kw", defaultValue = "") String kw,
                       @RequestParam(value = "field", defaultValue = "all") String field,
                       @RequestParam(value = "size", defaultValue = "10") int size) {

        Page<Board> paging = this.boardService.getList(cid, page, kw, field, size);

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        model.addAttribute("field", field);
        model.addAttribute("size", size);
        model.addAttribute("cid", cid);

        if (!kw.isBlank()) {
            Category citem = categoryService.getItem(cid);
            searchTextService.create(kw, citem);
        }
        return "board_list";
    }

    // ===============================================================
    // 상세 페이지
    // ===============================================================
    @GetMapping("/detail/{id}")
    public String detail(Model model, @PathVariable("id") Integer id, CommentForm commentForm,
                         @RequestParam(value = "cpage", defaultValue = "0") int cpage, Principal principal) {

        Board board = this.boardService.viewBoard(id);
        Page<Comment> cpaging = this.commentService.getPageByBoard(board, cpage);

        SiteUser me = (principal != null) ? this.userService.getUser(principal.getName()) : null;

        boolean favoritedBoard = (me != null) && board.getFavorite() != null && board.getFavorite().contains(me);

        Map<Integer, Boolean> commentFavMap = new HashMap<>();
        if (me != null) {
            for (Comment c : cpaging.getContent()) {
                boolean fav = (c.getFavorite() != null) && c.getFavorite().contains(me);
                commentFavMap.put(c.getId(), fav);
            }
        }

        model.addAttribute("menuName", board.getCategory().getName());
        model.addAttribute("board", board);
        model.addAttribute("cpaging", cpaging);
        model.addAttribute("favoritedBoard", favoritedBoard);
        model.addAttribute("commentFavMap", commentFavMap);
        model.addAttribute("cid", board.getCategory().getId());

        return "board_detail";
    }

    // ===============================================================
    // 생성 폼
    // ===============================================================
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create/{cid}")
    public String boardCreate(@PathVariable(name = "cid") Integer cid, BoardForm boardForm) {
        return "board_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create/{cid}")
    public String boardCreate(@PathVariable(name= "cid") Integer cid, @Valid BoardForm boardForm,
                              BindingResult bindingResult, Principal principal) {
        if (bindingResult.hasErrors()) return "board_form";

        SiteUser siteUser = this.userService.getUser(principal.getName());
        String con = URLDecoder.decode(boardForm.getContents(), StandardCharsets.UTF_8);
        this.boardService.create(boardForm.getSubject(), con, siteUser, cid);

        return "redirect:/board/list/{cid}";
    }

    // ===============================================================
    // 수정 폼 & 처리
    // ===============================================================
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/modify/{id}")
    public String boardModify(BoardForm boardForm, @PathVariable("id") Integer id, Principal principal) {
        Board board = this.boardService.getBoard(id);

        if (!board.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        boardForm.setSubject(board.getSubject());
        boardForm.setContents(board.getContents());
        return "board_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/modify/{id}")
    public String boardModify(@Valid BoardForm boardForm, BindingResult bindingResult, Principal principal,
                              @PathVariable("id") Integer id) {
        if (bindingResult.hasErrors()) {
            return "board_form";
        }

        Board board = this.boardService.getBoard(id);

        if (!board.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        String con = URLDecoder.decode(boardForm.getContents(), StandardCharsets.UTF_8);
        this.boardService.modify(board, boardForm.getSubject(), con);

        return String.format("redirect:/board/detail/%s", id);
    }

    // ===============================================================
    // 삭제
    // ===============================================================
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/delete/{id}")
    public String boardDelete(Principal principal, @PathVariable("id") Integer id) {
        Board board = this.boardService.getBoard(id);

        if (!board.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다.");
        }

        this.boardService.delete(board);
        return "redirect:/";
    }

    // ===============================================================
    // 추천
    // ===============================================================
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String boardVote(Principal principal, @PathVariable("id") Integer id) {
        Board board = this.boardService.getBoard(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.boardService.vote(board, siteUser);
        return String.format("redirect:/board/detail/%s", id);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/favorite/{id}")
    public String boardFavorite(Principal principal, @PathVariable("id") Integer id) {
        Board board = this.boardService.getBoard(id);
        SiteUser siteUser = this.userService.getUser(principal.getName());
        this.boardService.favorite(board, siteUser);
        return String.format("redirect:/board/detail/%s", id);
    }
}
