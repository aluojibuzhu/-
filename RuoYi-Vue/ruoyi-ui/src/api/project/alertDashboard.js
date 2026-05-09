import request from '@/utils/request'

export function getAlertSummary() {
  return request({ url: '/alert/dashboard/summary', method: 'get' })
}

export function getProjectHealth() {
  return request({ url: '/alert/dashboard/health', method: 'get' })
}

export function getBudgetTrend(query) {
  return request({ url: '/alert/dashboard/budgetTrend', method: 'get', params: query })
}

export function getCategoryCompare(query) {
  return request({ url: '/alert/dashboard/categoryCompare', method: 'get', params: query })
}

export function getAlertTop10() {
  return request({ url: '/alert/dashboard/top10', method: 'get' })
}
