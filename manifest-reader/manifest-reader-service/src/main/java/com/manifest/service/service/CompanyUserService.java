package com.manifest.service.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.manifest.model.entity.CompanyUser;
import com.manifest.service.mapper.CompanyUserMapper;
import org.springframework.stereotype.Service;

/**
 * 公司用户 Service（MP ServiceImpl，直接用 lambdaQuery 等 API）
 */
@Service
public class CompanyUserService extends ServiceImpl<CompanyUserMapper, CompanyUser> {

    public CompanyUser getByCompanyCode(String companyCode) {
        return lambdaQuery()
                .eq(CompanyUser::getCompanyCode, companyCode)
                .eq(CompanyUser::getStatus, "0")
                .one();
    }
}
