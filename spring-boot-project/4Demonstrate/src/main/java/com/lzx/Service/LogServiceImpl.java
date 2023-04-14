package com.lzx.Service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author liuzhixuan
 * @date 2023-04-12
 */
@Component
public class LogServiceImpl {

	public Boolean saveLog(User user) {
		return Boolean.TRUE;
	}

}
