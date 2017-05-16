package com.bonc.dw3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.bonc.dw3.common.datasource.DynamicDataSourceRegister;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@Import(DynamicDataSourceRegister.class)
@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@CrossOrigin(origins ="*")
@EnableAspectJAutoProxy
//@EnableCircuitBreaker
//@EnableScheduling
//ysl 修改，很多注解重复，且使用ribbon或feign才能使用断路由功能。
//extends SpringBootServletInitializer
public class HomepageApplication {
    @Bean
    @LoadBalanced
    RestTemplate restTemplate(){
        return new RestTemplate();
    }
	public static void main(String[] args) {
		SpringApplication.run(HomepageApplication.class, args);
	}
}

