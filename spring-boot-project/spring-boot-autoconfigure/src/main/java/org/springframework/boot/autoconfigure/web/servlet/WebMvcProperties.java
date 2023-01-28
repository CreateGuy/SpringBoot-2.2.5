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

package org.springframework.boot.autoconfigure.web.servlet;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.validation.DefaultMessageCodesResolver;

/**
 * 有关SpringMvc的配置
 * <p>一般情况下是由于 {@link org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.WebMvcAutoConfigurationAdapter} 中使用了 {@link org.springframework.boot.context.properties.EnableConfigurationProperties @EnableConfigurationProperties} 直接注册成Bean 的</p>
 */
@ConfigurationProperties(prefix = "spring.mvc")
public class WebMvcProperties {

	/**
	 * 消息代码的格式化策略
	 */
	private DefaultMessageCodesResolver.Format messageCodesResolverFormat;

	/**
	 * 使用的Locale。默认情况下，该区域设置会被请求的"Accept-Language"请求头覆盖
	 */
	private Locale locale;

	/**
	 * Locale解析策略
	 * <p>虽然和SpringMvc的同名,后面SpringMvc会通过这个策略确定 {@link LocaleResolver}</p>
	 */
	private LocaleResolver localeResolver = LocaleResolver.ACCEPT_HEADER;

	/**
	 * 日期格式
	 * <ul>
	 *     <li>例如：' dd/MM/yyyy '</li>
	 *     <li>后续会被使用到 {@link org.springframework.boot.autoconfigure.web.format.WebConversionService}</li>
	 * </ul>
	 */
	private String dateFormat;

	/**
	 * {@link org.springframework.web.servlet.DispatcherServlet} 是否允许接收 Trace 类型的请求方式
	 */
	private boolean dispatchTraceRequest = false;

	/**
	 * {@link org.springframework.web.servlet.DispatcherServlet} 是否允许接收 Options 类型的请求方式
	 */
	private boolean dispatchOptionsRequest = true;

	/**
	 * 使用默认模型还是重定向模型
	 * <p>如果指定设置为false，那么即使不是重定向视图也会使用重定向视图</p>
	 */
	private boolean ignoreDefaultModelOnRedirect = true;

	/**
	 * 是否在每此请求完毕发布 {@link org.springframework.web.context.support.ServletRequestHandledEvent}
	 */
	private boolean publishRequestHandledEvents = true;

	/**
	 * 如果没有发现Handler来处理请求，是否应该抛出 {@link org.springframework.web.servlet.NoHandlerFoundException}异常
	 */
	private boolean throwExceptionIfNoHandlerFound = false;

	/**
	 * 在Debug的情况下，如果调用了异常解析器，是否应该记录日志
	 */
	private boolean logResolvedException = false;

	/**
	 * 用于静态资源的路径模式
	 */
	private String staticPathPattern = "/**";

	/**
	 * 异步请求超时配置
	 */
	private final Async async = new Async();

	private final Servlet servlet = new Servlet();

	/**
	 * 视图名前缀和后缀
	 */
	private final View view = new View();

	/**
	 * 内容协商相关属性
	 */
	private final Contentnegotiation contentnegotiation = new Contentnegotiation();

	/**
	 * SpringMvc路径的匹配规则
	 */
	private final Pathmatch pathmatch = new Pathmatch();

	public DefaultMessageCodesResolver.Format getMessageCodesResolverFormat() {
		return this.messageCodesResolverFormat;
	}

	public void setMessageCodesResolverFormat(DefaultMessageCodesResolver.Format messageCodesResolverFormat) {
		this.messageCodesResolverFormat = messageCodesResolverFormat;
	}

