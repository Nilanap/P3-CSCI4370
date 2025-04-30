package edu.uga.csci4370.group12.project3.Controllers;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import java.util.List;
import edu.uga.csci4370.group12.project3.Models.User;

import edu.uga.csci4370.group12.project3.Services.ClassService;
import edu.uga.csci4370.group12.project3.Services.UserService;

@Controller
@RequestMapping("/follow")
public class FollowController {

    private final UserService userService;
    private final ClassService classService;

    @Autowired
    public FollowController(UserService userService, ClassService classService) {
        this.userService = userService;
        this.classService = classService;
    }

    @GetMapping
    public ModelAndView showFollowPage() {
        ModelAndView mv = new ModelAndView("follow_page");
        if (!userService.isAuthenticated()) {
            return new ModelAndView("redirect:/login");
        }
        User loggedInUser = userService.getLoggedInUser();
        List<User> followees = classService.getFollowees(loggedInUser.getUserId());
        List<User> followers = classService.getFollowers(loggedInUser.getUserId());
        mv.addObject("loggedInUser", loggedInUser);
        mv.addObject("followees", followees);
        mv.addObject("followers", followers);
        return mv;
    }
}