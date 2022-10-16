package org.lzx.springBootDemo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.annotation.Inherited;

/**
 * @author liuzhixuan
 * @date 2022-10-10
 */
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		System.out.println("编译环境的代建：https://baijiahao.baidu.com/s?id=1720638219097606536&wfr=spider&for=pc");
		SpringApplication.run(DemoApplication.class);
	}
	@Value("${user.j}")
	private Integer j;

	private Integer i;
}

@Service
@Scope(value = "prototype")
@A2
class AutoMessage{

	@Component
	class C {

	}

	@Value("${user.i}")
	private Integer i;

	public static void main(String[] args) {
		MergedAnnotations mergedAnnotations = MergedAnnotations.from(A1.class, MergedAnnotations.SearchStrategy.SUPERCLASS);
		mergedAnnotations.get(A1.class);
	}
}

@Inherited
@Configuration
@interface A1 {

}

@A1
@interface A2 {

}
