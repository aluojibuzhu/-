import request from '@/utils/request'

export function listCostCategories(query) {
  return request({ url: '/system/costCategory/list', method: 'get', params: query })
}

export function getCostCategory(categoryId) {
  return request({ url: '/system/costCategory/' + categoryId, method: 'get' })
}

export function addCostCategory(data) {
  return request({ url: '/system/costCategory', method: 'post', data })
}

export function updateCostCategory(data) {
  return request({ url: '/system/costCategory', method: 'put', data })
}

export function delCostCategory(categoryId) {
  return request({ url: '/system/costCategory/' + categoryId, method: 'delete' })
}
