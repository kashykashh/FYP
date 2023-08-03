/**
 * 
 * I declare that this code was written by me, 21033239. 
 * I will not copy or allow others to copy my code. 
 * I understand that copying code is considered as plagiarism.
 * 
 * Student Name: Parveer Singh
 * Student ID: 21033239
 * Class: E63C
 * Date created: 2023-Jan-18 6:52:02 PM

 */
package FYP;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author 21033239
 *
 */

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Bean
	public UserDetailService userDetailsService() {
		return new UserDetailService();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder);

		return authProvider;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/**/*.jsp");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		String[] staticResources = { "/css/**", "/images/**", "/fonts/**", "/uploads/**", "/bootstrap/*/*" };

		http.authorizeRequests()
				.antMatchers("/forgot-password/**", "/password-reset-requested/**", "/password-reset-success/**",
						"/reset-password/**", "/cart/process_order/**").permitAll()
				.antMatchers("/categories/add", "/categories/edit/*", "/categories/save", "/categories/delete/*",
						"/user/**").hasRole("ADMIN")
				.antMatchers("/items/add", "/items/edit/{id}", "/items/save", "/items/delete/{id}").hasAnyRole("ADMIN", "SELLER")
				.antMatchers("/items/{id}", "/categories/{id}", "/about", "/help_center", "/contact_us", "/faq",
						"/terms_of_service", "/contact_us_thank_you").permitAll()
				.antMatchers( "/send-email").permitAll()
				.antMatchers("/sellerInventory/**").hasRole("SELLER")
				.antMatchers("/adminInventory/**").hasRole("ADMIN")
				.antMatchers("/", "/register/**", "/login").permitAll()
				.antMatchers(staticResources)
				.permitAll().anyRequest().authenticated()
				.and()
				.formLogin().loginPage("/login").permitAll()
				.defaultSuccessUrl("/", true)
				.and()
				.logout().logoutUrl("/logout").permitAll()
				.and().exceptionHandling()
				.accessDeniedPage("/403");
	}
}