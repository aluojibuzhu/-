import request from '@/utils/request'
export function listCustomers(query) { return request({ url: '/project/customer/list', method: 'get', params: query }) }
export function addCustomer(data) { return request({ url: '/project/customer', method: 'post', data }) }

