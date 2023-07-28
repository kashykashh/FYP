/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Jul-27 12:18:25 AM

 */
package FYP;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * @author 21033239
 *
 */

@Entity
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private User reviewer;

	@ManyToOne
	private User seller;

	private int rating;

	private String comment;

	public Review() {
	}

	public Review(User reviewer, User seller, int rating, String comment) {
		this.reviewer = reviewer;
		this.seller = seller;
		this.rating = rating;
		this.comment = comment;
	}

	public static double calculateAverageRating(List<Review> reviews) {
		if (reviews == null || reviews.isEmpty()) {
			return 0.0;
		}

		int totalRating = 0;
		for (Review review : reviews) {
			totalRating += review.getRating();
		}

		return (double) totalRating / reviews.size();
	}

	public static Map<Integer, Double> calculateStarRatingsPercentage(List<Review> reviews) {
		Map<Integer, Long> ratingCounts = reviews.stream()
				.collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

		long totalRatings = reviews.size();

		Map<Integer, Double> ratingPercentage = new HashMap<>();
		for (int i = 1; i <= 5; i++) {
			long count = ratingCounts.getOrDefault(i, 0L);
			double percentage = (count / (double) totalRatings) * 100;
			ratingPercentage.put(i, percentage);
		}

		return ratingPercentage;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getReviewer() {
		return reviewer;
	}

	public void setReviewer(User reviewer) {
		this.reviewer = reviewer;
	}

	public User getSeller() {
		return seller;
	}

	public void setSeller(User seller) {
		this.seller = seller;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
