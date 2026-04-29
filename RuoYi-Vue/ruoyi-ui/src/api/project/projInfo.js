import request from '@/utils/request'

export function getProjForm(id) { return request({ url: '/project/info/' + id, method: 'get' }) }
export function listProjInfos(query) { return request({ url: '/project/info/list', method: 'get', params: query }) }
export function saveDraft(data) { return request({ url: '/project/info/draft', method: 'post', data }) }
export function submitForApproval(id) { return request({ url: '/project/info/submit/' + id, method: 'post' }) }
export function approve(id) { return request({ url: '/project/info/approve/' + id, method: 'put' }) }
export function reject(id, data) { return request({ url: '/project/info/reject/' + id, method: 'put', data }) }
export function delProjInfo(id) { return request({ url: '/project/info/' + id, method: 'delete' }) }

