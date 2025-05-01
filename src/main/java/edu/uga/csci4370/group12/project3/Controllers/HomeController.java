package edu.uga.csci4370.group12.project3.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import edu.uga.csci4370.group12.project3.Models.User;
import edu.uga.csci4370.group12.project3.Models.Class;
import edu.uga.csci4370.group12.project3.Services.ClassService;
import edu.uga.csci4370.group12.project3.Services.UserService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;



@Controller
@RequestMapping("/")
public class HomeController {

    private static final Logger LOGGER = Logger.getLogger(HomeController.class.getName());

    private final UserService userService;
    private final ClassService classService;

    @Autowired
    public HomeController(UserService userService, ClassService classService) {
        this.userService = userService;
        this.classService = classService;
    }

    @GetMapping
    public ModelAndView showHomePage() {
        LOGGER.info("Attempting to access homepage");
        try {
            if (!userService.isAuthenticated()) {
                LOGGER.info("User not authenticated, redirecting to /login");
                return new ModelAndView("redirect:/login");
            }
            User loggedInUser = userService.getLoggedInUser();
            if (loggedInUser == null) {
                LOGGER.warning("Logged-in user is null, redirecting to /login");
                return new ModelAndView("redirect:/login");
            }
            LOGGER.info("Fetching data for user: " + loggedInUser.getUserId());

            // Fetch followees' availability with shared classes
            List<ClassService.UserAvailability> followeesAvailability = classService.getFolloweesAvailability(loggedInUser.getUserId());
            // Format times for display
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
            List<UserAvailabilityDisplay> displayAvailability = new ArrayList<>();
            for (ClassService.UserAvailability ua : followeesAvailability) {
                displayAvailability.add(new UserAvailabilityDisplay(
                    ua.getUser(),
                    ua.getAvailability(),
                    ua.getSharedClasses(),
                    timeFormatter.format(ua.getAvailability().getStartTime()),
                    timeFormatter.format(ua.getAvailability().getEndTime())
                ));
            }

            // Format pending requests' times
            List<StudyRequestDisplay> displayRequests = new ArrayList<>();
            for (ClassService.StudyRequest sr : classService.getPendingStudyRequests(loggedInUser.getUserId())) {
                displayRequests.add(new StudyRequestDisplay(
                    sr.getRequestId(),
                    sr.getRequester(),
                    sr.getTargetUserId(),
                    sr.getAvailabilityId(),
                    sr.getStatus(),
                    sr.getRequestDate(),
                    sr.getStudyDate(),
                    timeFormatter.format(sr.getStartTime()),
                    timeFormatter.format(sr.getEndTime())
                ));
            }

            ModelAndView mv = new ModelAndView("home");
            mv.addObject("loggedInUser", loggedInUser);
            mv.addObject("followeesAvailability", displayAvailability);
            mv.addObject("pendingRequests", displayRequests);
            LOGGER.info("Rendering homepage for user: " + loggedInUser.getUserId());
            return mv;
        } catch (Exception e) {
            LOGGER.severe("Error rendering homepage: " + e.getMessage());
            e.printStackTrace();
            ModelAndView mv = new ModelAndView("error");
            mv.addObject("errorMessage", "Unable to load homepage: " + e.getMessage());
            return mv;
        }
    }

    @GetMapping("/sessions")
    public ModelAndView showUpcomingSessions() {
        LOGGER.info("Attempting to access upcoming study sessions page");
        try {
            if (!userService.isAuthenticated()) {
                LOGGER.info("User not authenticated, redirecting to /login");
                return new ModelAndView("redirect:/login");
            }
            User loggedInUser = userService.getLoggedInUser();
            if (loggedInUser == null) {
                LOGGER.warning("Logged-in user is null, redirecting to /login");
                return new ModelAndView("redirect:/login");
            }
            LOGGER.info("Fetching upcoming study sessions for user: " + loggedInUser.getUserId());

            // Fetch and format upcoming study sessions
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
            List<StudySessionDisplay> displaySessions = new ArrayList<>();
            for (ClassService.StudySession ss : classService.getUpcomingStudySessions(loggedInUser.getUserId())) {
                displaySessions.add(new StudySessionDisplay(
                    ss.getRequestId(),
                    ss.getPartner(),
                    ss.getAvailabilityId(),
                    ss.getStatus(),
                    ss.getRequestDate(),
                    ss.getStudyDate(),
                    timeFormatter.format(ss.getStartTime()),
                    timeFormatter.format(ss.getEndTime())
                ));
            }

            ModelAndView mv = new ModelAndView("sessions");
            mv.addObject("loggedInUser", loggedInUser);
            mv.addObject("upcomingSessions", displaySessions);
            LOGGER.info("Rendering upcoming study sessions page for user: " + loggedInUser.getUserId());
            return mv;
        } catch (Exception e) {
            LOGGER.severe("Error rendering upcoming study sessions page: " + e.getMessage());
            e.printStackTrace();
            ModelAndView mv = new ModelAndView("error");
            mv.addObject("errorMessage", "Unable to load upcoming study sessions: " + e.getMessage());
            return mv;
        }
    }

