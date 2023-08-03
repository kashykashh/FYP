/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Aug-03 12:02:17 PM

 */
package FYP;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 21033239
 *
 */

@Controller
public class ContactUsController {

	private final JavaMailSender emailSender;
	private final UserRepository userRepository;

	public ContactUsController(JavaMailSender emailSender, UserRepository userRepository) {
		this.emailSender = emailSender;
		this.userRepository = userRepository;
	}

	@GetMapping("/contact_us")
	public String showContactForm(Model model) {
		Long userId = getLoggedInUserId();

		if (userId != null) {
			User user = userRepository.getById(userId);
			System.out.println("User Role: " + user.getRole());
			model.addAttribute("user", user);
		}
		return "contact_us";
	}

	@PostMapping("/send-email")
	public String sendEmail(@RequestParam("role") String role, @RequestParam("name") String name,
			@RequestParam("username") String username, @RequestParam("email") String email,
			@RequestParam("banStatus") String banStatus, @RequestParam("message") String message) {

		String subject = "Contact Us - WorldBay Website - User Query";
		String receiverEmail = "worldbaywebsite@gmail.com";
		String emailText = "";
		String roleForEmail = "";

		if (role.equalsIgnoreCase("ROLE_ADMIN")) {
			roleForEmail = "Admin";
		} else if (role.equalsIgnoreCase("ROLE_SELLER")) {
			roleForEmail = "Seller";
		} else if (role.equalsIgnoreCase("ROLE_BUYER")) {
			roleForEmail = "Buyer";
		}

		if ((role.equals("")) || (role.equalsIgnoreCase("ROLE_USER"))) {
			emailText = "Role: Unregistered User" + "\n\nName: " + name + "\nEmail: " + email;
		} else {
			emailText = "Role: " + roleForEmail + "\n\nName: " + name + "\nUsername: " + username + "\nEmail: " + email
					+ "\nBanned: " + banStatus + "\n\n" + message;
		}

		// Create a SimpleMailMessage and send it
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setTo(receiverEmail);
		mailMessage.setSubject(subject);
		mailMessage.setText(emailText);
		emailSender.send(mailMessage);

		return "contact_us_thank_you";
	}

	public Long getLoggedInUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof UserDetails) {
				return ((UserDetail) principal).getId();
			}
		}
		return null;
	}

	@GetMapping("/contact_us_thank_you")
	public String showContactUsThankYou() {

		return "contact_us_thank_you";
	}
}
