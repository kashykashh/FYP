/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Aug-04 3:13:35 PM

 */
package FYP;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author 21033239
 *
 */

@Entity
public class Wallet {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private double balance;
	private double lastTopUp;
	private double lastSpendage;
	private double totalSpent;
	private double totalToppedUp;

	@JsonIgnore
	@OneToOne
	@JoinColumn(name = "user_id")
	private User user;

	public Wallet() {
		this.balance = 0.0;
		this.lastTopUp = 0.0;
		this.lastSpendage = 0.0;
		this.totalSpent = 0.0;
		this.totalToppedUp = 0.0;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public double getLastTopUp() {
		return lastTopUp;
	}

	public void setLastTopUp(double lastTopUp) {
		this.lastTopUp = lastTopUp;
	}

	public double getLastSpendage() {
		return lastSpendage;
	}

	public void setLastSpendage(double lastSpendage) {
		this.lastSpendage = lastSpendage;
	}

	public double getTotalSpent() {
		return totalSpent;
	}

	public void setTotalSpent(double totalSpent) {
		this.totalSpent = totalSpent;
	}

	public double getTotalToppedUp() {
		return totalToppedUp;
	}

	public void setTotalToppedUp(double totalToppedUp) {
		this.totalToppedUp = totalToppedUp;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
