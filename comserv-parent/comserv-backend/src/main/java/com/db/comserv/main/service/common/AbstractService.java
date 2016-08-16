/*
 * This computer program is the confidential information and proprietary trade
 * secret of DB. Possessions and use of this program must
 * conform strictly to the license agreement between the user and DB,
 */
package com.db.comserv.main.service.common;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.springframework.transaction.annotation.Transactional;

import com.db.comserv.main.dao.common.IOperations;
import com.db.comserv.main.model.Response;

@Transactional
public abstract class AbstractService<T extends Serializable> implements IOperations<T> {

    @Override
    public T findOne(final long id) throws HibernateException{
        return getDao().findOne(id);
    }
    
    @Override
    public List<T> findAll(final int offset, final int limit) throws HibernateException{
        return getDao().findAll(offset, limit);
    }
    
    
    
    @Override
    public List<T> findAll() throws HibernateException{
        return getDao().findAll();
    }

    @Override
    public T create(final T entity) throws HibernateException{
        return getDao().create(entity);
    }

    @Override
    public Response<T> update(final T entity) throws HibernateException{
        return getDao().update(entity);
    }

    @Override
    public Response<T> delete(final T entity) throws HibernateException{
       return getDao().delete(entity);
    }

    @Override
    public Response<T> deleteById(int id) throws HibernateException{
    	return getDao().deleteById(id);
    }
    
    protected abstract IOperations<T> getDao();

}
