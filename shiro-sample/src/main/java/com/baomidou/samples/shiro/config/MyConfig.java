package com.baomidou.samples.shiro.config;

import com.baomidou.dynamic.datasource.aop.DynamicDatasourceNamedInterceptor;
import com.baomidou.dynamic.datasource.processor.DsProcessor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class MyConfig {

    @Bean
    public Advisor defaultPointcutAdvisor(DsProcessor dsProcessor) {
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor();

        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution (* com.baomidou.samples.shiro.service.*.*(..))");

        Properties attributes = new Properties();
        //适合多租户
        attributes.setProperty("select*", "#session.tenant");
        //适合普通
//        attributes.setProperty("select*", "slave");
//        attributes.setProperty("add*", "master");
//        attributes.setProperty("update*", "master");
//        attributes.setProperty("delete*", "master");
        DynamicDatasourceNamedInterceptor advice = new DynamicDatasourceNamedInterceptor(dsProcessor);
        advice.fromProperties(attributes);

        advisor.setPointcut(pointcut);
        advisor.setAdvice(advice);
        return advisor;
    }

    @Bean
    public DsProcessor dsProcessor() {
        return new DsProcessor() {
            @Override
            public boolean matches(String s) {
                return true;
            }

            @Override
            public String doDetermineDatasource(MethodInvocation methodInvocation, String s) {
                Session session = SecurityUtils.getSubject().getSession();
                return (String) session.getAttribute("tenant");
            }
        };
    }
}
