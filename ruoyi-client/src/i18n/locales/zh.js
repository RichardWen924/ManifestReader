export default {
    sidebar: {
        docGen: '文档生成',
        history: '历史提单',
        lab: '模版实验室',
        templates: '模版管理',
        guide: '使用教程',
        upgrade: '账户升级',
        logout: '退出登录'
    },
    dashboard: {
        title: 'AI 智能分析',
        subtitle: '上传您的文档以进行即时数据提取',
        uploadTitle: '点击或拖拽文件上传',
        uploadDesc: '支持 PDF, 图片, Word 文档',
        startAnalysis: '开始 AI 分析',
        analysisResults: '分析结果',
        saveAll: '保存全部记录',
        reviewEdit: '审核与编辑',
        save: '保存',
        analyzed: '已分析',
        pending: '等待中',
        analyzing: '分析中...',
        error: '错误',
        success: '成功'
    },
    common: {
        loading: '加载中...',
        save: '保存',
        cancel: '取消',
        confirm: '确认',
        delete: '删除',
        edit: '编辑',
        export: '导出'
    },
    profile: {
        shipper: '发货人',
        premium: '高级会员'
    },
    history: {
        title: '历史提单',
        subtitle: '查看和管理您保存的单证申报记录',
        searchPlaceholder: '搜索订舱号或提单号...',
        columns: {
            bookingNo: '订舱号',
            blNo: '提单号',
            docNo: '单证号',
            vessel: '船名 / 航次',
            weight: '毛重 (KG)',
            volume: '体积 (CBM)',
            package: '包装信息',
            createdBy: '创建人',
            createdAt: '创建时间',
            actions: '操作'
        }
    },
    lab: {
        title: '模版实验室',
        subtitle: '设计和测试您的单证识别模版',
        upload: '上传模版图片',
        analyze: '分析模版',
        save: '保存模版',
        dragDrop: '使用鼠标框选以验证或纠正识别区域'
    },
    templates: {
        title: '模版管理',
        subtitle: '管理您保存的自定义模版',
        searchPlaceholder: '搜索模版...',
        recordCount: '{count} 个模版记录',
        columns: {
            name: '模版名称',
            code: '模版代码',
            createdAt: '创建时间',
            actions: '操作'
        }
    },
    upgrade: {
        title: '提升您的工作效率',
        subtitle: '解锁无限 AI 算力与专业功能',
        current: '当前方案',
        mostPopular: '最受欢迎',
        basic: '基础版',
        pro: 'VIP 专业版',
        active: '当前方案',
        upgradeNow: '立即升级',
        back: '返回控制台',
        features: {
            generations: '4 次文档生成',
            standardTemplates: '2 个标准模版',
            extraction: 'AI 提取支持',
            priority: '优先 API 访问',
            branding: '自定义品牌',
            unlimited: '无限次生成',
            proTemplates: '所有专业模版',
            processing: '优先 AI 处理',
            pdfTools: '高级 PDF 工具',
            support: '24/7 专属支持'
        },
        modal: {
            title: '确认升级？',
            text: '您即将升级到 VIP 专业版。这将立即解锁所有功能。',
            confirm: '是的，立即升级！',
            successTitle: '成功！',
            successText: '欢迎加入 VIP 专业版！'
        }
    },
    profilePage: {
        title: '用户资料',
        subtitle: '管理您的账户设置并查看会员状态',
        basicInfo: '基本信息',
        editProfile: '编辑资料',
        companyName: '公司名称',
        companyCode: '公司代码',
        shiplineAbbr: '航运代码',
        membershipPlan: '会员方案',
        packageType: '套餐类型',
        expiryDate: '过期时间',
        freeEdition: '免费版',
        noExpiration: '永久有效',
        upgradeHint: '升级到高级会员以获取无限导出和更多功能。',
        usageQuotas: '使用量与配额',
        exportQuota: 'B/L 导出配额',
        templateQuota: '模版配额',
        exportHint: '本月剩余可导出记录数。',
        templateHint: '允许创建的自定义 mustache 模版数量。',
        unlimited: '无限制',
        editModal: {
            title: '编辑资料',
            subtitle: '更新您的公司名称和密码',
            oldPassword: '旧密码',
            newPassword: '新密码',
            confirmPassword: '确认密码',
            save: '保存更改',
            cancel: '取消',
            requiredOld: '请输入旧密码',
            mismatch: '两次密码不一致',
            success: '资料更新成功！',
            updateFailed: '更新失败',
            networkError: '网络错误'
        }
    }
}
