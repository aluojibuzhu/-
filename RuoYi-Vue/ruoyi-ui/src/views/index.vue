<template>
  <div class="home-page app-container">
    <section class="user-card">
      <div class="user-main">
        <userAvatar />
        <div class="user-meta">
          <div class="user-title">
            <h2>{{ user.nickName || user.userName || '系统用户' }}</h2>
            <el-tag size="mini" effect="plain">{{ roleGroup || '普通用户' }}</el-tag>
          </div>
          <div class="user-subtitle">工程项目成本管理工作台</div>
        </div>
      </div>

      <div class="user-info-row">
        <span><i class="el-icon-office-building"></i>{{ user.dept ? user.dept.deptName : '未设置部门' }}</span>
        <span><i class="el-icon-suitcase"></i>{{ postGroup || '未设置岗位' }}</span>
      </div>

      <el-button type="primary" plain icon="el-icon-edit" @click="scrollToEdit">编辑资料</el-button>
    </section>

    <el-row :gutter="16" class="home-grid">
      <el-col :xs="24" :lg="8" class="home-col">
        <section class="section-card">
          <h3>我的待办</h3>
          <div v-loading="todoLoading" class="todo-list">
            <div v-for="item in todoList" :key="item.label" class="todo-item">
              <span class="todo-label">
                <i :class="item.icon"></i>
                {{ item.label }}
              </span>
              <el-badge :value="item.count" :max="99" :hidden="item.count === 0" class="todo-badge" />
              <router-link class="todo-link" :to="item.path">查看</router-link>
            </div>
          </div>
        </section>

        <section class="section-card notice-card">
          <h3>系统公告</h3>
          <div v-loading="noticeLoading" class="notice-list">
            <div v-if="!noticeLoading && noticeList.length === 0" class="empty-block">
              <i class="el-icon-message"></i>
              <span>暂无公告</span>
            </div>
            <div
              v-for="item in noticeList"
              :key="item.noticeId"
              class="notice-item"
              :class="{ 'is-read': item.isRead }"
              @click="previewNotice(item)"
            >
              <el-tag size="mini" :type="item.noticeType === '1' ? 'warning' : 'success'">
                {{ item.noticeType === '1' ? '通知' : '公告' }}
              </el-tag>
              <span class="notice-title">{{ item.noticeTitle }}</span>
              <span class="notice-date">{{ formatNoticeDate(item.createTime) }}</span>
            </div>
          </div>
          <div class="notice-more" @click="$router.push('/system/notice')">查看全部公告</div>
        </section>
      </el-col>

      <el-col :xs="24" :lg="16" class="home-col">
        <section class="section-card quick-card">
          <h3>快捷入口</h3>
          <div class="quick-grid">
            <div v-for="item in quickLinks" :key="item.label" class="quick-item" @click="$router.push(item.path)">
              <i :class="item.icon"></i>
              <span>{{ item.label }}</span>
            </div>
          </div>
        </section>

        <section id="home-edit-section" class="section-card edit-card">
          <h3>资料与安全</h3>
          <el-tabs v-model="selectedTab" class="profile-tabs">
            <el-tab-pane label="基本资料" name="userinfo">
              <userInfo :user="user" />
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="resetPwd">
              <resetPwd />
            </el-tab-pane>
          </el-tabs>
        </section>
      </el-col>
    </el-row>

    <notice-detail-view ref="noticeView" />
  </div>
</template>

<script>
import userAvatar from '@/views/system/user/profile/userAvatar'
import userInfo from '@/views/system/user/profile/userInfo'
import resetPwd from '@/views/system/user/profile/resetPwd'
import NoticeDetailView from '@/layout/components/HeaderNotice/DetailView'
import { getUserProfile } from '@/api/system/user'
import { listNoticeTop, markNoticeRead } from '@/api/system/notice'
import { listWorkHours } from '@/api/project/workHour'
import { listReimbursements } from '@/api/project/reimbursement'
import { listAlertRecords } from '@/api/project/alertRecord'

