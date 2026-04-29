export const PROJECT_STATUS_OPTIONS = [
  { value: '0', label: '草稿' },
  { value: '1', label: '审批中' },
  { value: '2', label: '已立项' },
  { value: '3', label: '已驳回' },
  { value: '4', label: '进行中' },
  { value: '5', label: '已完工' }
]

export function projectStatusLabel(status) {
  const item = PROJECT_STATUS_OPTIONS.find(i => i.value === status)
  return item ? item.label : status
}

export function projectStatusTagType(status) {
  return ({ '0': 'info', '1': 'warning', '2': 'success', '3': 'danger', '4': '', '5': 'success' })[status] || 'info'
}

export function formatMoney(value) {
  const amount = Number(value || 0)
  return amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export function canEditProject(status) {
  return status === '0' || status === '3'
}
