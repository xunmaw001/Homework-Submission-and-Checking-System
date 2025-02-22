package com.dao;

import com.entity.YonghuzuoyeEntity;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.plugins.pagination.Pagination;

import org.apache.ibatis.annotations.Param;
import com.entity.view.YonghuzuoyeView;

/**
 * 回答作业 Dao 接口
 *
 * @author 
 * @since 2021-04-22
 */
public interface YonghuzuoyeDao extends BaseMapper<YonghuzuoyeEntity> {

   List<YonghuzuoyeView> selectListView(Pagination page,@Param("params")Map<String,Object> params);

}
