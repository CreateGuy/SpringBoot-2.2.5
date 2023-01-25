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

package org.springframework.boot.autoconfigure.condition;

import java.util.Map;

import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.web.reactive.context.ConfigurableReactiveWebEnvironment;
import org.springframework.boot.web.reactive.context.ReactiveWebApplicationContext;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.WebApplicationContext;

/**
 * 检查当前服务的类型
 * @see ConditionalOnWebApplication
 * @see ConditionalOnNotWebApplication
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
class OnWebApplicationCondition extends FilteringSpringBootCondition {

	/**
	 * 这个类是web环境的ApplicationContext的父类
	 */
	private static final String SERVLET_WEB_APPLICATION_CLASS = "org.springframework.web.context.support.GenericWebApplicationContext";

	private static final String REACTIVE_WEB_APPLICATION_CLASS = "org.springframework.web.reactive.HandlerResult";

	/**
	 * 针对 {@link ConditionalOnWebApplication @ConditionalOnWebApplication} 获得匹配结果, 如若某个元素为空，就代表是匹配成功的
	 * @param autoConfigurationClasses  从META-INF/spring.factories中读取到的有关自动配置类
	 * @param autoConfigurationMetadata 从META-INF/spring-autoconfigure-metadata.properties中读取到的自动配置类规则
	 * @return
	 */
	@Override
	protected ConditionOutcome[] getOutcomes(String[] autoConfigurationClasses,
			AutoConfigurationMetadata autoConfigurationMetadata) {
		ConditionOutcome[] outcomes = new ConditionOutcome[autoConfigurationClasses.length];
		for (int i = 0; i < outcomes.length; i++) {
			String autoConfigurationClass = autoConfigurationClasses[i];
			if (autoConfigurationClass != null) {
				outcomes[i] = getOutcome(
						autoConfigurationMetadata.get(autoConfigurationClass, "ConditionalOnWebApplication"));
			}
		}
		return outcomes;
	}

	/**
	 * 返回匹配结果
	 * @param type
	 * @return
	 */
	private ConditionOutcome getOutcome(String type) {
		if (type == null) {
			return null;
		}
		ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnWebApplication.class);
		// Servlet的情况
		if (ConditionalOnWebApplication.Type.SERVLET.name().equals(type)) {
			if (!ClassNameFilter.isPresent(SERVLET_WEB_APPLICATION_CLASS, getBeanClassLoader())) {
				return ConditionOutcome.noMatch(message.didNotFind("servlet web application classes").atAll());
			}
		}
		// Reactive的情况
		if (ConditionalOnWebApplication.Type.REACTIVE.name().equals(type)) {
			if (!ClassNameFilter.isPresent(REACTIVE_WEB_APPLICATION_CLASS, getBeanClassLoader())) {
				return ConditionOutcome.noMatch(message.didNotFind("reactive web application classes").atAll());
			}
		}

		// 两个都不能加载的情况
		if (!ClassNameFilter.isPresent(SERVLET_WEB_APPLICATION_CLASS, getBeanClassLoader())
				&& !ClassUtils.isPresent(REACTIVE_WEB_APPLICATION_CLASS, getBeanClassLoader())) {
			return ConditionOutcome.noMatch(message.didNotFind("reactive or servlet web application classes").atAll());
		}
		return null;
	}

	/**
	 * 处理 {@link ConditionalOnWebApplication @ConditionalOnWebApplication}，返回匹配结果
	 * @param context the condition context
	 * @param metadata the annotation metadata
	 * @return
	 */
	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		// 是否携带了此注解
		boolean required = metadata.isAnnotated(ConditionalOnWebApplication.class.getName());
		// 是否是要求的应用程序环境
		ConditionOutcome outcome = isWebApplication(context, metadata, required);

		// 是携带了注解的，但是匹配失败了
		if (required && !outcome.isMatch()) {
			return ConditionOutcome.noMatch(outcome.getConditionMessage());
		}

		// 没有携带那个注解，并且匹配成功了
		// 是因为使用的是@ConditionalOnNotWebApplication，这两个注解使用的检查类是都是当前类
		if (!required && outcome.isMatch()) {
			return ConditionOutcome.noMatch(outcome.getConditionMessage());
		}

		// 匹配成功
		return ConditionOutcome.match(outcome.getConditionMessage());
	}

	/**
	 * 是否是要求的应用程序环境
	 * @param context
	 * @param metadata
	 * @param required
	 * @return
	 */
	private ConditionOutcome isWebApplication(ConditionContext context, AnnotatedTypeMetadata metadata,
			boolean required) {
		// 以要求的应用程序类型作为键
		switch (deduceType(metadata)) {
			case SERVLET:
				return isServletWebApplication(context);
			case REACTIVE:
				return isReactiveWebApplication(context);
			default:
				// 未指定应用程序环境
				return isAnyWebApplication(context, required);
		}
	}

	/**
	 * 没有指定应用程序环境的情况
	 * @param context
	 * @param required
	 * @return
	 */
	private ConditionOutcome isAnyWebApplication(ConditionContext context, boolean required) {
		ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnWebApplication.class,
				required ? "(required)" : "");
		// 是否是 ServletWeb 的应用程序环境
		ConditionOutcome servletOutcome = isServletWebApplication(context);
		// 要求是 ServletWeb，并且也是的情况
		if (servletOutcome.isMatch() && required) {
			return new ConditionOutcome(servletOutcome.isMatch(), message.because(servletOutcome.getMessage()));
		}

		// 是否是 ReactiveWeb 的应用程序环境
		ConditionOutcome reactiveOutcome = isReactiveWebApplication(context);
		// 要求是 ReactiveWeb，并且也是的情况
		if (reactiveOutcome.isMatch() && required) {
			return new ConditionOutcome(reactiveOutcome.isMatch(), message.because(reactiveOutcome.getMessage()));
		}
		return new ConditionOutcome(servletOutcome.isMatch() || reactiveOutcome.isMatch(),
				message.because(servletOutcome.getMessage()).append("and").append(reactiveOutcome.getMessage()));
	}

	/**
	 * 是否是 ServletWeb 的应用程序环境
	 * @param context
	 * @return
	 */
	private ConditionOutcome isServletWebApplication(ConditionContext context) {
		ConditionMessage.Builder message = ConditionMessage.forCondition("");
		// 判断是否是ServletWeb环境
		if (!ClassNameFilter.isPresent(SERVLET_WEB_APPLICATION_CLASS, context.getClassLoader())) {
			return ConditionOutcome.noMatch(message.didNotFind("servlet web application classes").atAll());
		}

		// 要求Bean有Session的作用域
		if (context.getBeanFactory() != null) {
			String[] scopes = context.getBeanFactory().getRegisteredScopeNames();
			if (ObjectUtils.containsElement(scopes, "session")) {
				return ConditionOutcome.match(message.foundExactly("'session' scope"));
			}
		}

		// 不懂这两个环境的区别
		if (context.getEnvironment() instanceof ConfigurableWebEnvironment) {
			return ConditionOutcome.match(message.foundExactly("ConfigurableWebEnvironment"));
		}
		if (context.getResourceLoader() instanceof WebApplicationContext) {
			return ConditionOutcome.match(message.foundExactly("WebApplicationContext"));
		}

		// 匹配失败
		return ConditionOutcome.noMatch(message.because("not a servlet web application"));
	}

	/**
	 * 是否是 ReactiveWeb 的应用程序环境
	 * @param context
	 * @return
	 */
	private ConditionOutcome isReactiveWebApplication(ConditionContext context) {
		ConditionMessage.Builder message = ConditionMessage.forCondition("");
		if (!ClassNameFilter.isPresent(REACTIVE_WEB_APPLICATION_CLASS, context.getClassLoader())) {
			return ConditionOutcome.noMatch(message.didNotFind("reactive web application classes").atAll());
		}
		if (context.getEnvironment() instanceof ConfigurableReactiveWebEnvironment) {
			return ConditionOutcome.match(message.foundExactly("ConfigurableReactiveWebEnvironment"));
		}
		if (context.getResourceLoader() instanceof ReactiveWebApplicationContext) {
			return ConditionOutcome.match(message.foundExactly("ReactiveWebApplicationContext"));
		}
		return ConditionOutcome.noMatch(message.because("not a reactive web application"));
	}

	/**
	 * 返回要求的应用程序类型
	 * @param metadata
	 * @return
	 */
	private Type deduceType(AnnotatedTypeMetadata metadata) {
		Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnWebApplication.class.getName());
		if (attributes != null) {
			return (Type) attributes.get("type");
		}
		return Type.ANY;
	}

}
