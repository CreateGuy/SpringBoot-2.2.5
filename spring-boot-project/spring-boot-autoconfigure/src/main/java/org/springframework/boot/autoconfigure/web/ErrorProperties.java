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

package org.springframework.boot.autoconfigure.web;

import org.springframework.beans.factory.annotation.Value;

/**
 * web错误处理的配置属性
 */
public class ErrorProperties {

	/**
	 * 错误处理器路径
	 */
	@Value("${error.path:/error}")
	private String path = "/error";

	/**
	 * 是否包含异常的信息
	 * <p>比如说404了，是否需要将 exception 发给客户端</p>
	 */
	private boolean includeException;

	/**
	 * 是否包含堆栈信息
	 */
	private IncludeStacktrace includeStacktrace = IncludeStacktrace.NEVER;

	/**
	 * 当服务器出现错误时，是否启用浏览器显示默认错误页面
	 */
	private final Whitelabel whitelabel = new Whitelabel();

	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isIncludeException() {
		return this.includeException;
	}

	public void setIncludeException(boolean includeException) {
		this.includeException = includeException;
	}

	public IncludeStacktrace getIncludeStacktrace() {
		return this.includeStacktrace;
	}

	public void setIncludeStacktrace(IncludeStacktrace includeStacktrace) {
		this.includeStacktrace = includeStacktrace;
	}

	public Whitelabel getWhitelabel() {
		return this.whitelabel;
	}

	/**
	 * 是否包含堆栈信息
	 */
	public enum IncludeStacktrace {

		/**
		 * 不添加stacktrace信息
		 */
		NEVER,

		/**
		 * 始终添加stacktrace信息
		 */
		ALWAYS,

		/**
		 * 当trace请求参数为true时，添加stacktrace信息
		 */
		ON_TRACE_PARAM

	}

	/**
	 * 当服务器出现错误时，是否启用浏览器显示默认错误页面
	 */
	public static class Whitelabel {

		/**
		 * 当服务器出现错误时，是否启用浏览器显示默认错误页面
		 */
		private boolean enabled = true;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

	}

}
