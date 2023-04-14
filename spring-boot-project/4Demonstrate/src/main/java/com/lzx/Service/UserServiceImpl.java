package com.lzx.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author liuzhixuan
 * @date 2023-04-12
 */
@Component
public class UserServiceImpl {

	public User getUser() {
		Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("admin"));
		return new User("lzx", "123456", authorities);
	}
}
