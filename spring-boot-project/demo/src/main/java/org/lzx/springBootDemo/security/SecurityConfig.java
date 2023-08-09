package org.lzx.springBootDemo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.intercept.RunAsManagerImpl;
import org.springframework.security.config.annotation.ObjectPostProcessor;
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
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;

import java.util.ArrayList;
import java.util.List;

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
				List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
				SimpleGrantedAuthority authorities = new SimpleGrantedAuthority("ROLE_admin");
				SimpleGrantedAuthority authorities2 = new SimpleGrantedAuthority("RUN_AS_security");

				simpleGrantedAuthorities.add(authorities);
				simpleGrantedAuthorities.add(authorities2);
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
				.antMatchers("/hello").hasRole("admin")
				.antMatchers("/hello1").hasAuthority("RUN_AS_security")
				.anyRequest().authenticated()
				.and()
				.formLogin()
				.and()
                .csrf()
                .disable();

        http.requiresChannel()
				.channelProcessors(new ArrayList<>());
				//.antMatchers()
				//.mvcMatchers("/*").requires("REQUIRES_SECURE_CHANNEL");
	}
}