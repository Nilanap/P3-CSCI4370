package edu.uga.csci4370.group12.project3.Controllers;



import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import edu.uga.csci4370.group12.project3.Services.UserService;

@Controller
@RequestMapping
public class LoginController {

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public ModelAndView showLoginPage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("login_page");
        mv.addObject("errorMessage", error);
        return mv;
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username, @RequestParam("password") String password) {
        if (userService.authenticateUser(username, password)) {
            return "redirect:/";
        } else {
            String errorMessage = URLEncoder.encode("Invalid username or password.", StandardCharsets.UTF_8);
            return "redirect:/login?error=" + errorMessage;
        }
    }

    @GetMapping("/register")
    public ModelAndView showRegisterPage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("register_page");
        mv.addObject("errorMessage", error);
        return mv;
    }

    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("firstName") String firstName,
                           @RequestParam("lastName") String lastName) {
        if (userService.registerUser(username, password, firstName, lastName)) {
            return "redirect:/login";
        } else {
            String errorMessage = URLEncoder.encode("Registration failed. Username may already exist.", StandardCharsets.UTF_8);
            return "redirect:/register?error=" + errorMessage;
        }
    }

    @GetMapping("/logout")
    public String logout() {
        userService.logout();
        return "redirect:/login";
    }
}
