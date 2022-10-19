package org.lzx.springBootDemo;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;

import java.lang.annotation.Inherited;

/**
 * @author liuzhixuan
 * @date 2022-10-10
 */
@SpringBootApplication
//@ImportResource(value = "classpath:bean.xml")
public class DemoApplication {

	public static void main(String[] args) {
		System.out.println("编译环境的代建：https://baijiahao.baidu.com/s?id=1720638219097606536&wfr=spider&for=pc");
		SpringApplication.run(DemoApplication.class);
	}
	@Value("${user.j}")
	private Integer j;

	private Integer i;

	private AutoMessage autoMessage;
}

//@Service
//@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
//@A2
//@Configuration
//@Import(value = DemoApplication.class)
class AutoMessage implements ImportBeanDefinitionRegistrar {

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

	@Autowired
	@Qualifier(value = "autoClint1")
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

@Component(value = "autoClint1")
class AutoClient implements ApplicationContextAware {

	@Bean(autowireCandidate = false)
	public SecurityProperties.User user() {
		return new SecurityProperties.User();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

	}
}

@Inherited
@Configuration
@interface A1 {

}

@A1
@interface A2 {

}
