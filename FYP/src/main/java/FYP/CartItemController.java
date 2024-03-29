/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Feb-09 12:31:39 PM

 */
package FYP;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
public class CartItemController {

	@Autowired
	private CartItemRepository cartItemRepo;

	@Autowired
	private ItemRepository itemRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private OrderItemRepository orderItemRepo;

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private TopSellingItemRepository topSellingItemRepository;

	@GetMapping("/cart")
	public String showCart(Model model, Principal principal) {
		UserDetail loggedInUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = loggedInUser.getUser();
		Long loggedInUserId = user.getId();

		model.addAttribute("userId", loggedInUserId);

		List<CartItem> cartItemList = cartItemRepo.findByUserId(loggedInUserId);
		model.addAttribute("cartItemList", cartItemList);

		for (CartItem cartItem : cartItemList) {
			cartItem.calculateSubtotal();
		}

		double cartTotal = calculateCartTotal(cartItemList);
		model.addAttribute("cartTotal", cartTotal);

		return "cart";
	}

	@PostMapping("/cart/add/{itemId}")
	public String addToCart(@PathVariable("itemId") Long itemId, @RequestParam("quantity") int quantity,
			Principal principal, RedirectAttributes redirectAttributes) {
		UserDetail loggedInUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = loggedInUser.getUser();
		Long loggedInUserId = user.getId();

		Item item = itemRepo.getById(itemId);
		int availableQuantity = item.getQuantity();

		if (quantity > availableQuantity) {
			redirectAttributes.addFlashAttribute("quantityError",
					"Quantity exceeds the available stock. Please enter a lower quantity.");
			return "redirect:/items/" + itemId;
		}

		CartItem cartItem = cartItemRepo.findByUserAndItemId(user, itemId);

		if (cartItem != null) {
			int cartItemQty = cartItem.getQuantity();
			int totalQuantity = cartItemQty + quantity;

			// If the total quantity exceeds the available quantity, set the cart item
			// quantity to the available quantity.
			if (totalQuantity > availableQuantity) {
				cartItem.setQuantity(availableQuantity);
			} else {
				cartItem.setQuantity(totalQuantity);
			}
			cartItemRepo.save(cartItem);
		} else {
			cartItem = new CartItem();
			cartItem.setItem(item);
			cartItem.setUser(user);

			// If the requested quantity exceeds the available quantity, set the cart item
			// quantity to the available quantity.
			if (quantity > availableQuantity) {
				cartItem.setQuantity(availableQuantity);
			} else {
				cartItem.setQuantity(quantity);
			}

			cartItem.calculateSubtotal();
			cartItemRepo.save(cartItem);
		}

		return "redirect:/cart";
	}

	@PostMapping("/cart/update/{id}")
	public String updateCart(@PathVariable("id") Long cartItemId, @RequestParam("qty") int qty) {
		// Get cartItem object by cartItem's id
		CartItem cartItem = cartItemRepo.getById(cartItemId);

		// Get the available quantity for the item
		int availableQuantity = cartItem.getItem().getQuantity();

		// If the updated quantity exceeds the available quantity, set the cart item
		// quantity to the available quantity.
		if (qty > availableQuantity) {
			cartItem.setQuantity(availableQuantity);
		} else {
			cartItem.setQuantity(qty);
		}

		// Save the cartItem back to the cartItemRepo
		cartItemRepo.save(cartItem);

		return "redirect:/cart";
	}

	@GetMapping("/cart/remove/{id}")
	public String removeFromCart(@PathVariable("id") Long cartItemId) {

		CartItem cartItem = cartItemRepo.getById(cartItemId);

		// Remove item from cartItemRepo
		cartItemRepo.delete(cartItem);

		return "redirect:/cart";
	}

	@PostMapping("/cart/process_order")
	public String processOrder(Model model, @RequestParam("cartTotal") double cartTotal,
			@RequestParam("userId") Long userId, @RequestParam("orderId") String orderId,
			@RequestParam("transactionId") String transactionId) {
		// Retrieve cart items purchased
		List<CartItem> cartItemList = cartItemRepo.findByUserId(userId);

		// Get user object
		User currentUser = userRepo.getById(userId);
		// Loop to iterate through all cart items
		for (int i = 0; i < cartItemList.size(); i++) {
			// Retrieve details about current cart item
			CartItem currentCartItem = cartItemList.get(i);

			Item currentItem = currentCartItem.getItem();

			// Update item table
			int qtyInventory = currentItem.getQuantity();
			int qtyInCart = currentCartItem.getQuantity();
			int qtyToUpdate = qtyInventory - qtyInCart;
			currentItem.setQuantity(qtyToUpdate);
			itemRepo.save(currentItem);

			// Update top selling item
			TopSellingItem topSellingItem = topSellingItemRepository.findByItem(currentItem);
			if (topSellingItem == null) {
				// Create a new top selling item
				topSellingItem = new TopSellingItem();
				topSellingItem.setItem(currentItem);
				topSellingItem.setSeller(currentItem.getUser());
				topSellingItem.setQuantitySold(qtyInCart);
			} else {
				// Increment the quantity sold of the existing top selling item
				topSellingItem.setQuantitySold(topSellingItem.getQuantitySold() + qtyInCart);
			}
			topSellingItemRepository.save(topSellingItem);

			// Add item to order table
			OrderItem newOrderItem = new OrderItem();

			newOrderItem.setItem(currentItem);
			newOrderItem.setUser(currentUser);
			newOrderItem.setQuantity(qtyInCart);
			newOrderItem.setSubtotal(currentCartItem.getSubtotal());
			newOrderItem.setOrderId(orderId);
			newOrderItem.setTransactionId(transactionId);

			orderItemRepo.save(newOrderItem);

		}
		// clear cart items belonging to user
		cartItemRepo.deleteAll(cartItemList);

		// Pass info to view to display success page
		model.addAttribute("transactionId", transactionId);
		model.addAttribute("orderId", orderId);

		// Send email
		String subject = "WorldBay order is confirmed!";
		String body = "Thank you for your order!\n" + "Order ID: " + orderId + "\n" + "Transaction ID: "
				+ transactionId;
		String to = currentUser.getEmail();
		sendEmail(to, subject, body);

		return "/success";
	}

