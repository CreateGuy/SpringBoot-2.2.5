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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * {@link DeferredImportSelector} to handle {@link EnableAutoConfiguration
 * auto-configuration}. This class can also be subclassed if a custom variant of
 * {@link EnableAutoConfiguration @EnableAutoConfiguration} is needed.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Stephane Nicoll
 * @author Madhura Bhave
 * @since 1.3.0
 * @see EnableAutoConfiguration
 */
public class AutoConfigurationImportSelector implements DeferredImportSelector, BeanClassLoaderAware,
		ResourceLoaderAware, BeanFactoryAware, EnvironmentAware, Ordered {

	private static final AutoConfigurationEntry EMPTY_ENTRY = new AutoConfigurationEntry();

	private static final String[] NO_IMPORTS = {};

	private static final Log logger = LogFactory.getLog(AutoConfigurationImportSelector.class);

	private static final String PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE = "spring.autoconfigure.exclude";

	private ConfigurableListableBeanFactory beanFactory;

	private Environment environment;

	private ClassLoader beanClassLoader;

	private ResourceLoader resourceLoader;

	/**
	 * 返回过滤过的自动配置类
	 * @param annotationMetadata
	 * @return
	 */
	@Override
	public String[] selectImports(AnnotationMetadata annotationMetadata) {
		if (!isEnabled(annotationMetadata)) {
			return NO_IMPORTS;
		}
		AutoConfigurationMetadata autoConfigurationMetadata = AutoConfigurationMetadataLoader
				.loadMetadata(this.beanClassLoader);
		// 获得所有自动配置类
		AutoConfigurationEntry autoConfigurationEntry = getAutoConfigurationEntry(autoConfigurationMetadata,
				annotationMetadata);
		return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
	}

	/**
	 * 返回所有自动配置类
	 * @param autoConfigurationMetadata 导入类(默认是启动类)的注解元数据
	 * @param annotationMetadata 所有的自动配置类
	 * @return 符合条件的自动配置类
	 */
	protected AutoConfigurationEntry getAutoConfigurationEntry(AutoConfigurationMetadata autoConfigurationMetadata,
			AnnotationMetadata annotationMetadata) {
		//判断是否开启了自动配置选项
		if (!isEnabled(annotationMetadata)) {
			return EMPTY_ENTRY;
		}
		//获得有关@EnableAutoConfiguration的属性
		AnnotationAttributes attributes = getAttributes(annotationMetadata);
		// 从指定路径下获得自动配置类
		List<String> configurations = getCandidateConfigurations(annotationMetadata, attributes);
		//移除重复的
		configurations = removeDuplicates(configurations);
		//获得需要排除的自动配置类
		Set<String> exclusions = getExclusions(annotationMetadata, attributes);
		//检查需要排除的类是否在自动配置类中
		checkExcludedClasses(configurations, exclusions);
		//移除需要排除的类
		configurations.removeAll(exclusions);
		//进行过滤
		configurations = filter(configurations, autoConfigurationMetadata);
		//推送自动配置类导入事件
		fireAutoConfigurationImportEvents(configurations, exclusions);
		return new AutoConfigurationEntry(configurations, exclusions);
	}

	@Override
	public Class<? extends Group> getImportGroup() {
		return AutoConfigurationGroup.class;
	}

	/**
	 * 判断是否开起了自动配置选项
	 * @param metadata 导入类的注解元数据 这个只能判断导入类上是否有@EnableAutoConfiguration注解
	 * @return
	 */
	protected boolean isEnabled(AnnotationMetadata metadata) {
		if (getClass() == AutoConfigurationImportSelector.class) {
			//从上下文看是否设置了开启自动配置选择，默认为true
			return getEnvironment().getProperty(EnableAutoConfiguration.ENABLED_OVERRIDE_PROPERTY, Boolean.class, true);
		}
		return true;
	}

	/**
	 * 获得导入类上有关 {@link EnableAutoConfiguration @EnableAutoConfiguration} 的属性，不能获取注解内部的注解
	 * @param metadata 导入类
	 * @return
	 */
	protected AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
		//就是EnableAutoConfiguration
		String name = getAnnotationClass().getName();
		AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(name, true));
		Assert.notNull(attributes, () -> "No auto-configuration attributes found. Is " + metadata.getClassName()
				+ " annotated with " + ClassUtils.getShortName(name) + "?");
		return attributes;
	}

	/**
	 * Return the source annotation class used by the selector.
	 * @return the annotation class
	 */
	protected Class<?> getAnnotationClass() {
		return EnableAutoConfiguration.class;
	}

	/**
	 * 从指定路径下获得自动配置类
	 * @param metadata
	 * @param attributes
	 * @return
	 */
	protected List<String> getCandidateConfigurations(AnnotationMetadata metadata, AnnotationAttributes attributes) {
		List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),
				getBeanClassLoader());
		Assert.notEmpty(configurations, "No auto configuration classes found in META-INF/spring.factories. If you "
				+ "are using a custom packaging, make sure that file is correct.");
		return configurations;
	}

	/**
	 * Return the class used by {@link SpringFactoriesLoader} to load configuration
	 * candidates.
	 * @return the factory class
	 */
	protected Class<?> getSpringFactoriesLoaderFactoryClass() {
		return EnableAutoConfiguration.class;
	}

	/**
	 * 检查需要排除的类是否在自动配置类中，如果有不在的将会抛出异常
	 * @param configurations 从META-INF/spring.factories中读取到的有关EnableAutoConfiguration类
	 * @param exclusions 需要排除的自动配置类
	 */
	private void checkExcludedClasses(List<String> configurations, Set<String> exclusions) {
		List<String> invalidExcludes = new ArrayList<>(exclusions.size());
		for (String exclusion : exclusions) {
			if (ClassUtils.isPresent(exclusion, getClass().getClassLoader()) && !configurations.contains(exclusion)) {
				invalidExcludes.add(exclusion);
			}
		}
		//无效的排除类
		if (!invalidExcludes.isEmpty()) {
			handleInvalidExcludes(invalidExcludes);
		}
	}

	/**
	 * 处理无效的排除类：需要排除的类并不在META-INF/spring.factories中的EnableAutoConfiguration类型中，并不需要排除，就抛出异常
	 */
	protected void handleInvalidExcludes(List<String> invalidExcludes) {
		StringBuilder message = new StringBuilder();
		for (String exclude : invalidExcludes) {
			message.append("\t- ").append(exclude).append(String.format("%n"));
		}
		throw new IllegalStateException(String.format(
				"The following classes could not be excluded because they are not auto-configuration classes:%n%s",
				message));
	}

	/**
	 * 返回排除过滤器
	 * @param metadata 导入类的注解源数据
	 * @param attributes 从导入类的注解源数据中获得有关于@EnableAutoConfiguration的属性
	 * @return
	 */
	protected Set<String> getExclusions(AnnotationMetadata metadata, AnnotationAttributes attributes) {
		Set<String> excluded = new LinkedHashSet<>();
		//从导入类上获取
		excluded.addAll(asList(attributes, "exclude"));
		//从导入类上获取
		excluded.addAll(Arrays.asList(attributes.getStringArray("excludeName")));
		//从当前上下文环境中获取
		excluded.addAll(getExcludeAutoConfigurationsProperty());
		return excluded;
	}

	/**
	 * 从当前上下文环境中获取有关spring.autoconfigure.exclude的配置
	 * @return
	 */
	private List<String> getExcludeAutoConfigurationsProperty() {
		if (getEnvironment() instanceof ConfigurableEnvironment) {
			Binder binder = Binder.get(getEnvironment());
			return binder.bind(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, String[].class).map(Arrays::asList)
					.orElse(Collections.emptyList());
		}
		String[] excludes = getEnvironment().getProperty(PROPERTY_NAME_AUTOCONFIGURE_EXCLUDE, String[].class);
		return (excludes != null) ? Arrays.asList(excludes) : Collections.emptyList();
	}

	/**
	 * 获取bean工厂的AutoConfigurationImportFilter，进行过滤
	 * spring.factories表示要导入的自动配置类，而spring-autoconfigure-metadata.properties中是自动配置类的规则
	 * @param configurations 从META-INF/spring.factories中读取到的有关EnableAutoConfiguration的类
	 * @param autoConfigurationMetadata 从META-INF/spring-autoconfigure-metadata.properties中读取到的类
	 * @return
	 */
	private List<String> filter(List<String> configurations, AutoConfigurationMetadata autoConfigurationMetadata) {
		long startTime = System.nanoTime();
		//候选自动配置类名称
		String[] candidates = StringUtils.toStringArray(configurations);
		//不需要自动配置名称的集合，顺序和configurations是对应起来的
		boolean[] skip = new boolean[candidates.length];
		//是否需要跳过某些自动配置类的标志位，默认不跳过
		boolean skipped = false;

		//执行所有的过滤器
		for (AutoConfigurationImportFilter filter : getAutoConfigurationImportFilters()) {
			//执行有关Aware的方法
			invokeAwareMethods(filter);
			//重点：进行匹配
			boolean[] match = filter.match(candidates, autoConfigurationMetadata);
			for (int i = 0; i < match.length; i++) {
				//如果无法匹配
				if (!match[i]) {
					skip[i] = true;
					candidates[i] = null;
					skipped = true;
				}
			}
		}
		//有需要跳过的自动配置类
		if (!skipped) {
			return configurations;
		}
		//将不需要跳过的自动配置类加入到result中
		//最终需要返回的也是result
		List<String> result = new ArrayList<>(candidates.length);
		for (int i = 0; i < candidates.length; i++) {
			if (!skip[i]) {
				result.add(candidates[i]);
			}
		}
		if (logger.isTraceEnabled()) {
			int numberFiltered = configurations.size() - result.size();
			logger.trace("Filtered " + numberFiltered + " auto configuration class in "
					+ TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) + " ms");
		}
		return new ArrayList<>(result);
	}

	/**
	 * 获得bean工厂中是AutoConfigurationImportFilter的bean，默认只有三个类
	 * <ul>
	 *    <li>{@link org.springframework.boot.autoconfigure.condition.OnBeanCondition OnBeanCondition}：检查bean工厂中是否有某些bean等等</li>
	 *    <li>{@link org.springframework.boot.autoconfigure.condition.OnClassCondition OnClassCondition}：检查是否有某些类等等</li>
	 *    <li>{@link org.springframework.boot.autoconfigure.condition.OnWebApplicationCondition OnWebApplicationCondition}：检查当前是否是web环境等等</li>
	 * </ul>
	 * @return
	 */
	protected List<AutoConfigurationImportFilter> getAutoConfigurationImportFilters() {
		return SpringFactoriesLoader.loadFactories(AutoConfigurationImportFilter.class, this.beanClassLoader);
	}

	protected final <T> List<T> removeDuplicates(List<T> list) {
		return new ArrayList<>(new LinkedHashSet<>(list));
	}

	protected final List<String> asList(AnnotationAttributes attributes, String name) {
		String[] value = attributes.getStringArray(name);
		return Arrays.asList(value);
	}

	/**
	 * 推送自动配置类导入事件
	 * @param configurations 最后符合条件的自动配置类
	 * @param exclusions 排除的类
	 */
	private void fireAutoConfigurationImportEvents(List<String> configurations, Set<String> exclusions) {
		//获得监听器
		List<AutoConfigurationImportListener> listeners = getAutoConfigurationImportListeners();
		if (!listeners.isEmpty()) {
			//创建自动配置类导入事件
			AutoConfigurationImportEvent event = new AutoConfigurationImportEvent(this, configurations, exclusions);
			for (AutoConfigurationImportListener listener : listeners) {
				invokeAwareMethods(listener);
				//推送事件
				listener.onAutoConfigurationImportEvent(event);
			}
		}
	}

	protected List<AutoConfigurationImportListener> getAutoConfigurationImportListeners() {
		return SpringFactoriesLoader.loadFactories(AutoConfigurationImportListener.class, this.beanClassLoader);
	}

	private void invokeAwareMethods(Object instance) {
		if (instance instanceof Aware) {
			if (instance instanceof BeanClassLoaderAware) {
				((BeanClassLoaderAware) instance).setBeanClassLoader(this.beanClassLoader);
			}
			if (instance instanceof BeanFactoryAware) {
				((BeanFactoryAware) instance).setBeanFactory(this.beanFactory);
			}
			if (instance instanceof EnvironmentAware) {
				((EnvironmentAware) instance).setEnvironment(this.environment);
			}
			if (instance instanceof ResourceLoaderAware) {
				((ResourceLoaderAware) instance).setResourceLoader(this.resourceLoader);
			}
		}
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory);
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	protected final ConfigurableListableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	@Override
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	protected ClassLoader getBeanClassLoader() {
		return this.beanClassLoader;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	protected final Environment getEnvironment() {
		return this.environment;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	protected final ResourceLoader getResourceLoader() {
		return this.resourceLoader;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1;
	}

	private static class AutoConfigurationGroup
			implements DeferredImportSelector.Group, BeanClassLoaderAware, BeanFactoryAware, ResourceLoaderAware {

		//保存了符合条件的自动配置类，是去了重的
		//key是自动配置类名称，value是导入类的注解元数据
		private final Map<String, AnnotationMetadata> entries = new LinkedHashMap<>();

		//保存了符合条件的自动配置类
		//一个AutoConfigurationEntry表示一次导入，也就是可能出现重复的导入
		private final List<AutoConfigurationEntry> autoConfigurationEntries = new ArrayList<>();

		private ClassLoader beanClassLoader;

		private BeanFactory beanFactory;

		private ResourceLoader resourceLoader;

		//自动配置类规则元数据：从META-INF/spring-autoconfigure-metadata.properties中获取的
		private AutoConfigurationMetadata autoConfigurationMetadata;

		@Override
		public void setBeanClassLoader(ClassLoader classLoader) {
			this.beanClassLoader = classLoader;
		}

		@Override
		public void setBeanFactory(BeanFactory beanFactory) {
			this.beanFactory = beanFactory;
		}

		@Override
		public void setResourceLoader(ResourceLoader resourceLoader) {
			this.resourceLoader = resourceLoader;
		}

		/**
		 * 使用指定的延迟ImportSelector导入自动配置类
		 * @param annotationMetadata 导入类的注解元数据
		 * @param deferredImportSelector 导入类上@Import中的类
		 */
		@Override
		public void process(AnnotationMetadata annotationMetadata, DeferredImportSelector deferredImportSelector) {
			Assert.state(deferredImportSelector instanceof AutoConfigurationImportSelector,
					() -> String.format("Only %s implementations are supported, got %s",
							AutoConfigurationImportSelector.class.getSimpleName(),
							deferredImportSelector.getClass().getName()));
			//获得符合条件的自动配置类
			AutoConfigurationEntry autoConfigurationEntry = ((AutoConfigurationImportSelector) deferredImportSelector)
					.getAutoConfigurationEntry(getAutoConfigurationMetadata(), annotationMetadata);
			//将符合条件的自动配置类添加到对应的集合中
			this.autoConfigurationEntries.add(autoConfigurationEntry);
			for (String importClassName : autoConfigurationEntry.getConfigurations()) {
				this.entries.putIfAbsent(importClassName, annotationMetadata);
			}
		}

		/**
		 * 获得符合条件的自动配置类的迭代器
		 * @return
		 */
		@Override
		public Iterable<Entry> selectImports() {
			//如果没有需要自动导入的类
			if (this.autoConfigurationEntries.isEmpty()) {
				return Collections.emptyList();
			}
			//获得所有的排除过滤器
			Set<String> allExclusions = this.autoConfigurationEntries.stream()
					.map(AutoConfigurationEntry::getExclusions).flatMap(Collection::stream).collect(Collectors.toSet());
			//获得所有符合条件的自动配置类
			Set<String> processedConfigurations = this.autoConfigurationEntries.stream()
					.map(AutoConfigurationEntry::getConfigurations).flatMap(Collection::stream)
					.collect(Collectors.toCollection(LinkedHashSet::new));
			//移除用户设定的不要的
			processedConfigurations.removeAll(allExclusions);

			//先对符合条件的自动配置类进行排序，然后转为迭代器
			return sortAutoConfigurations(processedConfigurations, getAutoConfigurationMetadata()).stream()
					.map((importClassName) ->
							new Entry(this.entries.get(importClassName), importClassName))
					.collect(Collectors.toList());
		}

		/**
		 * 获取spring-autoconfigure-metadata.properties中所有自动配置类的规则元数据
		 * @return
		 */
		private AutoConfigurationMetadata getAutoConfigurationMetadata() {
			if (this.autoConfigurationMetadata == null) {
				this.autoConfigurationMetadata = AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
			}
			return this.autoConfigurationMetadata;
		}

		/**
		 * 对自动配置类进行排序
		 * @param configurations 符合条件的自动配置类
		 * @param autoConfigurationMetadata 自动配置类规则元数据
		 * @return
		 */
		private List<String> sortAutoConfigurations(Set<String> configurations,
				AutoConfigurationMetadata autoConfigurationMetadata) {
			return new AutoConfigurationSorter(getMetadataReaderFactory(), autoConfigurationMetadata)
					.getInPriorityOrder(configurations);
		}

		private MetadataReaderFactory getMetadataReaderFactory() {
			try {
				return this.beanFactory.getBean(SharedMetadataReaderFactoryContextInitializer.BEAN_NAME,
						MetadataReaderFactory.class);
			}
			catch (NoSuchBeanDefinitionException ex) {
				return new CachingMetadataReaderFactory(this.resourceLoader);
			}
		}

	}

	/**
	 * 保存了所有符合条件的自动配置类
	 */
	protected static class AutoConfigurationEntry {

		//符合条件的自动配置类
		private final List<String> configurations;

		//排除的类
		private final Set<String> exclusions;

		private AutoConfigurationEntry() {
			this.configurations = Collections.emptyList();
			this.exclusions = Collections.emptySet();
		}

		AutoConfigurationEntry(Collection<String> configurations, Collection<String> exclusions) {
			this.configurations = new ArrayList<>(configurations);
			this.exclusions = new HashSet<>(exclusions);
		}

		public List<String> getConfigurations() {
			return this.configurations;
		}

		public Set<String> getExclusions() {
			return this.exclusions;
		}

	}

}