	public Locale getLocale() {
		return this.locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public LocaleResolver getLocaleResolver() {
		return this.localeResolver;
	}

	public void setLocaleResolver(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}

	public String getDateFormat() {
		return this.dateFormat;
	}

	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	public boolean isIgnoreDefaultModelOnRedirect() {
		return this.ignoreDefaultModelOnRedirect;
	}

	public void setIgnoreDefaultModelOnRedirect(boolean ignoreDefaultModelOnRedirect) {
		this.ignoreDefaultModelOnRedirect = ignoreDefaultModelOnRedirect;
	}

	public boolean isPublishRequestHandledEvents() {
		return this.publishRequestHandledEvents;
	}

	public void setPublishRequestHandledEvents(boolean publishRequestHandledEvents) {
		this.publishRequestHandledEvents = publishRequestHandledEvents;
	}

	public boolean isThrowExceptionIfNoHandlerFound() {
		return this.throwExceptionIfNoHandlerFound;
	}

	public void setThrowExceptionIfNoHandlerFound(boolean throwExceptionIfNoHandlerFound) {
		this.throwExceptionIfNoHandlerFound = throwExceptionIfNoHandlerFound;
	}

	public boolean isLogResolvedException() {
		return this.logResolvedException;
	}

	public void setLogResolvedException(boolean logResolvedException) {
		this.logResolvedException = logResolvedException;
	}

	public boolean isDispatchOptionsRequest() {
		return this.dispatchOptionsRequest;
	}

	public void setDispatchOptionsRequest(boolean dispatchOptionsRequest) {
		this.dispatchOptionsRequest = dispatchOptionsRequest;
	}

	public boolean isDispatchTraceRequest() {
		return this.dispatchTraceRequest;
	}

	public void setDispatchTraceRequest(boolean dispatchTraceRequest) {
		this.dispatchTraceRequest = dispatchTraceRequest;
	}

	public String getStaticPathPattern() {
		return this.staticPathPattern;
	}

	public void setStaticPathPattern(String staticPathPattern) {
		this.staticPathPattern = staticPathPattern;
	}

	public Async getAsync() {
		return this.async;
	}

	public Servlet getServlet() {
		return this.servlet;
	}

	public View getView() {
		return this.view;
	}

	public Contentnegotiation getContentnegotiation() {
		return this.contentnegotiation;
	}

	public Pathmatch getPathmatch() {
		return this.pathmatch;
	}

	/**
	 * 异步请求超时配置
	 */
	public static class Async {

		/**
		 * 异步请求处理超时前的时间。如果未设置此值，则使用底层实现的默认超时
		 */
		private Duration requestTimeout;

		public Duration getRequestTimeout() {
			return this.requestTimeout;
		}

		public void setRequestTimeout(Duration requestTimeout) {
			this.requestTimeout = requestTimeout;
		}

	}

	public static class Servlet {

		/**
		 * dispatcher servlet请求的默认前缀
		 */
		private String path = "/";

		/**
		 * 加载dispatcher servlet的启动优先级
		 */
		private int loadOnStartup = -1;

		public String getPath() {
			return this.path;
		}

		public void setPath(String path) {
			Assert.notNull(path, "Path must not be null");
			Assert.isTrue(!path.contains("*"), "Path must not contain wildcards");
			this.path = path;
		}

		public int getLoadOnStartup() {
			return this.loadOnStartup;
		}

		public void setLoadOnStartup(int loadOnStartup) {
			this.loadOnStartup = loadOnStartup;
		}

		public String getServletMapping() {
			if (this.path.equals("") || this.path.equals("/")) {
				return "/";
			}
			if (this.path.endsWith("/")) {
				return this.path + "*";
			}
			return this.path + "/*";
		}

		public String getPath(String path) {
			String prefix = getServletPrefix();
			if (!path.startsWith("/")) {
				path = "/" + path;
			}
			return prefix + path;
		}

		public String getServletPrefix() {
			String result = this.path;
			int index = result.indexOf('*');
			if (index != -1) {
				result = result.substring(0, index);
			}
			if (result.endsWith("/")) {
				result = result.substring(0, result.length() - 1);
			}
			return result;
		}

	}

	/**
	 * 视图名前缀和后缀
	 */
	public static class View {

		/**
		 * Spring MVC视图前缀
		 * <p>即使Controller返回了Hello，但是最后还是会加上这个前缀</p>
		 */
		private String prefix;

		/**
		 * Spring MVC视图后缀
		 */
		private String suffix;

		public String getPrefix() {
			return this.prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public String getSuffix() {
			return this.suffix;
		}

		public void setSuffix(String suffix) {
			this.suffix = suffix;
		}

	}

	/**
	 * 内容协商相关属性
	 */
	public static class Contentnegotiation {

		/**
		 * 是否应该使用URL路径中的路径扩展名来确定所请求的媒体类型。
		 * <p>如果是 ”/users.pdf“ 那么媒体类型将被解析为 "application/pdf" 而不管'Accept'头。</p>
		 */
		private boolean favorPathExtension = false;

		/**
		 * 是否使用请求参数(默认为“format”)来确定所请求的媒体类型。
		 */
		private boolean favorParameter = false;

		/**
		 * 文件类型和对应媒体类型的映射关系
		 * <p>这样 {@link org.springframework.web.accept.ContentNegotiationManager} 中就可以转换了</p>
		 */
		private Map<String, MediaType> mediaTypes = new LinkedHashMap<>();

		/**
		 * 如果启用了 "favorParameter", 那么就是从queryString获得文件扩展名的参数名称
		 */
		private String parameterName;

		public boolean isFavorPathExtension() {
			return this.favorPathExtension;
		}

		public void setFavorPathExtension(boolean favorPathExtension) {
			this.favorPathExtension = favorPathExtension;
		}

		public boolean isFavorParameter() {
			return this.favorParameter;
		}

		public void setFavorParameter(boolean favorParameter) {
			this.favorParameter = favorParameter;
		}

		public Map<String, MediaType> getMediaTypes() {
			return this.mediaTypes;
		}

		public void setMediaTypes(Map<String, MediaType> mediaTypes) {
			this.mediaTypes = mediaTypes;
		}

		public String getParameterName() {
			return this.parameterName;
		}

		public void setParameterName(String parameterName) {
			this.parameterName = parameterName;
		}

	}

	public static class Pathmatch {

		/**
		 * 是否使用后缀模式匹配(".*")来匹配模式请求
		 * <p>如果启用，映射到“/users”的方法也匹配到“/users.*”</p>
		 */
		private boolean useSuffixPattern = false;

		/**
		 * Whether suffix pattern matching should work only against extensions registered
		 * with "spring.mvc.contentnegotiation.media-types.*". This is generally
		 * recommended to reduce ambiguity and to avoid issues such as when a "." appears
		 * in the path for other reasons.
		 */
		private boolean useRegisteredSuffixPattern = false;

		public boolean isUseSuffixPattern() {
			return this.useSuffixPattern;
		}

		public void setUseSuffixPattern(boolean useSuffixPattern) {
			this.useSuffixPattern = useSuffixPattern;
		}

		public boolean isUseRegisteredSuffixPattern() {
			return this.useRegisteredSuffixPattern;
		}

		public void setUseRegisteredSuffixPattern(boolean useRegisteredSuffixPattern) {
			this.useRegisteredSuffixPattern = useRegisteredSuffixPattern;
		}

	}

	public enum LocaleResolver {

		/**
		 * 始终使用配置的区域
		 */
		FIXED,

		/**
		 * 使用请求的“Accept-Language”请求体，如果请求头中没有，则使用已配置的区域设置
		 */
		ACCEPT_HEADER

	}

}
