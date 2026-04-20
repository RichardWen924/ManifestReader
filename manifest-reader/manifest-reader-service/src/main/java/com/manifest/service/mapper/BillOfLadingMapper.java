package com.manifest.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.manifest.model.entity.BillOfLading;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BillOfLadingMapper extends BaseMapper<BillOfLading> {
    /** 级联查询：包含 parties、cargo、freight */
    BillOfLading selectWithDetailsById(Long id);
    List<BillOfLading> selectListByCompany(String companyCode);
}
