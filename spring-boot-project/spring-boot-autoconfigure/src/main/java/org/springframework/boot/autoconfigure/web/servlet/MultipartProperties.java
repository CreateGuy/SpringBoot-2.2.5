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

package org.springframework.boot.autoconfigure.web.servlet;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Properties to be used in configuring a {@link MultipartConfigElement}.
 * <ul>
 * <li>{@link #getLocation() location} specifies the directory where uploaded files will
 * be stored. When not specified, a temporary directory will be used.</li>
 * <li>{@link #getMaxFileSize() max-file-size} specifies the maximum size permitted for
 * uploaded files. The default is 1MB</li>
 * <li>{@link #getMaxRequestSize() max-request-size} specifies the maximum size allowed
 * for {@literal multipart/form-data} requests. The default is 10MB.</li>
 * <li>{@link #getFileSizeThreshold() file-size-threshold} specifies the size threshold
 * after which files will be written to disk. The default is 0.</li>
 * </ul>
 * <p>
 * These properties are ultimately passed to {@link MultipartConfigFactory} which means
 * you may specify numeric values using {@literal long} values or using more readable
 * {@link DataSize} variants.
 *
 * @author Josh Long
 * @author Toshiaki Maki
 * @author Stephane Nicoll
 * @since 2.0.0
 */
@ConfigurationProperties(prefix = "spring.servlet.multipart", ignoreUnknownFields = false)
public class MultipartProperties {

	/**
	 * 是否启用支持多部分上传
	 */
	private boolean enabled = true;

	/**
	 * 上传文件的保存位置
	 * <li>但是SpringMvc中使用了 {@link org.springframework.web.multipart.support.StandardServletMultipartResolver#cleanupMultipart(MultipartHttpServletRequest)}</li>
	 */
	private String location;

	/**
	 * 最大文件大小
	 */
	private DataSize maxFileSize = DataSize.ofMegabytes(1);

	/**
	 * 最大请求大小
	 */
	private DataSize maxRequestSize = DataSize.ofMegabytes(10);

	/**
	 * 文件写入磁盘的阈值
	 */
	private DataSize fileSizeThreshold = DataSize.ofBytes(0);

	/**
	 * 是否在文件或参数时惰性地解析多部分请求访问
	 */
	private boolean resolveLazily = false;

	public boolean getEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public DataSize getMaxFileSize() {
		return this.maxFileSize;
	}

	public void setMaxFileSize(DataSize maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public DataSize getMaxRequestSize() {
		return this.maxRequestSize;
	}

	public void setMaxRequestSize(DataSize maxRequestSize) {
		this.maxRequestSize = maxRequestSize;
	}

	public DataSize getFileSizeThreshold() {
		return this.fileSizeThreshold;
	}

	public void setFileSizeThreshold(DataSize fileSizeThreshold) {
		this.fileSizeThreshold = fileSizeThreshold;
	}

	public boolean isResolveLazily() {
		return this.resolveLazily;
	}

	public void setResolveLazily(boolean resolveLazily) {
		this.resolveLazily = resolveLazily;
	}

	/**
	 * Create a new {@link MultipartConfigElement} using the properties.
	 * @return a new {@link MultipartConfigElement} configured using there properties
	 */
	public MultipartConfigElement createMultipartConfig() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
		map.from(this.fileSizeThreshold).to(factory::setFileSizeThreshold);
		map.from(this.location).whenHasText().to(factory::setLocation);
		map.from(this.maxRequestSize).to(factory::setMaxRequestSize);
		map.from(this.maxFileSize).to(factory::setMaxFileSize);
		return factory.createMultipartConfig();
	}

}
