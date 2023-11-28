package com.bear.config;

import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;

import javax.sql.DataSource;

public class CustomJdbcClientDetailsService extends JdbcClientDetailsService {


    public CustomJdbcClientDetailsService(DataSource dataSource) {
        super(dataSource);
        setSelectClientDetailsSql("SELECT * FROM oauth_client_details WHERE client_id = ?");
    }

}
