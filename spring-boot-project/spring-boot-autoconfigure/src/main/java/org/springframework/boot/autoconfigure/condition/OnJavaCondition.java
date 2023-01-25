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

import org.springframework.boot.autoconfigure.condition.ConditionalOnJava.Range;
import org.springframework.boot.system.JavaVersion;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * 用于检测JVM的版本
 * @see ConditionalOnJava
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
class OnJavaCondition extends SpringBootCondition {

	/**
	 * 当前使用的JVM版本
	 */
	private static final JavaVersion JVM_VERSION = JavaVersion.getJavaVersion();

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		Map<String, Object> attributes = metadata.getAnnotationAttributes(ConditionalOnJava.class.getName());
		Range range = (Range) attributes.get("range");
		JavaVersion version = (JavaVersion) attributes.get("value");
		return getMatchOutcome(range, JVM_VERSION, version);
	}

	protected ConditionOutcome getMatchOutcome(Range range, JavaVersion runningVersion, JavaVersion version) {
		// 确定JVM版本是否在指定的版本范围内。
		boolean match = isWithin(runningVersion, range, version);

		// 封装返回信息
		String expected = String.format((range != Range.EQUAL_OR_NEWER) ? "(older than %s)" : "(%s or newer)", version);
		ConditionMessage message = ConditionMessage.forCondition(ConditionalOnJava.class, expected)
				.foundExactly(runningVersion);
		return new ConditionOutcome(match, message);
	}

	/**
	 * 确定 {@link JavaVersion JavaVersion} 是否在指定的版本范围内。
	 * @param runningVersion the current version.
	 * @param range the range
	 * @param version the bounds of the range
	 * @return if this version is within the specified range
	 */
	private boolean isWithin(JavaVersion runningVersion, Range range, JavaVersion version) {
		// 要求等于或者更高的情况
		if (range == Range.EQUAL_OR_NEWER) {
			return runningVersion.isEqualOrNewerThan(version);
		}
		// 要求更低的情况
		if (range == Range.OLDER_THAN) {
			return runningVersion.isOlderThan(version);
		}
		throw new IllegalStateException("Unknown range " + range);
	}

}
