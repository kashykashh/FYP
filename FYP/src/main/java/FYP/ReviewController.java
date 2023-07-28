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
import java.util.Map;
import java.util.stream.Collectors;

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

		// Create a list of reviews with comments
		List<Review> sellerReviewsWithComments = sellerReviews.stream()
				.filter(review -> review.getComment() != null && !review.getComment().isEmpty())
				.collect(Collectors.toList());

		model.addAttribute("user", seller);
		model.addAttribute("sellerReviews", sellerReviews);
		model.addAttribute("sellerReviewsWithComments", sellerReviewsWithComments);

		// Calculate the average rating for the seller based on reviews with comments
		double averageRating = Review.calculateAverageRating(sellerReviewsWithComments);
		String formattedAverageRating = String.format("%.1f", averageRating);
		model.addAttribute("averageRating", formattedAverageRating);

		// Calculate the star ratings percentage for all reviews (with and without
		// comments)
		Map<Integer, Double> starRatingsPercentage = Review.calculateStarRatingsPercentage(sellerReviews);
		model.addAttribute("starRatingsPercentage", starRatingsPercentage);

		List<Item> itemList = itemRepository.findByUserId(sellerId);
		model.addAttribute("itemList", itemList);

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
