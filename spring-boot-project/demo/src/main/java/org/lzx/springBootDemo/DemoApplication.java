package org.lzx.springBootDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * @author liuzhixuan
 * @date 2022-10-10
 */
@SpringBootApplication
//@EnableAutoConfiguration(exclude = UndertowServletWebServer.class)
@ImportResource(value = "classpath:bean.xml")
//@Import(value = AutoMessage.class)
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true, securedEnabled = true)
@EnableCaching
public class DemoApplication {

	public static void main(String[] args) {
		System.out.println("编译环境的代建：https://baijiahao.baidu.com/s?id=1720638219097606536&wfr=spider&for=pc");
		ConfigurableApplicationContext ac = SpringApplication.run(DemoApplication.class);
		System.out.println(1);
	}

	@Bean
	@ConditionalOnMissingBean
	public InternalResourceViewResolver defaultViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		return resolver;
	}
}

