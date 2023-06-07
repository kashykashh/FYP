/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-May-19 9:46:17 PM

 */
package FYP;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author 21033239
 *
 */

@Controller
public class ForgotPasswordController {

	@Autowired
	private UserService userService;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@GetMapping("/forgot-password")
	public String showForgotPasswordForm() {
		return "forgot-password";
	}

	@GetMapping("/password-reset-requested")
	public String showPasswordResetRequestedPage() {
		return "password-reset-requested";
	}

	@GetMapping("/password-reset-success")
	public String showPasswordResetSuccessPage() {
		return "password-reset-success";
	}

	@GetMapping("/reset-password/{token}")
	public String showResetPasswordForm(@PathVariable("token") String token, Model model) {
		User user = userService.getUserByResetToken(token);

		if (user == null) {
			return "redirect:/forgot-password";
		}

		model.addAttribute("token", token);
		return "reset-password";
	}

	@PostMapping("/reset-password/{token}")
	public String resetNewPassword(@PathVariable("token") String token,
			@RequestParam("new-password") String newPassword, @RequestParam("confirm-password") String confirmPassword,
			Model model) {

		User user = userService.getUserByResetToken(token);

		if (user == null) {
			return "redirect:/forgot-password";
		}

		// Check if the new password and confirm password match
		if (!newPassword.equals(confirmPassword)) {
			model.addAttribute("error", "New password and confirm password do not match.");
			return "reset-password";
		}

		// Update the user's password
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setResetToken(null);
		userService.saveUser(user);

		// Redirect to a password reset success page or show a success message
		return "redirect:/password-reset-success";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam("email") String email, HttpServletRequest request,
			RedirectAttributes redirectAttributes) {
		User user = userService.getUserByEmail(email);

		if (user == null) {
			return "redirect:/forgot-password";
		}
		// Generate a password reset token
		String token = UUID.randomUUID().toString();

		// Save the token in the user entity
		user.setResetToken(token);
		userService.saveUser(user);

		// Build the password reset URL
		String resetUrl = getResetUrl(request, token);

		// Send the password reset email
		sendResetEmail(user.getEmail(), resetUrl);

		// Add a success message to the redirect attributes
		redirectAttributes.addFlashAttribute("success", "Password reset email has been sent. Please check your email.");

		// Redirect to a confirmation page
		return "redirect:/password-reset-requested";
	}

	private String getResetUrl(HttpServletRequest request, String token) {
		String url = request.getRequestURL().toString();
		String baseUrl = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
		return baseUrl + "/reset-password/" + token;
	}

	private void sendResetEmail(String email, String resetUrl) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Password Reset");
		message.setText("To reset your password, click the link below:\n" + resetUrl);
		mailSender.send(message);
	}
}
