/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Aug-04 11:42:56 AM

 */
package FYP;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author 21033239
 *
 */

@Controller
public class WalletController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private WalletRepository walletRepository;

	@GetMapping("/wallet/details")
	@ResponseBody
	public Map<String, Object> getWalletDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Map<String, Object> response = new HashMap<>();

		if (authentication != null && authentication.isAuthenticated()) {
			Long loggedInUserId = getLoggedInUserId();
			if (loggedInUserId != null) {
				Optional<User> optionalUser = userRepository.findById(loggedInUserId);
				if (optionalUser.isPresent()) {
					User user = optionalUser.get();
					Wallet wallet = user.getWallet();

					response.put("walletBalance", wallet.getBalance());
					response.put("lastTopUp", wallet.getLastTopUp());
					response.put("lastSpendage", wallet.getLastSpendage());
					response.put("totalSpent", wallet.getTotalSpent());
					response.put("totalToppedUp", wallet.getTotalToppedUp());

					return response;
				}
			}
		}

		response.put("message", "Log In to view details");
		return response;
	}

	@GetMapping("/wallet/view")
	public String viewWallet(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			Long loggedInUserId = getLoggedInUserId();
			if (loggedInUserId != null) {
				Optional<User> optionalUser = userRepository.findById(loggedInUserId);
				if (optionalUser.isPresent()) {
					User user = optionalUser.get();
					Wallet wallet = user.getWallet();

					model.addAttribute("walletBalance", wallet.getBalance());
					model.addAttribute("lastTopUp", wallet.getLastTopUp());
					model.addAttribute("lastSpendage", wallet.getLastSpendage());
					model.addAttribute("totalSpent", wallet.getTotalSpent());
					model.addAttribute("totalToppedUp", wallet.getTotalToppedUp());
				}
			}
		}
		return "viewWallet";
	}

	@GetMapping("/wallet/top-up/enterAmount")
	public String topUpWalletEnterAmount(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			Long loggedInUserId = getLoggedInUserId();
			if (loggedInUserId != null) {
				Optional<User> optionalUser = userRepository.findById(loggedInUserId);
				if (optionalUser.isPresent()) {
					User user = optionalUser.get();
					Wallet wallet = user.getWallet();

					model.addAttribute("walletBalance", wallet.getBalance());
					model.addAttribute("lastTopUp", wallet.getLastTopUp());
					model.addAttribute("lastSpendage", wallet.getLastSpendage());
					model.addAttribute("totalSpent", wallet.getTotalSpent());
					model.addAttribute("totalToppedUp", wallet.getTotalToppedUp());
				}
			}
		}
		return "topUpWallet";
	}

	@PostMapping("/wallet/top-up")
	public String topUpWallet(HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			Long loggedInUserId = getLoggedInUserId();
			if (loggedInUserId != null) {
				Optional<User> optionalUser = userRepository.findById(loggedInUserId);
				if (optionalUser.isPresent()) {
					User user = optionalUser.get();
					Wallet wallet = user.getWallet();

					double topUpAmount = Double.parseDouble(request.getParameter("amount"));

					double currentBalance = wallet.getBalance();
					wallet.setBalance(currentBalance + topUpAmount);
					wallet.setLastTopUp(topUpAmount);
					wallet.setTotalToppedUp(wallet.getTotalToppedUp() + topUpAmount);

					walletRepository.save(wallet);
					userRepository.save(user);
				}
			}
		}
		return "redirect:/wallet/view";
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
}
