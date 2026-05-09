import request from '@/utils/request'

export function listAlertRecords(query) {
  return request({ url: '/alert/record/list', method: 'get', params: query })
}

export function getAlertRecord(alertId) {
  return request({ url: '/alert/record/' + alertId, method: 'get' })
}

export function listAlertLogs(alertId) {
  return request({ url: '/alert/record/log/' + alertId, method: 'get' })
}

export function confirmAlert(alertId, data) {
  return request({ url: '/alert/record/confirm/' + alertId, method: 'put', data })
}

export function ignoreAlert(alertId, data) {
  return request({ url: '/alert/record/ignore/' + alertId, method: 'put', data })
}

export function followAlert(alertId, data) {
  return request({ url: '/alert/record/follow/' + alertId, method: 'put', data })
}

export function closeAlert(alertId, data) {
  return request({ url: '/alert/record/close/' + alertId, method: 'put', data })
}
