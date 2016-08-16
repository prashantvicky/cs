/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.config;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.db.comserv.main.utilities.DataSettingProperties;
import com.google.common.base.Preconditions;

@Configuration
@EnableTransactionManagement
@ComponentScan({ "com.db.comserv.main" })
public class PersistenceConfig {

    @Autowired
    private DataSettingProperties dbProperties;
    
    
    
    public PersistenceConfig() {
        super();
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() throws InvalidKeyException, NoSuchAlgorithmException,
    		NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(restDataSource());
        sessionFactory.setPackagesToScan(new String[] { "com.db.comserv.main.model" });
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
    }

    @Bean
    public DataSource restDataSource() throws InvalidKeyException, NoSuchAlgorithmException,
    NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(Preconditions.checkNotNull(dbProperties.getJSONData("jdbc.driverClassName")));
        dataSource.setUrl(Preconditions.checkNotNull(dbProperties.getJSONData("jdbc.url")));
        dataSource.setUsername(Preconditions.checkNotNull(dbProperties.getJSONData("jdbc.user")));
      //  dataSource.setPassword(Preconditions.checkNotNull(hasher.decrypt("jdbc.pass",dbProperties.getValue("jdbc.pass"))));

        return dataSource;
    }

    @Bean
    @Autowired
    public HibernateTransactionManager transactionManager(final SessionFactory sessionFactory) {
        final HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory);

        return txManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

    final Properties hibernateProperties() {
        final Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", dbProperties.getValue("hibernate.hbm2ddl.auto"));
        hibernateProperties.setProperty("hibernate.dialect", dbProperties.getValue("hibernate.dialect"));
        hibernateProperties.setProperty("hibernate.temp.use_jdbc_metadata_defaults","false");
        hibernateProperties.setProperty("hibernate.show_sql", dbProperties.getValue("hibernate.show_sql"));
        hibernateProperties.setProperty("hibernate.jdbc.batch_size", dbProperties.getValue("hibernate.jdbc.batch_size"));

        return hibernateProperties;
    }
    
   

}