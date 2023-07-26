

/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2022-Dec-01 11:58:09 PM

 */
package FYP;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;

/**
 * @author 21033239
 *
 */

@Entity
public class Item {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String description;
	private double price;
	private int quantity;
	private String imgName;
	private boolean advertise;
	private Integer duration;
	private String currency;

	@Column(name = "previous_quantity", nullable = false, columnDefinition = "INT DEFAULT 0")
	private Integer previousQuantity;

	@ElementCollection
	@OrderColumn(name = "change_index")
	private List<Integer> quantityHistory = new ArrayList<>();

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	public String getImgName() {
		return imgName;
	}

	public void setImgName(String imgName) {
		this.imgName = imgName;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		if (this.quantity != -1) {
			this.previousQuantity = this.quantity;
		}
		this.quantity = quantity;
		this.quantityHistory.add(quantity);
	}

	public Integer getPreviousQuantity() {
		if (quantityHistory.size() > 1) {
			return quantityHistory.get(quantityHistory.size() - 2);
		}
		if (previousQuantity == null) {
			return 0;
		}
		return previousQuantity;
	}

	public List<Integer> getQuantityHistory() {
		return quantityHistory;
	}

	public void setQuantityHistory(List<Integer> quantityHistory) {
		if (this.quantityHistory.isEmpty()) {
			this.quantityHistory.add(0);
		}
		this.quantityHistory.addAll(quantityHistory);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isAdvertise() {
		return advertise;
	}

	public void setAdvertise(boolean advertise) {
		this.advertise = advertise;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
}
