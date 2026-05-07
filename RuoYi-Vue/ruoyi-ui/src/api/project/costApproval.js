import request from '@/utils/request'

export function listCostApprovalBills(query) {
  return request({ url: '/cost/approval/list', method: 'get', params: query })
}

export function getCostApprovalSummary() {
  return request({ url: '/cost/approval/summary', method: 'get' })
}

export function approveAndPostBill(billType, billId) {
  return request({ url: '/cost/approval/approve/' + billType + '/' + billId, method: 'put' })
}

export function rejectBill(billType, billId, data) {
  return request({ url: '/cost/approval/reject/' + billType + '/' + billId, method: 'put', data })
}

export function postApprovedBill(billType, billId) {
  return request({ url: '/cost/approval/post/' + billType + '/' + billId, method: 'post' })
}
