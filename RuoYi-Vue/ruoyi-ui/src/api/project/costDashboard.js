import request from '@/utils/request'

export function getCostDashboardSummary(params) {
  return request({ url: '/cost/dashboard/summary', method: 'get', params })
}

export function listCostDashboardProjects(params) {
  return request({ url: '/cost/dashboard/list', method: 'get', params })
}
