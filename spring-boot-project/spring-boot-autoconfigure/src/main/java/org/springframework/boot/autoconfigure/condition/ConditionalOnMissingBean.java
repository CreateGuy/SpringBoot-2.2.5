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

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Conditional;

/**
 * {@link Conditional @Conditional}的衍生注解，检查容器中是否没有某个Bean
 * <p>
 * When placed on a {@code @Bean} method, the bean class defaults to the return type of
 * the factory method:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyAutoConfiguration {
 *
 *     &#064;ConditionalOnMissingBean
 *     &#064;Bean
 *     public MyService myService() {
 *         ...
 *     }
 *
 * }</pre>
 * <p>
 * In the sample above the condition will match if no bean of type {@code MyService} is
 * already contained in the {@link BeanFactory}.
 * <p>
 * The condition can only match the bean definitions that have been processed by the
 * application context so far and, as such, it is strongly recommended to use this
 * condition on auto-configuration classes only. If a candidate bean may be created by
 * another auto-configuration, make sure that the one using this condition runs after.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @since 1.0.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBeanCondition.class)
public @interface ConditionalOnMissingBean {

	/**
	 * 要求容器中不能有此类型的Bean
	 */
	Class<?>[] value() default {};

	/**
	 * 要求容器中不能有此类型的Bean
	 * <p>注意：如果 type 未指定并且这个注解放在了方法上，那么 type 就等于方法返回值</p>
	 */
	String[] type() default {};

	/**
	 * 需要忽略的Bean类型
	 */
	Class<?>[] ignored() default {};

	/**
	 * 需要忽略的Bean类型名称
	 */
	String[] ignoredType() default {};

	/**
	 * 要求容器中不能有此注解的Bean
	 */
	Class<? extends Annotation>[] annotation() default {};

	/**
	 * 要求容器中不能有此名称的Bean
	 */
	String[] name() default {};

	/**
	 * Bean搜索策略
	 */
	SearchStrategy search() default SearchStrategy.ALL;

	/**
	 * 检查在泛型参数中包含指定bean类型的附加类
	 * <p>value=A.class和parameterizedContainer=B.class将同时检测A和A< B ></p>
	 */
	Class<?>[] parameterizedContainer() default {};

}
