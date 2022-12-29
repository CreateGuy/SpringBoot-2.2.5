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

package org.springframework.boot.web.servlet.error;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * {@link ErrorAttributes} 的默认实现。在可能会提供以下属性:
 * <ul>
 * 		<li>timestamp - 错误发生的时间</li>
 * 		<li>status - 错误的响应码</li>
 * 		<li>error - 错误的原因</li>
 * 		<li>exception - The class name of the root exception (if configured)</li>
 * 		<li>message - The exception message</li>
 * 		<li>errors - {@link BindingResult} 中的绑定异常
 * 		<li>trace - 异常堆栈信息 trace</li>
 * 		<li>path - 引发错误的请求路径</li>
 * </ul>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DefaultErrorAttributes implements ErrorAttributes, HandlerExceptionResolver, Ordered {

	/**
	 * 请求域中保存异常的属性名
	 */
	private static final String ERROR_ATTRIBUTE = DefaultErrorAttributes.class.getName() + ".ERROR";

	private final boolean includeException;

	/**
	 * Create a new {@link DefaultErrorAttributes} instance that does not include the
	 * "exception" attribute.
	 */
	public DefaultErrorAttributes() {
		this(false);
	}

	/**
	 * Create a new {@link DefaultErrorAttributes} instance.
	 * @param includeException whether to include the "exception" attribute
	 */
	public DefaultErrorAttributes(boolean includeException) {
		this.includeException = includeException;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		// 将异常保存在请求域中
		storeErrorAttributes(request, ex);
		return null;
	}

	/**
	 * 将异常保存在请求域中
	 * @param request
	 * @param ex
	 */
	private void storeErrorAttributes(HttpServletRequest request, Exception ex) {
		request.setAttribute(ERROR_ATTRIBUTE, ex);
	}

	/**
	 * 返回错误的相关属性
	 * @param webRequest the source request
	 * @param includeStackTrace if stack trace elements should be included
	 * @return
	 */
	@Override
	public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
		Map<String, Object> errorAttributes = new LinkedHashMap<>();
		errorAttributes.put("timestamp", new Date());
		// 添加错误的状态
		addStatus(errorAttributes, webRequest);
		// 添加错误的详情
		addErrorDetails(errorAttributes, webRequest, includeStackTrace);
		// 添加错误的请求路径
		addPath(errorAttributes, webRequest);
		return errorAttributes;
	}

	/**
	 * 添加错误的状态
	 * @param errorAttributes
	 * @param requestAttributes
	 */
	private void addStatus(Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
		// 从请求域中读取 javax.servlet.error.status_code 属性
		Integer status = getAttribute(requestAttributes, "javax.servlet.error.status_code");
		if (status == null) {
			errorAttributes.put("status", 999);
			errorAttributes.put("error", "None");
			return;
		}
		errorAttributes.put("status", status);
		try {
			errorAttributes.put("error", HttpStatus.valueOf(status).getReasonPhrase());
		}
		catch (Exception ex) {
			// Unable to obtain a reason
			errorAttributes.put("error", "Http Status " + status);
		}
	}

	/**
	 * 添加错误的详情
	 * @param errorAttributes
	 * @param webRequest
	 * @param includeStackTrace
	 */
	private void addErrorDetails(Map<String, Object> errorAttributes, WebRequest webRequest,
			boolean includeStackTrace) {
		//  返回错误的根本原因
		Throwable error = getError(webRequest);
		if (error != null) {
			while (error instanceof ServletException && error.getCause() != null) {
				error = error.getCause();
			}
			if (this.includeException) {
				errorAttributes.put("exception", error.getClass().getName());
			}
			// 添加绑定结果
			addErrorMessage(errorAttributes, error);
			// 是否添加错误的堆栈信息
			if (includeStackTrace) {
				addStackTrace(errorAttributes, error);
			}
		}
		Object message = getAttribute(webRequest, "javax.servlet.error.message");
		if ((!StringUtils.isEmpty(message) || errorAttributes.get("message") == null)
				&& !(error instanceof BindingResult)) {
			errorAttributes.put("message", StringUtils.isEmpty(message) ? "No message available" : message);
		}
	}

	/**
	 * 添加绑定结果
	 * @param errorAttributes
	 * @param error
	 */
	private void addErrorMessage(Map<String, Object> errorAttributes, Throwable error) {
		// 提取绑定结果
		BindingResult result = extractBindingResult(error);
		if (result == null) {
			errorAttributes.put("message", error.getMessage());
			return;
		}
		if (result.hasErrors()) {
			errorAttributes.put("errors", result.getAllErrors());
			errorAttributes.put("message", "Validation failed for object='" + result.getObjectName()
					+ "'. Error count: " + result.getErrorCount());
		}
		else {
			errorAttributes.put("message", "No errors");
		}
	}

	/**
	 * 提取绑定结果
	 * @param error
	 * @return
	 */
	private BindingResult extractBindingResult(Throwable error) {
		if (error instanceof BindingResult) {
			return (BindingResult) error;
		}
		if (error instanceof MethodArgumentNotValidException) {
			return ((MethodArgumentNotValidException) error).getBindingResult();
		}
		return null;
	}

	/**
	 * 添加错误的堆栈信息
	 * @param errorAttributes
	 * @param error
	 */
	private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
		StringWriter stackTrace = new StringWriter();
		error.printStackTrace(new PrintWriter(stackTrace));
		stackTrace.flush();
		errorAttributes.put("trace", stackTrace.toString());
	}

	/**
	 * 添加错误的请求路径
	 * @param errorAttributes
	 * @param requestAttributes
	 */
	private void addPath(Map<String, Object> errorAttributes, RequestAttributes requestAttributes) {
		String path = getAttribute(requestAttributes, "javax.servlet.error.request_uri");
		if (path != null) {
			errorAttributes.put("path", path);
		}
	}

	/**
	 * 返回错误的根本原因，如果无法提取错误，则返回空
	 * @param webRequest the source request
	 * @return
	 */
	@Override
	public Throwable getError(WebRequest webRequest) {
		Throwable exception = getAttribute(webRequest, ERROR_ATTRIBUTE);
		if (exception == null) {
			exception = getAttribute(webRequest, "javax.servlet.error.exception");
		}
		return exception;
	}

	/**
	 * 从请求域中读取属性
	 * @param requestAttributes
	 * @param name 属性名称
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
		return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
	}

}
