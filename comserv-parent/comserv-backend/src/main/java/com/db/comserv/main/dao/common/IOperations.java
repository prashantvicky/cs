/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */


package com.db.comserv.main.dao.common;
import java.util.List;

import org.hibernate.HibernateException;

import com.db.comserv.main.model.Response;


public interface IOperations<T> {

    T findOne(final long id);
       
    
    List<T> findAll(int offset, int limit) throws HibernateException;
      
    List<T> findAll() throws HibernateException;
    
    T create(final T entity) throws HibernateException;

    Response<T> update(final T entity) throws HibernateException;

    Response<T> delete(final T entity) throws HibernateException;
    
    Response<T> deleteById(int id) throws HibernateException;
   
    

}
