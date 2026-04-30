import request from '@/utils/request'

export function listWorkHours(query) {
  return request({ url: '/cost/workHour/list', method: 'get', params: query })
}

export function getWorkHour(id) {
  return request({ url: '/cost/workHour/' + id, method: 'get' })
}

export function listWorkHourWbsNodes(projId) {
  return request({ url: '/cost/workHour/wbsNodes/' + projId, method: 'get' })
}

export function saveWorkHourDraft(data) {
  return request({ url: '/cost/workHour/draft', method: 'post', data })
}

export function submitWorkHour(id) {
  return request({ url: '/cost/workHour/submit/' + id, method: 'post' })
}

export function approveWorkHour(id) {
  return request({ url: '/cost/workHour/approve/' + id, method: 'put' })
}

export function rejectWorkHour(id, data) {
  return request({ url: '/cost/workHour/reject/' + id, method: 'put', data })
}

export function postWorkHour(id) {
  return request({ url: '/cost/workHour/post/' + id, method: 'put' })
}

export function delWorkHour(id) {
  return request({ url: '/cost/workHour/' + id, method: 'delete' })
}
