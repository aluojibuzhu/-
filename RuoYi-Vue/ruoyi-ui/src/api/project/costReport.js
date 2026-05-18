import request from '@/utils/request'

export function getCostReportOptions() {
  return request({ url: '/cost/report/options', method: 'get' })
}

export function previewCostReport(data) {
  return request({ url: '/cost/report/preview', method: 'post', data })
}

export function listCostReport(type, params) {
  return request({ url: '/cost/report/' + type, method: 'get', params })
}

export function exportCostReport(data) {
  return request({
    url: '/cost/report/export',
    method: 'post',
    data,
    responseType: 'blob'
  })
}

export function exportTypedCostReport(type, data) {
  return request({
    url: '/cost/report/export/' + type,
    method: 'post',
    data,
    responseType: 'blob'
  })
}
