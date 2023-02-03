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

package org.springframework.boot.web.servlet;

/**
 * 派发类型
 * <ul>
 *      <li>比如说将某个过滤器的派发类型设置为 forward，那么只有在内部使用request.getRequestDispatcher("/").forward(request,response);，才会执行此过滤器</li>
 *      <li>详情见：{@link org.apache.catalina.core.ApplicationFilterFactory#matchDispatcher(FilterMap, javax.servlet.DispatcherType)}</li>
 * </ul>

 */
public enum DispatcherType {

	/**
	 * 在 "RequestDispatcher.forward()" 的情况下才调用此过滤器
	 */
	FORWARD,

	/**
	 * 在 "RequestDispatcher.inclue()" 的情况下才调用此过滤器
	 */
	INCLUDE,

	/**
	 * 正常的请求下调用此过滤器
	 */
	REQUEST,

	/**
	 * 从 {@link javax.servlet.AsyncContext} 的分派的调用下此过滤器
	 * <p>比如说通过 {@link org.springframework.web.context.request.async.WebAsyncManager} 开启异步任务</p>
	 */
	ASYNC,

	/**
	 * 错误的请求
	 * <p>比如说不存在在请求地址</p>
	 */
	ERROR

}
