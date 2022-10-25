package org.lzx.springBootDemo;

import ch.qos.logback.core.pattern.color.BoldWhiteCompositeConverter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.annotation.Inherited;

/**
 * @author liuzhixuan
 * @date 2022-10-10
 */
@SpringBootApplication
//@EnableAutoConfiguration(exclude = UndertowServletWebServer.class)
@ImportResource(value = "classpath:bean.xml")
//@Import(value = AutoMessage.class)
public class DemoApplication {

	public static void main(String[] args) {
		System.out.println("编译环境的代建：https://baijiahao.baidu.com/s?id=1720638219097606536&wfr=spider&for=pc");
		ConfigurableApplicationContext ac = SpringApplication.run(DemoApplication.class);
		System.out.println(1);
	}
	@Value("${user.j}")
	private Integer j;

	private Integer i;

	@Bean
	public AutoMessage autoMessage(AutoClient autoClient) {
		return new AutoMessage();
	}

}

//@Service
//@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
//@A2
//@Configuration
//@Import(value = DemoApplication.class)
//@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class AutoMessage {

	@Value("${user.i}")
	private Integer i;

	public static void main(String[] args) {
		MergedAnnotations mergedAnnotations = MergedAnnotations.from(A1.class, MergedAnnotations.SearchStrategy.SUPERCLASS);
		mergedAnnotations.get(A1.class);
	}

	@Bean(autowireCandidate = false)
	public SecurityProperties.User user() {
		return new SecurityProperties.User();
	}

//	@Autowired
//	@Qualifier(value = "autoClint1")
	private AutoClient autoClient;

	public AutoMessage(Integer i, AutoClient autoClient) {
		this.i = i;
		this.autoClient = autoClient;
	}
	//
	//public AutoMessage(Integer i) {
	//	this.i = i;
	//}

	public AutoMessage() {
	}
}

//@Component
class AutoClient implements ApplicationContextAware, FactoryBean {

	@Bean(autowireCandidate = false, name = {"1", "2", "2"})
	public SecurityProperties.User user() {
		return new SecurityProperties.User();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

	}

	private Long id;
	private String addr;//地址
	private String idCard;//身份证号

	@Override
	public Object getObject() throws Exception {
		return new AutoClient();
	}

	@Override
	public Class<?> getObjectType() {
		return AutoClient.class;
	}
}

@Inherited
@Configuration
@interface A1 {

}

@A1
@interface A2 {

	Component aa();
}