	@PostMapping("/cart/payWithWallet")
	public String payWithWallet(HttpServletRequest request, Model model, @RequestParam("cartTotal") double cartTotal,
			@RequestParam("orderId") String orderId, @RequestParam("transactionId") String transactionId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.isAuthenticated()) {
			Long loggedInUserId = getLoggedInUserId();
			if (loggedInUserId != null) {
				Optional<User> optionalUser = userRepo.findById(loggedInUserId);
				if (optionalUser.isPresent()) {
					// Retrieve cart items purchased
					List<CartItem> cartItemList = cartItemRepo.findByUserId(loggedInUserId);

					User currentUser = userRepo.getById(loggedInUserId);
					Wallet wallet = currentUser.getWallet();

					if (cartTotal <= wallet.getBalance()) {
						// Deduct the price of the items from the user's wallet balance.
						double beforePurchaseTotalSpent = wallet.getTotalSpent() + cartTotal;
						double currentBalance = wallet.getBalance();
						wallet.setBalance(currentBalance - cartTotal);
						wallet.setLastSpendage(cartTotal);
						wallet.setTotalSpent(beforePurchaseTotalSpent);

						for (int i = 0; i < cartItemList.size(); i++) {
							// Retrieve details about current cart item
							CartItem currentCartItem = cartItemList.get(i);

							Item currentItem = currentCartItem.getItem();

							// Update item table
							int qtyInventory = currentItem.getQuantity();
							int qtyInCart = currentCartItem.getQuantity();
							int qtyToUpdate = qtyInventory - qtyInCart;
							currentItem.setQuantity(qtyToUpdate);
							itemRepo.save(currentItem);

							// Update top selling item
							TopSellingItem topSellingItem = topSellingItemRepository.findByItem(currentItem);
							if (topSellingItem == null) {
								// Create a new top selling item
								topSellingItem = new TopSellingItem();
								topSellingItem.setItem(currentItem);
								topSellingItem.setSeller(currentItem.getUser());
								topSellingItem.setQuantitySold(qtyInCart);
							} else {
								// Increment the quantity sold of the existing top selling item
								topSellingItem.setQuantitySold(topSellingItem.getQuantitySold() + qtyInCart);
							}
							topSellingItemRepository.save(topSellingItem);

							// Add item to order table
							OrderItem newOrderItem = new OrderItem();

							newOrderItem.setItem(currentItem);
							newOrderItem.setUser(currentUser);
							newOrderItem.setQuantity(qtyInCart);
							newOrderItem.setSubtotal(currentCartItem.getSubtotal());
							newOrderItem.setOrderId("Paid with Wallet");
							newOrderItem.setTransactionId("Paid with Wallet");

							orderItemRepo.save(newOrderItem);

						}

						// Send an email to the user confirming the order.
						String subject = "WorldBay order is confirmed!";
						String body = "Thank you for your order!\n" + "Order ID: " + orderId;
						String to = currentUser.getEmail();
						sendEmail(to, subject, body);

						cartItemRepo.deleteAll(cartItemList);

						// Notify the user that the payment was successful.
						model.addAttribute("message", "Payment successful!");

						return "successWallet";

					} else {
						model.addAttribute("message", "Insufficient balance in wallet");
						model.addAttribute("cartItemList", cartItemList);
						model.addAttribute("cartTotal", cartTotal);

						return "cart";
					}
				}
			}
		}
		return "successWallet";
	}

	public void sendEmail(String to, String subject, String body) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setTo(to);
		msg.setSubject(subject);
		msg.setText(body);
		System.out.println("Sending");
		javaMailSender.send(msg);
		System.out.println("Sent");
	}

	private double calculateCartTotal(List<CartItem> cartItemList) {
		double cartTotal = 0;
		for (CartItem cartItem : cartItemList) {
			cartItem.calculateSubtotal();
			cartTotal += cartItem.getSubtotal();
		}
		return cartTotal;
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
