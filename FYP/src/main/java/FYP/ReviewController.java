/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Jul-27 12:14:59 AM

 */
package FYP;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author 21033239
 *
 */

@RestController
@RequestMapping("/seller")
public class ReviewController {

	@Autowired
	private ReviewService reviewService;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
    private ItemRepository itemRepository;

	@GetMapping("/{sellerId}")
    public ModelAndView viewSellerProfile(@PathVariable Long sellerId, ModelMap model) {
        User seller = userRepository.getById(sellerId);
        List<Review> sellerReviews = reviewService.getReviewsBySellerId(sellerId);

        // Fetch the items associated with the seller using the ItemRepository
        List<Item> itemList = itemRepository.findByUserId(sellerId);

        model.addAttribute("user", seller);
        model.addAttribute("sellerReviews", sellerReviews);
        model.addAttribute("itemList", itemList); // Pass the itemList to the template

        return new ModelAndView("seller_profile", model);
    }

	@PostMapping("/{sellerId}/addReview")
	public ModelAndView addSellerReview(@PathVariable Long sellerId, @RequestParam int rating,
			@RequestParam String comment, Principal principal) {
		// Get the authenticated user (the one submitting the review)
		String username = principal.getName();
		User currentUser = userRepository.findByUsername(username);

		// Get the seller by sellerId
		User seller = userRepository.getById(sellerId);

		// Create a new review
		Review review = new Review(currentUser, seller, rating, comment);

		// Save the review to the database
		reviewService.saveReview(review);

		// Redirect back to the seller's profile page with the updated data
		return new ModelAndView("redirect:/seller/" + sellerId);
	}
}
