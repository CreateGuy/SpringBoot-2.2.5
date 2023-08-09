/*
 * Copyright 2012-2020 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.boot.autoconfigure.condition.ConditionMessage.Style;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotation.Adapt;
import org.springframework.core.annotation.MergedAnnotationCollectors;
import org.springframework.core.annotation.MergedAnnotationPredicates;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 检查是否存在特定的bean
 * @see ConditionalOnBean
 * @see ConditionalOnMissingBean
 * @see ConditionalOnSingleCandidate
 */
@Order(Ordered.LOWEST_PRECEDENCE)
class OnBeanCondition extends FilteringSpringBootCondition implements ConfigurationCondition {

	@Override
	public ConfigurationPhase getConfigurationPhase() {
		return ConfigurationPhase.REGISTER_BEAN;
	}

	/**
	 * 针对 {@link ConditionalOnBean @ConditionalOnClass} 和 {@link ConditionalOnSingleCandidate @ConditionalOnSingleCandidate} 获得匹配结果, 如若某个元素为空，就代表是匹配成功的
	 * @param autoConfigurationClasses  从META-INF/spring.factories中读取到的有关自动配置类
	 * @param autoConfigurationMetadata 从META-INF/spring-autoconfigure-metadata.properties中读取到的自动配置类规则
	 * @return
	 */
	@Override
	protected final ConditionOutcome[] getOutcomes(String[] autoConfigurationClasses,
			AutoConfigurationMetadata autoConfigurationMetadata) {
		ConditionOutcome[] outcomes = new ConditionOutcome[autoConfigurationClasses.length];
		for (int i = 0; i < outcomes.length; i++) {
			String autoConfigurationClass = autoConfigurationClasses[i];
			if (autoConfigurationClass != null) {
				// 可以看出此条件匹配类只支持 ConditionalOnBean
				// Set是因为内部用逗号切分成Set的
				Set<String> onBeanTypes = autoConfigurationMetadata.getSet(autoConfigurationClass, "ConditionalOnBean");
				// 根据是否能加载指定Class，然后返回匹配结果
				// 貌似这个时候，只当成ConditionalOnClass来处理，但是后面也会调用条件评估器来计算Bean是否应该跳过
				outcomes[i] = getOutcome(onBeanTypes, ConditionalOnBean.class);
				// 为空表示onBeanTypes中的Class都能加载
				if (outcomes[i] == null) {
					Set<String> onSingleCandidateTypes = autoConfigurationMetadata.getSet(autoConfigurationClass,
							"ConditionalOnSingleCandidate");
					// 和上面一样当成ConditionalOnClass来处理
					outcomes[i] = getOutcome(onSingleCandidateTypes, ConditionalOnSingleCandidate.class);
				}
			}
		}
		return outcomes;
	}

	/**
	 * 根据是否能加载指定Class，然后返回匹配结果
	 * @param requiredBeanTypes
	 * @param annotation
	 * @return
	 */
	private ConditionOutcome getOutcome(Set<String> requiredBeanTypes, Class<? extends Annotation> annotation) {
		List<String> missing = filter(requiredBeanTypes, ClassNameFilter.MISSING, getBeanClassLoader());
		if (!missing.isEmpty()) {
			ConditionMessage message = ConditionMessage.forCondition(annotation)
					.didNotFind("required type", "required types").items(Style.QUOTE, missing);
			return ConditionOutcome.noMatch(message);
		}
		// 返回空代表都能加载
		return null;
	}

	/**
	 * 处理 {@link ConditionalOnBean @ConditionalOnBean}，{@link ConditionalOnSingleCandidate @ConditionalOnSingleCandidate}，
	 * {@link ConditionalOnMissingBean @ConditionalOnMissingBean}，返回匹配结果
	 * @param context the condition context
	 * @param metadata the annotation metadata
	 * @return
	 */
	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		ConditionMessage matchMessage = ConditionMessage.empty();
		MergedAnnotations annotations = metadata.getAnnotations();
		//  检查 ConditionalOnBean 情况
		if (annotations.isPresent(ConditionalOnBean.class)) {
			Spec<ConditionalOnBean> spec = new Spec<>(context, metadata, annotations, ConditionalOnBean.class);
			// 返回匹配结果
			MatchResult matchResult = getMatchingBeans(context, spec);
			// 无法匹配成功
			if (!matchResult.isAllMatched()) {
				// 创建无法匹配成功的原因
				String reason = createOnBeanNoMatchReason(matchResult);
				return ConditionOutcome.noMatch(spec.message().because(reason));
			}
			// 到这就说明条件成立，封装返回消息
			matchMessage = spec.message(matchMessage).found("bean", "beans").items(Style.QUOTE,
					matchResult.getNamesOfAllMatches());
		}

