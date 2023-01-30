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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.Assert;

/**
 * 排序通过 {@link EnableAutoConfiguration auto-configuration} 导入的自动配置类，规则如下
 * <ul>
 *     <li>{@link AutoConfigureOrder @AutoConfigureOrder}</li>
 *     <li>{@link AutoConfigureBefore @AutoConfigureBefore}</li>
 *     <li>{@link AutoConfigureAfter @AutoConfigureAfter}</li>
 * </ul>
 */
class AutoConfigurationSorter {

	/**
	 * 用于读取类的
	 */
	private final MetadataReaderFactory metadataReaderFactory;

	/**
	 * 从 META-INF/spring-autoconfigure-metadata.properties 中读取的自动配置类规则元数据
	 */
	private final AutoConfigurationMetadata autoConfigurationMetadata;

	AutoConfigurationSorter(MetadataReaderFactory metadataReaderFactory,
			AutoConfigurationMetadata autoConfigurationMetadata) {
		Assert.notNull(metadataReaderFactory, "MetadataReaderFactory must not be null");
		this.metadataReaderFactory = metadataReaderFactory;
		this.autoConfigurationMetadata = autoConfigurationMetadata;
	}

	/**
	 * 获得排序过后的自动配置类集合
	 * @param classNames
	 * @return
	 */
	List<String> getInPriorityOrder(Collection<String> classNames) {
		AutoConfigurationClasses classes = new AutoConfigurationClasses(this.metadataReaderFactory,
				this.autoConfigurationMetadata, classNames);
		List<String> orderedClassNames = new ArrayList<>(classNames);
		// 先按照自动配置类名称排序
		Collections.sort(orderedClassNames);
		// 利用配置文件和自动配置类中的 @AutoConfigureOrder，进行排序
		orderedClassNames.sort((o1, o2) -> {
			//classes是一个class，并不是上面的
			int i1 = classes.get(o1).getOrder();
			int i2 = classes.get(o2).getOrder();
			return Integer.compare(i1, i2);
		});
		// 利用配置文件和自动配置类中的 @AutoConfigureBefore 和 AutoConfigureAfter，进行排序
		orderedClassNames = sortByAnnotation(classes, orderedClassNames);
		return orderedClassNames;
	}

	/**
	 * 利用配置文件和自动配置类中的 {@link AutoConfigureBefore @AutoConfigureBefore} 和 {@link AutoConfigureAfter @AutoConfigureAfter}，对自动配置类进行排序
	 * @param classes
	 * @param classNames
	 * @return
	 */
	private List<String> sortByAnnotation(AutoConfigurationClasses classes, List<String> classNames) {
		List<String> toSort = new ArrayList<>(classNames);
		toSort.addAll(classes.getAllNames());
		Set<String> sorted = new LinkedHashSet<>();
		Set<String> processing = new LinkedHashSet<>();
		while (!toSort.isEmpty()) {
			doSortByAfterAnnotation(classes, toSort, sorted, processing, null);
		}
		sorted.retainAll(classNames);
		return new ArrayList<>(sorted);
	}

	/**
	 * 对所有自动配置类进行排序
	 * <li>根据 {@link AutoConfigureBefore} 和 {@link AutoConfigureAfter} 排序</li>
	 * @param classes 自动配置类规则源
	 * @param toSort 要排序的自动配置类
	 * @param sorted 最终排序好的自动配置类集合
	 * @param processing 处理过排序的自动配置类集合
	 * @param current
	 */
	private void doSortByAfterAnnotation(AutoConfigurationClasses classes, List<String> toSort, Set<String> sorted,
			Set<String> processing, String current) {
		if (current == null) {
			current = toSort.remove(0);
		}
		processing.add(current);
		// 拿到要在此自动配置类之前初始化的自动配置类
		for (String after : classes.getClassesRequestedAfter(current)) {
			Assert.state(!processing.contains(after),
					"AutoConfigure cycle detected between " + current + " and " + after);
			// 不能是已经排了序的(如果已排了序，肯定在前面)，也不能不在要导入的自动配置类中
			if (!sorted.contains(after) && toSort.contains(after)) {
				// 继续递归分析
				doSortByAfterAnnotation(classes, toSort, sorted, processing, after);
			}
		}
		// 移除掉做过排序的
		processing.remove(current);
		// 注册到已经排序的集合中
		sorted.add(current);
	}

