package edu.uga.csci4370.group12.project3.Controllers;




import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import edu.uga.csci4370.group12.project3.Models.Class;
import edu.uga.csci4370.group12.project3.Models.User;
import edu.uga.csci4370.group12.project3.Services.ClassService;
import edu.uga.csci4370.group12.project3.Services.UserService;



@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final ClassService classService;

    @Autowired
    public ProfileController(UserService userService, ClassService classService) {
        this.userService = userService;
        this.classService = classService;
    }

    @GetMapping
    public ModelAndView showProfilePage(@RequestParam(name = "error", required = false) String error,
                                        @RequestParam(name = "success", required = false) String success) {
        ModelAndView mv = new ModelAndView("profile_page");
        if (!userService.isAuthenticated()) {
            return new ModelAndView("redirect:/login");
        }
        User loggedInUser = userService.getLoggedInUser();
        mv.addObject("loggedInUser", loggedInUser);
        mv.addObject("profileUser", loggedInUser);
        mv.addObject("isOwnProfile", true);
        mv.addObject("classes", classService.getUserClasses(loggedInUser.getUserId()));
        mv.addObject("errorMessage", error);
        mv.addObject("successMessage", success);
        return mv;
    }

    @GetMapping("/view")
    public ModelAndView viewUserProfile(@RequestParam("userId") String userId) {
        ModelAndView mv = new ModelAndView("profile_page");
        if (!userService.isAuthenticated()) {
            return new ModelAndView("redirect:/login");
        }
        User loggedInUser = userService.getLoggedInUser();
        User profileUser = classService.getUserById(userId);
        if (profileUser == null) {
            mv.addObject("errorMessage", "User not found.");
            return mv;
        }
        boolean isOwnProfile = loggedInUser.getUserId().equals(userId);
        mv.addObject("loggedInUser", loggedInUser);
        mv.addObject("profileUser", profileUser);
        mv.addObject("isOwnProfile", isOwnProfile);
        mv.addObject("classes", classService.getUserClasses(userId));
        mv.addObject("isFollowing", classService.isFollowing(loggedInUser.getUserId(), userId));
        return mv;
    }

    @PostMapping("/follow")
    public String followUser(@RequestParam("userId") String userId,
                            @RequestParam(name = "classCode", required = false) String classCode) {
        if (!userService.isAuthenticated()) {
            return "redirect:/login";
        }
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser.getUserId().equals(userId)) {
            String errorMessage = URLEncoder.encode("You cannot follow yourself.", StandardCharsets.UTF_8);
            return classCode != null && !classCode.isEmpty()
                    ? "redirect:/search-classes?classCode=" + classCode + "&error=" + errorMessage
                    : "redirect:/profile/view?userId=" + userId + "&error=" + errorMessage;
        }
        boolean success = classService.followUser(loggedInUser.getUserId(), userId);
        if (success) {
            String successMessage = URLEncoder.encode("You are now following this user!", StandardCharsets.UTF_8);
            return classCode != null && !classCode.isEmpty()
                    ? "redirect:/search-classes?classCode=" + classCode + "&success=" + successMessage
                    : "redirect:/profile/view?userId=" + userId + "&success=" + successMessage;
        } else {
            String errorMessage = URLEncoder.encode("You are already following this user.", StandardCharsets.UTF_8);
            return classCode != null && !classCode.isEmpty()
                    ? "redirect:/search-classes?classCode=" + classCode + "&error=" + errorMessage
                    : "redirect:/profile/view?userId=" + userId + "&error=" + errorMessage;
        }
    }

    @PostMapping("/unfollow")
    public String unfollowUser(@RequestParam("userId") String userId,
                               @RequestParam(name = "classCode", required = false) String classCode) {
        if (!userService.isAuthenticated()) {
            return "redirect:/login";
        }
        User loggedInUser = userService.getLoggedInUser();
        boolean success = classService.unfollowUser(loggedInUser.getUserId(), userId);
        if (success) {
            String successMessage = URLEncoder.encode("You have unfollowed this user.", StandardCharsets.UTF_8);
            return classCode != null && !classCode.isEmpty()
                    ? "redirect:/search-classes?classCode=" + classCode + "&success=" + successMessage
                    : "redirect:/profile/view?userId=" + userId + "&success=" + successMessage;
        } else {
            String errorMessage = URLEncoder.encode("You are not following this user.", StandardCharsets.UTF_8);
            return classCode != null && !classCode.isEmpty()
                    ? "redirect:/search-classes?classCode=" + classCode + "&error=" + errorMessage
                    : "redirect:/profile/view?userId=" + userId + "&error=" + errorMessage;
        }
    }

    @PostMapping("/add-class")
    public String addClass(@RequestParam("classCode") String classCode) {
        if (!userService.isAuthenticated()) {
            return "redirect:/login";
        }
        try {
            Class newClass = classService.findOrCreateClass(classCode.toUpperCase());
            boolean enrolled = classService.enrollUserInClass(userService.getLoggedInUser().getUserId(), newClass.getClassId());
            if (enrolled) {
                String successMessage = URLEncoder.encode("Class " + classCode + " added successfully!", StandardCharsets.UTF_8);
                return "redirect:/profile?success=" + successMessage;
            } else {
                String errorMessage = URLEncoder.encode("You are already enrolled in " + classCode + ".", StandardCharsets.UTF_8);
                return "redirect:/profile?error=" + errorMessage;
            }
        } catch (IllegalArgumentException e) {
            String errorMessage = URLEncoder.encode("Invalid class code format. Use 4 letters followed by 4 digits (e.g., CSCI4370).", StandardCharsets.UTF_8);
            return "redirect:/profile?error=" + errorMessage;
        } catch (SQLException e) {
            String errorMessage = URLEncoder.encode("Failed to add class: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/profile?error=" + errorMessage;
        }
    }
}