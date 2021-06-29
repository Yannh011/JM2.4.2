package web.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import web.model.Role;
import web.model.User;
import web.service.UserService;

import java.util.HashSet;
import java.util.Set;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String showSignUpForm() {
        return "login";
    }

    @GetMapping("/login-error")
    public String login(Model model) {
        String errorMessage = "Incorrect name or email";
        model.addAttribute("errorMessage", errorMessage);
        return "login";
    }

    @GetMapping("/admin")
    public String showAdminPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin";
    }

    @GetMapping("/user")
    @PreAuthorize("hasAuthority('USER')")
    public ModelAndView showUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("user");
        modelAndView.addObject("user", user);
        return modelAndView;
    }

    @GetMapping("/addNewUser")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String adduser(Model model) {
        model.addAttribute("user", new User());
        return "/adduser";
    }

    @PostMapping("/adduser")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String saveUser(@RequestParam("name") String name,
                           @RequestParam("lastname") String lastname,
                           @RequestParam("email") String email,
                           @RequestParam("password") String password,
                           @RequestParam(required = false, name = "ADMIN") String ADMIN,
                           @RequestParam(required = false, name = "USER") String USER) {

        Set<Role> roles = new HashSet<>();
        if (ADMIN != null) {
            roles.add(new Role(1L, ADMIN));
        }
        if (USER != null) {
            roles.add(new Role(2L, USER));
        }
        if(ADMIN==null&&USER==null){
            roles.add(new Role(2L, USER));
        }

        User user = new User(name, lastname, email, password, roles);
        userService.add(user);

        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String showUpdateForm(@PathVariable("id") long id, Model model) {
//        User user = userService.getById(id);
        model.addAttribute("user", userService.getById(id));//user);
        return "update-user";
    }

    @PostMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String updateUser(@ModelAttribute("user") User user,
                             @PathVariable("id") long id,
                             @RequestParam(required = false, name = "ADMIN") String ADMIN,
                             @RequestParam(required = false, name = "USER") String USER) {

        Set<Role> roles = new HashSet<>();
        if (ADMIN != null) {
            roles.add(new Role(1L, ADMIN));
        }
        if (USER != null) {
            roles.add(new Role(2L, USER));
        }
        if(ADMIN==null&&USER==null){
            roles.add(new Role(2L, USER));
        }
        user.setRoles(roles);
        userService.update(user);
        return "redirect:/admin";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String deleteUser(@PathVariable("id") long id, Model model) {
        userService.delete(userService.getById(id));
        model.addAttribute("users", userService.getAllUsers());
        return "admin";
    }
}
