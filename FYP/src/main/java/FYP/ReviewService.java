/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Jul-27 12:17:00 AM

 */
package FYP;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 21033239
 *
 */

@Service
public class ReviewService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private UserDetailService userDetailService;

	public User getUserByUsername(String username) {
		UserDetail userDetail = userDetailService.loadUserByUsername(username);
		return userDetail.getUser();
	}

	public User getUserById(Long id) {
		return userRepository.findById(id).orElse(null);
	}

	public void saveReview(Review review) {
		reviewRepository.save(review);
	}

	public List<Review> getReviewsBySellerId(Long sellerId) {
		return reviewRepository.findBySellerId(sellerId);
	}
}
