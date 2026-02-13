// @author Richard
import request from './request'

const baseUrl = '/client-api/template-lab'

// 分析文档
export function analyzeTemplate(data) {
    return request({
        url: `${baseUrl}/analyze`,
        method: 'post',
        data: data,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 同步预览
export function previewTemplate(data) {
    return request({
        url: `${baseUrl}/preview`,
        method: 'post',
        data: data,
        responseType: 'blob',
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 保存模版
export function saveTemplate(data) {
    return request({
        url: `${baseUrl}/save`,
        method: 'post',
        data: data,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 获取HTML内容
export function getHtml(data) {
    return request({
        url: `${baseUrl}/get-html`,
        method: 'post',
        data: data,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

// 转换HTML为Docx
export function convertToDocx(data) {
    return request({
        url: `${baseUrl}/convert-to-docx`,
        method: 'post',
        data: data,
        responseType: 'blob', // Important for file download
        headers: { 'Content-Type': 'application/json' }
    })
}
