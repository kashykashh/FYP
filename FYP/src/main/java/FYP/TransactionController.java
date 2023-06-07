/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-May-31 1:46:42 AM

 */
package FYP;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 21033239
 *
 */
@Controller
public class TransactionController {

	@Autowired
	private OrderItemService orderItemService;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderItemRepository orderItemRepository;

	@GetMapping("/transactionHistory")
	public String viewTransactionHistory(Model model, Principal principal) {
		UserDetail loggedInUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Long loggedInUserId = loggedInUser.getUser().getId();

		model.addAttribute("userId", loggedInUser);

		List<OrderItem> OrderItemList = orderItemRepository.findByUserId(loggedInUserId);

		model.addAttribute("OrderItemList", OrderItemList);

		return "transactions";
	}

	@GetMapping("/user/{id}/transactions")
	public String viewUserTransactions(@PathVariable Long id, Model model) {
		List<OrderItem> transactions = orderItemRepository.findByUserId(id);
		if (transactions.isEmpty()) {
			model.addAttribute("message", "There are no transactions.");
			User user = userRepository.getById(id);
			model.addAttribute("userName", user.getName());
		} else {
			model.addAttribute("transactions", transactions);
		}
		return "viewUserTransactions";
	}

//	@GetMapping("/transactionHistory")
//	public String showTransactionHistory(Model model) {
//		List<OrderItem> orderItems = orderItemService.retrieveOrderItems();
//
//		// Retrieve only the orderItem.orderId values and store them in a separate list
//		List<String> orderItemIds = orderItems.stream().map(OrderItem::getOrderId).collect(Collectors.toList());
//
//		model.addAttribute("OrderItemList", orderItems);
//		model.addAttribute("orderItemIds", orderItemIds);
//		return "transactionHistory";
//	}
//
//	@GetMapping("/transactionSuccess")
//	public String showTransactionSuccess(@RequestParam("orderId") String orderId, Model model) {
//	    // Get the current timestamp
//	    Date timestamp = new Date();
//
//	    OrderItem orderItem = new OrderItem();
//	    orderItem.setOrderId(orderId);
//	    orderItem.setTimestamp(timestamp);
//
//	    orderItemRepository.save(orderItem);
//
//	    model.addAttribute("orderId", orderId);
//	    model.addAttribute("timestamp", timestamp);
//
//	    return "success";
//	}
//
//	@GetMapping("/success")
//	public String showSuccessPage(Model model) {
//	    OrderItem orderItem = orderItemService.retrieveOrderItemWithTimestamp();
//
//	    model.addAttribute("orderItem", orderItem);
//
//	    return "success";
//	}
}
