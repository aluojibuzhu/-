import request from '@/utils/request'
export function listCostCategories() { return request({ url: '/system/costCategory/list', method: 'get' }) }