    @PostMapping("/study/request")
    public ModelAndView createStudyRequest(
            @RequestParam("availabilityId") String availabilityId) {
        LOGGER.info("Processing study request for availabilityId: " + availabilityId);
        ModelAndView mv = new ModelAndView("redirect:/");
        if (!userService.isAuthenticated()) {
            LOGGER.info("User not authenticated, redirecting to /login");
            return new ModelAndView("redirect:/login");
        }
        User loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null) {
            LOGGER.warning("Logged-in user is null, redirecting to /login");
            return new ModelAndView("redirect:/login");
        }
        try {
            boolean success = classService.createStudyRequest(
                loggedInUser.getUserId(),
                    availabilityId
            );
            if (success) {
                mv.addObject("successMessage", URLEncoder.encode("Study request sent successfully.", StandardCharsets.UTF_8));
            } else {
                mv.addObject("errorMessage", URLEncoder.encode("Failed to send study request. You may have already requested this slot.", StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            LOGGER.severe("Error creating study request: " + e.getMessage());
            mv.addObject("errorMessage", URLEncoder.encode("Error sending study request: " + e.getMessage(), StandardCharsets.UTF_8));
        }
        return mv;
    }

    @PostMapping("/study/approve")
    public ModelAndView approveStudyRequest(@RequestParam("requestId") String requestId) {
        LOGGER.info("Approving study request: " + requestId);
        ModelAndView mv = new ModelAndView("redirect:/");
        if (!userService.isAuthenticated()) {
            LOGGER.info("User not authenticated, redirecting to /login");
            return new ModelAndView("redirect:/login");
        }
        boolean success = classService.updateStudyRequestStatus(requestId, "APPROVED");
        if (success) {
            mv.addObject("successMessage", URLEncoder.encode("Study request approved.", StandardCharsets.UTF_8));
        } else {
            mv.addObject("errorMessage", URLEncoder.encode("Failed to approve study request.", StandardCharsets.UTF_8));
        }
        return mv;
    }

    @PostMapping("/study/reject")
    public ModelAndView rejectStudyRequest(@RequestParam("requestId") String requestId) {
        LOGGER.info("Rejecting study request: " + requestId);
        ModelAndView mv = new ModelAndView("redirect:/");
        if (!userService.isAuthenticated()) {
            LOGGER.info("User not authenticated, redirecting to /login");
            return new ModelAndView("redirect:/login");
        }
        boolean success = classService.updateStudyRequestStatus(requestId, "REJECTED");
        if (success) {
            mv.addObject("successMessage", URLEncoder.encode("Study request rejected.", StandardCharsets.UTF_8));
        } else {
            mv.addObject("errorMessage", URLEncoder.encode("Failed to reject study request.", StandardCharsets.UTF_8));
        }
        return mv;
    }

    // Helper class for display
    private static class UserAvailabilityDisplay {
        private final User user;
        private final ClassService.Availability availability;
        private final List<Class> sharedClasses;
        private final String startTimeFormatted;
        private final String endTimeFormatted;

        public UserAvailabilityDisplay(User user, ClassService.Availability availability, List<Class> sharedClasses,
                                      String startTimeFormatted, String endTimeFormatted) {
            this.user = user;
            this.availability = availability;
            this.sharedClasses = sharedClasses;
            this.startTimeFormatted = startTimeFormatted;
            this.endTimeFormatted = endTimeFormatted;
        }

        public User getUser() { return user; }
        public ClassService.Availability getAvailability() { return availability; }
        public List<Class> getSharedClasses() { return sharedClasses; }
        public String getStartTimeFormatted() { return startTimeFormatted; }
        public String getEndTimeFormatted() { return endTimeFormatted; }
    }

    private static class StudyRequestDisplay {
        private final int requestId;
        private final User requester;
        private final String targetUserId;
        private final int availabilityId;
        private final String status;
        private final LocalDateTime requestDate;
        private final LocalDate studyDate;
        private final String startTimeFormatted;
        private final String endTimeFormatted;

        public StudyRequestDisplay(int requestId, User requester, String targetUserId, int availabilityId, String status,
                                  LocalDateTime requestDate, LocalDate studyDate, String startTimeFormatted, String endTimeFormatted) {
            this.requestId = requestId;
            this.requester = requester;
            this.targetUserId = targetUserId;
            this.availabilityId = availabilityId;
            this.status = status;
            this.requestDate = requestDate;
            this.studyDate = studyDate;
            this.startTimeFormatted = startTimeFormatted;
            this.endTimeFormatted = endTimeFormatted;
        }

        public int getRequestId() { return requestId; }
        public User getRequester() { return requester; }
        public String getTargetUserId() { return targetUserId; }
        public int getAvailabilityId() { return availabilityId; }
        public String getStatus() { return status; }
        public LocalDateTime getRequestDate() { return requestDate; }
        public LocalDate getStudyDate() { return studyDate; }
        public String getStartTimeFormatted() { return startTimeFormatted; }
        public String getEndTimeFormatted() { return endTimeFormatted; }
    }

    private static class StudySessionDisplay {
        private final int requestId;
        private final User partner;
        private final int availabilityId;
        private final String status;
        private final LocalDateTime requestDate;
        private final LocalDate studyDate;
        private final String startTimeFormatted;
        private final String endTimeFormatted;

        public StudySessionDisplay(int requestId, User partner, int availabilityId, String status,
                                  LocalDateTime requestDate, LocalDate studyDate, String startTimeFormatted, String endTimeFormatted) {
            this.requestId = requestId;
            this.partner = partner;
            this.availabilityId = availabilityId;
            this.status = status;
            this.requestDate = requestDate;
            this.studyDate = studyDate;
            this.startTimeFormatted = startTimeFormatted;
            this.endTimeFormatted = endTimeFormatted;
        }

        public int getRequestId() { return requestId; }
        public User getPartner() { return partner; }
        public int getAvailabilityId() { return availabilityId; }
        public String getStatus() { return status; }
        public LocalDateTime getRequestDate() { return requestDate; }
        public LocalDate getStudyDate() { return studyDate; }
        public String getStartTimeFormatted() { return startTimeFormatted; }
        public String getEndTimeFormatted() { return endTimeFormatted; }
    }
}