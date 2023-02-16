package org.lzx.springBootDemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.lzx.springBootDemo.entity.Statements;

/**
 * <p>
 * 对账单列表 Mapper 接口
 * </p>
 *
 * @author zengfeiyue
 * @since 2022-05-13
 */
@Mapper
public interface StatementsMapper extends BaseMapper<Statements> {

}
