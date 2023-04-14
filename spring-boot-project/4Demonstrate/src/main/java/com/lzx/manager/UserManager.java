package com.lzx.manager;

import com.lzx.Service.LogServiceImpl;
import com.lzx.Service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
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
public class UserManager {

	@Resource
	private UserServiceImpl userService;

	@Autowired
	private LogServiceImpl logService;

	private User getUser() {
		User user = userService.getUser();

		logService.saveLog(user);
		return user;
	}

}