	private static class AutoConfigurationClasses {

		/**
		 * 自动配置类全路径和对应的 {@link AutoConfigurationClass} 的映射关系
		 */
		private final Map<String, AutoConfigurationClass> classes = new HashMap<>();

		AutoConfigurationClasses(MetadataReaderFactory metadataReaderFactory,
				AutoConfigurationMetadata autoConfigurationMetadata, Collection<String> classNames) {
			addToClasses(metadataReaderFactory, autoConfigurationMetadata, classNames, true);
		}

		Set<String> getAllNames() {
			return this.classes.keySet();
		}

		/**
		 * 注册自动配置类
		 * @param metadataReaderFactory
		 * @param autoConfigurationMetadata
		 * @param classNames
		 * @param required 是否必须
		 */
		private void addToClasses(MetadataReaderFactory metadataReaderFactory,
				AutoConfigurationMetadata autoConfigurationMetadata, Collection<String> classNames, boolean required) {
			for (String className : classNames) {
				if (!this.classes.containsKey(className)) {
					AutoConfigurationClass autoConfigurationClass = new AutoConfigurationClass(className,
							metadataReaderFactory, autoConfigurationMetadata);
					// 不是可激活的：可能是无法加载此类
					boolean available = autoConfigurationClass.isAvailable();
					// 不可激活，又不是必须的
					if (required || available) {
						this.classes.put(className, autoConfigurationClass);
					}

					// 如果是必须的，递归读取他要求的自动配置类
					if (available) {
						addToClasses(metadataReaderFactory, autoConfigurationMetadata,
								autoConfigurationClass.getBefore(), false);
						addToClasses(metadataReaderFactory, autoConfigurationMetadata,
								autoConfigurationClass.getAfter(), false);
					}
				}
			}
		}

		AutoConfigurationClass get(String className) {
			return this.classes.get(className);
		}

		Set<String> getClassesRequestedAfter(String className) {
			Set<String> classesRequestedAfter = new LinkedHashSet<>(get(className).getAfter());
			this.classes.forEach((name, autoConfigurationClass) -> {
				if (autoConfigurationClass.getBefore().contains(className)) {
					classesRequestedAfter.add(name);
				}
			});
			return classesRequestedAfter;
		}

	}

	/**
	 * 封装了自动配置类的信息
	 */
	private static class AutoConfigurationClass {

		/**
		 * 当前自动配置类类名
		 */
		private final String className;

		/**
		 * 元数据读取工厂
		 * <p>在这里主要是用于读取某个类上的注解信息</p>
		 */
		private final MetadataReaderFactory metadataReaderFactory;

		/**
		 * 从 META-INF/spring-autoconfigure-metadata.properties 中读取的自动配置类规则元数据
		 */
		private final AutoConfigurationMetadata autoConfigurationMetadata;

		/**
		 * 当前自动配置类的注解元数据
		 */
		private volatile AnnotationMetadata annotationMetadata;

		/**
		 * 当前自动配置类的应该在什么自动配置类前初始化
		 * <li>可能是配置文件中的值，也有可能是自动配置类上的 {@link AutoConfigureBefore}的值</li>
		 */
		private volatile Set<String> before;

		/**
		 * 当前自动配置类的应该在什么自动配置类前初始化
		 * <li>可能是配置文件中的值，也有可能是自动配置类上的 {@link AutoConfigureAfter}的值</li>
		 */
		private volatile Set<String> after;

