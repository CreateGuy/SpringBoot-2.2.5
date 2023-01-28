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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 通过 {@link EnableConfigurationProperties @EnableConfigurationProperties} 注册了 {@link ImportBeanDefinitionRegistrar} 然后注册配置类到容器中
 */
class EnableConfigurationPropertiesRegistrar implements ImportBeanDefinitionRegistrar {

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		registerInfrastructureBeans(registry);
		ConfigurationPropertiesBeanRegistrar beanRegistrar = new ConfigurationPropertiesBeanRegistrar(registry);
		// 往容器中注册 @EnableConfigurationProperties 中指定的类
		getTypes(metadata).forEach(beanRegistrar::register);
	}

	/**
	 * 获得需要导入到容器中的配置类
	 * @param metadata
	 * @return
	 */
	private Set<Class<?>> getTypes(AnnotationMetadata metadata) {
		return metadata.getAnnotations().stream(EnableConfigurationProperties.class)
				.flatMap((annotation) -> Arrays.stream(annotation.getClassArray(MergedAnnotation.VALUE)))
				.filter((type) -> void.class != type).collect(Collectors.toSet());
	}

	/**
	 * 注册框架中有关 {@link ConfigurationProperties} 的Bean
	 * @param registry
	 */
	@SuppressWarnings("deprecation")
	static void registerInfrastructureBeans(BeanDefinitionRegistry registry) {
		ConfigurationPropertiesBindingPostProcessor.register(registry);
		ConfigurationPropertiesBeanDefinitionValidator.register(registry);
		ConfigurationBeanFactoryMetadata.register(registry);
	}

}
