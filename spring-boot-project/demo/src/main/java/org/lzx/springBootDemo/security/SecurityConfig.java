package org.lzx.springBootDemo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

/**
 * lzx
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}

	@Bean
	@Override
	public UserDetailsService userDetailsService() {
		return new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
				SimpleGrantedAuthority authorities = new SimpleGrantedAuthority("admin");
				ArrayList<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
				simpleGrantedAuthorities.add(authorities);
				return new User("javaboy", "123", simpleGrantedAuthorities);
			}
		};
	}
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/js/**", "/css/**", "/images/**");
        //web.ignoring().antMatchers("/hello2");
//        web.ignoring().antMatchers("/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
				.antMatchers("/hello1").hasRole("amdin11")
				.anyRequest().authenticated()
				.and()
				.sessionManagement()
				.maximumSessions(1)
				.and()
				.and()
				.formLogin()
				.and()
                .csrf().disable();
//        http.requestMatchers().mvcMatchers(HttpMethod.GET, "/hello");
    }
//
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//
//	}
}