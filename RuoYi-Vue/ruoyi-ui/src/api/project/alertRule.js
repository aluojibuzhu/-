import request from '@/utils/request'

export function listAlertRules(query) {
  return request({ url: '/alert/rule/list', method: 'get', params: query })
}

export function getAlertRule(ruleId) {
  return request({ url: '/alert/rule/' + ruleId, method: 'get' })
}

export function addAlertRule(data) {
  return request({ url: '/alert/rule', method: 'post', data })
}

export function updateAlertRule(data) {
  return request({ url: '/alert/rule', method: 'put', data })
}

export function toggleAlertRule(ruleId, enabled) {
  return request({ url: '/alert/rule/toggle/' + ruleId + '/' + enabled, method: 'put' })
}

export function delAlertRule(ruleIds) {
  return request({ url: '/alert/rule/' + ruleIds, method: 'delete' })
}
