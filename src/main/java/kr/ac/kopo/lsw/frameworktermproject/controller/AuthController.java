package kr.ac.kopo.lsw.frameworktermproject.controller;

import kr.ac.kopo.lsw.frameworktermproject.dto.JoinRequest;
import kr.ac.kopo.lsw.frameworktermproject.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("joinRequest", new JoinRequest());
        return "member/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute JoinRequest joinRequest,
                           BindingResult bindingResult,
                           Model model) {

        if(bindingResult.hasErrors()) {
            return "member/register";
        }

        boolean result = memberService.register(joinRequest);

        if(!result) {
            model.addAttribute("error", "이미 사용 중인 이메일입니다.");
            return "member/register";
        }

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }
}
