package org.lzx.springBootDemo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import org.lzx.springBootDemo.entity.FilterOrder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author liuzhixuan
 * @date 2022-10-10
 */
@SpringBootApplication
//@EnableAutoConfiguration(exclude = UndertowServletWebServer.class)
@ImportResource(value = "classpath:bean.xml")
//@Import(value = AutoMessage.class)
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true, securedEnabled = true)
@EnableAsync
public class DemoApplication {

	public static void main(String[] args) {
		System.out.println("编译环境的代建：https://baijiahao.baidu.com/s?id=1720638219097606536&wfr=spider&for=pc");
		ConfigurableApplicationContext ac = SpringApplication.run(DemoApplication.class);

		String filterComparatorFilterToOrderJson = "{\"org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationRequestFilter\":1000,\"org.springframework.security.openid.OpenIDAuthenticationFilter\":1800,\"org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter\":1400,\"org.springframework.security.web.authentication.ui.DefaultLogoutPageGeneratingFilter\":2000,\"org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter\":900,\"org.springframework.security.web.context.SecurityContextPersistenceFilter\":400,\"org.springframework.security.web.access.intercept.FilterSecurityInterceptor\":3300,\"org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter\":1900,\"org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter\":1100,\"org.springframework.security.web.session.SessionManagementFilter\":3100,\"org.springframework.security.web.authentication.logout.LogoutFilter\":800,\"org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter\":1200,\"org.springframework.security.web.jaasapi.JaasApiIntegrationFilter\":2700,\"org.springframework.security.web.authentication.switchuser.SwitchUserFilter\":3400,\"org.springframework.security.web.access.channel.ChannelProcessingFilter\":100,\"org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter\":2800,\"org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter\":1500,\"org.springframework.security.web.session.ConcurrentSessionFilter\":2100,\"org.springframework.security.web.authentication.www.BasicAuthenticationFilter\":2400,\"org.springframework.security.web.authentication.AnonymousAuthenticationFilter\":2900,\"org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter\":2300,\"org.springframework.security.web.csrf.CsrfFilter\":700,\"org.springframework.security.cas.web.CasAuthenticationFilter\":1300,\"org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter\":2600,\"org.springframework.security.web.access.ExceptionTranslationFilter\":3200,\"org.springframework.security.web.authentication.www.DigestAuthenticationFilter\":2200,\"org.springframework.web.filter.CorsFilter\":600,\"org.springframework.security.web.savedrequest.RequestCacheAwareFilter\":2500,\"org.springframework.security.oauth2.client.web.OAuth2AuthorizationCodeGrantFilter\":3000,\"org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter\":1600,\"org.springframework.security.web.header.HeaderWriterFilter\":500,\"org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter\":300}";
		Map<String, Integer> map = JSONUtil.toBean(filterComparatorFilterToOrderJson, Map.class);
		List<FilterOrder> filterOrderList = map.entrySet().stream().map(x -> new FilterOrder(x.getValue(), x.getKey())).sorted(Comparator.comparing(FilterOrder::getOrder)).map(filter -> {
			List<String> list = StrUtil.split(filter.getFilterName(), ".");
			filter.setFilterName(CollUtil.getLast(list));
			return filter;
		}).collect(Collectors.toList());
		filterOrderList.forEach(x -> System.out.println(StrUtil.format("{} : {}", x.getOrder(), x.getFilterName())));
	}

	@Bean
	@ConditionalOnMissingBean
	public InternalResourceViewResolver defaultViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		return resolver;
	}
}

