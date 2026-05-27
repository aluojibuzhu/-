<template>
  <div class="app-container home-page">
    <section class="home-hero">
      <div>
        <span>PROJECT COST</span>
        <h2>工程项目成本管理</h2>
        <p>立项、填报、审批、预警、看板一体化</p>
      </div>
      <el-button type="primary" icon="el-icon-s-data" @click="$router.push('/costDashboard/overview')">成本看板</el-button>
    </section>

    <section class="module-grid">
      <div v-for="item in modules" :key="item.title" class="module-card" @click="$router.push(item.path)">
        <div class="module-icon">
          <svg-icon :icon-class="item.icon" />
        </div>
        <div>
          <strong>{{ item.title }}</strong>
          <span>{{ item.tag }}</span>
        </div>
        <i class="el-icon-arrow-right"></i>
      </div>
    </section>

    <section class="home-bottom">
      <div class="notice-panel">
        <div class="panel-title">
          <h3>公告板</h3>
          <el-tag size="small" type="success">运行中</el-tag>
        </div>
        <div v-for="item in notices" :key="item" class="notice-line">
          <span></span>
          <p>{{ item }}</p>
        </div>
      </div>

      <div class="visual-panel">
        <div class="visual-title">
          <h3>运行概览</h3>
          <span>业务闭环</span>
        </div>
        <div class="visual-steps">
          <div v-for="item in overview" :key="item.label" class="visual-step">
            <i :class="item.icon"></i>
            <strong>{{ item.value }}</strong>
            <span>{{ item.label }}</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'Index',
  data() {
    return {
      modules: [
        { title: '项目管理', tag: '立项 / WBS', icon: 'clipboard', path: '/project/projInfo' },
        { title: '成本填报', tag: '工时 / 报销', icon: 'edit', path: '/cost/workHour' },
        { title: '审批入账', tag: '审核 / 入账', icon: 'checkbox', path: '/approvalCenter/cost' },
        { title: '监控预警', tag: '风险 / 规则', icon: 'monitor', path: '/alertMonitor/dashboard' },
        { title: '成本看板', tag: '总览 / 报表', icon: 'chart', path: '/costDashboard/overview' },
        { title: '系统管理', tag: '用户 / 权限', icon: 'system', path: '/system/user' }
      ],
      notices: [
        '项目状态按计划日期自动流转',
        '完工项目支持继续补录成本',
        '审批入口已统一到审批入账模块'
      ],
      overview: [
        { label: '模块入口', value: '6', icon: 'el-icon-menu' },
        { label: '流程节点', value: '5', icon: 'el-icon-connection' },
        { label: '权限角色', value: '4', icon: 'el-icon-user' }
      ]
    }
  }
}
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - 84px);
  background: #f5f7fa;
}

.home-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 188px;
  margin-bottom: 16px;
  padding: 32px 36px;
  border: 1px solid #dbe8f7;
  border-radius: 6px;
  background:
    linear-gradient(90deg, rgba(24, 144, 255, .18), rgba(255, 255, 255, .88)),
    url("../assets/images/profile.jpg") center center / cover no-repeat;
}

.home-hero span {
  display: block;
  margin-bottom: 10px;
  color: #1890ff;
  font-size: 12px;
  font-weight: 700;
}

.home-hero h2 {
  margin: 0 0 10px;
  color: #1f2d3d;
  font-size: 32px;
  font-weight: 700;
}

.home-hero p {
  margin: 0;
  color: #5f6f84;
  font-size: 15px;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.module-card {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) 18px;
  align-items: center;
  min-height: 104px;
  padding: 20px;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  transition: border-color .18s, background .18s;
}

.module-card:hover {
  border-color: #b8dafc;
  background: #f8fbff;
}

.module-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 6px;
  background: #eef6ff;
  color: #1890ff;
}

.module-icon .svg-icon {
  width: 21px;
  height: 21px;
}

.module-card strong,
.module-card span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.module-card strong {
  color: #1f2d3d;
  font-size: 17px;
  font-weight: 700;
}

.module-card span {
  margin-top: 8px;
  color: #8c98a8;
  font-size: 13px;
}

.module-card i {
  color: #c0c4cc;
  font-size: 16px;
}

.home-bottom {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, .8fr);
  gap: 16px;
}

.notice-panel,
.visual-panel {
  min-height: 186px;
  padding: 22px 26px;
  border: 1px solid #e6ebf2;
  border-radius: 6px;
  background: #fff;
}

.panel-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.panel-title h3 {
  margin: 0;
  color: #1f2d3d;
  font-size: 18px;
}

.notice-line {
  display: flex;
  align-items: center;
  min-height: 34px;
  border-top: 1px solid #f0f3f7;
}

.notice-line span {
  width: 7px;
  height: 7px;
  margin-right: 10px;
  border-radius: 50%;
  background: #1890ff;
}

.notice-line p {
  margin: 0;
  color: #5f6f84;
  font-size: 14px;
}

.visual-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
}

.visual-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.visual-title h3 {
  margin: 0;
  color: #1f2d3d;
  font-size: 18px;
}

.visual-title span {
  color: #8c98a8;
  font-size: 13px;
}

.visual-steps {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.visual-step {
  min-height: 102px;
  padding: 16px 12px;
  border: 1px solid #edf1f6;
  border-radius: 6px;
  background: #f8fbff;
  text-align: center;
}

.visual-step i {
  display: block;
  color: #1890ff;
  font-size: 22px;
}

.visual-step strong {
  display: block;
  margin: 10px 0 6px;
  color: #1f2d3d;
  font-size: 24px;
  font-weight: 700;
}

.visual-step span {
  display: block;
  color: #8c98a8;
  font-size: 13px;
}

@media (max-width: 1180px) {
  .module-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .home-hero,
  .home-bottom {
    display: block;
  }

  .home-hero .el-button,
  .visual-panel {
    margin-top: 16px;
  }
}

@media (max-width: 640px) {
  .module-grid {
    grid-template-columns: 1fr;
  }

  .home-hero {
    padding: 24px;
  }

  .home-hero h2 {
    font-size: 26px;
  }
}
</style>
