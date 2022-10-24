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

package org.springframework.boot.autoconfigure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.context.annotation.DeterminableImports;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;


/**
 * 用于存储包路径(默认就是启动类的包路径)，方便比如 jpa entity 的扫描
 * 我理解：是springboot不会扫描@Entity的类当作组件，那么实体和数据表的映射关系就是靠这个包路径扫描所有标注了@Entity的实体表和其中的name(表名)属性进行一个映射关系
 */
public abstract class AutoConfigurationPackages {

	private static final Log logger = LogFactory.getLog(AutoConfigurationPackages.class);

	//当前类名称，用于 作为bean名称存储包路径
	private static final String BEAN = AutoConfigurationPackages.class.getName();

	/**
	 * Determine if the auto-configuration base packages for the given bean factory are
	 * available.
	 * @param beanFactory the source bean factory
	 * @return true if there are auto-config packages available
	 */
	public static boolean has(BeanFactory beanFactory) {
		return beanFactory.containsBean(BEAN) && !get(beanFactory).isEmpty();
	}

	/**
	 * Return the auto-configuration base packages for the given bean factory.
	 * @param beanFactory the source bean factory
	 * @return a list of auto-configuration packages
	 * @throws IllegalStateException if auto-configuration is not enabled
	 */
	public static List<String> get(BeanFactory beanFactory) {
		try {
			return beanFactory.getBean(BEAN, BasePackages.class).get();
		}
		catch (NoSuchBeanDefinitionException ex) {
			throw new IllegalStateException("Unable to retrieve @EnableAutoConfiguration base packages");
		}
	}

	/**
	 * 注册AutoConfigurationPackages类
	 * @param registry
	 * @param packageNames
	 */
	public static void register(BeanDefinitionRegistry registry, String... packageNames) {
		if (registry.containsBeanDefinition(BEAN)) {
			BeanDefinition beanDefinition = registry.getBeanDefinition(BEAN);
			ConstructorArgumentValues constructorArguments = beanDefinition.getConstructorArgumentValues();
			//给这个构造方法的第一个参数设置为包路径
			constructorArguments.addIndexedArgumentValue(0, addBasePackages(constructorArguments, packageNames));
		}
		else {
			GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
			beanDefinition.setBeanClass(BasePackages.class);
			//给这个构造方法的第一个参数设置为包路径
			beanDefinition.getConstructorArgumentValues().addIndexedArgumentValue(0, packageNames);
			//设置角色为 框架
			beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			//执行注册
			registry.registerBeanDefinition(BEAN, beanDefinition);
		}
	}

	/**
	 * 将原来的包路径和后面的包路径进行合并，并返回
	 * @param constructorArguments 构造方法持有者
	 * @param packageNames 新的包路径
	 * @return
	 */
	private static String[] addBasePackages(ConstructorArgumentValues constructorArguments, String[] packageNames) {
		String[] existing = (String[]) constructorArguments.getIndexedArgumentValue(0, String[].class).getValue();
		Set<String> merged = new LinkedHashSet<>();
		merged.addAll(Arrays.asList(existing));
		merged.addAll(Arrays.asList(packageNames));
		return StringUtils.toStringArray(merged);
	}

	/**
	 * {@link ImportBeanDefinitionRegistrar} to store the base package from the importing
	 * configuration.
	 */
	static class Registrar implements ImportBeanDefinitionRegistrar, DeterminableImports {

		@Override
		public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
			register(registry, new PackageImport(metadata).getPackageName());
		}

		@Override
		public Set<Object> determineImports(AnnotationMetadata metadata) {
			return Collections.singleton(new PackageImport(metadata));
		}

	}

	/**
	 * Wrapper for a package import.
	 */
	private static final class PackageImport {

		private final String packageName;

		PackageImport(AnnotationMetadata metadata) {
			this.packageName = ClassUtils.getPackageName(metadata.getClassName());
		}

		String getPackageName() {
			return this.packageName;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return this.packageName.equals(((PackageImport) obj).packageName);
		}

		@Override
		public int hashCode() {
			return this.packageName.hashCode();
		}

		@Override
		public String toString() {
			return "Package Import " + this.packageName;
		}

	}

	/**
	 * Holder for the base package (name may be null to indicate no scanning).
	 */
	static final class BasePackages {

		private final List<String> packages;

		private boolean loggedBasePackageInfo;

		BasePackages(String... names) {
			List<String> packages = new ArrayList<>();
			for (String name : names) {
				if (StringUtils.hasText(name)) {
					packages.add(name);
				}
			}
			this.packages = packages;
		}

		List<String> get() {
			if (!this.loggedBasePackageInfo) {
				if (this.packages.isEmpty()) {
					if (logger.isWarnEnabled()) {
						logger.warn("@EnableAutoConfiguration was declared on a class "
								+ "in the default package. Automatic @Repository and "
								+ "@Entity scanning is not enabled.");
					}
				}
				else {
					if (logger.isDebugEnabled()) {
						String packageNames = StringUtils.collectionToCommaDelimitedString(this.packages);
						logger.debug("@EnableAutoConfiguration was declared on a class in the package '" + packageNames
								+ "'. Automatic @Repository and @Entity scanning is enabled.");
					}
				}
				this.loggedBasePackageInfo = true;
			}
			return this.packages;
		}

	}

}
