import request from './request'

const baseUrl = '/client-api/template-lab'

// 查询模版列表
export function listTemplate(query) {
    return request({
        url: `${baseUrl}/list`,
        method: 'get',
        params: query
    })
}

// 查询模版详细
export function getTemplate(templateId) {
    return request({
        url: `${baseUrl}/${templateId}`,
        method: 'get'
    })
}

// 修改模版
export function updateTemplate(data) {
    return request({
        url: baseUrl,
        method: 'put',
        data: data
    })
}

// 删除模版
export function delTemplate(templateIds) {
    return request({
        url: `${baseUrl}/${templateIds}`,
        method: 'delete'
    })
}

// 使用模版导出docx
export function exportWithTemplate(templateId, businessData) {
    return request({
        url: `${baseUrl}/export`,
        method: 'post',
        data: { templateId, businessData },
        responseType: 'blob'
    })
}