		//  检查 ConditionalOnSingleCandidate 情况
		if (metadata.isAnnotated(ConditionalOnSingleCandidate.class.getName())) {
			Spec<ConditionalOnSingleCandidate> spec = new SingleCandidateSpec(context, metadata, annotations);
			// 返回匹配结果
			MatchResult matchResult = getMatchingBeans(context, spec);
			// 先判断是否有此Bean
			if (!matchResult.isAllMatched()) {
				return ConditionOutcome.noMatch(spec.message().didNotFind("any beans").atAll());
			}
			// 再看是否只存在一个候选Bean，或者只有一个主要候选Bean
			else if (!hasSingleAutowireCandidate(context.getBeanFactory(), matchResult.getNamesOfAllMatches(),
					spec.getStrategy() == SearchStrategy.ALL)) {
				return ConditionOutcome.noMatch(spec.message().didNotFind("a primary bean from beans")
						.items(Style.QUOTE, matchResult.getNamesOfAllMatches()));
			}
			// 到这就说明条件成立，封装返回消息
			matchMessage = spec.message(matchMessage).found("a primary bean from beans").items(Style.QUOTE,
					matchResult.getNamesOfAllMatches());
		}

		// 检查 ConditionalOnMissingBean 情况
		if (metadata.isAnnotated(ConditionalOnMissingBean.class.getName())) {
			Spec<ConditionalOnMissingBean> spec = new Spec<>(context, metadata, annotations,
					ConditionalOnMissingBean.class);
			// 返回匹配结果
			MatchResult matchResult = getMatchingBeans(context, spec);
			// 如果匹配成功，在这个注解的情况下，表示不能加入容器中
			if (matchResult.isAnyMatched()) {
				// 创建的 @OnMissingBean 的无法匹配结果
				String reason = createOnMissingBeanNoMatchReason(matchResult);
				// 返回不匹配的信息
				return ConditionOutcome.noMatch(spec.message().because(reason));
			}
			matchMessage = spec.message(matchMessage).didNotFind("any beans").atAll();
		}
		// 返回匹配成功的信息
		return ConditionOutcome.match(matchMessage);
	}

	/**
	 * 返回匹配结果
	 * @param context
	 * @param spec
	 * @return
	 */
	protected final MatchResult getMatchingBeans(ConditionContext context, Spec<?> spec) {
		ClassLoader classLoader = context.getClassLoader();
		ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
		boolean considerHierarchy = spec.getStrategy() != SearchStrategy.CURRENT;
		Set<Class<?>> parameterizedContainers = spec.getParameterizedContainers();
		// 是否在所有父容器中查找
		if (spec.getStrategy() == SearchStrategy.ANCESTORS) {
			BeanFactory parent = beanFactory.getParentBeanFactory();
			Assert.isInstanceOf(ConfigurableListableBeanFactory.class, parent,
					"Unable to use SearchStrategy.ANCESTORS");
			beanFactory = (ConfigurableListableBeanFactory) parent;
		}
		MatchResult result = new MatchResult();
		// 获得所有可以忽略的Bean名称
		Set<String> beansIgnoredByType = getNamesOfBeansIgnoredByType(classLoader, beanFactory, considerHierarchy,
				spec.getIgnoredTypes(), parameterizedContainers);

		// 类型的匹配
		for (String type : spec.getTypes()) {
			// 获得指定类型Bean的名称
			Collection<String> typeMatches = getBeanNamesForType(classLoader, considerHierarchy, beanFactory, type,
					parameterizedContainers);
			// 移除需要忽略的
			typeMatches.removeAll(beansIgnoredByType);

			// 记录匹配不成功或者成功
			if (typeMatches.isEmpty()) {
				result.recordUnmatchedType(type);
			}
			else {
				result.recordMatchedType(type, typeMatches);
			}
		}

		// 注解的匹配，和类型的匹配差不多
		for (String annotation : spec.getAnnotations()) {
			Set<String> annotationMatches = getBeanNamesForAnnotation(classLoader, beanFactory, annotation,
					considerHierarchy);
			annotationMatches.removeAll(beansIgnoredByType);
			if (annotationMatches.isEmpty()) {
				result.recordUnmatchedAnnotation(annotation);
			}
			else {
				result.recordMatchedAnnotation(annotation, annotationMatches);
			}
		}

		// 名称的匹配，和类型的匹配差不多
		for (String beanName : spec.getNames()) {
			if (!beansIgnoredByType.contains(beanName) && containsBean(beanFactory, beanName, considerHierarchy)) {
				result.recordMatchedName(beanName);
			}
			else {
				result.recordUnmatchedName(beanName);
			}
		}
		return result;
	}

	/**
	 * 返回可以忽略的Bean名称
	 * @param classLoader
	 * @param beanFactory
	 * @param considerHierarchy
	 * @param ignoredTypes
	 * @param parameterizedContainers
	 * @return
	 */
	private Set<String> getNamesOfBeansIgnoredByType(ClassLoader classLoader, ListableBeanFactory beanFactory,
			boolean considerHierarchy, Set<String> ignoredTypes, Set<Class<?>> parameterizedContainers) {
		Set<String> result = null;
		for (String ignoredType : ignoredTypes) {
			Collection<String> ignoredNames = getBeanNamesForType(classLoader, considerHierarchy, beanFactory,
					ignoredType, parameterizedContainers);
			result = addAll(result, ignoredNames);
		}
		return (result != null) ? result : Collections.emptySet();
	}

	/**
	 * 获得指定类型Bean的名称
	 * @param classLoader
	 * @param considerHierarchy
	 * @param beanFactory
	 * @param type
	 * @param parameterizedContainers
	 * @return
	 * @throws LinkageError
	 */
	private Set<String> getBeanNamesForType(ClassLoader classLoader, boolean considerHierarchy,
			ListableBeanFactory beanFactory, String type, Set<Class<?>> parameterizedContainers) throws LinkageError {
		try {
			return getBeanNamesForType(beanFactory, considerHierarchy, resolve(type, classLoader),
					parameterizedContainers);
		}
		catch (ClassNotFoundException | NoClassDefFoundError ex) {
			return Collections.emptySet();
		}
	}

	private Set<String> getBeanNamesForType(ListableBeanFactory beanFactory, boolean considerHierarchy, Class<?> type,
			Set<Class<?>> parameterizedContainers) {
		Set<String> result = collectBeanNamesForType(beanFactory, considerHierarchy, type, parameterizedContainers,
				null);
		return (result != null) ? result : Collections.emptySet();
	}

	private Set<String> collectBeanNamesForType(ListableBeanFactory beanFactory, boolean considerHierarchy,
			Class<?> type, Set<Class<?>> parameterizedContainers, Set<String> result) {
		result = addAll(result, beanFactory.getBeanNamesForType(type, true, false));
		for (Class<?> container : parameterizedContainers) {
			ResolvableType generic = ResolvableType.forClassWithGenerics(container, type);
			result = addAll(result, beanFactory.getBeanNamesForType(generic, true, false));
		}

		// 递归去找父容器
		if (considerHierarchy && beanFactory instanceof HierarchicalBeanFactory) {
			BeanFactory parent = ((HierarchicalBeanFactory) beanFactory).getParentBeanFactory();
			if (parent instanceof ListableBeanFactory) {
				result = collectBeanNamesForType((ListableBeanFactory) parent, considerHierarchy, type,
						parameterizedContainers, result);
			}
		}
		return result;
	}

	/**
	 * 从容器中获得标注了指定注解的Bean的名称
	 * @param classLoader
	 * @param beanFactory
	 * @param type
	 * @param considerHierarchy
	 * @return
	 * @throws LinkageError
	 */
	private Set<String> getBeanNamesForAnnotation(ClassLoader classLoader, ConfigurableListableBeanFactory beanFactory,
			String type, boolean considerHierarchy) throws LinkageError {
		Set<String> result = null;
		try {
			result = collectBeanNamesForAnnotation(beanFactory, resolveAnnotationType(classLoader, type),
					considerHierarchy, result);
		}
		catch (ClassNotFoundException ex) {
			// Continue
		}
		return (result != null) ? result : Collections.emptySet();
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Annotation> resolveAnnotationType(ClassLoader classLoader, String type)
			throws ClassNotFoundException {
		return (Class<? extends Annotation>) resolve(type, classLoader);
	}

	private Set<String> collectBeanNamesForAnnotation(ListableBeanFactory beanFactory,
			Class<? extends Annotation> annotationType, boolean considerHierarchy, Set<String> result) {
		result = addAll(result, beanFactory.getBeanNamesForAnnotation(annotationType));
		if (considerHierarchy) {
			BeanFactory parent = ((HierarchicalBeanFactory) beanFactory).getParentBeanFactory();
			if (parent instanceof ListableBeanFactory) {
				result = collectBeanNamesForAnnotation((ListableBeanFactory) parent, annotationType, considerHierarchy,
						result);
			}
		}
		return result;
	}

	/**
	 * 容器中是否包含指定bean
	 * @param beanFactory
	 * @param beanName
	 * @param considerHierarchy
	 * @return
	 */
	private boolean containsBean(ConfigurableListableBeanFactory beanFactory, String beanName,
			boolean considerHierarchy) {
		if (considerHierarchy) {
			return beanFactory.containsBean(beanName);
		}
		return beanFactory.containsLocalBean(beanName);
	}

	/**
	 * 创建无法匹配成功的原因
	 * @param matchResult
	 * @return
	 */
	private String createOnBeanNoMatchReason(MatchResult matchResult) {
		StringBuilder reason = new StringBuilder();
		appendMessageForNoMatches(reason, matchResult.getUnmatchedAnnotations(), "annotated with");
		appendMessageForNoMatches(reason, matchResult.getUnmatchedTypes(), "of type");
		appendMessageForNoMatches(reason, matchResult.getUnmatchedNames(), "named");
		return reason.toString();
	}

	private void appendMessageForNoMatches(StringBuilder reason, Collection<String> unmatched, String description) {
		if (!unmatched.isEmpty()) {
			if (reason.length() > 0) {
				reason.append(" and ");
			}
			reason.append("did not find any beans ");
			reason.append(description);
			reason.append(" ");
			reason.append(StringUtils.collectionToDelimitedString(unmatched, ", "));
		}
	}

	/**
	 * 创建 {@code @OnMissingBean} 无法匹配结果
	 * @param matchResult
	 * @return
	 */
	private String createOnMissingBeanNoMatchReason(MatchResult matchResult) {
		StringBuilder reason = new StringBuilder();
		appendMessageForMatches(reason, matchResult.getMatchedAnnotations(), "annotated with");
		appendMessageForMatches(reason, matchResult.getMatchedTypes(), "of type");
		if (!matchResult.getMatchedNames().isEmpty()) {
			if (reason.length() > 0) {
				reason.append(" and ");
			}
			reason.append("found beans named ");
			reason.append(StringUtils.collectionToDelimitedString(matchResult.getMatchedNames(), ", "));
		}
		return reason.toString();
	}

	private void appendMessageForMatches(StringBuilder reason, Map<String, Collection<String>> matches,
			String description) {
		if (!matches.isEmpty()) {
			matches.forEach((key, value) -> {
				if (reason.length() > 0) {
					reason.append(" and ");
				}
				reason.append("found beans ");
				reason.append(description);
				reason.append(" '");
				reason.append(key);
				reason.append("' ");
				reason.append(StringUtils.collectionToDelimitedString(value, ", "));
			});
		}
	}

	/**
	 * 是否只存在一个候选Bean
	 * @param beanFactory
	 * @param beanNames
	 * @param considerHierarchy
	 * @return
	 */
	private boolean hasSingleAutowireCandidate(ConfigurableListableBeanFactory beanFactory, Set<String> beanNames,
			boolean considerHierarchy) {
		return (beanNames.size() == 1 || getPrimaryBeans(beanFactory, beanNames, considerHierarchy).size() == 1);
	}

	/**
	 * 返回主要候选Bean的数量
	 * @param beanFactory
	 * @param beanNames
	 * @param considerHierarchy
	 * @return
	 */
	private List<String> getPrimaryBeans(ConfigurableListableBeanFactory beanFactory, Set<String> beanNames,
			boolean considerHierarchy) {
		List<String> primaryBeans = new ArrayList<>();
		for (String beanName : beanNames) {
			BeanDefinition beanDefinition = findBeanDefinition(beanFactory, beanName, considerHierarchy);
			// 只有是主要候选Bean才算
			if (beanDefinition != null && beanDefinition.isPrimary()) {
				primaryBeans.add(beanName);
			}
		}
		return primaryBeans;
	}

	/**
	 * 找到指定Bean的BeanDefinition
	 * @param beanFactory
	 * @param beanName
	 * @param considerHierarchy
	 * @return
	 */
	private BeanDefinition findBeanDefinition(ConfigurableListableBeanFactory beanFactory, String beanName,
			boolean considerHierarchy) {
		if (beanFactory.containsBeanDefinition(beanName)) {
			return beanFactory.getBeanDefinition(beanName);
		}
		if (considerHierarchy && beanFactory.getParentBeanFactory() instanceof ConfigurableListableBeanFactory) {
			return findBeanDefinition(((ConfigurableListableBeanFactory) beanFactory.getParentBeanFactory()), beanName,
					considerHierarchy);
		}
		return null;
	}

	private static Set<String> addAll(Set<String> result, Collection<String> additional) {
		if (CollectionUtils.isEmpty(additional)) {
			return result;
		}
		result = (result != null) ? result : new LinkedHashSet<>();
		result.addAll(additional);
		return result;
	}

	private static Set<String> addAll(Set<String> result, String[] additional) {
		if (ObjectUtils.isEmpty(additional)) {
			return result;
		}
		result = (result != null) ? result : new LinkedHashSet<>();
		Collections.addAll(result, additional);
		return result;
	}

	/**
	 * 从底层注解提取的搜索规范，貌似只为了 {@link Conditional @Conditional} 服务
	 */
	private static class Spec<A extends Annotation> {

		private final ClassLoader classLoader;

		/**
		 * 原注解的类型
		 */
		private final Class<? extends Annotation> annotationType;

		/**
		 * 要求容器中不能有此名称的Bean
		 */
		private final Set<String> names;

		/**
		 * 要求容器中不能有此类型的Bean
		 */
		private final Set<String> types;

		/**
		 * 要求容器中不能有此注解的Bean
		 */
		private final Set<String> annotations;

		/**
		 * 需要忽略的Bean类型名称
		 */
		private final Set<String> ignoredTypes;

		/**
		 * 检查在泛型参数中包含指定bean类型的附加类
		 * <p>value=A.class和parameterizedContainer=B.class将同时检测A和A< B ></p>
		 */
		private final Set<Class<?>> parameterizedContainers;

		/**
		 * Bean搜索策略
		 */
		private final SearchStrategy strategy;

		Spec(ConditionContext context, AnnotatedTypeMetadata metadata, MergedAnnotations annotations,
				Class<A> annotationType) {
			MultiValueMap<String, Object> attributes = annotations.stream(annotationType)
					.filter(MergedAnnotationPredicates.unique(MergedAnnotation::getMetaTypes))
					.collect(MergedAnnotationCollectors.toMultiValueMap(Adapt.CLASS_TO_STRING));
			MergedAnnotation<A> annotation = annotations.get(annotationType);
			this.classLoader = context.getClassLoader();
			this.annotationType = annotationType;
			this.names = extract(attributes, "name");
			this.annotations = extract(attributes, "annotation");
			this.ignoredTypes = extract(attributes, "ignored", "ignoredType");
			this.parameterizedContainers = resolveWhenPossible(extract(attributes, "parameterizedContainer"));
			this.strategy = annotation.getValue("search", SearchStrategy.class).orElse(null);
			Set<String> types = extractTypes(attributes);
			BeanTypeDeductionException deductionException = null;
			// 如果没有指定匹配名称和类型
			// 比如说@ConditionalOnMissingBean
			if (types.isEmpty() && this.names.isEmpty()) {
				try {
					// 如果是放在方法上的注解，返回这个方法的返回值类型，作为要求的类型
					types = deducedBeanType(context, metadata);
				}
				catch (BeanTypeDeductionException ex) {
					deductionException = ex;
				}
			}
			this.types = types;
			// 校验匹配规则
			validate(deductionException);
		}

		/**
		 * 将value和type都解析出来, 这两个是一样的
		 * @param attributes
		 * @return
		 */
		protected Set<String> extractTypes(MultiValueMap<String, Object> attributes) {
			return extract(attributes, "value", "type");
		}

		private Set<String> extract(MultiValueMap<String, Object> attributes, String... attributeNames) {
			if (attributes.isEmpty()) {
				return Collections.emptySet();
			}
			Set<String> result = new LinkedHashSet<>();
			for (String attributeName : attributeNames) {
				List<Object> values = attributes.getOrDefault(attributeName, Collections.emptyList());
				for (Object value : values) {
					if (value instanceof String[]) {
						merge(result, (String[]) value);
					}
					else if (value instanceof String) {
						merge(result, (String) value);
					}
				}
			}
			return result.isEmpty() ? Collections.emptySet() : result;
		}

		private void merge(Set<String> result, String... additional) {
			Collections.addAll(result, additional);
		}

		private Set<Class<?>> resolveWhenPossible(Set<String> classNames) {
			if (classNames.isEmpty()) {
				return Collections.emptySet();
			}
			Set<Class<?>> resolved = new LinkedHashSet<>(classNames.size());
			for (String className : classNames) {
				try {
					resolved.add(resolve(className, this.classLoader));
				}
				catch (ClassNotFoundException | NoClassDefFoundError ex) {
				}
			}
			return resolved;
		}

		/**
		 * 校验匹配规则
		 * @param ex
		 */
		protected void validate(BeanTypeDeductionException ex) {
			// 如果要求的类型，名称，注解都为空，那还匹配什么，直接抛出异常
			if (!hasAtLeastOneElement(this.types, this.names, this.annotations)) {
				String message = getAnnotationName() + " did not specify a bean using type, name or annotation";
				if (ex == null) {
					throw new IllegalStateException(message);
				}
				throw new IllegalStateException(message + " and the attempt to deduce the bean's type failed", ex);
			}
		}

		/**
		 * 集合数组不都为空，就返回True
		 * @param sets
		 * @return
		 */
		private boolean hasAtLeastOneElement(Set<?>... sets) {
			for (Set<?> set : sets) {
				if (!set.isEmpty()) {
					return true;
				}
			}
			return false;
		}

		protected final String getAnnotationName() {
			return "@" + ClassUtils.getShortName(this.annotationType);
		}

		/**
		 * 如果是放在方法上的注解，返回这个方法的返回值类型
		 * @param context
		 * @param metadata
		 * @return
		 */
		private Set<String> deducedBeanType(ConditionContext context, AnnotatedTypeMetadata metadata) {
			// 如果是放在方法上的注解，返回这个方法的返回值类型
			if (metadata instanceof MethodMetadata && metadata.isAnnotated(Bean.class.getName())) {
				return deducedBeanTypeForBeanMethod(context, (MethodMetadata) metadata);
			}
			return Collections.emptySet();
		}

		/**
		 * 注解在方法上，然后返回这个方法的返回值类型
		 * @param context
		 * @param metadata
		 * @return
		 */
		private Set<String> deducedBeanTypeForBeanMethod(ConditionContext context, MethodMetadata metadata) {
			try {
				Class<?> returnType = getReturnType(context, metadata);
				return Collections.singleton(returnType.getName());
			}
			catch (Throwable ex) {
				throw new BeanTypeDeductionException(metadata.getDeclaringClassName(), metadata.getMethodName(), ex);
			}
		}

		private Class<?> getReturnType(ConditionContext context, MethodMetadata metadata)
				throws ClassNotFoundException, LinkageError {
			// Safe to load at this point since we are in the REGISTER_BEAN phase
			ClassLoader classLoader = context.getClassLoader();
			Class<?> returnType = resolve(metadata.getReturnTypeName(), classLoader);
			if (isParameterizedContainer(returnType)) {
				returnType = getReturnTypeGeneric(metadata, classLoader);
			}
			return returnType;
		}

		private boolean isParameterizedContainer(Class<?> type) {
			for (Class<?> parameterizedContainer : this.parameterizedContainers) {
				if (parameterizedContainer.isAssignableFrom(type)) {
					return true;
				}
			}
			return false;
		}

		private Class<?> getReturnTypeGeneric(MethodMetadata metadata, ClassLoader classLoader)
				throws ClassNotFoundException, LinkageError {
			Class<?> declaringClass = resolve(metadata.getDeclaringClassName(), classLoader);
			Method beanMethod = findBeanMethod(declaringClass, metadata.getMethodName());
			return ResolvableType.forMethodReturnType(beanMethod).resolveGeneric();
		}

		private Method findBeanMethod(Class<?> declaringClass, String methodName) {
			Method method = ReflectionUtils.findMethod(declaringClass, methodName);
			if (isBeanMethod(method)) {
				return method;
			}
			Method[] candidates = ReflectionUtils.getAllDeclaredMethods(declaringClass);
			for (Method candidate : candidates) {
				if (candidate.getName().equals(methodName) && isBeanMethod(candidate)) {
					return candidate;
				}
			}
			throw new IllegalStateException("Unable to find bean method " + methodName);
		}

		private boolean isBeanMethod(Method method) {
			return method != null && MergedAnnotations.from(method, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY)
					.isPresent(Bean.class);
		}

		private SearchStrategy getStrategy() {
			return (this.strategy != null) ? this.strategy : SearchStrategy.ALL;
		}

		Set<String> getNames() {
			return this.names;
		}

		Set<String> getTypes() {
			return this.types;
		}

		Set<String> getAnnotations() {
			return this.annotations;
		}

		Set<String> getIgnoredTypes() {
			return this.ignoredTypes;
		}

		Set<Class<?>> getParameterizedContainers() {
			return this.parameterizedContainers;
		}

		ConditionMessage.Builder message() {
			return ConditionMessage.forCondition(this.annotationType, this);
		}

		ConditionMessage.Builder message(ConditionMessage message) {
			return message.andCondition(this.annotationType, this);
		}

		@Override
		public String toString() {
			boolean hasNames = !this.names.isEmpty();
			boolean hasTypes = !this.types.isEmpty();
			boolean hasIgnoredTypes = !this.ignoredTypes.isEmpty();
			StringBuilder string = new StringBuilder();
			string.append("(");
			if (hasNames) {
				string.append("names: ");
				string.append(StringUtils.collectionToCommaDelimitedString(this.names));
				string.append(hasTypes ? " " : "; ");
			}
			if (hasTypes) {
				string.append("types: ");
				string.append(StringUtils.collectionToCommaDelimitedString(this.types));
				string.append(hasIgnoredTypes ? " " : "; ");
			}
			if (hasIgnoredTypes) {
				string.append("ignored: ");
				string.append(StringUtils.collectionToCommaDelimitedString(this.ignoredTypes));
				string.append("; ");
			}
			string.append("SearchStrategy: ");
			string.append(this.strategy.toString().toLowerCase(Locale.ENGLISH));
			string.append(")");
			return string.toString();
		}

	}

	/**
	 * 特殊的 {@link Spec}，为了
	 * {@link ConditionalOnSingleCandidate @ConditionalOnSingleCandidate} 服务
	 */
	private static class SingleCandidateSpec extends Spec<ConditionalOnSingleCandidate> {

		private static final Collection<String> FILTERED_TYPES = Arrays.asList("", Object.class.getName());

		SingleCandidateSpec(ConditionContext context, AnnotatedTypeMetadata metadata, MergedAnnotations annotations) {
			super(context, metadata, annotations, ConditionalOnSingleCandidate.class);
		}

		@Override
		protected Set<String> extractTypes(MultiValueMap<String, Object> attributes) {
			Set<String> types = super.extractTypes(attributes);
			types.removeAll(FILTERED_TYPES);
			return types;
		}

		@Override
		protected void validate(BeanTypeDeductionException ex) {
			Assert.isTrue(getTypes().size() == 1,
					() -> getAnnotationName() + " annotations must specify only one type (got "
							+ StringUtils.collectionToCommaDelimitedString(getTypes()) + ")");
		}

	}

	/**
	 * 匹配结果
	 */
	private static final class MatchResult {

		/**
		 * 注解匹配成功的Bean，键是注解名称，值是Bean名称
		 */
		private final Map<String, Collection<String>> matchedAnnotations = new HashMap<>();

		/**
		 * 注解匹配成功的Bean的名称
		 */
		private final List<String> matchedNames = new ArrayList<>();

		/**
		 * 类型匹配成功的Bean，键是类型类型，值是Bean名称
		 */
		private final Map<String, Collection<String>> matchedTypes = new HashMap<>();

		/**
		 * 注解没有匹配成功的Bean的名称，有可能是因为设置了忽略
		 */
		private final List<String> unmatchedAnnotations = new ArrayList<>();

		/**
		 * 名称匹配成功的Bean名称
		 */
		private final List<String> unmatchedNames = new ArrayList<>();

		/**
		 * 类型没有匹配成功的Bean的名称，有可能是因为设置了忽略
		 */
		private final List<String> unmatchedTypes = new ArrayList<>();

		/**
		 * 名称匹配成功的Bean的名称
		 */
		private final Set<String> namesOfAllMatches = new HashSet<>();

		private void recordMatchedName(String name) {
			this.matchedNames.add(name);
			this.namesOfAllMatches.add(name);
		}

		private void recordUnmatchedName(String name) {
			this.unmatchedNames.add(name);
		}

		private void recordMatchedAnnotation(String annotation, Collection<String> matchingNames) {
			this.matchedAnnotations.put(annotation, matchingNames);
			this.namesOfAllMatches.addAll(matchingNames);
		}

		/**
		 * 记录匹配成功的类型
		 * @param annotation
		 */
		private void recordUnmatchedAnnotation(String annotation) {
			this.unmatchedAnnotations.add(annotation);
		}

		/**
		 * 记录类型匹配成功
		 * @param type
		 * @param matchingNames
		 */
		private void recordMatchedType(String type, Collection<String> matchingNames) {
			this.matchedTypes.put(type, matchingNames);
			this.namesOfAllMatches.addAll(matchingNames);
		}

		/**
		 * 记录类型匹配失败
		 * @param type
		 */
		private void recordUnmatchedType(String type) {
			this.unmatchedTypes.add(type);
		}

		/**
		 * 返回匹配结果
		 * <p>此方法是为了 {@code @ConditionalOnBean} 和 {@code @ConditionalOnSingleCandidate}服务，所以只要无法匹配的集合为空就好了</p>
		 * @return
		 */
		boolean isAllMatched() {
			return this.unmatchedAnnotations.isEmpty() && this.unmatchedNames.isEmpty()
					&& this.unmatchedTypes.isEmpty();
		}

		/**
		 * 返回匹配结果
		 * <p>此方法是为了 {@code @ConditionalOnMissingBean} 服务，所以只要能够匹配的集合为空就好了</p>
		 * @return
		 */
		boolean isAnyMatched() {
			return (!this.matchedAnnotations.isEmpty()) || (!this.matchedNames.isEmpty())
					|| (!this.matchedTypes.isEmpty());
		}

		Map<String, Collection<String>> getMatchedAnnotations() {
			return this.matchedAnnotations;
		}

		List<String> getMatchedNames() {
			return this.matchedNames;
		}

		Map<String, Collection<String>> getMatchedTypes() {
			return this.matchedTypes;
		}

		List<String> getUnmatchedAnnotations() {
			return this.unmatchedAnnotations;
		}

		List<String> getUnmatchedNames() {
			return this.unmatchedNames;
		}

		List<String> getUnmatchedTypes() {
			return this.unmatchedTypes;
		}

		Set<String> getNamesOfAllMatches() {
			return this.namesOfAllMatches;
		}

	}

	/**
	 * Exteption thrown when the bean type cannot be deduced.
	 */
	static final class BeanTypeDeductionException extends RuntimeException {

		private BeanTypeDeductionException(String className, String beanMethodName, Throwable cause) {
			super("Failed to deduce bean type for " + className + "." + beanMethodName, cause);
		}

	}

}
