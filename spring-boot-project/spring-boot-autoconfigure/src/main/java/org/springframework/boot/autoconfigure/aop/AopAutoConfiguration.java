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

package org.springframework.boot.autoconfigure.aop;

import org.aspectj.weaver.Advice;

import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration
 * Auto-configuration} for Spring's AOP support. Equivalent to enabling
 * {@link EnableAspectJAutoProxy @EnableAspectJAutoProxy} in your configuration.
 * <p>
 * The configuration will not be activated if {@literal spring.aop.auto=false}. The
 * {@literal proxyTargetClass} attribute will be {@literal true}, by default, but can be
 * overridden by specifying {@literal spring.aop.proxy-target-class=false}.
 *
 * @author Dave Syer
 * @author Josh Long
 * @since 1.0.0
 * @see EnableAspectJAutoProxy
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "spring.aop", name = "auto", havingValue = "true", matchIfMissing = true)
public class AopAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(Advice.class)
	static class AspectJAutoProxyingConfiguration {

		/**
		 * JDK, 重点就代理模式是否根据具体的类型判断
		 * <li>根据{@code spring.aop.proxy-target-class}决定</li>
		 */
		@Configuration(proxyBeanMethods = false)
		@EnableAspectJAutoProxy(proxyTargetClass = false)
		@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "false",
				matchIfMissing = false)
		static class JdkDynamicAutoProxyConfiguration {

		}

		/**
		 * Cglib, 重点就代理模式直接设置为Cglib
		 * <li>根据{@code spring.aop.proxy-target-class}决定</li>
		 */
		@Configuration(proxyBeanMethods = false)
		// 开启自动配置
		@EnableAspectJAutoProxy(proxyTargetClass = true)
		@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
				matchIfMissing = true)
		static class CglibAutoProxyConfiguration {

		}

	}

	/**
	 * 注册{@link org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator InfrastructureAdvisorAutoProxyCreator}
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnMissingClass("org.aspectj.weaver.Advice")
	@ConditionalOnProperty(prefix = "spring.aop", name = "proxy-target-class", havingValue = "true",
			matchIfMissing = true)
	static class ClassProxyingConfiguration {

		ClassProxyingConfiguration(BeanFactory beanFactory) {
			if (beanFactory instanceof BeanDefinitionRegistry) {
				BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
				// 重点注册自动代理创建器的BeanDefinition
				AopConfigUtils.registerAutoProxyCreatorIfNecessary(registry);
				// 使用Cglib代理
				AopConfigUtils.forceAutoProxyCreatorToUseClassProxying(registry);
			}
		}

	}

}
