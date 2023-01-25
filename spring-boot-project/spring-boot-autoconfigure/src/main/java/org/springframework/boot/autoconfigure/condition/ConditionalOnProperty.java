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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;

/**
 * {@link Conditional @Conditional} 的衍生类，用于匹配配置文件中的某个属性
 * default the properties must be present in the {@link Environment}
 * and <strong>not</strong> equal to {@code false}. The {@link #havingValue()} and
 * {@link #matchIfMissing()} attributes allow further customizations.
 * <p>
 * The {@link #havingValue} attribute can be used to specify the value that the property
 * should have. The table below shows when a condition matches according to the property
 * value and the {@link #havingValue()} attribute:
 *
 * <table border="1">
 * <caption>Having values</caption>
 * <tr>
 * <th>Property Value</th>
 * <th>{@code havingValue=""}</th>
 * <th>{@code havingValue="true"}</th>
 * <th>{@code havingValue="false"}</th>
 * <th>{@code havingValue="foo"}</th>
 * </tr>
 * <tr>
 * <td>{@code "true"}</td>
 * <td>yes</td>
 * <td>yes</td>
 * <td>no</td>
 * <td>no</td>
 * </tr>
 * <tr>
 * <td>{@code "false"}</td>
 * <td>no</td>
 * <td>no</td>
 * <td>yes</td>
 * <td>no</td>
 * </tr>
 * <tr>
 * <td>{@code "foo"}</td>
 * <td>yes</td>
 * <td>no</td>
 * <td>no</td>
 * <td>yes</td>
 * </tr>
 * </table>
 * <p>
 * If the property is not contained in the {@link Environment} at all, the
 * {@link #matchIfMissing()} attribute is consulted. By default missing attributes do not
 * match.
 * <p>
 * This condition cannot be reliably used for matching collection properties. For example,
 * in the following configuration, the condition matches if {@code spring.example.values}
 * is present in the {@link Environment} but does not match if
 * {@code spring.example.values[0]} is present.
 *
 * <pre class="code">
 * &#064;ConditionalOnProperty(prefix = "spring", name = "example.values")
 * class ExampleAutoConfiguration {
 * }
 * </pre>
 *
 * It is better to use a custom condition for such cases.
 *
 * @author Maciej Walkowiak
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 1.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnPropertyCondition.class)
public @interface ConditionalOnProperty {

	/**
	 * Alias for {@link #name()}.
	 * @return the names
	 */
	String[] value() default {};

	/**
	 * 属性的前缀。如果没有指定，前缀自动以点结束，一个有效的前缀由一个或多个单词定义
	 * separated with dots (e.g. {@code "acme.system.feature"}).
	 * @return the prefix
	 */
	String prefix() default "";

	/**
	 * 具体的属性名称. If a prefix has been defined, it is applied to
	 * compute the full key of each property. For instance if the prefix is
	 * {@code app.config} and one value is {@code my-value}, the full key would be
	 * {@code app.config.my-value}
	 * <p>
	 * Use the dashed notation to specify each property, that is all lower case with a "-"
	 * to separate words (e.g. {@code my-long-property}).
	 * @return the names
	 */
	String[] name() default {};

	/**
	 * 属性的期望值。如果未指定，该属性不能等于false
	 */
	String havingValue() default "";

	/**
	 * 当属性找不到的时候，是否忽略匹配，false为不忽略
	 */
	boolean matchIfMissing() default false;

}
