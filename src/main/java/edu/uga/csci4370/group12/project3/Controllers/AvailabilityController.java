package edu.uga.csci4370.group12.project3.Controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import edu.uga.csci4370.group12.project3.Models.User;
import edu.uga.csci4370.group12.project3.Services.ClassService;
import edu.uga.csci4370.group12.project3.Services.UserService;

@Controller
@RequestMapping("/profile/availability")
public class AvailabilityController {

    private static final Logger LOGGER = Logger.getLogger(AvailabilityController.class.getName());

    private final UserService userService;
    private final ClassService classService;

    @Autowired
    public AvailabilityController(UserService userService, ClassService classService) {
        this.userService = userService;
        this.classService = classService;
    }

    @GetMapping
    public ModelAndView showAvailability() {
        LOGGER.info("Accessing availability page");
        try {
            if (!userService.isAuthenticated()) {
                return new ModelAndView("redirect:/login");
            }
            User loggedInUser = userService.getLoggedInUser();
            ModelAndView mv = new ModelAndView("availability_page");
            mv.addObject("loggedInUser", loggedInUser);
            mv.addObject("availabilityList", classService.getAvailability(loggedInUser.getUserId()));
            return mv;
        } catch (Exception e) {
            LOGGER.severe("Error rendering availability page: " + e.getMessage());
            ModelAndView mv = new ModelAndView("error");
            mv.addObject("errorMessage", "Unable to load availability: " + e.getMessage());
            return mv;
        }
    }

    @PostMapping("/add")
    public ModelAndView addAvailability(
            @RequestParam("studyDate") String studyDate,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime) {
        ModelAndView mv = new ModelAndView("redirect:/profile/availability");
        if (!userService.isAuthenticated()) {
            return new ModelAndView("redirect:/login");
        }
        User loggedInUser = userService.getLoggedInUser();
        try {
            LocalDate date = LocalDate.parse(studyDate);
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);
            if (start.isAfter(end) || start.equals(end)) {
                mv.addObject("errorMessage", URLEncoder.encode("Start time must be before end time.", StandardCharsets.UTF_8));
                return mv;
            }
            boolean success = classService.saveAvailability(loggedInUser.getUserId(), date, start, end);
            if (success) {
                mv.addObject("successMessage", URLEncoder.encode("Availability added successfully.", StandardCharsets.UTF_8));
            } else {
                mv.addObject("errorMessage", URLEncoder.encode("Failed to add availability. Slot may already exist.", StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            mv.addObject("errorMessage", URLEncoder.encode("Invalid date or time format.", StandardCharsets.UTF_8));
        }
        return mv;
    }

    @PostMapping("/delete")
    public ModelAndView deleteAvailability(@RequestParam("availabilityId") String availabilityId) {
        ModelAndView mv = new ModelAndView("redirect:/profile/availability");
        if (!userService.isAuthenticated()) {
            return new ModelAndView("redirect:/login");
        }
        boolean success = classService.deleteAvailability(availabilityId);
        if (success) {
            mv.addObject("successMessage", URLEncoder.encode("Availability deleted successfully.", StandardCharsets.UTF_8));
        } else {
            mv.addObject("errorMessage", URLEncoder.encode("Failed to delete availability.", StandardCharsets.UTF_8));
        }
        return mv;
    }
}