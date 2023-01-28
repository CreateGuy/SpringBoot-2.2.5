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

package org.springframework.boot.context.properties;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * {@link ConfigurationProperties @ConfigurationProperties} 类型的 {@link BeanDefinition}
 * <p>但是我看只有当 {@link  ConfigurationProperties} 是使用构造方法注入属性的时候才会创建此 {@link BeanDefinition}， set方法是直接创建 {@link GenericBeanDefinition}</p>
 */
final class ConfigurationPropertiesValueObjectBeanDefinition extends GenericBeanDefinition {

	private final BeanFactory beanFactory;

	private final String beanName;

	ConfigurationPropertiesValueObjectBeanDefinition(BeanFactory beanFactory, String beanName, Class<?> beanClass) {
		this.beanFactory = beanFactory;
		this.beanName = beanName;
		setBeanClass(beanClass);
		setInstanceSupplier(this::createBean);
	}

	private Object createBean() {
		ConfigurationPropertiesBean bean = ConfigurationPropertiesBean.forValueObject(getBeanClass(), this.beanName);
		ConfigurationPropertiesBinder binder = ConfigurationPropertiesBinder.get(this.beanFactory);
		try {
			return binder.bindOrCreate(bean);
		}
		catch (Exception ex) {
			throw new ConfigurationPropertiesBindException(bean, ex);
		}
	}

}
