<template>
  <div class="profile-page app-container">
    <div class="profile-hero">
      <div class="profile-identity">
        <userAvatar />
        <div class="identity-text">
          <div class="identity-kicker">个人中心</div>
          <h2>{{ user.nickName || user.userName || '系统用户' }}</h2>
          <p>{{ roleGroup || '工程项目成本管理系统用户' }}</p>
        </div>
      </div>
      <div class="profile-summary">
        <div class="summary-item">
          <span>所属部门</span>
          <strong>{{ user.dept ? user.dept.deptName : '-' }}</strong>
        </div>
        <div class="summary-item">
          <span>岗位</span>
          <strong>{{ postGroup || '-' }}</strong>
        </div>
        <div class="summary-item">
          <span>创建日期</span>
          <strong>{{ user.createTime || '-' }}</strong>
        </div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="7" :xs="24">
        <el-card class="profile-card" shadow="never">
          <div slot="header" class="card-header">
            <span>账号信息</span>
          </div>
          <ul class="info-list">
            <li>
              <span><svg-icon icon-class="user" /> 用户名称</span>
              <strong>{{ user.userName || '-' }}</strong>
            </li>
            <li>
              <span><svg-icon icon-class="phone" /> 手机号码</span>
              <strong>{{ user.phonenumber || '-' }}</strong>
            </li>
            <li>
              <span><svg-icon icon-class="email" /> 用户邮箱</span>
              <strong>{{ user.email || '-' }}</strong>
            </li>
            <li>
              <span><svg-icon icon-class="tree" /> 所属部门</span>
              <strong>{{ user.dept ? user.dept.deptName : '-' }}</strong>
            </li>
            <li>
              <span><svg-icon icon-class="peoples" /> 所属角色</span>
              <strong>{{ roleGroup || '-' }}</strong>
            </li>
            <li>
              <span><svg-icon icon-class="date" /> 创建日期</span>
              <strong>{{ user.createTime || '-' }}</strong>
            </li>
          </ul>
        </el-card>
      </el-col>

      <el-col :span="17" :xs="24">
        <el-card class="profile-card profile-form-card" shadow="never">
          <div slot="header" class="card-header">
            <span>资料维护</span>
          </div>
          <el-tabs v-model="selectedTab" class="profile-tabs">
            <el-tab-pane label="基本资料" name="userinfo">
              <userInfo :user="user" />
            </el-tab-pane>
            <el-tab-pane label="修改密码" name="resetPwd">
              <resetPwd />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import userAvatar from './userAvatar'
import userInfo from './userInfo'
import resetPwd from './resetPwd'
import { getUserProfile } from '@/api/system/user'

export default {
  name: 'Profile',
  components: { userAvatar, userInfo, resetPwd },
  data() {
    return {
      user: {},
      roleGroup: '',
      postGroup: '',
      selectedTab: 'userinfo'
    }
  },
  created() {
    const activeTab = this.$route.params && this.$route.params.activeTab
    if (activeTab) {
      this.selectedTab = activeTab
    }
    this.getUser()
  },
  methods: {
    getUser() {
      getUserProfile().then(response => {
        this.user = response.data || {}
        this.roleGroup = response.roleGroup || ''
        this.postGroup = response.postGroup || ''
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
  background: #f5f7fb;
}

.profile-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 156px;
  margin-bottom: 18px;
  padding: 24px 28px;
  border: 1px solid #e5edf6;
  border-radius: 6px;
  background:
    linear-gradient(90deg, rgba(24, 144, 255, .12), rgba(255, 255, 255, .95)),
    url('~@/assets/images/profile.jpg') center/cover no-repeat;
}

.profile-identity {
  display: flex;
  align-items: center;
  min-width: 0;

  ::v-deep .user-avatar {
    border: 4px solid rgba(255, 255, 255, .86);
  }
}

.identity-text {
  min-width: 0;
  margin-left: 22px;

  .identity-kicker {
    margin-bottom: 8px;
    color: #1890ff;
    font-size: 13px;
    font-weight: 600;
  }

  h2 {
    margin: 0 0 8px;
    color: #1f2d3d;
    font-size: 26px;
    line-height: 1.25;
  }

  p {
    margin: 0;
    color: #667085;
    font-size: 14px;
  }
}

.profile-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(120px, 1fr));
  gap: 12px;
  width: 46%;
}

.summary-item {
  padding: 14px 16px;
  border: 1px solid rgba(213, 224, 238, .9);
  border-radius: 6px;
  background: rgba(255, 255, 255, .82);

  span {
    display: block;
    margin-bottom: 8px;
    color: #8a97a8;
    font-size: 12px;
  }

  strong {
    display: block;
    overflow: hidden;
    color: #1f2d3d;
    font-size: 15px;
    font-weight: 600;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.profile-card {
  margin-bottom: 18px;
  border: 1px solid #e5edf6;
  border-radius: 6px;
}

.card-header {
  color: #1f2d3d;
  font-size: 18px;
  font-weight: 700;
}

.info-list {
  margin: 0;
  padding: 0;
  list-style: none;

  li {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 48px;
    border-bottom: 1px solid #edf2f7;
    color: #667085;
    gap: 14px;

    &:last-child {
      border-bottom: 0;
    }
  }

  span {
    display: inline-flex;
    align-items: center;
    flex: 0 0 94px;

    .svg-icon {
      margin-right: 8px;
      color: #1890ff;
    }
  }

  strong {
    min-width: 0;
    color: #1f2d3d;
    font-weight: 500;
    text-align: right;
    word-break: break-all;
  }
}

.profile-form-card {
  min-height: 392px;
}

.profile-tabs {
  ::v-deep .el-tabs__item {
    font-weight: 600;
  }
}

@media (max-width: 992px) {
  .profile-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .profile-summary {
    width: 100%;
    margin-top: 18px;
  }
}

@media (max-width: 768px) {
  .profile-hero {
    padding: 18px;
  }

  .profile-summary {
    grid-template-columns: 1fr;
  }
}
</style>
