/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.DispatcherType;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;

/**
 * SpringSecurity 的属性
 */
@ConfigurationProperties(prefix = "spring.security")
public class SecurityProperties {

	/**
	 * 默认的 {@link org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter} 的初始化顺序
	 * <li>这里的默认指的是系统配置的</li>
	 */
	public static final int BASIC_AUTH_ORDER = Ordered.LOWEST_PRECEDENCE - 5;

	/**
	 * Order applied to the WebSecurityConfigurer that ignores standard static resource
	 * paths.
	 */
	public static final int IGNORED_ORDER = Ordered.HIGHEST_PRECEDENCE;

	/**
	 * springSecurityFilterChain 在 Servlet 容器中的默认顺序(即在容器中注册的其他过滤器之间的顺序)
	 */
	public static final int DEFAULT_FILTER_ORDER = OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 100;

	/**
	 * springSecurityFilterChain 的属性
	 */
	private final Filter filter = new Filter();

	/**
	 * 基于内存的用户
	 */
	private User user = new User();

	public User getUser() {
		return this.user;
	}

	public Filter getFilter() {
		return this.filter;
	}

	/**
	 * 配置 springSecurityFilterChain 的属性
	 */
	public static class Filter {

		/**
		 * springSecurityFilterChain 在 Servlet 容器中的默认顺序(即在容器中注册的其他过滤器之间的顺序)
		 */
		private int order = DEFAULT_FILTER_ORDER;

		/**
		 * 此过滤器支持的派发类型
		 */
		private Set<DispatcherType> dispatcherTypes = new HashSet<>(
				Arrays.asList(DispatcherType.ASYNC, DispatcherType.ERROR, DispatcherType.REQUEST));

		public int getOrder() {
			return this.order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public Set<DispatcherType> getDispatcherTypes() {
			return this.dispatcherTypes;
		}

		public void setDispatcherTypes(Set<DispatcherType> dispatcherTypes) {
			this.dispatcherTypes = dispatcherTypes;
		}

	}

	/**
	 * 基于配置文件的内存用户
	 */
	public static class User {

		/**
		 * 用户名
		 */
		private String name = "user";

		/**
		 * 密码
		 */
		private String password = UUID.randomUUID().toString();

		/**
		 * 用户角色
		 */
		private List<String> roles = new ArrayList<>();

		/**
		 * 是否使用随机生成的密码
		 */
		private boolean passwordGenerated = true;

		public String getName() {
			return this.name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPassword() {
			return this.password;
		}

		/**
		 * 设置密码
		 * <li>配置文件的原理貌似就是调用set方法</li>
		 * @param password
		 */
		public void setPassword(String password) {
			if (!StringUtils.hasLength(password)) {
				return;
			}
			this.passwordGenerated = false;
			this.password = password;
		}

		public List<String> getRoles() {
			return this.roles;
		}

		public void setRoles(List<String> roles) {
			this.roles = new ArrayList<>(roles);
		}

		public boolean isPasswordGenerated() {
			return this.passwordGenerated;
		}

	}

}
