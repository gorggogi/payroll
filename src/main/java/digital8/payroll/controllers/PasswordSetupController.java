package digital8.payroll.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

import digital8.payroll.entities.PasswordResetToken;
import digital8.payroll.entities.Users;
import digital8.payroll.repositories.PasswordResetRepository;
import digital8.payroll.repositories.UsersRepository;
import java.util.Optional;

@Controller
public class PasswordSetupController {

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/setup-password")
    public String showSetupPage(@RequestParam("token") String token, Model model, RedirectAttributes redirectAttributes) {
        Optional<PasswordResetToken> tokenOpt = passwordResetRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Invalid or missing setup link.");
            return "redirect:/"; 
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.isExpired()) {
            redirectAttributes.addFlashAttribute("error", "This setup link has expired. Please contact HR for a new one.");
            passwordResetRepository.delete(resetToken); 
            return "redirect:/";
        }

        model.addAttribute("token", token);
        return "html/setupPassword"; 
    }

    @PostMapping("/setup-password")
    public String processSetupPassword(@RequestParam("token") String token, 
                                       @RequestParam("password") String password,
                                       Model model,
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        
        Optional<PasswordResetToken> tokenOpt = passwordResetRepository.findByToken(token);
        
        if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired token.");
            return "redirect:/";
        }

        PasswordResetToken resetToken = tokenOpt.get();
        Users user = resetToken.getUser();

        user.setPasswordHash(passwordEncoder.encode(password));
        usersRepository.save(user);

        passwordResetRepository.delete(resetToken);

        if (request.getSession(false) != null) {
            request.getSession().invalidate();
        }

        redirectAttributes.addFlashAttribute("setupSuccessEmail", user.getEmail());
        return "redirect:/setup-success";
    }

    @GetMapping("/setup-success")
    public String setupSuccess(Model model) {
        if (!model.containsAttribute("setupSuccessEmail")) {
            return "redirect:/";
        }
        return "html/setup-success";
    }
}