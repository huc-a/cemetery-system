export const money = v => '¥' + Number(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
export const PLOT_STATUS = { FREE: '空闲', RESERVED: '已预订', SOLD: '已售', BURIED: '已安葬', MAINTENANCE: '维护中' }
export const PLOT_COLOR = { FREE: '#7fb069', RESERVED: '#e9c46a', SOLD: '#457b9d', BURIED: '#173f35', MAINTENANCE: '#b08968' }
export const CONTRACT_STATUS = { PENDING_SIGN: '待签约', SIGNED: '已签约', CANCELLED: '已取消' }
export const PAY_STATUS = { UNPAID: '未缴费', PARTIAL: '部分缴费', PAID: '已缴清' }
export const FEE_STATUS = { UNPAID: '待缴', DUE_SOON: '临期', OVERDUE: '已逾期', PAID: '已缴' }
export const ORDER_STATUS = { PENDING: '待派单', ASSIGNED: '已派单', PROCESSING: '服务中', COMPLETED: '已完成', CANCELLED: '已取消' }
export const WO_STATUS = { PENDING: '待派单', ASSIGNED: '已派单', PROCESSING: '处理中', COMPLETED: '已完成', CANCELLED: '已取消' }
export const VISIT_STATUS = { BOOKED: '已预约', CHECKED_IN: '已入园', CANCELLED: '已取消' }
export const BURIAL_STATUS = { BOOKED: '已预约', COMPLETED: '已安葬', CANCELLED: '已取消' }
export const BIZ_TYPE = { CONTRACT: '购墓合同', MANAGE_FEE: '管理费', SERVICE: '增值服务' }
export const SLOTS = ['08:00-10:00', '10:00-12:00', '13:00-15:00', '15:00-17:00']