export default {
  name: 'Index',
  components: { userAvatar, userInfo, resetPwd, NoticeDetailView },
  data() {
    return {
      user: {},
      roleGroup: '',
      postGroup: '',
      selectedTab: 'userinfo',
      todoLoading: false,
      noticeLoading: false,
      noticeList: [],
      todoList: [
        { label: '待审批工时', count: 0, icon: 'el-icon-time', path: '/approvalCenter/cost' },
        { label: '待审批报销', count: 0, icon: 'el-icon-wallet', path: '/approvalCenter/cost' },
        { label: '预警待处理', count: 0, icon: 'el-icon-warning-outline', path: '/alertMonitor/record' }
      ],
      quickLinks: [
        { label: '填工时', icon: 'el-icon-edit-outline', path: '/cost/workHour' },
        { label: '填报销', icon: 'el-icon-money', path: '/cost/reimbursement' },
        { label: '我的项目', icon: 'el-icon-folder-opened', path: '/project/projInfo' },
        { label: '成本看板', icon: 'el-icon-s-data', path: '/costDashboard/overview' }
      ]
    }
  },
  created() {
    this.getUser()
    this.loadTodoCounts()
    this.loadNoticeTop()
  },
  methods: {
    getUser() {
      getUserProfile().then(response => {
        this.user = response.data || {}
        this.roleGroup = response.roleGroup || ''
        this.postGroup = response.postGroup || ''
      })
    },
    loadTodoCounts() {
      this.todoLoading = true
      Promise.all([
        listWorkHours({ status: '1', pageNum: 1, pageSize: 1 }).catch(() => ({ total: 0 })),
        listReimbursements({ status: '1', pageNum: 1, pageSize: 1 }).catch(() => ({ total: 0 })),
        listAlertRecords({ status: '0', pageNum: 1, pageSize: 1 }).catch(() => ({ total: 0 }))
      ]).then(([workHourRes, reimbursementRes, alertRes]) => {
        this.todoList = this.todoList.map((item, index) => ({
          ...item,
          count: Number([workHourRes, reimbursementRes, alertRes][index].total || 0)
        }))
      }).finally(() => {
        this.todoLoading = false
      })
    },
    loadNoticeTop() {
      this.noticeLoading = true
      listNoticeTop().then(res => {
        this.noticeList = (res.data || []).slice(0, 5)
      }).catch(() => {
        this.noticeList = []
      }).finally(() => {
        this.noticeLoading = false
      })
    },
    previewNotice(item) {
      if (!item.isRead) {
        markNoticeRead(item.noticeId).catch(() => {})
        item.isRead = true
      }
      this.$refs.noticeView.open(item.noticeId)
    },
    scrollToEdit() {
      const target = document.getElementById('home-edit-section')
      if (target) {
        target.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }
    },
    formatNoticeDate(value) {
      if (!value) return '-'
      return String(value).slice(5, 10)
    }
  }
}
</script>

<style lang="scss" scoped>
.home-page {
  min-height: calc(100vh - 84px);
  background: #f5f7fb;
}

.user-card,
.section-card {
  border: 1px solid #e8ecf1;
  border-radius: 8px;
  background: #fff;
}

.user-card {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 24px 28px;
}

.user-main {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.user-meta {
  flex: 1;
  min-width: 0;
  margin-left: 20px;
}

.user-title {
  display: flex;
  align-items: center;
  gap: 10px;

  h2 {
    margin: 0;
    overflow: hidden;
    color: #1f2d3d;
    font-size: 20px;
    font-weight: 600;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.user-subtitle {
  margin-top: 8px;
  color: #8a97a8;
  font-size: 13px;
}

.user-info-row {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-right: 24px;

  span {
    display: inline-flex;
    align-items: center;
    max-width: 180px;
    overflow: hidden;
    color: #667085;
    font-size: 13px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  i {
    margin-right: 6px;
    color: #1890ff;
  }
}

.section-card {
  margin-bottom: 16px;
  padding: 20px;

  h3 {
    margin: 0 0 16px;
    padding-bottom: 12px;
    border-bottom: 1px solid #f0f2f5;
    color: #1f2d3d;
    font-size: 16px;
    font-weight: 600;
  }
}

.home-grid {
  display: flex;
  align-items: stretch;
}

.home-col {
  display: flex;
  flex-direction: column;
}

.notice-card,
.edit-card {
  flex: 1;
}

.notice-card {
  display: flex;
  flex-direction: column;
}

.home-col > .section-card:last-child {
  margin-bottom: 0;
}

.todo-list {
  min-height: 146px;
}

.todo-item {
  display: flex;
  align-items: center;
  min-height: 48px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }
}

.todo-label {
  display: inline-flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  color: #333;
  font-size: 14px;

  i {
    margin-right: 8px;
    color: #1890ff;
    font-size: 16px;
  }
}

.todo-badge {
  width: 36px;
  margin-right: 14px;
}

.todo-link {
  color: #1890ff;
  font-size: 13px;
  cursor: pointer;
}

.quick-card {
  padding-bottom: 18px;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 82px;
  padding: 14px 12px;
  border: 1px solid transparent;
  border-radius: 6px;
  background: #f7f9fb;
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;

  &:hover {
    border-color: #c9e6ff;
    background: #ecf5ff;
  }

  i {
    margin-bottom: 8px;
    color: #1890ff;
    font-size: 22px;
  }

  span {
    color: #333;
    font-size: 13px;
  }
}

.notice-list {
  flex: 1;
  min-height: 212px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 46px;
  padding: 11px 0;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: #fafbfc;
  }

  &:last-child {
    border-bottom: none;
  }

  &.is-read {
    opacity: 0.55;
  }
}

.notice-title {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  color: #333;
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-date {
  flex-shrink: 0;
  color: #b5bdc8;
  font-size: 12px;
}

.notice-more {
  margin-top: auto;
  padding-top: 12px;
  color: #1890ff;
  font-size: 13px;
  text-align: center;
  cursor: pointer;
}

.empty-block {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  color: #a8b3c1;
  font-size: 13px;

  i {
    margin-right: 8px;
    font-size: 18px;
  }
}

.edit-card {
  min-height: 480px;
}

.profile-tabs {
  ::v-deep .el-tabs__item {
    font-weight: 600;
  }
}

@media (max-width: 992px) {
  .home-grid {
    display: block;
  }

  .home-col > .section-card:last-child {
    margin-bottom: 16px;
  }

  .user-card {
    align-items: flex-start;
    flex-direction: column;
  }

  .user-info-row {
    flex-wrap: wrap;
    margin: 18px 0;
  }
}

@media (max-width: 640px) {
  .user-card,
  .section-card {
    padding: 18px;
  }

  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
