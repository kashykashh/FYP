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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

		// Get currently logged in user
		UserDetail loggedInUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Long loggedInUserId = loggedInUser.getUser().getId();

		model.addAttribute("userId", loggedInUserId);

		// Get shopping cart items added by this user
		// *Hint: You will need to use the method we added in the CartItemRepository
		List<CartItem> cartItemList = cartItemRepo.findByUser_Id(loggedInUserId);
		// Add the shopping cart items to the model
		model.addAttribute("cartItemList", cartItemList);

		int cartTotal = 0;
		// Calculate the total cost of all items in the shopping cart
		for (int i = 0; i < cartItemList.size(); i++) {
			CartItem currentCartItem = cartItemList.get(i);
			double price = currentCartItem.getItem().getPrice();
			double subTotal = currentCartItem.getQuantity() * price;

			currentCartItem.setSubtotal(subTotal);
			cartTotal += subTotal;
		}
		// Add the shopping cart total to the model
		model.addAttribute("cartTotal", cartTotal);
		return "cart";
	}

	@PostMapping("/cart/add/{itemId}")
	public String addToCart(@PathVariable("itemId") Long itemId, @RequestParam("quantity") int quantity,
			Principal principal) {

		// Get currently logged in user
		UserDetail loggedInUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Long loggedInUserId = loggedInUser.getUser().getId();
		// Check in the cartItemRepo if item was previously added by user.
		// *Hint: we will need to write a new method in the CartItemRepository
		CartItem cartItem = cartItemRepo.findByUserIdAndItemId(loggedInUserId, itemId);

		if (cartItem != null) {
			// if the item was previously added, then we get the quantity that was
			// previously added and increase that
			// Save the CartItem object back to the repository
			int cartItemQty = cartItem.getQuantity();
			int totalQuantity = cartItemQty + quantity;
			cartItem.setQuantity(totalQuantity);
			// if the item was NOT previously added,
			// then prepare the item and user objects
			cartItemRepo.save(cartItem);
		} else {
			// Create a new CartItem object
			Item item = itemRepo.getById(itemId);

			User user = userRepo.getById(loggedInUserId);

			CartItem newCartItem = new CartItem();

			// Set the item and user as well as the new quantity in the new CartItem
			// object
			newCartItem.setItem(item);
			newCartItem.setUser(user);
			newCartItem.setQuantity(quantity);
			// Save the new CartItem object to the repository
			cartItemRepo.save(newCartItem);
		}

		return "redirect:/cart";
	}

	@PostMapping("/cart/update/{id}")
	public String updateCart(@PathVariable("id") Long cartItemId, @RequestParam("qty") int qty) {

		// Get cartItem object by cartItem's id
		CartItem cartItem = cartItemRepo.getById(cartItemId);

		// Set the quantity of the carItem object retrieved
		cartItem.setQuantity(qty);

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
		List<CartItem> cartItemList = cartItemRepo.findByUser_Id(userId);

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

			// clear cart items belonging to user
			cartItemRepo.deleteById(currentCartItem.getId());
		}
		// Pass info to view to display success page
		model.addAttribute("transactionId", transactionId);
		model.addAttribute("orderId", orderId);

		// Send email
		String subject = "Booklink order is confirmed!";
		String body = "Thank you for your order!\n" + "Order ID: " + orderId + "\n" + "Transaction ID: "
				+ transactionId;
		String to = currentUser.getEmail();
		sendEmail(to, subject, body);

		// Send email
		return "success";
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
}
