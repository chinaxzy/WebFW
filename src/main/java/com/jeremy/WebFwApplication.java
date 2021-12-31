package com.jeremy;

import com.jeremy.controller.JwtFilter;
import com.jeremy.filter.CharsetFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableAutoConfiguration
@SpringBootApplication
public class WebFwApplication {



	@Bean
	public FilterRegistrationBean jwtFilter() {
		final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(new JwtFilter());
		registrationBean.addUrlPatterns("/web");
		registrationBean.addUrlPatterns("/table/*");
		return registrationBean;
	}

	public static void main(String[] args) {
		SpringApplication.run(WebFwApplication.class, args);
	}

	@Bean
	public FilterRegistrationBean testFilterRegistration() {

		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(new CharsetFilter());
		registration.addUrlPatterns("/*");
		registration.addInitParameter("paramName", "paramValue");
		registration.setName("testFilter");
		registration.setOrder(1);
		return registration;
	}

}


//@ServletComponentScan
//@SpringBootApplication
//public class WebFwApplication extends SpringBootServletInitializer {
//    public static void main(String[] args) {
//        System.out.println("Hello World!");
//        SpringApplication.run(WebFwApplication.class, args);
//    }
//
//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//        return builder.sources(WebFwApplication.class);
//    }
//}
