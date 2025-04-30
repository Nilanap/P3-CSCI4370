package edu.uga.csci4370.group12.project3.Controllers;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import java.util.logging.Logger;


import edu.uga.csci4370.group12.project3.Models.User;
import edu.uga.csci4370.group12.project3.Services.ClassService;
import edu.uga.csci4370.group12.project3.Services.UserService;


@Controller
@RequestMapping("/search-classes")
public class ClassSearchController {

    private static final Logger LOGGER = Logger.getLogger(ClassSearchController.class.getName());

    private final UserService userService;
    private final ClassService classService;

    @Autowired
    public ClassSearchController(UserService userService, ClassService classService) {
        this.userService = userService;
        this.classService = classService;
    }

    @GetMapping
    public ModelAndView showSearchPage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("class_search_page");
        if (!userService.isAuthenticated()) {
            return new ModelAndView("redirect:/login");
        }
        mv.addObject("loggedInUser", userService.getLoggedInUser());
        mv.addObject("errorMessage", error);
        return mv;
    }

    @PostMapping
    public ModelAndView searchClass(@RequestParam("classCode") String classCode) {
        if (!userService.isAuthenticated()) {
            return new ModelAndView("redirect:/login");
        }
        ModelAndView mv = new ModelAndView("class_search_page");
        User loggedInUser = userService.getLoggedInUser();
        mv.addObject("loggedInUser", loggedInUser);

        // Validate classCode format
        if (!classCode.matches("^[A-Z]{4}\\d{4}$")) {
            String errorMessage = URLEncoder.encode("Invalid class code format. Use 4 letters followed by 4 digits (e.g., CSCI4370).", StandardCharsets.UTF_8);
            mv.addObject("errorMessage", errorMessage);
            return mv;
        }

        String classCodeUpper = classCode.toUpperCase();
        List<User> usersInClass = classService.getUsersInClass(classCodeUpper);
        List<Map<String, Object>> usersWithStatus = new ArrayList<>();
        for (User user : usersInClass) {
            // Exclude the logged-in user
            if (!loggedInUser.getUserId().equals(user.getUserId())) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", user.getUserId() != null ? user.getUserId() : "");
                userData.put("username", user.getUsername() != null ? user.getUsername() : "");
                userData.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
                userData.put("lastName", user.getLastName() != null ? user.getLastName() : "");
                userData.put("classCode", classCodeUpper);
                userData.put("isFollowing", classService.isFollowing(loggedInUser.getUserId(), user.getUserId()));
                usersWithStatus.add(userData);
            }
        }
        LOGGER.info("Users in class " + classCodeUpper + " (excluding active user): " + usersWithStatus);
        mv.addObject("classCode", classCodeUpper);
        mv.addObject("usersInClass", usersWithStatus);
        return mv;
    }
}