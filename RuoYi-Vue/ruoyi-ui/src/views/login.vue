<template>
  <div class="login">
    <div class="login-brand">
      <img src="@/assets/logo/logo.png" alt="系统标识">
      <span>{{ title }}</span>
    </div>

    <el-form ref="loginForm" :model="loginForm" :rules="loginRules" class="login-form">
      <div class="form-heading">
        <h1>登录</h1>
      </div>

      <el-form-item prop="username">
        <el-input v-model="loginForm.username" type="text" auto-complete="off" placeholder="账号">
          <svg-icon slot="prefix" icon-class="user" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>

      <el-form-item prop="password">
        <el-input v-model="loginForm.password" type="password" auto-complete="off" placeholder="密码" @keyup.enter.native="handleLogin">
          <svg-icon slot="prefix" icon-class="password" class="el-input__icon input-icon" />
        </el-input>
      </el-form-item>

      <div class="form-options">
        <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
        <span>验证码已省略</span>
      </div>

      <el-form-item class="login-action">
        <el-button :loading="loading" type="primary" @click.native.prevent="handleLogin">
          <span v-if="!loading">进入系统</span>
          <span v-else>登录中...</span>
        </el-button>
      </el-form-item>

      <router-link v-if="register" class="register-link" :to="'/register'">立即注册</router-link>
    </el-form>

    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script>
import Cookies from 'js-cookie'
import { encrypt, decrypt } from '@/utils/jsencrypt'
import defaultSettings from '@/settings'

export default {
  name: 'Login',
  data() {
    return {
      title: process.env.VUE_APP_TITLE,
      footerContent: defaultSettings.footerContent,
      loginForm: {
        username: 'admin',
        password: 'admin123',
        rememberMe: false
      },
      loginRules: {
        username: [{ required: true, trigger: 'blur', message: '请输入账号' }],
        password: [{ required: true, trigger: 'blur', message: '请输入密码' }]
      },
      loading: false,
      register: false,
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.getCookie()
  },
  methods: {
    getCookie() {
      const username = Cookies.get('username')
      const password = Cookies.get('password')
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.loading = true
          if (this.loginForm.rememberMe) {
            Cookies.set('username', this.loginForm.username, { expires: 30 })
            Cookies.set('password', encrypt(this.loginForm.password), { expires: 30 })
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 })
          } else {
            Cookies.remove('username')
            Cookies.remove('password')
            Cookies.remove('rememberMe')
          }
          this.$store.dispatch('Login', this.loginForm).then(() => {
            this.$router.push({ path: this.redirect || '/' }).catch(() => {})
          }).catch(() => {
            this.loading = false
          })
        }
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.login {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
  padding: 80px 24px 56px;
  background:
    linear-gradient(180deg, rgba(9, 24, 45, .42), rgba(9, 24, 45, .66)),
    url("../assets/images/login-background.jpg") center center / cover no-repeat;
}

.login-brand {
  position: fixed;
  top: 34px;
  left: 42px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
  font-size: 18px;
  font-weight: 700;

  img {
    width: 34px;
    height: 34px;
  }
}

.login-form {
  width: 388px;
  padding: 34px 34px 30px;
  border: 1px solid rgba(255, 255, 255, .72);
  border-radius: 8px;
  background: rgba(255, 255, 255, .94);

  .el-input {
    height: 42px;

    input {
      height: 42px;
      border-radius: 4px;
    }
  }

  .input-icon {
    width: 15px;
    height: 42px;
    margin-left: 2px;
  }
}

.form-heading {
  margin-bottom: 26px;
  text-align: center;

  h1 {
    margin: 0;
    color: #1f2d3d;
    font-size: 28px;
    font-weight: 700;
  }
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: -2px 0 22px;
  color: #8c98a8;
  font-size: 13px;
}

.login-action {
  margin-bottom: 0;

  .el-button {
    width: 100%;
    height: 42px;
    border-radius: 4px;
    font-weight: 600;
  }
}

.register-link {
  display: block;
  margin-top: 14px;
  text-align: right;
}

.el-login-footer {
  position: fixed;
  bottom: 14px;
  left: 0;
  width: 100%;
  color: rgba(255, 255, 255, .78);
  font-size: 12px;
  text-align: center;
  pointer-events: none;
}

@media (max-width: 640px) {
  .login {
    padding: 88px 16px 48px;
  }

  .login-brand {
    left: 20px;
    right: 20px;
    font-size: 16px;
  }

  .login-form {
    width: 100%;
    max-width: 388px;
    padding: 30px 24px 26px;
  }
}
</style>