		AutoConfigurationClass(String className, MetadataReaderFactory metadataReaderFactory,
				AutoConfigurationMetadata autoConfigurationMetadata) {
			this.className = className;
			this.metadataReaderFactory = metadataReaderFactory;
			this.autoConfigurationMetadata = autoConfigurationMetadata;
		}

		/**
		 * 是否是可以激活的自动配置类
		 * <p>如果无法加载此类，是不是就会抛出异常，然后返回false了</p>
		 * @return
		 */
		boolean isAvailable() {
			try {
				if (!wasProcessed()) {
					getAnnotationMetadata();
				}
				return true;
			}
			catch (Exception ex) {
				return false;
			}
		}

		/**
		 * 返回Before值
		 * <li>如果当前自动配置类在配置文件中有设置Before值，那就用配置文件中的，否则就用类上的 {@link AutoConfigureBefore} 值</li>
		 * @return
		 */
		Set<String> getBefore() {
			if (this.before == null) {
				this.before = (wasProcessed() ? this.autoConfigurationMetadata.getSet(this.className,
						"AutoConfigureBefore", Collections.emptySet()) : getAnnotationValue(AutoConfigureBefore.class));
			}
			return this.before;
		}

		/**
		 * 返回Before值
		 * <li>如果当前自动配置类在配置文件中有设置Before值，那就用配置文件中的，否则就用类上的 {@link AutoConfigureAfter} 值</li>
		 * @return
		 */
		Set<String> getAfter() {
			if (this.after == null) {
				this.after = (wasProcessed() ? this.autoConfigurationMetadata.getSet(this.className,
						"AutoConfigureAfter", Collections.emptySet()) : getAnnotationValue(AutoConfigureAfter.class));
			}
			return this.after;
		}

		/**
		 * 从配置文件或者自动配置类上的 {@link AutoConfigureOrder} 获得排序值
		 * @return
		 */
		private int getOrder() {
			// 当前自动配置类是否在配置文件中，就从配置文件中读取
			if (wasProcessed()) {
				return this.autoConfigurationMetadata.getInteger(this.className, "AutoConfigureOrder",
						AutoConfigureOrder.DEFAULT_ORDER);
			}

			// 从自动配置类上的AutoConfigureOrder中获取排序值
			Map<String, Object> attributes = getAnnotationMetadata()
					.getAnnotationAttributes(AutoConfigureOrder.class.getName());
			return (attributes != null) ? (Integer) attributes.get("value") : AutoConfigureOrder.DEFAULT_ORDER;
		}

		/**
		 * 当前自动配置类是否在配置文件中
		 * @return
		 */
		private boolean wasProcessed() {
			return (this.autoConfigurationMetadata != null
					&& this.autoConfigurationMetadata.wasProcessed(this.className));
		}

		/**
		 * 获取当前自动配置类上的指定注解的 value 和 name 值
		 * @param annotation
		 * @return
		 */
		private Set<String> getAnnotationValue(Class<?> annotation) {
			Map<String, Object> attributes = getAnnotationMetadata().getAnnotationAttributes(annotation.getName(),
					true);
			if (attributes == null) {
				return Collections.emptySet();
			}
			Set<String> value = new LinkedHashSet<>();
			Collections.addAll(value, (String[]) attributes.get("value"));
			Collections.addAll(value, (String[]) attributes.get("name"));
			return value;
		}

		/**
		 * 返回当前自动配置类上的注解元数据
		 * @return
		 */
		private AnnotationMetadata getAnnotationMetadata() {
			if (this.annotationMetadata == null) {
				try {
					MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(this.className);
					this.annotationMetadata = metadataReader.getAnnotationMetadata();
				}
				catch (IOException ex) {
					throw new IllegalStateException("Unable to read meta-data for class " + this.className, ex);
				}
			}
			return this.annotationMetadata;
		}

	}

}
