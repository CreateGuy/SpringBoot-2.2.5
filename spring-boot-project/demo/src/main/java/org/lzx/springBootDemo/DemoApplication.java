package org.lzx.springBootDemo;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author liuzhixuan
 * @date 2022-10-10
 */
@SpringBootApplication
@PropertySource(value = {"application.yml"})
@ConfigurationProperties(prefix = "user")
public class DemoApplication {

	public static void main(String[] args) {
		System.out.println("编译环境的代建：https://baijiahao.baidu.com/s?id=1720638219097606536&wfr=spider&for=pc");
		SpringApplication.run(DemoApplication.class);
	}

	private Integer i;
}

@Component
@ConditionalOnBean(value = {ConfigurationClassPostProcessor.class, ApplicationArguments.class})
@ConditionalOnClass(value = {ConfigurationClassPostProcessor.class, ApplicationArguments.class})
class B {

	@Component
	class C {

	}
}
