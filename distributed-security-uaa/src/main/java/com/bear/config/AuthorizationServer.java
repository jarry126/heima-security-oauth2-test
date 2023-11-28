package com.bear.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Arrays;

/**
 * @author Administrator
 * @version 1.0
 * 授权服务配置
 **/
@Configuration
@EnableAuthorizationServer   // 启用 OAuth2 授权服务器
public class AuthorizationServer extends AuthorizationServerConfigurerAdapter {  // AuthorizationServerConfigurerAdapter针对Oauth2，有4个实现方法，快速搭建一个授权服务器

    @Autowired
    private TokenStore tokenStore;


    @Autowired
    private ClientDetailsService clientDetailsService;

    // todo 授权码模式需要的
    @Autowired
    private AuthorizationCodeServices authorizationCodeServices;

    // todo 密码模式需要的
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtAccessTokenConverter accessTokenConverter;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Resource
    private DataSource dataSource;


//    @Bean
//    public CustomJdbcClientDetailsService customClientDetailsService() {
//        return new CustomJdbcClientDetailsService(dataSource);
//    }


    //将客户端信息存储到数据库
    // todo 教程上使用springsecurity oauth2数据库中默认表
    @Bean
    public ClientDetailsService clientDetailsService(DataSource dataSource) {
        ClientDetailsService clientDetailsService = new JdbcClientDetailsService(dataSource);
        ((JdbcClientDetailsService) clientDetailsService).setPasswordEncoder(passwordEncoder);
        return clientDetailsService;
    }

    // chatgpt:配置客户端信息;你可以配置客户端应用程序的详细信息，包括客户端ID、客户端密码、授权类型等。
    // todo 不是所有人都可以访问我们的授权服务器，必要携带正确的client_id和secret
    // todo 授权服务支持哪些客服端；支持的客户端信息应该放在数据库中，现在是在内存中
    //客户端详情服务
    @Override
    public void configure(ClientDetailsServiceConfigurer clients)
            throws Exception {
//        clients.withClientDetails(customClientDetailsService());
        clients.withClientDetails(clientDetailsService);
//        clients.inMemory()// 使用in-memory存储
//                .withClient("c1")// client_id
//                .secret(new BCryptPasswordEncoder().encode("secret"))//客户端密钥
//                .resourceIds("res1")//资源列表
//                .authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token")// 该client允许的授权类型authorization_code,password,refresh_token,implicit,client_credentials
//                .scopes("all")// 允许的授权范围
//                .autoApprove(false)//false跳转到授权页面
//                //加上验证回调地址
//                .redirectUris("http://www.baidu.com")
//        ;
    }



//    @Bean
//    public JdbcClientDetailsService clientDetailsService() {
//        return new JdbcClientDetailsService(dataSource);
//    }



    //令牌管理服务
    // todo 令牌管理模式是必须要的
    @Bean
    public AuthorizationServerTokenServices tokenService() {
        DefaultTokenServices service = new DefaultTokenServices();
        service.setClientDetailsService(clientDetailsService);//客户端详情服务
        service.setSupportRefreshToken(true);//支持刷新令牌
        service.setTokenStore(tokenStore);//令牌存储策略
        //令牌增强
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(Arrays.asList(accessTokenConverter));
        service.setTokenEnhancer(tokenEnhancerChain);

        service.setAccessTokenValiditySeconds(7200); // 令牌默认有效期2小时
        service.setRefreshTokenValiditySeconds(259200); // 刷新令牌默认有效期3天
        return service;
    }

    //设置授权码模式的授权码如何存取，暂时采用内存方式
//    @Bean
//    public AuthorizationCodeServices authorizationCodeServices() {
//        return new InMemoryAuthorizationCodeServices();
//    }

    @Bean
    public AuthorizationCodeServices authorizationCodeServices(DataSource dataSource) {
        return new JdbcAuthorizationCodeServices(dataSource);//设置授权码模式的授权码如何存取
    }

    // chatgpt:配置令牌端点;你可以配置授权服务器的令牌端点，包括令牌的存储方式、用户信息服务、授权码模式的配置等。
    // todo: 生成令牌的地址的配置；和令牌怎么发放的配置
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
                .authenticationManager(authenticationManager)//认证管理器  // todo 密码模式需要的
                .authorizationCodeServices(authorizationCodeServices)//授权码服务 // todo 授权码模式需要的
                .tokenServices(tokenService())//令牌管理服务  //todo 令牌管理模式是必须要的
                .allowedTokenEndpointRequestMethods(HttpMethod.POST);
    }

    // chatgpt:配置授权服务器的安全性;你可以配置授权服务器的安全性，例如设置令牌端点的访问权限、配置客户端凭据的验证方式等。
    // todo: 不是所有的令牌都可以过来，这里配置令牌的安全约束
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security
                .tokenKeyAccess("permitAll()")                    //oauth/token_key是公开
                .checkTokenAccess("permitAll()")                  //oauth/check_token公开
                .allowFormAuthenticationForClients()                //表单认证（申请令牌）
        ;
    }

}
