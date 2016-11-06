/**
 * Copyright (c) 2011-2016, hubin (jobob@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.baomidou.framework.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.framework.service.IService;
import com.baomidou.mybatisplus.annotations.IdType;
import com.baomidou.mybatisplus.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.toolkit.CollectionUtil;
import com.baomidou.mybatisplus.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.toolkit.TableInfo;
import com.baomidou.mybatisplus.toolkit.TableInfoHelper;

/**
 * <p>
 * IService 实现类（ 泛型：M 是 mapper 对象，T 是实体 ， PK 是主键泛型 ）
 * </p>
 *
 * @author hubin
 * @Date 2016-04-20
 */
public class ServiceImpl<M extends BaseMapper<T>, T> implements IService<T> {

	protected static final Logger logger = Logger.getLogger("ServiceImpl");

	@Autowired
	protected M baseMapper;

	/**
	 * 判断数据库操作是否成功
	 *
	 * @param result
	 *            数据库操作返回影响条数
	 * @return boolean
	 */
	protected boolean retBool(int result) {
		return result >= 1;
	}

	/**
	 * <p>
	 * TableId 注解存在更新记录，否插入一条记录
	 * </p>
	 *
	 * @param entity
	 *            实体对象
	 * @return boolean
	 */
	public boolean insertOrUpdate(T entity) {
		if (null != entity) {
			Class<?> cls = entity.getClass();
			TableInfo tableInfo = TableInfoHelper.getTableInfo(cls);
			if (null != tableInfo) {
				Object idVal = ReflectionKit.getMethodValue(cls, entity, tableInfo.getKeyProperty());
				if (null == idVal || "".equals(idVal)) {
					return insert(entity);
				} else {
					/* 特殊处理 INPUT 主键策略逻辑 */
					if (IdType.INPUT == tableInfo.getIdType()) {
						T entityValue = selectById((Serializable) idVal);
						if (null != entityValue) {
							return updateById(entity);
						} else {
							return insert(entity);
						}
					}
					return updateById(entity);
				}
			} else {
				throw new MybatisPlusException("Error:  Cannot execute. Could not find @TableId.");
			}
		}
		return false;
	}

	public boolean insert(T entity) {
		return retBool(baseMapper.insert(entity));
	}

	public boolean insertBatch(List<T> entityList) {
		if (CollectionUtil.isEmpty(entityList)) {
			throw new IllegalArgumentException("Error: entityList must not be empty");
		}
		return retBool(baseMapper.insertBatch(entityList));
	}

	/**
	 * 批量插入
	 *
	 * @param entityList
	 * @param batchSize
	 * @return
	 */
	public boolean insertBatch(List<T> entityList, int batchSize) {
		if (CollectionUtil.isEmpty(entityList)) {
			throw new IllegalArgumentException("Error: entityList must not be empty");
		}
		TableInfo tableInfo = TableInfoHelper.getTableInfo(currentModleClass());
		if (null == tableInfo) {
			throw new MybatisPlusException("Error: Cannot execute insertBatch Method, ClassGenricType not found .");
		}
		SqlSession batchSqlSession = tableInfo.getSqlMapper().getSqlSessionFactory().openSession(ExecutorType.BATCH,
				false);
		try {
			int size = entityList.size();
			for (int i = 0; i < size; i++) {
				baseMapper.insert(entityList.get(i));
				if (i % batchSize == 0) {
					batchSqlSession.flushStatements();
				}
			}
			batchSqlSession.flushStatements();
		} catch (Exception e) {
			logger.warning("Error: Cannot execute insertBatch Method. Cause:" + e);
			return false;
		}
		return true;

	}

	@SuppressWarnings("unchecked")
	protected Class<T> currentModleClass() {
		return ReflectionKit.getSuperClassGenricType(getClass(), 1);
	}

	public boolean insertSql(SQL sql, Object... args) {
		return retBool(baseMapper.insertSql(sql, args));
	}

	public boolean deleteById(Serializable id) {
		return retBool(baseMapper.deleteById(id));
	}

	public boolean deleteByMap(Map<String, Object> columnMap) {
		return retBool(baseMapper.deleteByMap(columnMap));
	}

	public boolean delete(T entity) {
		return delete(new EntityWrapper<T>(entity));
	}

	public boolean delete(EntityWrapper<T> entityWrapper) {
		return retBool(baseMapper.delete(entityWrapper));
	}

	public boolean deleteBatchIds(List<? extends Serializable> idList) {
		return retBool(baseMapper.deleteBatchIds(idList));
	}

	public boolean deleteSql(SQL sql, Object... args) {
		return retBool(baseMapper.deleteSql(sql, args));
	}

	public boolean updateById(T entity) {
		return retBool(baseMapper.updateById(entity));
	}

	public boolean update(T entity, T whereEntity) {
		return update(entity, new EntityWrapper<T>(whereEntity));
	}

	public boolean update(T entity, EntityWrapper<T> entityWrapper) {
		return retBool(baseMapper.update(entity, entityWrapper));
	}

	public boolean updateBatchById(List<T> entityList) {
		return retBool(baseMapper.updateBatchById(entityList));
	}

	public boolean updateSql(SQL sql, Object... args) {
		return retBool(baseMapper.updateSql(sql, args));
	}

	public T selectById(Serializable id) {
		return baseMapper.selectById(id);
	}

	public List<T> selectBatchIds(List<? extends Serializable> idList) {
		return baseMapper.selectBatchIds(idList);
	}

	public List<T> selectByMap(Map<String, Object> columnMap) {
		return baseMapper.selectByMap(columnMap);
	}

	public T selectOne(T entity) {
		return baseMapper.selectOne(entity);
	}

	public T selectOne(EntityWrapper<T> entityWrapper) {
		List<T> list = baseMapper.selectList(entityWrapper);
		if (CollectionUtil.isNotEmpty(list)) {
			int size = list.size();
			if (size > 1) {
				logger.warning(String.format("Warn: selectOne Method There are  %s results.", size));
			}
			return list.get(0);
		}
		return null;
	}

	public int selectCount(T entity) {
		return baseMapper.selectCount(new EntityWrapper<T>(entity));
	}

	public int selectCount(EntityWrapper<T> entityWrapper) {
		return baseMapper.selectCount(entityWrapper);
	}

	public List<T> selectList(T entity) {
		return selectList(new EntityWrapper<T>(entity));
	}

	public List<T> selectList(EntityWrapper<T> entityWrapper) {
		return baseMapper.selectList(entityWrapper);
	}

	public List<T> selectListSql(SQL sql, Object... args) {
		return baseMapper.selectListSql(sql, args);
	}

	public Page<T> selectPage(Page<T> page) {
		page.setRecords(baseMapper.selectPage(page, null));
		return page;
	}

	public Page<T> selectPage(Page<T> page, T entity) {
		return selectPage(page, new EntityWrapper<T>(entity));
	}

	public Page<T> selectPage(Page<T> page, EntityWrapper<T> entityWrapper) {
		if (null != entityWrapper) {
			entityWrapper.orderBy(page.getOrderByField(), page.isAsc());
		}
		page.setRecords(baseMapper.selectPage(page, entityWrapper));
		return page;
	}

	public Page<T> selectPageSql(Page<T> page, SQL sql, Object... args) {
		page.setRecords(baseMapper.selectPageSql(page, sql, args));
		return page;
	}

}
