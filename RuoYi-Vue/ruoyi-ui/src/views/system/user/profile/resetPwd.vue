<template>
  <el-form ref="form" class="profile-edit-form" :model="user" :rules="formRules" label-width="92px">
    <el-row :gutter="18">
      <el-col :span="12" :xs="24">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="user.oldPassword" placeholder="请输入旧密码" type="password" show-password />
        </el-form-item>
      </el-col>
      <el-col :span="12" :xs="24">
        <el-form-item label="新密码" prop="newPassword" :rules="infoPwdValidator">
          <el-input v-model="user.newPassword" placeholder="请输入新密码" type="password" show-password />
        </el-form-item>
      </el-col>
      <el-col :span="12" :xs="24">
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="user.confirmPassword" placeholder="请再次输入新密码" type="password" show-password />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item class="form-actions">
      <el-button type="primary" size="small" icon="el-icon-check" @click="submit">保存</el-button>
      <el-button size="small" icon="el-icon-close" @click="close">关闭</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import { updateUserPwd } from '@/api/system/user'
import passwordRule from '@/utils/passwordRule'

export default {
  mixins: [passwordRule],
  data() {
    return {
      user: {
        oldPassword: undefined,
        newPassword: undefined,
        confirmPassword: undefined
      }
    }
  },
  computed: {
    formRules() {
      return {
        oldPassword: [
          { required: true, message: '旧密码不能为空', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '确认密码不能为空', trigger: 'blur' },
          {
            validator: (rule, value, callback) => {
              if (this.user.newPassword !== value) {
                callback(new Error('两次输入的密码不一致'))
              } else {
                callback()
              }
            },
            trigger: 'blur'
          }
        ]
      }
    }
  },
  methods: {
    submit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          updateUserPwd(this.user.oldPassword, this.user.newPassword).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.user.oldPassword = undefined
            this.user.newPassword = undefined
            this.user.confirmPassword = undefined
          })
        }
      })
    },
    close() {
      this.$tab.closePage()
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-edit-form {
  padding-top: 10px;

  ::v-deep .el-form-item__label {
    color: #4e5969;
    font-weight: 600;
  }
}

.form-actions {
  margin-top: 8px;
}
</style>
