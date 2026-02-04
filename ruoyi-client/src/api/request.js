import axios from 'axios'

const api = axios.create({
    baseURL: '',
    timeout: 60000,
    withCredentials: true // 务必开启，以便在跨域请求中发送 Session Cookie
})

// 响应拦截器
api.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code && res.code !== 0 && res.code !== 200) {
            return Promise.reject(new Error(res.msg || 'Error'))
        }
        return res
    },
    error => {
        return Promise.reject(error)
    }
)

export default api
