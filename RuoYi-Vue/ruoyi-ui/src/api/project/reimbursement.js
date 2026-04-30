import request from '@/utils/request'

export function listReimbursements(query) {
  return request({ url: '/cost/reimbursement/list', method: 'get', params: query })
}

export function getReimbursement(id) {
  return request({ url: '/cost/reimbursement/' + id, method: 'get' })
}

export function listReimbursementWbsNodes(projId) {
  return request({ url: '/cost/reimbursement/wbsNodes/' + projId, method: 'get' })
}

export function saveReimbursementDraft(data) {
  return request({ url: '/cost/reimbursement/draft', method: 'post', data })
}

export function submitReimbursement(id) {
  return request({ url: '/cost/reimbursement/submit/' + id, method: 'post' })
}

export function approveReimbursement(id) {
  return request({ url: '/cost/reimbursement/approve/' + id, method: 'put' })
}

export function rejectReimbursement(id, data) {
  return request({ url: '/cost/reimbursement/reject/' + id, method: 'put', data })
}

export function postReimbursement(id) {
  return request({ url: '/cost/reimbursement/post/' + id, method: 'put' })
}

export function delReimbursement(id) {
  return request({ url: '/cost/reimbursement/' + id, method: 'delete' })
}
